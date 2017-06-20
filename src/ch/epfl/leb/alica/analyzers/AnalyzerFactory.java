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
import ch.epfl.leb.alica.AlicaLogger;
import ch.epfl.leb.alica.Analyzer;
import ch.epfl.leb.alica.analyzers.autolase.AutoLaseSetupPanel;
import ch.epfl.leb.alica.analyzers.integrator.IntegratorSetupPanel;
import ch.epfl.leb.alica.analyzers.quickpalm.QuickPalmSetupPanel;
import ch.epfl.leb.alica.analyzers.spotcounter.SpotCounterSetupPanel;
import ij.IJ;
import ij.io.PluginClassLoader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        for (AnalyzerSetupPanel sp: AnalyzerSetupPanelLoader.getAnalyzerSetupPanels()) {
            addSetupPanel(sp.getName(), sp);
        }
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

class AnalyzerSetupPanelLoader {
    // I use this just to print jars in the working dir, whatever
    private static PluginClassLoader class_loader = new PluginClassLoader("./");
    
    /**
     * Dynamically loads AnalyzerSetupPanels from available jar files. 
     * The jar filename MUST begin with "ALICA_" for the jar to be recognized.
     * @return list of loaded setup panels
     */
    public static ArrayList<AnalyzerSetupPanel> getAnalyzerSetupPanels() {
        
        ArrayList<AnalyzerSetupPanel> retval = new ArrayList<AnalyzerSetupPanel>();
        
        // print urls of files in mmplugins folder
        for (URL u: class_loader.getURLs()) {
            // if it doesnt match desired filename, skip it
            if (!u.toString().toUpperCase().contains("ALICA_")) {
                continue;
            } else {
                AlicaLogger.getInstance().logMessage("Loading ALICA addons from:\n" + u.toString());
            }
            
            // open the jar file
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(new File(u.toURI()));
            } catch (IOException ex) {
                Logger.getLogger(AnalyzerSetupPanelLoader.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            } catch (URISyntaxException ex) {
                Logger.getLogger(AnalyzerSetupPanelLoader.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }

            // list all entries, and initiate classloader
            Enumeration<JarEntry> e = jarFile.entries();
            URL[] urls = { u };
            // it is vital to include a parent classloader from original ALICA package
            URLClassLoader cl = URLClassLoader.newInstance(urls, AnalyzerSetupPanelLoader.class.getClassLoader());
            
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
                    Logger.getLogger(AnalyzerSetupPanelLoader.class.getName()).log(Level.SEVERE, null, ex);
                    continue;
                }
                
                // verify that class is subclass of AnalyzerSetupPanel and add it
                if (AnalyzerSetupPanel.class.isAssignableFrom(c)) {
                    try {
                        retval.add((AnalyzerSetupPanel) c.newInstance());
                    } catch (InstantiationException ex) {
                        Logger.getLogger(AnalyzerSetupPanelLoader.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(AnalyzerSetupPanelLoader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return retval;
    }
}
