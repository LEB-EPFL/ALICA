/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.alica.analyzers.spotcounter;

import ch.epfl.leb.alica.Analyzer;
import ij.measure.ResultsTable;
import ij.process.ShortProcessor;
import java.awt.image.ColorModel;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author stefko
 */
public class SpotCounter implements Analyzer {
    private final int noise_tolerance;
    private final int box_size;
    private final SpotCounterCore core;
    
    private double current_output = 0.0;
    
    public SpotCounter(int noise_tolerance, int box_size) {
        this.noise_tolerance = noise_tolerance;
        this.box_size = box_size;
        this.core = new SpotCounterCore(noise_tolerance, box_size);
    }
    

    @Override
    public double getCurrentOutput() {
        return current_output;
    }

    @Override
    public String getName() {
        return "SpotCounter";
    }

    @Override
    public void processImage(Object image, int image_width, int image_height, double pixel_size_um, long time_ms) {
        
        double fov_area = pixel_size_um*pixel_size_um*image_width*image_height;
        ShortProcessor sp = new ShortProcessor(image_width, image_height);
        sp.setPixels(image);
        ResultsTable results = core.analyze(sp.duplicate());
        current_output = results.getValue("n", results.getCounter()-1) ;// fov_area;
        Logger.getLogger(this.getName()).log(Level.SEVERE, String.format("Image: %d, Density: %10.5f\n", results.getCounter(), current_output));
    }
    
}
