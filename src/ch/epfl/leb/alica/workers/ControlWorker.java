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

import ch.epfl.leb.alica.Controller;
import ch.epfl.leb.alica.Laser;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author stefko
 */

public class ControlWorker extends Timer {
    
    private final ControlTask control_task;
    
    public ControlWorker(AnalysisWorker analysis_worker, Controller controller, Laser laser) {
        super();
        this.control_task = new ControlTask(analysis_worker, controller, laser);
    }
    
    public void scheduleExecution(long delay_ms, long period_ms) {
        this.scheduleAtFixedRate(control_task, delay_ms, period_ms);
    }
    
    public double getLastAnalyzerOutput() {
        return control_task.getLastAnalyzerOutput();
    }
    
    public double getLastControllerOutput() {
        return control_task.getLastControllerOutput();
    }
}


class ControlTask extends TimerTask {
    private final AnalysisWorker analysis_worker;
    private final Controller controller;
    private final Laser laser;
    
    private double last_analyzer_output = 0.0;
    private double last_controller_output = 0.0;

    public ControlTask(AnalysisWorker analysis_worker, Controller controller, Laser laser) {
        super();
        this.analysis_worker = analysis_worker;
        this.controller = controller;
        this.laser = laser;
    }
    
    @Override
    public void run() {
        synchronized(this) {
            double analyzer_output = analysis_worker.queryAnalyzerForBatchOutput();
            if (Double.isNaN(analyzer_output))
                analyzer_output = last_analyzer_output;
            else
                last_analyzer_output = analyzer_output;
            controller.nextValue(analyzer_output);
            last_controller_output = controller.getCurrentOutput();
            try {
                laser.setLaserPower(last_controller_output);
            } catch (Exception ex) {
                Logger.getLogger(ControlWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public double getLastAnalyzerOutput() {
        synchronized(this) {
            return last_analyzer_output;
        }
    }
    
    public double getLastControllerOutput() {
        synchronized(this) {
            return last_controller_output;
        }
    }
    
    
    
}
