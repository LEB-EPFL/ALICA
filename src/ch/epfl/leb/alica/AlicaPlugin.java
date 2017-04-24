/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.alica;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.micromanager.MenuPlugin;
import org.micromanager.Studio;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;
/**
 *
 * @author stefko
 */
@Plugin(type = MenuPlugin.class)
public class AlicaPlugin implements MenuPlugin, SciJavaPlugin {
    
    @Override
    public String getSubMenu() {
        return "";
    }

    @Override
    public void onPluginSelected() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainGUI.initialize(AlicaCore.getInstance()).setVisible(true);
                } catch (MainGUI.AlreadyInitializedException ex) {
                    MainGUI.getInstance().setVisible(true);
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, null, ex);
                }
            }
        });
    }

    @Override
    public void setContext(Studio studio) {
        try {
            AlicaCore.initialize(studio);
        } catch (AlicaCore.AlreadyInitializedException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING,"ALICA core was already initialized.",ex);
        }
    }

    @Override
    public String getName() {
        return "ALICA";
    }

    @Override
    public String getHelpText() {
        return "Help!";
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }

    @Override
    public String getCopyright() {
        return "GPLv3";
    }
    
}
