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
package ch.epfl.leb.alica.controllers.selftuningpi;

import ch.epfl.leb.alica.AlicaLogger;
import ch.epfl.leb.alica.controllers.ControllerStatusPanel;
import ch.epfl.leb.alica.controllers.pi.PI_controller;

/**
 * A self-tuning implementation of the PI controller. It waits for 10 cycles,
 * and over next 20 cycles generates a step pulse and measures response. From
 * this response it estimates the P and I components of the PID controller.
 * @author Marcel Stefko
 */
public class SelfTuningController extends PI_controller {
    private final SelfTuningStatusPanel status_panel;
    
    private double step_height;
    private final double sampling_period_s;
    private double p_factor;
    private double i_factor;
    private int init_counter;
    private boolean init_sequence;
    private double zero_power_signal;
    private double with_power_signal;
    
    /**
     * Initialize a new SelfTuningController
     * @param max_output max output
     * @param sampling_period_s tick rate of controller in seconds
     * @param step_height how big step pulse should be generated
     * @param p_factor scaling factor for P in tuning
     * @param i_factor scaling factor for I in tuning
     */
    public SelfTuningController(double max_output, double sampling_period_s) {
        super(0.0,0.0, max_output, sampling_period_s);

        this.sampling_period_s = sampling_period_s;
        this.status_panel = new SelfTuningStatusPanel(this);
        // initial values
        this.P = 0.0;
        this.I = 0.0;
        this.init_sequence = false;
    }
    
    public final void recalibrate(double step_height, double p_factor, double i_factor) {
        if (step_height <= 0.0 || p_factor <= 0.0 || i_factor <= 0.0)
            throw new IllegalArgumentException("Invalid self-tuning controller parameter!");
        this.with_power_signal = 0.0;
        this.zero_power_signal = 0.0;
        this.init_sequence = true;
        this.init_counter = -5;
        this.step_height = step_height;
        this.p_factor = p_factor;
        this.i_factor = i_factor;       
    }
    

    @Override
    public double nextValue(double value) {
        if (is_blocked) {
            return 0.0;
        }
        if (Double.isNaN(value) || Double.isInfinite(value))
            return current_output;
        if (!init_sequence) {
            return super.nextValue(value);
        } else {
            return initSequence(value);
        }
    }
    
    private double initSequence(double value) {
        init_counter++;
        AlicaLogger.getInstance().logDebugMessage("Counter: "+init_counter+"\nSignal value: "+value);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                status_panel.setCalibrationStatusDisplay(
                   String.format("Calibrating... %d%%",100*(5 + init_counter)/21));
            }
        });
        
        
        if (init_counter <= 0) {
            // just wait for sample stabilization
            current_output = 0.0;
        } else if (init_counter == 1) {
            //skip
        } else if (init_counter < 8) {
            // accumulate zero power signal 
            zero_power_signal += value;
        } else if (init_counter == 8) {
            // average out zero power signal
            zero_power_signal += value;
            zero_power_signal /= 7;
            AlicaLogger.getInstance().logDebugMessage(
             String.format("Tuning: signal at zero power: %e", zero_power_signal));
            // increase output to step height
            current_output = step_height;
        } else if (init_counter == 9) {
            //skip
        } else if (init_counter < 16) {
            // accumulate signal with power
            with_power_signal += value;
        } else if (init_counter == 16) {
            // average out signal with power
            with_power_signal += value;
            with_power_signal /= 7;
            AlicaLogger.getInstance().logDebugMessage(
             String.format("Tuning: signal at %5.2f power: %e", step_height, with_power_signal));
            // calculate difference of signals with and without power
            double error =  with_power_signal - zero_power_signal;
            // calculate P and I according to coefficients
            P = step_height * p_factor / error;
            I = step_height * sampling_period_s * i_factor / error;
            if (P<0.0 || I<0.0) {
                AlicaLogger.getInstance().showMessage("Self-tuning failed! Components were calculated to be negative. Turning laser off.");
                P = 0.0; I = 0.0;
            } else {
                AlicaLogger.getInstance().logMessage("Self-tuning successful!\n - P = " + P + "\n - I = " + I);
            }
            AlicaLogger.getInstance().logMessage(
             "Controller calibrated:\n - P = " + P + "\n - I = " + I);
            // set output to 0 and start normal operation
            current_output = 0.0;
            init_sequence = false;
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    status_panel.setCalibrationStatusDisplay("Calibrated.");
                    status_panel.setValuesDisplay(P, I);
                }
            });

        } else {
            //skip
        }
        return current_output;
    }

    @Override
    public String getName() {
        return "Self-Tuning PI";
    }
    
    @Override
    public ControllerStatusPanel getStatusPanel() {
        return status_panel;
    }
    

}
