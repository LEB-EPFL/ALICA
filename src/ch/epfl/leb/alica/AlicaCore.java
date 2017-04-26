/* 
 * Copyright (C) 2017 Laboratory of Experimental Biophysics
 * Ecole Polytechnique Federale de Lausanne
 * 
 * Author: Marcel Stefko
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.epfl.leb.alica;

import ch.epfl.leb.alica.analyzers.AnalyzerFactory;
import ch.epfl.leb.alica.worker.WorkerThread;
import ch.epfl.leb.alica.controllers.ControllerFactory;
import ch.epfl.leb.alica.lasers.LaserFactory;
import ij.IJ;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcorej.StrVector;
import org.micromanager.Studio;

/**
 * The core's settings are controlled by MainGUI, and the Core then produces
 * products from its factories, and initializes the WorkerThread, and later
 * terminates it.
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
    
    /**
     * Initialize the Singleton core
     * @param studio MicroManager studio
     * @return the singleton instance of the core
     * @throws AlreadyInitializedException if it was already initialized
     */
    public static AlicaCore initialize(Studio studio) throws AlreadyInitializedException {
        if (instance != null) {
            throw new AlreadyInitializedException("ALICA core was already initialized.");
        } else {
            instance = new AlicaCore(studio);
        }
        return instance;
    }
    
    /**
     * Returns the singleton instance, or an exception if it was not yet initialized
     * @return the singleton instance of the core
     */
    public static AlicaCore getInstance() {
        if (instance == null) {
            throw new NullPointerException("ALICA core was not yet initialized.");
        }
        return instance;
    }
    
    /**
     * Print all loaded devices in the MMCore to the log.
     */
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
    
    /**
     *
     * @return AnalyzerFactory
     */
    public AnalyzerFactory getAnalyzerFactory() {
        return analyzer_factory;
    }
    
    /**
     *
     * @return ControllerFactory
     */
    public ControllerFactory getControllerFactory() {
        return controller_factory;
    }
    
    /**
     *
     * @return LaserFactory
     */
    public LaserFactory getLaserFactory() {
        return laser_factory;
    }
     
    /**
     * Builds products from their factories using current settings, and
     * starts the WorkerThread (analysis is started)
     * @param draw_from_core true if images should be drawn directly from the
     *  MMCore, false if the processing pipeline should be used
     */
    public void startWorker(boolean draw_from_core) {
        worker = new WorkerThread(studio, analyzer_factory.build(), 
                controller_factory.build(), laser_factory.build(), draw_from_core);
        worker.start();
    }
    
    /**
     * Requests the worker to stop and then waits for it to join.
     */
    public void stopWorker() {
        worker.requestStop();
        try {
            worker.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(AlicaCore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Thrown if a double initialization is requested
     */
    public static class AlreadyInitializedException extends RuntimeException {

        /**
         *
         * @param message exception message
         */
        public AlreadyInitializedException(String message) {
            super(message);
        }
    }
}