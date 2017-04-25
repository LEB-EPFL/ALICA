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
import org.micromanager.internal.graph.GraphFrame;

/**
 *
 * @author stefko
 */
public class WorkerThread extends Thread {
    private final boolean draw_from_core;
    private boolean stop_flag = false;
    private long time_delay_ms = 1;
    
    private final Studio studio;
    private final Analyzer analyzer;
    private final Controller controller;
    private final Laser laser;
    
    private final MonitorGUI gui;
    private final Grapher grapher;
    
    
    public WorkerThread(Studio studio, Analyzer analyzer, Controller controller, Laser laser, boolean draw_from_core) {
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
        
        this.gui = new MonitorGUI(this, analyzer.getName(), controller.getName(), laser.getDeviceName()+"-"+laser.getPropertyName());
        gui.setLaserPowerMax(laser.getMaxPower());
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                gui.setVisible(true);
            }
        });
        this.grapher = new Grapher(100);
    }
    
    public void requestStop() {
        stop_flag = true;
    }
    
    public void setSetpoint(double value) {
        controller.setTarget(value);
    }
    
    @Override
    public void run() {
        long last_time = System.currentTimeMillis();
        long fps_time = last_time;
        int fps_count = 0;
        while (!stop_flag) {
            CMMCore core = studio.core();
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
            final int analysis_time_ms = (int) (System.currentTimeMillis() - last_time);
            fps_count++;
            final double analyzer_output = analyzer.getCurrentOutput();
            grapher.addDataPoint(analyzer_output);
            controller.nextValue(analyzer_output, last_time);
            final double controller_output = controller.getCurrentOutput();
            try {
                laser.setLaserPower(controller_output);
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        gui.showLaserPower(controller_output);
                    }
                });
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
            
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    gui.updatePlot(grapher.getGraphData());
                    gui.showLastAnalysisDuration(analysis_time_ms);
                }
            });
            
            long time_now = System.currentTimeMillis();
            final int fps_disp = fps_count;
            if (time_now-fps_time > 1000) {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        gui.showFPS(fps_disp);
                    }
                });
                fps_count = 0;
                fps_time = time_now;
            }
            
                
            if (time_now-last_time < time_delay_ms) {
                try {
                    Thread.sleep(time_delay_ms - (time_now-last_time));
                    
                } catch (InterruptedException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
                
            }
            last_time = System.currentTimeMillis();
        }
    }
}

class Grapher {
    private final GraphData graph_data;
    private int n_max;
    private int n_cur;
    private double[] datapoints;    
    
    public Grapher(int n_points) {
        graph_data = new GraphData();
        this.n_max = n_points;
        this.n_cur = 0;
        datapoints = new double[n_points];
    }
    
    public GraphData getGraphData() {
        return graph_data;
    }
    
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