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

import ch.epfl.leb.alica.Controller;
import ch.epfl.leb.alica.controllers.AbstractController;

/**
 * Manual controller. Output is equal to setpoint value, any input is ignored.
 * @author Marcel Stefko
 */
public class ManualController implements Controller {
    
    /**
     * Maximal possible output value.
     */
    protected double maximum = 0.0;
    
    protected double setpoint;
    
    /**
     * Initialize with maximal output value
     * @param maximum max output value
     */
    public ManualController(double maximum, double initial_output) {
        if (maximum<=0.0) {
            throw new IllegalArgumentException("Maximum must be positive.");
        }
        this.maximum = maximum;
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
        return getSetpoint();
    }
    
    @Override
    public String getName() {
        return "Manuals";
    }

    @Override
    public double nextValue(double value) {
        return getCurrentOutput();
    }

    @Override
    public void setSetpoint(double new_setpoint) {
        if (new_setpoint>maximum)
            throw new IllegalArgumentException("New setpoint can't be higher than maximum!");
        if (new_setpoint<=0.0)
            throw new IllegalArgumentException("New setpoint must be positive!");
        setpoint = new_setpoint;
    }

    @Override
    public double getSetpoint() {
        return setpoint;
    }

    
}
