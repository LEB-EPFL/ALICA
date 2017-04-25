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

import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author stefko
 */
public abstract class AbstractFactory<ProductSetupPanel> {
    String selected_name;
    private final HashMap<String, ProductSetupPanel> setup_panels;
    
    public AbstractFactory() {
        setup_panels = new HashMap<String, ProductSetupPanel>();
    }
    
    protected void addSetupPanel(String name, ProductSetupPanel panel) {
        if (setup_panels.containsKey(name)) {
            throw new IllegalArgumentException("Such setup panel already exists!");
        }
        setup_panels.put(name, panel);
    }
    
    public String getSelectedProductName() {
        if (selected_name == null)
            throw new NullPointerException("No product selected.");
        return selected_name;
    }
    
    public Set<String> getProductNameList() {
        return setup_panels.keySet();
    }
    
    public void selectProduct(String name) {
        if (!setup_panels.containsKey(name)) {
            throw new IllegalArgumentException("No such product: "+name);
        }
        selected_name = name;
    }
    
    public ProductSetupPanel getSelectedSetupPanel() {
        return setup_panels.get(selected_name);
    }
}
