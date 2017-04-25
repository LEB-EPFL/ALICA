/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.alica.controllers;

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
public class ControllerFactory {
    private String current_selected_name = null;
    private final HashMap<String, ControllerSetupPanel> controller_setup_panels;
    
    
    public ControllerFactory() {
        controller_setup_panels = new HashMap<String,ControllerSetupPanel>();
        controller_setup_panels.put("PID",new PIDSetupPanel());
        controller_setup_panels.put("Manual", new ManualSetupPanel());
        controller_setup_panels.put("Inverter", new InverterSetupPanel());
        
        current_selected_name = "Manual";
    }
    
    public Controller buildController() {
        return controller_setup_panels.get(current_selected_name).initController();
    }
    
    public String getCurrentController() {
        return current_selected_name;
    }
    
    public Set<String> getControllerList() {
        return controller_setup_panels.keySet();
    }
    
    public void selectController(String name) {
        if (!controller_setup_panels.containsKey(name)) {
            throw new IllegalArgumentException("No such controller: "+name);
        }
        current_selected_name = name;
    }
    
    public ControllerSetupPanel getSelectedSetupPanel() {
        return controller_setup_panels.get(current_selected_name);
    }
    
    
}
