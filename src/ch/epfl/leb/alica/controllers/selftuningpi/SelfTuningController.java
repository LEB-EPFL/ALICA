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

import ch.epfl.leb.alica.Controller;
import org.micromanager.internal.MMStudio;

/**
 * A self-tuning implementation of the PI controller. It waits for 10 cycles,
 * and over next 10 cycles generates a step pulse and measures response. From
 * this response it estimates the P and I components of the PID controller.
 * @author Marcel Stefko
 */
public class SelfTuningController implements Controller {
    private double P;
    private double I;
    private final double max_output;
    private final double min_output;
    private final double step_height;
    private final double sampling_period_s;
    private final double p_factor;
    private final double i_factor;
    
    private double setpoint = 0.0;
    private double current_output = 0.0;
    
    private double integral = 0.0;
    
    private int init_counter = -10;
    private boolean init_sequence = true;
    private double zero_power_signal = 0.0;
    private double with_power_signal = 0.0;
    
    /**
     * Initialize a new SelfTuningController
     * @param max_output max output
     * @param sampling_period_s tick rate of controller in seconds
     * @param step_height how big step pulse should be generated
     * @param p_factor scaling factor for P in tuning
     * @param i_factor scaling factor for I in tuning
     */
    public SelfTuningController(double max_output, double sampling_period_s, double step_height, double p_factor, double i_factor) {
        if (max_output < 0.0 || sampling_period_s < 0.0 || step_height < 0.0)
            throw new IllegalArgumentException("Invalid PI controller parameter.");
        this.max_output = max_output;
        this.min_output = 0.0;
        this.step_height = step_height;
        this.sampling_period_s = sampling_period_s;
        this.p_factor = p_factor;
        this.i_factor = i_factor;
    }
    
    @Override
    public void setSetpoint(double new_setpoint) {
        if (new_setpoint < 0.0 || Double.isNaN(new_setpoint)) {
            throw new IllegalArgumentException("Setpoint can't be negative or NaN!");
        }
        this.setpoint = new_setpoint;
    }

    @Override
    public double getSetpoint() {
        return this.setpoint;
    }

    @Override
    public double nextValue(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value))
            return current_output;
        if (!init_sequence) {
            final double error = setpoint - value;

            final double P_output = P * error;

            integral += I * error;
            // back-calculate integral so we don't have windup
            final double desiredMaxIoutput = max_output - P_output;
            if (integral > desiredMaxIoutput)
                integral = desiredMaxIoutput;
            final double desiredMinIoutput = min_output - P_output;
            if (integral < desiredMinIoutput)
                integral = desiredMinIoutput;

            final double I_output = integral;

            current_output = P_output + I_output;
            return current_output;
        } else {
            init_counter++;
            if (init_counter == 0) {
                current_output = 0.0;
                
            } else if (init_counter == 1) {
                //skip
            } else if (init_counter < 3) {
                zero_power_signal += value;
            } else if (init_counter == 4) {
                zero_power_signal += value;
                zero_power_signal /= 3;
                MMStudio.getInstance().logs().logMessage(
                 String.format("Tuning: signal at zero power: %e", zero_power_signal));
                current_output = step_height;
            } else if (init_counter == 5) {
                //skip
            } else if (init_counter < 8) {
                with_power_signal += value;
            } else if (init_counter == 8) {
                with_power_signal += value;
                with_power_signal /= 3;
                MMStudio.getInstance().logs().logMessage(
                 String.format("Tuning: signal at %5.2f power: %e", step_height, with_power_signal));
                double error = with_power_signal - zero_power_signal;
                P = step_height * p_factor / error;
                I = P * sampling_period_s * i_factor;
                if (P<0.0 || I<0.0) {
                    MMStudio.getInstance().logs().showError("Self-tuning failed! Components were calculated to be negative. Turning laser off.");
                    P = 0.0; I = 0.0;
                } else {
                    MMStudio.getInstance().logs().showMessage("Self-tuning successful!\n - P = " + P + "\n - I = " + I);
                }
                MMStudio.getInstance().logs().logMessage(
                 "Controller calibrated:\n - P = " + P + "\n - I = " + I);
                
                current_output = 0.0;
                init_sequence = false;
            } else {
                //skip
            }
            return current_output;
        }
    }

    @Override
    public double getCurrentOutput() {
        return this.current_output;
    }

    @Override
    public String getName() {
        return "Self-Tuning PI";
    }
    
}
