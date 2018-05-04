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

import ch.epfl.leb.alica.AlicaLogger;
import ch.epfl.leb.alica.interfaces.Controller;
import ch.epfl.leb.alica.Laser;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A Timer which schedules a task that regularly queries the AnalysisWorker
 * for batched output, and passes it on to the controller, then gets the
 * controller's output and passes it on to the laser.
 * @author Marcel Stefko
 */

public class ControlWorker extends Timer {
    
    private final ControlTask control_task;
    
    /**
     * Initialize the ControlWorker
     * @param analysis_worker AnalysisWorker which will be queried for output
     * @param controller Controller to which output of AnalysisWorker is fed
     * @param laser Laser to which output of Controller is fed
     */
    public ControlWorker(AnalysisWorker analysis_worker, Controller controller, Laser laser) {
        super();
        // initialize the task
        this.control_task = new ControlTask(analysis_worker, controller, laser);
    }
    
    /**
     * The task of this worker will be executed regularly.
     * @param delay_ms initial delay
     * @param period_ms period of the task
     */
    public void scheduleExecution(long delay_ms, long period_ms) {
        this.scheduleAtFixedRate(control_task, delay_ms, period_ms);
    }
    
    /**
     * 
     * @return last controller output
     */
    public double getLastControllerOutput() {
        return control_task.getLastControllerOutput();
    }
}

/**
 * This TimerTask is run periodically by the ControlWorker
 * @author Marcel Stefko
 */
class ControlTask extends TimerTask {
    private final AnalysisWorker analysis_worker;
    private final Controller controller;
    private final Laser laser;
    
    private double last_analyzer_output = 0.0;
    private double last_controller_output = 0.0;
    
    private boolean laser_error_displayed = false;

    /**
     * Initialize the ControlTask
     * @param analysis_worker AnalysisWorker which will be queried for output
     * @param controller Controller to which output of AnalysisWorker is fed
     * @param laser Laser to which output of Controller is fed
     */
    public ControlTask(AnalysisWorker analysis_worker, Controller controller, Laser laser) {
        super();
        this.analysis_worker = analysis_worker;
        this.controller = controller;
        this.laser = laser;
    }
    
    @Override
    public void run() {
        synchronized(this) {
            // get batch output of the analyzer
            double analyzer_output = analysis_worker.queryAnalyzerForBatchOutput();
            last_analyzer_output = analyzer_output;
            // pass output to the controller and get next output
            last_controller_output = controller.nextValue(analyzer_output);
            
            AlicaLogger.getInstance().addControllerOutput(analysis_worker.getCurrentImageCount(), last_controller_output);
            
            // adjust the laser power
            try {
                laser.setLaserPower(last_controller_output);
            } catch (Exception ex) {
                if (!laser_error_displayed) {
                    AlicaLogger.getInstance().showError(ex, "Error in setting laser power to " + 
                            last_controller_output + ". Further errors will not be displayed.");
                    laser_error_displayed = true;
                } else {
                    AlicaLogger.getInstance().logError(ex, "Error in setting laser power to " + 
                            last_controller_output);
                }
            }
        }
    }
    
    /**
     * 
     * @return last controller output
     */
    public double getLastControllerOutput() {
        synchronized(this) {
            return last_controller_output;
        }
    }
    
    
    
}
