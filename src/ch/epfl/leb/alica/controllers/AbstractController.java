/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.alica.controllers;

import ch.epfl.leb.alica.Controller;

/**
 *
 * @author stefko
 */
public abstract class AbstractController implements Controller {
    protected double setpoint = 0.0;
    protected double maximum = 0.0;
    
    public AbstractController(double maximum) {
        this.maximum = maximum;
    }
    
    @Override
    public void setTarget(double target) {
        setpoint = target;
    }
    
}
