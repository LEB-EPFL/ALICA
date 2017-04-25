/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.alica.lasers;

import ch.epfl.leb.alica.Laser;
import java.util.Set;
import mmcorej.StrVector;
import org.micromanager.Studio;

/**
 *
 * @author stefko
 */
public class LaserFactory {
    private final Studio studio;
    private String selected_name;
    private String selected_property;
    public LaserFactory(Studio studio) {
        this.studio = studio;
        selected_name = getPossibleLasers().get(0);
    }
    
    public StrVector getPossibleLasers() {
        return studio.core().getLoadedDevices();
    }
    
    public void selectDevice(String name) {
        selected_name = name;
    }
    
    public String getSelectedDeviceName() {
        return selected_name;
    }
    
    public StrVector getSelectedDeviceProperties() throws Exception {
        return studio.core().getDevicePropertyNames(selected_name);
    }
    
    public void selectProperty(String property) {
        selected_property = property;
    }
    
    public Laser build() {
        return new MMLaser(studio, selected_name, selected_property, 0.0, 42.0);
    }
}
