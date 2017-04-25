/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.alica.controllers.manual;

import ch.epfl.leb.alica.controllers.AbstractController;

/**
 *
 * @author stefko
 */
public class ManualController extends AbstractController {

    public ManualController(double maximum) {
        super(maximum);
    }

    @Override
    public void nextValue(double value, long time_ms) {
        return;
    }

    @Override
    public double getCurrentOutput() {
        if (setpoint < maximum)
            return setpoint;
        else
            return maximum;
    }
    
    @Override
    public String getName() {
        return "Manuals";
    }
    
}
