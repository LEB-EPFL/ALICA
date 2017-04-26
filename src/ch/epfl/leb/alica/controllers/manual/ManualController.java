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
package ch.epfl.leb.alica.controllers.manual;

import ch.epfl.leb.alica.controllers.AbstractController;

/**
 * Manual controller. Output is equal to setpoint value, any input is ignored.
 * @author Marcel Stefko
 */
public class ManualController extends AbstractController {
    /**
     * Initialize with maximal output value
     * @param maximum max output value
     */
    public ManualController(double maximum, double initial_output) {
        super(maximum);
        if (initial_output<0.0 || initial_output>maximum)
            throw new IllegalArgumentException("Initial output must not be negative or higher than maximum.");
        setSetpoint(initial_output);
    }

    @Override
    public void nextValue(double value, long time_ms) {
        // ignore any input
        return;
    }

    @Override
    public double getCurrentOutput() {
        // don't return a value higher than maximum
        if (setpoint < maximum)
            return setpoint;
        else
            return maximum;
    }
    
    @Override
    public String getName() {
        return "Manuals";
    }

    
}
