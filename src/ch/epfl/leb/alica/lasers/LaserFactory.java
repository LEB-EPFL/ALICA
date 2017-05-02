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
package ch.epfl.leb.alica.lasers;

import ch.epfl.leb.alica.Laser;
import mmcorej.StrVector;
import org.micromanager.Studio;

/**
 * LaserFactory
 * @author Marcel Stefko
 */
public final class LaserFactory {
    private final Studio studio;
    private String selected_name;
    private String selected_property;
    private boolean is_laser_virtual;
    private double max_laser_power;
    private double laser_power_deadzone = 0.0;

    /**
     * Initialize the factory with the MM studio
     * @param studio
     */
    public LaserFactory(Studio studio) {
        is_laser_virtual = true;
        this.studio = studio;
        selected_name = getPossibleLasers().get(0);
    }
    
    /**
     * Query the MMCore for list of loaded devices
     * @return StrVector list of loaded devices in the core
     */
    public StrVector getPossibleLasers() {
        return studio.core().getLoadedDevices();
    }
    
    /**
     * Select a device
     * @param name unique device identifier from the MMCore
     */
    public void selectDevice(String name) {
        selected_name = name;
    }
    
    /**
     * 
     * @return currently selected device identifier
     */
    public String getSelectedDeviceName() {
        return selected_name;
    }
    
    /**
     * Query the MMCore for properties of the selected device
     * @return StrVector list of properties
     * @throws Exception if hardware communication fails
     */
    public StrVector getSelectedDeviceProperties() throws Exception {
        return studio.core().getDevicePropertyNames(selected_name);
    }
    
    /**
     * Select a property of the currently selected device
     * @param property unique property identifier from MMCore
     */
    public void selectProperty(String property) {
        selected_property = property;
    }
    
    /**
     * If true, create a VirtualLaser, otherwise a MMLaser
     * @param is_laser_virtual
     */
    public void setLaserVirtual(boolean is_laser_virtual) {
        this.is_laser_virtual = is_laser_virtual;
    }
    
    /**
     * Sets upper boundary for laser output, higher inputs from controller will
     * be constrained.
     * @param max_laser_power
     */
    public void setMaxLaserPower(double max_laser_power) {
        this.max_laser_power = max_laser_power;
    }
    
    /**
     * Sets the deadzone of change of laser power output. For example, if set
     * to 0.1, the laser would ignore requests for change of power that would
     * be different by less than 10% from current output power.
     * @param laser_power_deadzone deadzone size (NOT in percent)
     */
    public void setLaserPowerDeadzone(double laser_power_deadzone) {
        this.laser_power_deadzone = laser_power_deadzone;
    }
    
    /**
     * Build the laser using the current state
     * @return initialized Laser
     */
    public Laser build() {
        if (this.is_laser_virtual)
            return new VirtualLaser(studio, selected_name, selected_property, 0.0, max_laser_power);
        else
            return new MMLaser(studio, selected_name, selected_property, 0.0, max_laser_power, laser_power_deadzone);
    }
}
