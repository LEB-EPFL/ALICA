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
    
    private LinkedHashMap<Integer, Double> analyzer_intermittent_output;
    private LinkedHashMap<Integer, Double> analyzer_batched_output;
    private LinkedHashMap<Integer, Double> controller_output;
    
    private AlicaLogger() {
        clear();
    }
    
    private void clear() {
        analyzer_intermittent_output = new LinkedHashMap<Integer, Double>();
        analyzer_batched_output = new LinkedHashMap<Integer, Double>();
        controller_output = new LinkedHashMap<Integer, Double>();
    }
    
    public static AlicaLogger getInstance() {
        if (instance == null) {
            instance = new AlicaLogger();
        }
        return instance;
    }
    
    public void addIntermittentOutput(int frame_no, double value) {
        analyzer_intermittent_output.put(frame_no, value);
    }
    
    public void addBatchedOutput(int frame_no, double value) {
        analyzer_batched_output.put(frame_no, value);
    }
    
    public void addControllerOutput(int frame_no, double value) {
        controller_output.put(frame_no, value);
    }
    
    public void saveLog() {
        if (analyzer_intermittent_output.isEmpty()) {
            MMStudio.getInstance().logs().showError("Log is empty!");
            return;
        }
        
        
        int max_frame_no = 0;
        // intermittent output should have the largest value
        for (int key: analyzer_intermittent_output.keySet()) {
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
        
        writer.println("#Frame no.,Analyzer intermittent output, Analyzer batched output, Controller output");
        for (int i=1; i<=max_frame_no; i++) {
            writer.print(i);
            writer.print(",");
            
            Double intermittent_output = analyzer_intermittent_output.get(i);
            if (intermittent_output == null)
                intermittent_output = Double.NaN;
            writer.print(intermittent_output);
            writer.print(",");
            
            Double batched_output = analyzer_batched_output.get(i);
            if (batched_output == null)
                batched_output = Double.NaN;
            writer.print(batched_output);
            writer.print(",");
            
            Double controller_output = this.controller_output.get(i);
            if (controller_output == null)
                controller_output = Double.NaN;
            writer.print(controller_output);
            writer.print("\n");
        }
        writer.close();
    }
}
