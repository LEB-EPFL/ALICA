/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.alica;

import ij.process.ImageProcessor;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcorej.CMMCore;
import org.micromanager.Studio;
import org.micromanager.data.Datastore;

/**
 *
 * @author stefko
 */
public class WorkerThread extends Thread {
    private boolean stop_flag = false;
    private long time_delay_ms = 100;
    
    private final Studio studio;
    private final Analyzer analyzer;
    private final Controller controller;
    private final Laser laser;
    
    public WorkerThread(Studio studio, Analyzer analyzer, Controller controller, Laser laser) {
        if (studio == null)
            throw new NullPointerException("You need to set a studio!");
        if (analyzer == null)
            throw new NullPointerException("You need to set an analyzer!");
        if (controller == null)
            throw new NullPointerException("You need to set a controller!");
        if (laser == null)
            throw new NullPointerException("You need to set a laser!");
        this.studio = studio;
        this.analyzer = analyzer;
        this.controller = controller;
        this.laser = laser;
    }
    
    public void requestStop() {
        stop_flag = true;
    }
    
    @Override
    public void run() {
        long last_time = System.currentTimeMillis();
        while (!stop_flag) {
            double analyzer_output; double controller_output;
            CMMCore core = studio.core();
            try {
                //analyzer.processImage(studio.core().getLastImage(), (int) studio.core().getImageWidth(), (int) studio.core().getImageHeight(), studio.core().getPixelSizeUm(), last_time);
                Datastore store = studio.live().getDisplay().getDatastore();
                ImageProcessor ip = studio.live().getDisplay().getImagePlus().getProcessor();
                analyzer.processImage(ip.getPixels(), ip.getWidth(), ip.getHeight(), studio.core().getPixelSizeUm(), last_time);
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
            analyzer_output = analyzer.getCurrentOutput();
            controller.nextValue(analyzer_output, last_time);
            controller_output = controller.getCurrentOutput();
            try {
                laser.setLaserPower(controller_output);
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
            
            long time_now = System.currentTimeMillis();
            if (time_now-last_time < time_delay_ms) {
                try {
                    Thread.sleep(time_delay_ms - (time_now-last_time));
                    last_time = System.currentTimeMillis();
                } catch (InterruptedException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        }
    }
    
    
    
}