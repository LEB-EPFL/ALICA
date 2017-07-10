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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.micromanager.MenuPlugin;
import org.micromanager.Studio;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;
/**
 * MicroManager2.0 MenuPlugin for automated laser illumination intensity
 * control.
 * @author Marcel Stefko
 */
@Plugin(type = MenuPlugin.class)
public class AlicaPlugin implements MenuPlugin, SciJavaPlugin {
    
    /**
     * 
     * @return Sub-menu location of the plugin
     */
    @Override
    public String getSubMenu() {
        return "Device Control";
    }

    /**
     * Display the MainGUI singleton if it was hidden, if it doesn't exist,
     * initialize it. AlicaCore must be initialized before calling this method.
     */
    @Override
    public void onPluginSelected() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainGUI.initialize(AlicaCore.getInstance()).setVisible(true);
                } catch (MainGUI.AlreadyInitializedException ex) {
                    if (AlicaCore.getInstance().isCoordinatorRunning()) {
                        AlicaLogger.getInstance().showMessage("ALICA already running!");
                    } else {
                    MainGUI.getInstance().setVisible(true);
                    }
                }
            }
        });
    }

    /**
     * Initialize the AlicaCore, if it already exists, do nothing.
     * @param studio MMStudio
     */
    @Override
    public void setContext(Studio studio) {
        try {
            AlicaCore.initialize(studio);
            AlicaLogger.getInstance().setStudio(studio);
        } catch (AlicaCore.AlreadyInitializedException ex) {

        }
    }

    /**
     *
     * @return name of the plugin
     */
    @Override
    public String getName() {
        return "ALICA";
    }

    /**
     *
     * @return
     */
    @Override
    public String getHelpText() {
        return "Help!";
    }

    /**
     *
     * @return current plugin version
     */
    @Override
    public String getVersion() {
        return "0.2.4";
    }

    /**
     *
     * @return plugin copyright
     */
    @Override
    public String getCopyright() {
        return "GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007";
    }
    
    /**
     *
     * @return singleton core of ALICA plugin
     */
    public AlicaCore getCore() {
        return AlicaCore.getInstance();
    }
    
}
