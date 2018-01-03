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
package ch.epfl.leb.alica.controllers;

import ch.epfl.leb.alica.AbstractFactory;
import ch.epfl.leb.alica.Controller;
import ch.epfl.leb.alica.controllers.inverter.InverterSetupPanel;
import ch.epfl.leb.alica.controllers.manual.ManualSetupPanel;
import ch.epfl.leb.alica.controllers.pi.PI_SetupPanel;
import ch.epfl.leb.alica.controllers.selftuningpi.SelfTuningSetupPanel;
import ij.io.PluginClassLoader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller Factory
 * @author Marcel Stefko
 */
public class ControllerFactory extends AbstractFactory<ControllerSetupPanel>{
    private double max_controller_output = 0.0;
    private double tick_rate_ms = 500;
    
    /**
     * Initialize the factory with known controllers
     */
    public ControllerFactory() {
        super();
        // add known controllers
        addSetupPanel("PI", new PI_SetupPanel());
        addSetupPanel("Manual", new ManualSetupPanel());
        addSetupPanel("Inverter", new InverterSetupPanel());
        addSetupPanel("Self-tuning PI", new SelfTuningSetupPanel());
        for (ControllerSetupPanel sp: ControllerSetupPanelLoader.getControllerSetupPanels()) {
            addSetupPanel(sp.getName(), sp);
        }
        // set up default choice
        selectProduct("Self-tuning PI");
    }
    
    /**
     * Set maximal output value of the constructed controller
     * @param max_controller_output
     */
    public void setMaxControllerOutput(double max_controller_output) {
        this.max_controller_output = max_controller_output; 
    }
    
    /**
     * Set the tick rate at which the controller will operate.
     * @param tick_rate_ms tick rate in milliseconds
     */
    public void setControllerTickRateMs(double tick_rate_ms) {
        this.tick_rate_ms = tick_rate_ms;
    }
    
    /**
     * Build the selected controller using current settings
     * @return initialized controller
     */
    public Controller build() {
        return getSelectedSetupPanel().initController(max_controller_output, tick_rate_ms);
    }
}


class ControllerSetupPanelLoader {
    // I use this just to print jars in the working dir, whatever
    private static PluginClassLoader class_loader = new PluginClassLoader("./");
    
    /**
     * Dynamically loads ControllerSetupPanels from available jar files. 
     * The jar filename MUST begin with "ALICA_" for the jar to be recognized.
     * @return list of loaded setup panels
     */
    public static ArrayList<ControllerSetupPanel> getControllerSetupPanels() {
        
        ArrayList<ControllerSetupPanel> retval = new ArrayList<ControllerSetupPanel>();
        
        // print urls of files in mmplugins folder
        for (URL u: class_loader.getURLs()) {
            // if it doesnt match desired filename, skip it
            if (!u.toString().toUpperCase().contains("ALICA_")) {
                continue;
            } else {
                Logger.getLogger(ControllerSetupPanelLoader.class.getName()).log(Level.INFO, "Loading ALICA analyzers from:\n" + u.toString());
            }
            
            // open the jar file
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(new File(u.toURI()));
            } catch (IOException ex) {
                Logger.getLogger(ControllerSetupPanelLoader.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            } catch (URISyntaxException ex) {
                Logger.getLogger(ControllerSetupPanelLoader.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }

            // list all entries, and initiate classloader
            Enumeration<JarEntry> e = jarFile.entries();
            URL[] urls = { u };
            // it is vital to include a parent classloader from original ALICA package
            URLClassLoader cl = URLClassLoader.newInstance(urls, ControllerSetupPanelLoader.class.getClassLoader());
            
            // iterate over entries, filter out those which arent a class
            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                if(je.isDirectory() || !je.getName().endsWith(".class")){
                    continue;
                }
                // remove .class from end of string
                String className = je.getName().substring(0,je.getName().length()-6);
                className = className.replace('/', '.');
                Class c = null;
                // try to load class
                try {
                    c = cl.loadClass(className);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ControllerSetupPanelLoader.class.getName()).log(Level.SEVERE, null, ex);
                    continue;
                }
                
                // verify that class is subclass of ControllerSetupPanel and add it
                if (ControllerSetupPanel.class.isAssignableFrom(c)) {
                    try {
                        retval.add((ControllerSetupPanel) c.newInstance());
                    } catch (InstantiationException ex) {
                        Logger.getLogger(ControllerSetupPanelLoader.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(ControllerSetupPanelLoader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return retval;
    }
}