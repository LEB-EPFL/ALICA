/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.alica.controllers.pid;

import ch.epfl.leb.alica.Controller;
import ch.epfl.leb.alica.controllers.AbstractController;

/**
 *
 * @author stefko
 */
public class PID_controller extends AbstractController {
    private final MiniPID core;
    private final double P,I,D,F;
    
    private double current_output = 0.0;
    
    
    public PID_controller(double P, double I, double D, double F, double output_max) {
        super(output_max);
        this.P = P; this.I = I; this.D = D; this.F = F; 
        this.core = new MiniPID(P,I,D,F);
        this.core.setOutputLimits(0.0, maximum);
    }
    
    @Override
    public void setTarget(double target) {
        core.setSetpoint(target);
    }

    @Override
    public void nextValue(double value, long time_ms) {
        current_output = core.getOutput(value);
    }

    @Override
    public double getCurrentOutput() {
        return current_output;
    }
    
}
