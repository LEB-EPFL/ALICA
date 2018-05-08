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

import ij.IJ;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.micromanager.LogManager;
import org.micromanager.Studio;
import org.micromanager.internal.MMStudio;

/**
 *
 * @author stefko
 */
public class AlicaLogger {
    private static AlicaLogger instance = null;
    private LogManager mm_logger = null;
    
    private LinkedHashMap<Integer, LinkedHashMap<String,Object>> log_map;
    private LinkedHashSet<String> parameter_set;
    
    private AlicaLogger() {
        clear();
    }
    
    /**
     * Resets logger, removes all data.
     */
    public final void clear() {
        log_map = new LinkedHashMap<Integer, LinkedHashMap<String,Object>>();
        parameter_set = new LinkedHashSet<String>();
    }
    
    /**
     *
     * @return AlicaLogger singleton
     */
    public static AlicaLogger getInstance() {
        if (instance == null) {
            instance = new AlicaLogger();
        }
        return instance;
    }
    
    /**
     * Set studio to allow general logging
     * @param studio MMStudio
     */
    public void setStudio(Studio studio) {
        this.mm_logger = studio.getLogManager();
    }
    
    /**
     * Log message to MicroManager or to a general logger
     * @param message message to be logged
     */
    public void logMessage(String message) {
        try {
            this.mm_logger.logMessage(message);
        } catch (Throwable ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, message);
        }
    }
    
    
    /**
     * Log message to MicroManager or to a general logger
     * @param message message to be logged
     */
    public void logDebugMessage(String message) {
        try {
            this.mm_logger.logDebugMessage(message);
        } catch (Throwable ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.FINER, message);
        }
    }
    
    /**
     * Show message in MicroManager or in ImageJ
     * @param message message to be logged
     */
    public void showMessage(String message) {
        try {
            this.mm_logger.showMessage(message);
        } catch (Throwable ex) {
            IJ.showMessage(message);
        }
    }
    
    /**
     * Show exception in MicroManager or in ImageJ
     * @param exc exception to be shown
     * @param message message to be shown
     */
    public void showError(Exception exc, String message) {
        try {
            this.mm_logger.showError(exc, message);
        } catch (Throwable ex) {
            IJ.showMessage(message);
        }
    }
    
    /**
     * Log exception in MicroManager or in general logger
     * @param exc exception to be logged
     * @param message message to be logged
     */
    public void logError(Exception exc, String message) {
        try {
            this.mm_logger.logError(exc, message);
        } catch (Throwable ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, message, exc);
        }
    }
    
    /**
     * Add intermittent output of analyzer into log
     * @param frame_no
     * @param value value of the output
     */
    public void addIntermittentOutput(int frame_no, double value) {
        addToLog(frame_no,"analyzer_intermittent_output",value);
    }
    /**
     * Add batched output of analyzer into log
     * @param frame_no
     * @param value value of the output
     */
    public void addBatchedOutput(int frame_no, double value) {
        addToLog(frame_no,"analyzer_batched_output",value);
    }
    
    /**
     * Add output of controller into log
     * @param frame_no
     * @param value value of the output
     */
    public void addControllerOutput(int frame_no, double value) {
        addToLog(frame_no,"controller_output",value);
    }
    
    /**
     * Add setpoint of controller into log
     * @param frame_no
     * @param setpoint value of the output
     */
    public void addSetpoint(int frame_no, double setpoint) {
        addToLog(frame_no,"setpoint", setpoint);
    }
    
    
    /**
     * Add a parameter into log
     * @param frame_no
     * @param value_name name of parameter
     * @param value value of parameter
     */
    public void addToLog(int frame_no, String value_name, double value) {
        parameter_set.add(value_name);
        if (!log_map.containsKey(frame_no))
            log_map.put(frame_no, new LinkedHashMap<String,Object>());
        log_map.get(frame_no).put(value_name, Double.valueOf(value));
    }
    
    /**
     * Add a parameter into log
     * @param frame_no
     * @param value_name name of parameter
     * @param value value of parameter
     */
    public void addToLog(int frame_no, String value_name, String value) {
        parameter_set.add(value_name);
        if (!log_map.containsKey(frame_no))
            log_map.put(frame_no, new LinkedHashMap<String,Object>());
        log_map.get(frame_no).put(value_name, value);
    }
    
    /**
     * Saves the log into a csv file chosen by file selection dialog.
     * @return true if save was successful, false otherwise
     */
    public boolean saveLog() {
        if (log_map.isEmpty()) {
            this.showMessage("Log is empty!");
            return true;
        }
        
        
        int max_frame_no = 0;
        // intermittent output should have the largest value
        for (int key: log_map.keySet()) {
            if (max_frame_no < key)
                max_frame_no = key;
        }
        
        JFileChooser fc = new JFileChooser();
        int returnVal;
        
        // File chooser dialog for saving output csv
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        //set a default filename 
        fc.setSelectedFile(new File("alica_log.csv"));
        //Set an extension filter
        fc.setFileFilter(new FileNameExtensionFilter("CSV file","csv"));
        returnVal = fc.showSaveDialog(null);
        if  (returnVal != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        File csv_output = fc.getSelectedFile();
        
        PrintWriter writer;
        try {
            writer = new PrintWriter(csv_output.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            this.showError(ex, "Can't find file to save.");
            return false;
        }
        
        writer.print("#frame_no");
        for (String s: parameter_set) {
            writer.print(','+s);
        }
        writer.print("\n");
        
        
        LinkedHashMap<String,Object> value_cache = log_map.get(1);
        if (value_cache == null) {
            value_cache = new LinkedHashMap<String,Object>();
        }
        for (String s: parameter_set) {
            if (value_cache.get(s) == null) {
                value_cache.put(s, 0.0);
            }
        }
        
        for (int i=1; i<=max_frame_no; i++) {
            writer.print(i);
            
            LinkedHashMap<String,Object> frame_map = log_map.get(i);
            if (frame_map == null)
                frame_map = new LinkedHashMap<String,Object>();
            
            for (String s: parameter_set) {
                Object output = frame_map.get(s);
                if (output==null) {
                    output = value_cache.get(s);
                } else {
                    value_cache.put(s, output);
                }
                writer.print(",");
                writer.print(output.toString());
            }
            writer.print("\n");
        }
        writer.close();
        this.clear();
        return true;
    }
}
