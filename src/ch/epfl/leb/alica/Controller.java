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
public interface Controller {
    /**
     * Sets target value for error signal.
     * @param target desired error signal value (setpoint)
     */
    public void setTarget(double target);
    
    public void nextValue(double value, long time_ms);
    
    public double getCurrentOutput();
    
    public String getName();
}
