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

import ch.epfl.leb.alica.analyzers.AnalyzerFactory;
import ch.epfl.leb.alica.workers.Coordinator;
import ch.epfl.leb.alica.controllers.ControllerFactory;
import ch.epfl.leb.alica.lasers.LaserFactory;
import ij.IJ;
import ij.gui.Roi;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcorej.StrVector;
import org.micromanager.Studio;

/**
 * The core's settings are controlled by MainGUI, and the Core then produces
 products from its factories, and initializes the Coordinator, and later
 terminates it.
 * @author stefko
 */
public final class AlicaCore {
    private static AlicaCore instance = null;
    
    private final Studio studio;
    private Coordinator coordinator;
    
    private final AnalyzerFactory analyzer_factory;
    private final ControllerFactory controller_factory;
    private final LaserFactory laser_factory;
    
    private int controller_tick_rate_ms = 500;
    private Roi ROI;
    

    
    // private constructor disables instantiation
    private AlicaCore(Studio studio) {
        this.studio = studio;
        this.analyzer_factory = new AnalyzerFactory();
        this.controller_factory = new ControllerFactory();
        this.laser_factory = new LaserFactory(studio);
    }
    
    /**
     * Initialize the Singleton core
     * @param studio MicroManager studio
     * @return the singleton instance of the core
     * @throws AlreadyInitializedException if it was already initialized
     */
    public static AlicaCore initialize(Studio studio) throws AlreadyInitializedException {
        if (instance != null) {
            throw new AlreadyInitializedException("ALICA core was already initialized.");
        } else {
            instance = new AlicaCore(studio);
        }
        return instance;
    }
    
    /**
     * Returns the singleton instance, or an exception if it was not yet initialized
     * @return the singleton instance of the core
     */
    public static AlicaCore getInstance() {
        if (instance == null) {
            throw new NullPointerException("ALICA core was not yet initialized.");
        }
        return instance;
    }
    
    /**
     * Print all loaded devices in the MMCore to the log.
     */
    public void printLoadedDevices() {
        StrVector v = studio.core().getLoadedDevices();
        String out = "";
        for (String label: v.toArray()) {
            out = out.concat(" "+label+":\n");
            try {
                for (String property: studio.core().getDevicePropertyNames(label).toArray())
                {
                    String propValue;
                    try {
                        propValue = studio.core().getProperty(label, property);
                    } catch (Exception ex) {
                        propValue = "Error.";
                    }
                    out = out.concat("  - "+property+": "+propValue+"\n");
                }
            } catch (Exception ex) {
                studio.logs().logError(ex, "Error in printing loaded devices.");
            }
        }
        studio.logs().showMessage(out);
        studio.logs().logMessage(out);
    }
    
    /**
     * Inform factories of maximal laser power value.
     * @param max_laser_power maximal laser power value
     */
    public void setMaxLaserPower(double max_laser_power) {
        this.controller_factory.setMaxControllerOutput(max_laser_power);
        this.laser_factory.setMaxLaserPower(max_laser_power);
    }
    
    /**
     * Sets the deadzone of change of laser power output. For example, if set
     * to 0.1, the laser would ignore requests for change of power that would
     * be different by less than 10% from current output power.
     * @param laser_power_deadzone deadzone size (NOT in percent)
     */
    public void setLaserPowerDeadzone(double laser_power_deadzone) {
        this.laser_factory.setLaserPowerDeadzone(laser_power_deadzone);
    }
    
    /**
     * Inform factories that the laser should only display its output, not
     * really communicate with the hardware.
     * @param is_laser_virtual true if virtual, false if real
     */
    public void setLaserVirtual(boolean is_laser_virtual) {
        this.laser_factory.setLaserVirtual(is_laser_virtual);
    }
    
    /**
     * Sets the tick rate for the controller.
     * @param controller_tick_rate_ms delay between two runs of the ControlTask
     */
    public void setControlWorkerTickRate(int controller_tick_rate_ms) {
        this.controller_tick_rate_ms = controller_tick_rate_ms;
        this.controller_factory.setControllerTickRateMs(controller_tick_rate_ms);
    }
    
    public boolean setCurrentROI() {
        this.ROI = studio.displays().getCurrentWindow().getImagePlus().getRoi();
        if (this.ROI == null)
            return false;
        else
            return true;
    }
    
    
    
    /**
     *
     * @return AnalyzerFactory
     */
    public AnalyzerFactory getAnalyzerFactory() {
        return analyzer_factory;
    }
    
    /**
     *
     * @return ControllerFactory
     */
    public ControllerFactory getControllerFactory() {
        return controller_factory;
    }
    
    /**
     *
     * @return LaserFactory
     */
    public LaserFactory getLaserFactory() {
        return laser_factory;
    }
     
    /**
     * Builds products from their factories using current settings, and
 starts the Coordinator (analysis is started)
     * @param imaging_mode
     */
    public void startWorkers(ImagingMode imaging_mode) {
        if (coordinator != null) {
            coordinator.dispose();
        }
        coordinator = new Coordinator(studio, analyzer_factory.build(), 
                controller_factory.build(), laser_factory.build(), imaging_mode,
                controller_tick_rate_ms, ROI);
    }
    
    /**
     * Requests the coordinator to stop and then waits for it to join.
     */
    public void stopWorkers() {
        coordinator.requestStop();
    }
    
    /**
     * Thrown if a double initialization is requested
     */
    public static class AlreadyInitializedException extends RuntimeException {

        /**
         *
         * @param message exception message
         */
        public AlreadyInitializedException(String message) {
            super(message);
        }
    }
}