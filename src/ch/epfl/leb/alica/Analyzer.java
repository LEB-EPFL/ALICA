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
package ch.epfl.leb.alica;

import ij.gui.Roi;


/**
 * An analyzer receives images from the WorkerThread, processes them, and
 * adjusts its internal state (and output) accordingly. The WorkerThread 
 * can request the output at any time. 
 * @author Marcel Stefko
 */
public interface Analyzer {

    /**
     * Process the next image and adjust internal state.
     * @param image image to be processed
     * @param image_width width in pixels
     * @param image_height height in pixels
     * @param pixel_size_um length of one pixel side in micrometers
     * @param time_ms time of image acquisition in milliseconds
     */
    public void processImage(Object image, int image_width, int image_height, double pixel_size_um, long time_ms);
    
    /**
     * Return intermittent output of the analyzer based on current internal state.
     * Internal state of the controller should stay the same. Use
     *  getBatchOutput() for values to be passed to the controller.
     * @return output value of the analyzer to be plotted by the GUI.
     */
    public double getIntermittentOutput();
    
    /**
     * Returns output of the analyzer to be used by a controller. This can also
     * modify the internal state of the analyzer (such as flushing intermittent
     * value cache).
     * @return output value of the analyzer to be used by the controller
     */
    public double getBatchOutput();
    
    /**
     * Sets the region of interest in the image, so that only a section of the
     * whole image is analyzed.
     * @param roi ROI to constrain to
     */
    public void setROI(Roi roi);
    
    /**
     * Close all windows that were opened and are owned by this analyzer.
     */
    public void dispose();
    
    /**
     * 
     * @return unique name of the analyzer.
     */
    public String getName();
}
