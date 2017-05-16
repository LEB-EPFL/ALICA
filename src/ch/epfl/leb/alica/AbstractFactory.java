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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Template for factories that are set up using a JPanel. Currently
 * used by Analyzer and Controller
 * @author Marcel Stefko
 * @param <ProductSetupPanel> A JPanel which is used to set up the parameters.
 */
public abstract class AbstractFactory<ProductSetupPanel> {
    String selected_name;
    private final LinkedHashMap<String, ProductSetupPanel> setup_panels;
    
    /**
     * Initialize the map which stores different setup panels.
     */
    public AbstractFactory() {
        setup_panels = new LinkedHashMap<String, ProductSetupPanel>();
    }
    
    /**
     * Add new setup panel to list
     * @param name Identifier of the product associated with panel, 
     *  displayed in GUI
     * @param panel The setup JPanel
     */
    protected void addSetupPanel(String name, ProductSetupPanel panel) {
        if (setup_panels.containsKey(name)) {
            throw new IllegalArgumentException("Such setup panel already exists!");
        }
        setup_panels.put(name, panel);
    }
    
    /**
     *
     * @return currently selected factory product
     */
    public String getSelectedProductName() {
        if (selected_name == null)
            throw new NullPointerException("No product selected.");
        return selected_name;
    }
    
    /**
     *
     * @return list of all possible products
     */
    public Set<String> getProductNameList() {
        return setup_panels.keySet();
    }
    
    public Collection<ProductSetupPanel> getProductSetupPanelCollection() {
        return setup_panels.values();
    }
    
    /**
     * Select a product of the factory
     * @param name identifier of the product
     */
    public void selectProduct(String name) {
        if (!setup_panels.containsKey(name)) {
            throw new IllegalArgumentException("No such product: "+name);
        }
        selected_name = name;
    }
    
    /**
     *
     * @return Return setup panel of currently selected product
     */
    public ProductSetupPanel getSelectedSetupPanel() {
        return setup_panels.get(selected_name);
    }
}
