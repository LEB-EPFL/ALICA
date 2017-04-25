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

/**
 *
 * @author stefko
 */
public class InvertController extends AbstractController {
    private final double value_at_1_mw;
    private double last_input = 1.0;
    
    public InvertController(double maximum, double value_at_1_mw) {
        super(maximum);
        this.value_at_1_mw = value_at_1_mw;
    }

    @Override
    public void nextValue(double value, long time_ms) {
        last_input = value;
    }

    @Override
    public double getCurrentOutput() {
        if (last_input == 0.0)
            return maximum;
        
        double out = value_at_1_mw * (1/last_input);
        if (out > maximum)
            return maximum;
        else
            return out;
    }
    
    @Override
    public String getName() {
        return "Inverter";
    }
}
