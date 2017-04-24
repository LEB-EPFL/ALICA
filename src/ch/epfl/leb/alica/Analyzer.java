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
public interface Analyzer {
    public void processImage(Object image, int image_width, int image_height, double pixel_size_um, long time_ms);
    
    public double getCurrentOutput();
    
    public String getName();
}
