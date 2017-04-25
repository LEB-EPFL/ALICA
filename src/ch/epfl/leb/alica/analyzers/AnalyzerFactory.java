/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.alica.analyzers;

import ch.epfl.leb.alica.AbstractFactory;
import ch.epfl.leb.alica.Analyzer;
import ch.epfl.leb.alica.analyzers.autolase.AutoLaseSetupPanel;
import ch.epfl.leb.alica.analyzers.spotcounter.SpotCounterSetupPanel;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author stefko
 */
public class AnalyzerFactory extends AbstractFactory<AnalyzerSetupPanel>{
    
    public AnalyzerFactory() {
        super();
        addSetupPanel("SpotCounter", new SpotCounterSetupPanel());
        addSetupPanel("AutoLase", new AutoLaseSetupPanel());
        
        selectProduct("SpotCounter");
    }
    
    public Analyzer build() {
        return getSelectedSetupPanel().initAnalyzer();
    }
}
