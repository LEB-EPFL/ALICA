/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.alica.controllers;

import ch.epfl.leb.alica.AbstractFactory;
import ch.epfl.leb.alica.Controller;
import ch.epfl.leb.alica.controllers.inverter.InverterSetupPanel;
import ch.epfl.leb.alica.controllers.manual.ManualSetupPanel;
import ch.epfl.leb.alica.controllers.pid.PIDSetupPanel;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author stefko
 */
public class ControllerFactory extends AbstractFactory<ControllerSetupPanel>{
    
    public ControllerFactory() {
        super();
        addSetupPanel("PID",new PIDSetupPanel());
        addSetupPanel("Manual", new ManualSetupPanel());
        addSetupPanel("Inverter", new InverterSetupPanel());
        
        selectProduct("Manual");
    }
    
    public Controller build() {
        return getSelectedSetupPanel().initController();
    }
}
