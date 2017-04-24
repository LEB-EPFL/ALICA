/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.alica.analyzers;

import ch.epfl.leb.alica.Analyzer;

/**
 *
 * @author stefko
 */
public abstract class AnalyzerSetupPanel extends javax.swing.JPanel {
    
    public abstract Analyzer initAnalyzer();
    
    @Override
    public abstract String toString();
    
}
