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
package ch.epfl.leb.alica.controllers;

import ch.epfl.leb.alica.AbstractFactory;
import ch.epfl.leb.alica.Controller;
import ch.epfl.leb.alica.controllers.inverter.InverterSetupPanel;
import ch.epfl.leb.alica.controllers.manual.ManualSetupPanel;
import ch.epfl.leb.alica.controllers.pid.PIDSetupPanel;

/**
 * Controller Factory
 * @author Marcel Stefko
 */
public class ControllerFactory extends AbstractFactory<ControllerSetupPanel>{
    
    /**
     * Add known Controllers
     */
    public ControllerFactory() {
        super();
        addSetupPanel("PID",new PIDSetupPanel());
        addSetupPanel("Manual", new ManualSetupPanel());
        addSetupPanel("Inverter", new InverterSetupPanel());
        
        selectProduct("Manual");
    }
    
    /**
     * Build the selected controller using current settings
     * @return initialized controller
     */
    public Controller build() {
        return getSelectedSetupPanel().initController();
    }
}
