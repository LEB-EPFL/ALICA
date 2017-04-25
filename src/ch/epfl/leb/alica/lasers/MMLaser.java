/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.alica.lasers;

import ch.epfl.leb.alica.Laser;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.micromanager.Studio;

/**
 *
 * @author stefko
 */
public class MMLaser implements Laser {
    private final Studio studio;
    
    private final String device_name;
    private final String property_name;
    private final double min_power;
    private final double max_power;
    
    public MMLaser(Studio studio, String device_name, String property_name, 
            double min_power, double max_power) {
        this.studio = studio;
        this.device_name = device_name;
        this.property_name = property_name;
        this.min_power = min_power;
        this.max_power = max_power;
    }

    @Override
    public double setLaserPower(double desired_power) throws Exception {
        double actual_power;
        if (desired_power > max_power) {
            actual_power = max_power;
        } else if (desired_power < min_power) {
            actual_power = min_power;
        } else {
            actual_power = desired_power;
        }
        
        //studio.core().setProperty(device_name, property_name, actual_power);
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, 
                String.format("Setting power to: %8.4f\n", actual_power));
        
        return actual_power;
    }

    @Override
    public double getLaserPower() throws Exception {
        return Double.parseDouble(studio.core().getProperty(device_name, property_name));
    }

    @Override
    public double getMaxPower() {
        return max_power;
    }

    @Override
    public double getMinPower() {
        return min_power;
    }
    
    @Override
    public String getDeviceName() {
        return device_name;
    }
    
    @Override
    public String getPropertyName() {
        return property_name;
    }
}
