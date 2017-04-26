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
package ch.epfl.leb.alica;

/**
 * Laser recieves input from the controller and adjusts the laser power
 * accordingly.
 * @author Marcel Stefko
 */
public interface Laser {

    /**
     * Set the laser power to desired value
     * @param desired_power desired laser power value
     * @return actual laser power value
     * @throws Exception if error occurred during communication with hardware
     */
    public double setLaserPower(double desired_power) throws Exception;
    
    /**
     * Asks the hardware for current actual value of laser power
     * @return actual laser power value
     * @throws Exception if error occurred during communication with hardware
     */
    public double getLaserPower() throws Exception;
    
    /**
     * 
     * @return maximal allowed value of laser power
     */
    public double getMaxPower();
    
    /**
     *
     * @return minimal allowed value of laser power
     */
    public double getMinPower();
    
    /**
     *
     * @return unique device name (assigned by MicroManager)
     */
    public String getDeviceName();
    
    /**
     *
     * @return unique device property name (assigned by MicroManager)
     */
    public String getPropertyName();
}
