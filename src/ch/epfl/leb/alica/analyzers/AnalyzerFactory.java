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
package ch.epfl.leb.alica.analyzers;

import ch.epfl.leb.alica.AbstractFactory;
import ch.epfl.leb.alica.Analyzer;
import ch.epfl.leb.alica.analyzers.autolase.AutoLaseSetupPanel;
import ch.epfl.leb.alica.analyzers.integrator.IntegratorSetupPanel;
import ch.epfl.leb.alica.analyzers.quickpalm.QuickPalmSetupPanel;
import ch.epfl.leb.alica.analyzers.spotcounter.SpotCounterSetupPanel;

/**
 * Analyzer factory.
 * @author Marcel Stefko
 */
public class AnalyzerFactory extends AbstractFactory<AnalyzerSetupPanel>{
    
    /**
     * Adds the known algorithms to the list.
     */
    public AnalyzerFactory() {
        super();
        addSetupPanel("SpotCounter", new SpotCounterSetupPanel());
        addSetupPanel("AutoLase", new AutoLaseSetupPanel());
        addSetupPanel("QuickPalm", new QuickPalmSetupPanel());
        addSetupPanel("Integrator", new IntegratorSetupPanel());
        selectProduct("SpotCounter");
    }
    
    /**
     * Build the selected analyzer using current settings
     * @return initialized analyzer
     */
    public Analyzer build() {
        return getSelectedSetupPanel().initAnalyzer();
    }
}
