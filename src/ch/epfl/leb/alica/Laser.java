/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.alica;

/**
 *
 * @author stefko
 */
public interface Laser {
    public double setLaserPower(double desired_power) throws Exception;
    
    public double getLaserPower() throws Exception;
    
    public double getMaxPower();
    
    public double getMinPower();
    
    public String getDeviceName();
    
    public String getPropertyName();
}
