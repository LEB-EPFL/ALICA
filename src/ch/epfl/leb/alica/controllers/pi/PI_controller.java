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
package ch.epfl.leb.alica.controllers.pi;

import ch.epfl.leb.alica.Controller;
import ch.epfl.leb.alica.controllers.ControllerStatusPanel;

/**
 * Simple implementation of PI controller with output constraining and windup
 * prevention.
 * @author Marcel Stefko
 */
public class PI_controller implements Controller {
    /**
     * Proportional component
     */
    protected double P;
    
    /**
     * Integral component
     */
    protected double I;
    
    private final double max_output;
    private final double min_output;
    
    private double setpoint = 0.0;
    
    /**
     * Last calculated output of the controller
     */
    protected double current_output = 0.0;
    
    private double integral = 0.0;
    
    /**
     * Initialize the PI controller.
     * @param P proportional component
     * @param I_per_second integral component (per second)
     * @param max_output maximal output value
     * @param sampling_period_s controller tick rate in seconds
     */
    public PI_controller(double P, double I_per_second, double max_output, double sampling_period_s) {
        if (max_output < 0.0 || P < 0.0 || I_per_second < 0.0 || sampling_period_s < 0.0)
            throw new IllegalArgumentException("Invalid PI controller parameter.");
        this.P = P;
        this.I = I_per_second * sampling_period_s;
        this.max_output = max_output;
        this.min_output = 0.0;
    }
    
    @Override
    public void setSetpoint(double new_setpoint) {
        if (new_setpoint < 0.0 || Double.isNaN(new_setpoint)) {
            throw new IllegalArgumentException("Setpoint can't be negative or NaN!");
        }
        if (new_setpoint == 0.0 && integral > 0.0)
            // of setpoint is set to 0, reset integral so it winds down immediately
            this.integral = 0.0;
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
    }

    @Override
    public double getCurrentOutput() {
        return this.current_output;
    }

    @Override
    public String getName() {
        return "PI controller";
    }

    @Override
    public ControllerStatusPanel getStatusPanel() {
        return null;
    }
    
}
