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
package ch.epfl.leb.alica.worker;

import ch.epfl.leb.alica.Analyzer;
import ch.epfl.leb.alica.Controller;
import ch.epfl.leb.alica.Laser;
import ij.process.ImageProcessor;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcorej.CMMCore;
import org.micromanager.Studio;
import org.micromanager.data.Datastore;
import org.micromanager.internal.graph.GraphData;

/**
 * The workhorse of the analysis. Grabs images from the MM core, analyzes them,
 * and controls the laser power while running.
 * @author Marcel Stefko
 */
public class WorkerThread extends Thread {
    private final boolean draw_from_core;
    private boolean stop_flag = false;
    private final long time_delay_ms = 1;
    private final long thread_start_time_ms;
    
    private final Studio studio;
    private final Analyzer analyzer;
    private final Controller controller;
    private final Laser laser;
    
    private final MonitorGUI gui;
    private final Grapher grapher;
    
    /**
     * Initialize the worker
     * @param studio MM studio
     * @param analyzer
     * @param controller
     * @param laser
     * @param draw_from_core whether the images should be drawn from the MMCore
     *  (true), or from the end of the processing pipeline (false)
     */
    public WorkerThread(Studio studio, Analyzer analyzer, Controller controller, Laser laser, boolean draw_from_core) {
        // sanitize input
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
        this.draw_from_core = draw_from_core;
        
        // initialize the GUI
        this.gui = new MonitorGUI(this, analyzer.getName(), controller.getName(), laser.getDeviceName()+"-"+laser.getPropertyName());
        gui.setLaserPowerDisplayMax(laser.getMaxPower());
        
        // display the GUI
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                gui.setVisible(true);
            }
        });
        
        // initialize the grapher
        this.grapher = new Grapher(100);
        
        // log the start time
        this.thread_start_time_ms = System.currentTimeMillis();
    }
    
    
    /**
     * Request the thread to stop after analyzing the current picture.
     */
    public void requestStop() {
        stop_flag = true;
    }
    
    /**
     * Set the controller setpoint to value
     * @param value new value of controller setpoint
     */
    public void setSetpoint(double value) {
        controller.setSetpoint(value);
    }
    
    /**
     * Returns time in milliseconds since the worker was initialized
     * @return elapsed time in milliseconds
     */
    public long getTimeMillis() {
        return System.currentTimeMillis() - thread_start_time_ms;
    }
    
    
    
    @Override
    public void run() {
        // starting time values
        long last_time = getTimeMillis();
        long fps_time = last_time;
        int fps_count = 0;
        
        // for easier access to the core
        final CMMCore core = studio.core();
        while (!stop_flag) {
            
            // draw the next image from the core or from the display
            try {
                if (draw_from_core) {
                    analyzer.processImage(studio.core().getLastImage(), (int) studio.core().getImageWidth(), (int) studio.core().getImageHeight(), studio.core().getPixelSizeUm(), last_time);
                } else {
                    Datastore store = studio.live().getDisplay().getDatastore();
                    ImageProcessor ip = studio.live().getDisplay().getImagePlus().getProcessor();
                    analyzer.processImage(ip.getPixels(), ip.getWidth(), ip.getHeight(), studio.core().getPixelSizeUm(), last_time);
                } 
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
            // log analysis time and increment FPS counter
            final int analysis_time_ms = (int) (getTimeMillis() - last_time);
            fps_count++;
            
            // query analyzer for output, plot it in the graph, send the value
            // to the controller, and get controller's output
            final double analyzer_output = analyzer.getCurrentOutput();
            grapher.addDataPoint(analyzer_output);
            controller.nextValue(analyzer_output, last_time);
            final double controller_output = controller.getCurrentOutput();
            
            try {
                // set the laser power to the controller's output
                laser.setLaserPower(controller_output);
                // update the displayed laser power value
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        gui.updateLaserPowerDisplay(controller_output);
                    }
                });
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
            
            // update general GUI elements
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    gui.updatePlot(grapher.getGraphData());
                    gui.updateLastAnalysisDuration(analysis_time_ms);
                }
            });
            
            // check if one second has elapsed since last FPS counter reset
            long time_now = getTimeMillis();
            final int fps_disp = fps_count;
            // if yes, display the FPS and reset FPS counter
            if (time_now-fps_time > 1000) {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        gui.updateFPS(fps_disp);
                    }
                });
                fps_count = 0;
                fps_time = time_now;
            }
            
            // this limits the FPS
            if (time_now-last_time < time_delay_ms) {
                try {
                    Thread.sleep(time_delay_ms - (time_now-last_time));
                } catch (InterruptedException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
            // 
            last_time = getTimeMillis();
        }
    }
}

class Grapher {
    private final GraphData graph_data;
    private int n_max;
    private int n_cur;
    private double[] datapoints;    
    
    /**
     * Initialize a grapher with set length of point plotting
     * @param n_points no. of points to be plotted
     */
    public Grapher(int n_points) {
        graph_data = new GraphData();
        this.n_max = n_points;
        this.n_cur = 0;
        datapoints = new double[n_points];
    }
    
    /**
     * Return GraphData which can then be plotted
     * @return GraphData
     */
    public GraphData getGraphData() {
        return graph_data;
    }
    
    /**
     * Add the next point to the grapher
     * @param value value to be added
     */
    public void addDataPoint(double value) {
        if (n_cur < n_max) {
            datapoints[n_cur] = value;
            n_cur++;
        } else {//shift everthing over one
            for(int ii = 0;ii<n_max-1;ii++){
                datapoints[ii]=datapoints[ii+1];
            }
            datapoints[n_max-1] = value;
        }
        graph_data.setData(datapoints);
    }
    
}