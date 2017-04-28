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
package ch.epfl.leb.alica.controllers.inverter;

import ch.epfl.leb.alica.Controller;
import ch.epfl.leb.alica.controllers.AbstractController;
import ij.IJ;

/**
 * Controller which inverts and scales the input using 1/x function. 
 * (high input -> low output, low input -> high output)
 * @author Marcel Stefko
 */
public class InvertController implements Controller {
    private double value_at_1_mw;
    private double last_input = 1.0;

    /**
     * Maximal possible output value.
     */
    protected double maximum = 0.0;


    
    /**
     * Initializes the InvertController
     * @param maximum max output value
     * @param value_at_1_mw what is the value of input that you want to cause
     *  an output value of 1.0 (scaling constant)
     */
    public InvertController(double maximum, double value_at_1_mw) {
        if (maximum<=0.0) {
            throw new IllegalArgumentException("Maximum must be positive.");
        }
        this.maximum = maximum;
        this.value_at_1_mw = value_at_1_mw;
    }
    
    /**
     * Sets the scaling constant, since it basically fulfills the role of 
     * setpoint for this controller.
     * @param value new scaling constant 
     */
    @Override
    public void setSetpoint(double value) {
        if (value<=0.0) {
            IJ.showMessage("Setpoint must be positive!");
            return;
        }
        this.value_at_1_mw = value;
    }

    @Deprecated
    public void nextValue(double value, long time_ms) {
        last_input = value;
    }

    @Override
    public double getCurrentOutput() {
        // edge case of input being 0, return the maximal output value
        if (last_input == 0.0)
            return maximum;
        
        // calculate the output
        double out = value_at_1_mw * (1/last_input);
        
        // make sure we don't return more than the maximal possible value
        out = (out > maximum) ? maximum : out;
        return out;
    }
    
    @Override
    public String getName() {
        return "Inverter";
    }
    
    public void setValueAt1Mw(double value) {
        if (value<=0.0)
            throw new IllegalArgumentException("Value must be positive!");
        this.value_at_1_mw = value;
    }
    
    public double getSetpoint() {
        return this.value_at_1_mw;
    }

    @Override
    public double nextValue(double value) {
        last_input = value;
        return getCurrentOutput();
    }
}
