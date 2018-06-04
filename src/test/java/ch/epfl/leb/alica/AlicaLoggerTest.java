/*
 * Copyright (C) 2017-2018 Laboratory of Experimental Biophysics
 * Ecole Polytechnique Federale de Lausanne
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

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.LinkedHashMap;

/**
 * Unit tests for the ALICA logger class.
 * @author Kyle M. Douglass
 */
public class AlicaLoggerTest {
    
    @Before
    public void setUp() {
        AlicaLogger.getInstance().clear();
    }

    /**
     * Test of addToLog method, of class AlicaLogger.
     */
    @Test
    public void testAddToLog_3args_1() {
        System.out.println("addToLog");
        int frame_no = 0;
        String value_name = "batched_output";
        double value = 42.0;
        AlicaLogger instance = AlicaLogger.getInstance();
        
        instance.addToLog(frame_no, value_name, value);
        LinkedHashMap<Integer,LinkedHashMap<String, Object>> log
                = instance.getLogMap();
        assertEquals(value, log.get(frame_no).get(value_name));
    }
    
    /**
     * Test of addToLog method, of class AlicaLogger.
     */
    @Test
    public void testAddToLogNoOverwrite() {
        System.out.println("addToLogNoOverwrite");
        int frame_no = 0;
        String value_name = "batched_output";
        double value = 42.0;
        AlicaLogger instance = AlicaLogger.getInstance();
        
        instance.addToLog(frame_no, value_name, value);
        instance.addToLog(frame_no, value_name, Double.NaN);
        LinkedHashMap<Integer,LinkedHashMap<String, Object>> log
                = instance.getLogMap();
        assertEquals(value, log.get(frame_no).get(value_name));
    }

    /**
     * Test of addToLog method, of class AlicaLogger.
     */
    @Test
    public void testAddToLog_3args_2() {
        System.out.println("addToLog");
        int frame_no = 0;
        String value_name = "batched_output";
        String value = "hello";
        AlicaLogger instance = AlicaLogger.getInstance();
        
        instance.addToLog(frame_no, value_name, value);
        LinkedHashMap<Integer,LinkedHashMap<String, Object>> log
                = instance.getLogMap();
        assertEquals(value, log.get(frame_no).get(value_name));
    }
    
}
