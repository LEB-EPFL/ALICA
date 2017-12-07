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

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 *
 * @author douglass
 */
public class IntegratorTest {
    private Integrator integrator;
    
    @Before
    public void setUp() {
        integrator = new Integrator();
    }

    /**
     * Test of processImage method, of class Integrator.
     */
    @Test
    public void testProcessImage() {
        int width = 3;
        int height = 3;
        short pixelValue = 42;
        double expResult = 42.1111;

        // Create a test image whose average value is 42.
        short[] pixels = new short[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                pixels[i * width + j] = pixelValue;
            }
        }
        
        // Change one pixel to 43
        pixels[0] = 43;
        
        // Process the image and get the controller's value
        integrator.processImage(pixels, width, height, 0.1, 0);
        double controlValue = integrator.getIntermittentOutput();
        
        assertEquals(expResult, controlValue, 0.0001);
        
    }
    
    /**
     * Test of getBatchOutput method, of class Integrator.
     */
    @Test
    public void testGetBatchOutput() {
        int width = 3;
        int height = 3;
        short pixelValue1 = 42;
        short pixelValue2 = 44;
        // Create two test images, one whose average value is 42 and the other
        // whose average is 44.
        short[] pixels1 = new short[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                pixels1[i * width + j] = pixelValue1;
            }
        }
        
        short[] pixels2 = new short[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                pixels2[i * width + j] = pixelValue2;
            }
        }
        
        integrator.processImage(pixels1, width, height, 0.1, 0);
        integrator.processImage(pixels2, width, height, 0.1, 0);
        
        // Average should be 43
        double result = integrator.getBatchOutput();
        double expResult = 43.0;
        assertEquals(expResult, result, 0.0);
    }
}
