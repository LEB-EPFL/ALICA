/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.alica.controllers.inverter;

import ch.epfl.leb.alica.Controller;
import ch.epfl.leb.alica.controllers.AbstractController;

/**
 *
 * @author stefko
 */
public class InvertController extends AbstractController {
    private final double value_at_1_mw;
    private double last_input = 1.0;
    
    public InvertController(double maximum, double value_at_1_mw) {
        super(maximum);
        this.value_at_1_mw = value_at_1_mw;
    }

    @Override
    public void nextValue(double value, long time_ms) {
        last_input = value;
    }

    @Override
    public double getCurrentOutput() {
        if (last_input == 0.0)
            return maximum;
        
        double out = value_at_1_mw * (1/last_input);
        if (out > maximum)
            return maximum;
        else
            return out;
    }
    
    @Override
    public String getName() {
        return "Inverter";
    }
}
