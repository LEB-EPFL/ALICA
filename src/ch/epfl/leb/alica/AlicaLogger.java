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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.micromanager.internal.MMStudio;

/**
 *
 * @author stefko
 */
public class AlicaLogger {
    private static AlicaLogger instance = null;
    
    private LinkedHashMap<Integer, LinkedHashMap<String,Double>> log_map;
    private LinkedHashSet<String> parameter_set;
    
    private AlicaLogger() {
        clear();
    }
    
    public void clear() {
        log_map = new LinkedHashMap<Integer, LinkedHashMap<String,Double>>();
        parameter_set = new LinkedHashSet<String>();
    }
    
    public static AlicaLogger getInstance() {
        if (instance == null) {
            instance = new AlicaLogger();
        }
        return instance;
    }
    
    public void addIntermittentOutput(int frame_no, double value) {
        addToLog(frame_no,"analyzer_intermittent_output",value);
    }
    
    public void addBatchedOutput(int frame_no, double value) {
        addToLog(frame_no,"analyzer_batched_output",value);
    }
    
    public void addControllerOutput(int frame_no, double value) {
        addToLog(frame_no,"controller_output",value);
    }
    
    public void addSetpoint(int frame_no, double setpoint) {
        addToLog(frame_no,"setpoint", setpoint);
    }
    
    public void addToLog(int frame_no, String value_name, double value) {
        parameter_set.add(value_name);
        if (!log_map.containsKey(frame_no))
            log_map.put(frame_no, new LinkedHashMap<String,Double>());
        log_map.get(frame_no).put(value_name, value);
    }
    
    public void saveLog() {
        if (log_map.isEmpty()) {
            MMStudio.getInstance().logs().showError("Log is empty!");
            return;
        }
        
        
        int max_frame_no = 0;
        // intermittent output should have the largest value
        for (int key: log_map.keySet()) {
            if (max_frame_no < key)
                max_frame_no = key;
        }
        
        JFileChooser fc = new JFileChooser();
        int returnVal;
        
        //*
        // File chooser dialog for saving output csv
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        //set a default filename 
        fc.setSelectedFile(new File("alica_log.csv"));
        //Set an extension filter
        fc.setFileFilter(new FileNameExtensionFilter("CSV file","csv"));
        returnVal = fc.showSaveDialog(null);
        if  (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File csv_output = fc.getSelectedFile();
        
        PrintWriter writer;
        try {
            writer = new PrintWriter(csv_output.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            MMStudio.getInstance().logs().showError(ex, "Can't find file to save.");
            return;
        }
        
        writer.print("#frame_no");
        for (String s: parameter_set) {
            writer.print(','+s);
        }
        writer.print("\n");

        for (int i=1; i<=max_frame_no; i++) {
            writer.print(i);
            
            LinkedHashMap<String,Double> frame_map = log_map.get(i);
            if (frame_map == null)
                frame_map = new LinkedHashMap<String,Double>();
            
            for (String s: parameter_set) {
                Double output_value = frame_map.get(s);
                if (output_value==null) {
                    output_value = Double.NaN;
                }
                writer.print(",");
                writer.print(output_value);
            }
            writer.print("\n");
        }
        writer.close();
    }
}
