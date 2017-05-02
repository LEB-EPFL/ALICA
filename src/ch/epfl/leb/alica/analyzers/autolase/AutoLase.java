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
package ch.epfl.leb.alica.analyzers.autolase;


import ch.epfl.leb.alica.Analyzer;
import ij.process.ShortProcessor;
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Wrapper for Thomas Pengo's implementation of AutoLase algorithm.
 * @author Marcel Stefko
 */
public class AutoLase implements Analyzer {
    private final AutoLaseAnalyzer autolase_core;
    private ArrayList<Double> raw_value_history;
    

    private final int threshold;
    private final int averaging;
    
    /**
     * Initializes AutoLase with default threshold (120) and averaging (30) 
     * values.
     * @param threshold
     * @param averaging
     */
    public AutoLase(int threshold, int averaging) {
        this.threshold = threshold;
        this.averaging = averaging;
        autolase_core = new AutoLaseAnalyzer(threshold, averaging);
    }

    @Override
    public void processImage(Object image, int image_width, int image_height, double pixel_size_um, long time_ms) {
        ShortProcessor sp = new ShortProcessor(image_width, image_height, true);
        sp.setPixels(image);
        autolase_core.nextImage(sp);
    }

    @Override
    public double getIntermittentOutput() {
        return autolase_core.getCurrentValue();
    }

    @Override
    public String getName() {
        return "AutoLase";
    }

    @Override
    public double getBatchOutput() {
        return autolase_core.getCurrentValue();
    }

}


/**
 * This class estimates the density of activations. The density at a particular 
 * point relates 
 * to the maximum time a certain pixel is "on", or above a certain threshold. 
 * The density is calculated as a moving average 30 frames.
 * 
 * The code only works for 2 bytes per pixel cameras for now. 
 * 
 * @author Thomas Pengo
 */
class AutoLaseAnalyzer {
    public final int threshold;
    public final int averaging;
    
    boolean running = true;
    boolean stopping = false;
    
    double currentDensity = 0;
    int[][] accumulator = null;
    long lastTime;

    ArrayDeque<Double> density_fifo;
    
    public AutoLaseAnalyzer(int threshold, int averaging ) {
        this.density_fifo = new ArrayDeque<Double>(averaging);
        this.threshold = threshold;
        this.averaging = averaging;
        lastTime = System.currentTimeMillis();
    }
    
    /**
     * Analyzes next image and adjusts internal state.
     * @param image image to be analyzed
     */
    public void nextImage(ShortProcessor sp) {
        int width = sp.getWidth();
        int height = sp.getHeight();
        if (accumulator == null) {
            accumulator = new int[width][height];
        }
        
        if (accumulator.length!=width || accumulator[0].length!=height)
            accumulator = new int[width][height];
        // Threshold the image
        boolean[][] curMask = new boolean[width][height];
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                curMask[i][j]=sp.getPixel(i, j)>threshold;
            }
        }// A_i = (I_i > t) (1 + A_i-1)
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                if (!curMask[i][j]) {
                    accumulator[i][j] = 0;
                } else {
                    accumulator[i][j]++;
                }
            }
        }
        // Density measure: max(A_i)
        double curd = 0;
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                if (accumulator[i][j]>curd)
                    curd = accumulator[i][j];
            }
        }

        // Moving average estimate
        if (density_fifo.size() == averaging)
            density_fifo.remove();
        density_fifo.offer(curd);

        double mean_density = 0;
        for (Double d : density_fifo)
            mean_density+=d;
        mean_density /= density_fifo.size();

        currentDensity = mean_density;  
    }
    
    /**
     * Returns error signal value from AutoLase
     * @return estimated averaged max emitter density
     */
    public double getCurrentValue() {
        return currentDensity;
    }
    
    /**
     * Returns raw error signal value from AutoLase
     * @return estimated max emitter density for most recent frame
     */
    public double getRawCurrentValue() {
        return density_fifo.getLast();
    }
}