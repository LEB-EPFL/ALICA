/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
