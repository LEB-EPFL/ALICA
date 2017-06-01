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
package ch.epfl.leb.alica.analyzers.integrator;

import ch.epfl.leb.alica.Analyzer;
import ch.epfl.leb.alica.analyzers.AnalyzerStatusPanel;
import ij.gui.Roi;
import ij.process.ShortProcessor;
import java.util.ArrayList;

/**
 * Analyzer which just outputs average pixel value per frame. Not really useless,
 * but good as an example.
 * @author Marcel Stefko
 */
public class Integrator implements Analyzer {
    // array for storing outputs since last batchedoutput query
    private final ArrayList<Double> intermittent_outputs = new ArrayList<Double>();
    private int min;
    private boolean start = true;
    
    // region of interest to confine analysis to
    private Roi roi;
    
    // last calculated output
    private double intermittent_output = 0.0;
    
    public Integrator() {
        this.min = Short.MAX_VALUE;
    }
    
    @Override
    public void processImage(Object image, int image_width, int image_height, double pixel_size_um, long time_ms) {
        // set boundaries from roi or image edges
        int x_min, x_max, y_min, y_max;
        if (roi == null) {
            x_min = 0; y_min = 0;
            x_max = image_width; y_max = image_height;
        } else {
            x_min = roi.getBounds().x;
            x_max = x_min + roi.getBounds().width;
            y_min = roi.getBounds().y;
            y_max = y_min + roi.getBounds().height;
        }
        
        // create shortprocessor for accessing pixels
        ShortProcessor sp = new ShortProcessor(image_width, image_height);
        sp.setPixels(image);
        
        // sum up all pixels
        long sum = 0;
        for (int x=x_min; x<x_max; x++) {
            for (int y=y_min; y<y_max; y++) {
                sum += sp.getPixel(x, y);
            }
        }
        
        if (start) {
            for (int x=x_min; x<x_max; x++) {
                for (int y=y_min; y<y_max; y++) {
                    if (sp.getPixel(x, y)<min) {
                        min = sp.getPixel(x,y);
                    }
                }
            }
            start = false;
        }
        
        // divide by area and store
        intermittent_output = ((double)sum)/((x_max-x_min)*(y_max-y_min)) - min;
        intermittent_outputs.add(intermittent_output);
    }

    @Override
    public double getIntermittentOutput() {
        return intermittent_output;
    }

    @Override
    public double getBatchOutput() {
        // return arithmetic average of stored values and flush array
        if (intermittent_outputs.size() == 0)
            return Double.NaN;
        double mean_output = 0.0;
        for (double d: intermittent_outputs) {
            mean_output += d;
        }
        mean_output /= intermittent_outputs.size();
        
        intermittent_outputs.clear();
        return mean_output;
    }

    @Override
    public void setROI(Roi roi) {
        this.roi = roi;
    }

    @Override
    public void dispose() {
        // nothing to dispose of
        return;
    }

    @Override
    public String getName() {
        return "Brightness integrator";
    }

    @Override
    public AnalyzerStatusPanel getStatusPanel() {
        // no status panel
        return null;
    }
    
}
