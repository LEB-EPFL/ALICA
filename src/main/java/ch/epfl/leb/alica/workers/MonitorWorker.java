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
package ch.epfl.leb.alica.workers;

import java.util.Timer;
import java.util.TimerTask;
import org.micromanager.internal.graph.GraphData;

/**
 * Updates GUI with recent information from other workers
 * @author Marcel Stefko
 */
public class MonitorWorker extends Timer {
    private final MonitorTask monitor_task;
    private final MonitorGUI gui;
    
    /**
     * Initialize new worker for monitoring
     * @param gui MonitorGUI to be updated
     * @param analysis_worker
     * @param control_worker
     */
    public MonitorWorker(MonitorGUI gui, AnalysisWorker analysis_worker, ControlWorker control_worker) {
        super();
        monitor_task = new MonitorTask(gui, analysis_worker, control_worker);
        this.gui = gui;
    }
    
    /**
     * The task of this worker will be executed regularly.
     * @param delay_ms initial delay
     * @param period_ms period of the task
     */
    public void scheduleExecution(long delay_ms, long period_ms) {
        this.scheduleAtFixedRate(monitor_task, delay_ms, period_ms);
    }
    
    @Override
    public void cancel() {
        super.cancel();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                gui.setStopped();
            }
        });
    }
}

/**
 * This TimerTask updates GUI with recent information from other workers
 * @author Marcel Stefko
 */
class MonitorTask extends TimerTask {
    private final AnalysisWorker analysis_worker;
    private final ControlWorker control_worker;
    private final MonitorGUI gui;
    
    private final Grapher grapher;
    
    /**
     * Initialize new task with relevant members
     * @param gui MonitorGUI to be updated
     * @param analysis_worker
     * @param control_worker
     */
    public MonitorTask(MonitorGUI gui, AnalysisWorker analysis_worker, ControlWorker control_worker) {
        super();
        this.gui = gui;
        this.analysis_worker = analysis_worker;
        this.control_worker = control_worker;
        this.grapher = new Grapher(100);
    }
    
    @Override
    public void run() {
        // gather relevant data from workers
        final double laser_power = control_worker.getLastControllerOutput();
        final double analyzer_output = analysis_worker.queryAnalyzerForIntermittentOutput();
        final int FPS = analysis_worker.getCurrentFPS();
        final int last_analysis_time = (int) analysis_worker.getLastAnalysisTime();
        
        // update the grapher
        grapher.addDataPoint(analyzer_output);
        
        // display data in MonitorGUI
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                gui.updateLaserPowerDisplay(laser_power);
                gui.updateFPS(FPS);
                gui.updateLastAnalysisDuration(last_analysis_time);
                gui.updatePlot(grapher.getGraphData());
            }
        });
    }
}

/** 
 * Wrapped around GraphData for easier processing
 * @author Marcel Stefko
 */
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
