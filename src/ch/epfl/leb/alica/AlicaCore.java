/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.alica;

import ch.epfl.leb.alica.analyzers.AnalyzerFactory;
import ch.epfl.leb.alica.worker.WorkerThread;
import ch.epfl.leb.alica.analyzers.spotcounter.SpotCounter;
import ch.epfl.leb.alica.controllers.ControllerFactory;
import ch.epfl.leb.alica.controllers.inverter.InvertController;
import ch.epfl.leb.alica.controllers.pid.PID_controller;
import ch.epfl.leb.alica.lasers.LaserFactory;
import ch.epfl.leb.alica.lasers.MMLaser;
import ij.IJ;
import ij.process.ImageProcessor;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcorej.CMMCore;
import mmcorej.StrVector;
import org.micromanager.Studio;
import org.micromanager.data.Datastore;

/**
 *
 * @author stefko
 */
public final class AlicaCore {
    private static AlicaCore instance = null;
    
    private final Studio studio;
    private WorkerThread worker;
    
    private final AnalyzerFactory analyzer_factory;
    private final ControllerFactory controller_factory;
    private final LaserFactory laser_factory;
    

    
    // private constructor disables instantiation
    private AlicaCore(Studio studio) {
        this.studio = studio;
        this.analyzer_factory = new AnalyzerFactory();
        this.controller_factory = new ControllerFactory();
        this.laser_factory = new LaserFactory(studio);
    }
    
    public static AlicaCore initialize(Studio studio) throws RuntimeException {
        if (instance != null) {
            throw new AlreadyInitializedException("ALICA core was already initialized.");
        } else {
            instance = new AlicaCore(studio);
        }
        return instance;
    }
    
    public static AlicaCore getInstance() {
        if (instance == null) {
            throw new NullPointerException("ALICA core was not yet initialized.");
        }
        return instance;
    }
    
    public void printLoadedDevices() {
        StrVector v = studio.core().getLoadedDevices();
        String out = "";
        for (String label: v.toArray()) {
            out = out.concat(" "+label+":\n");
            try {
                for (String property: studio.core().getDevicePropertyNames(label).toArray())
                {
                    String propValue;
                    try {
                        propValue = studio.core().getProperty(label, property);
                    } catch (Exception ex) {
                        propValue = "Error.";
                    }
                    out = out.concat("  - "+property+": "+propValue+"\n");
                }
            } catch (Exception ex) {
                Logger.getLogger(AlicaCore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        IJ.log(out);
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, out);
    }
    
    public AnalyzerFactory getAnalyzerFactory() {
        return analyzer_factory;
    }
    
    public ControllerFactory getControllerFactory() {
        return controller_factory;
    }
    
    public LaserFactory getLaserFactory() {
        return laser_factory;
    }
     
    public void startWorker(boolean draw_from_core) {
        worker = new WorkerThread(studio, analyzer_factory.build(), 
                controller_factory.build(), laser_factory.build(), draw_from_core);
        worker.start();
    }
    
    public void stopWorker() {
        worker.requestStop();
        try {
            worker.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(AlicaCore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static class AlreadyInitializedException extends RuntimeException {
        public AlreadyInitializedException(String message) {
            super(message);
        }
    }
}