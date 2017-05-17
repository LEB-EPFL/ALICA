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
import ch.epfl.leb.alica.controllers.pi.PI_SetupPanel;
import ch.epfl.leb.alica.controllers.pid.PIDSetupPanel;
import ch.epfl.leb.alica.controllers.selftuningpi.SelfTuningSetupPanel;

/**
 * Controller Factory
 * @author Marcel Stefko
 */
public class ControllerFactory extends AbstractFactory<ControllerSetupPanel>{
    private double max_controller_output = 0.0;
    private double tick_rate_ms = 500;
    
    /**
     * Initialize the factory with known controllers
     */
    public ControllerFactory() {
        super();
        // add known controllers
        addSetupPanel("PI", new PI_SetupPanel());
        addSetupPanel("PID",new PIDSetupPanel());
        addSetupPanel("Manual", new ManualSetupPanel());
        addSetupPanel("Inverter", new InverterSetupPanel());
        addSetupPanel("Self-tuning", new SelfTuningSetupPanel());
        
        // set up default choice
        selectProduct("PI");
    }
    
    /**
     * Set maximal output value of the constructed controller
     * @param max_controller_output
     */
    public void setMaxControllerOutput(double max_controller_output) {
        this.max_controller_output = max_controller_output; 
    }
    
    /**
     * Set the tick rate at which the controller will operate.
     * @param tick_rate_ms tick rate in milliseconds
     */
    public void setControllerTickRateMs(double tick_rate_ms) {
        this.tick_rate_ms = tick_rate_ms;
    }
    
    /**
     * Build the selected controller using current settings
     * @return initialized controller
     */
    public Controller build() {
        return getSelectedSetupPanel().initController(max_controller_output, tick_rate_ms);
    }
}
