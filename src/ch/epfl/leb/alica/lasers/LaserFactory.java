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
package ch.epfl.leb.alica.lasers;

import ch.epfl.leb.alica.Laser;
import java.util.Set;
import mmcorej.StrVector;
import org.micromanager.Studio;

/**
 *
 * @author stefko
 */
public class LaserFactory {
    private final Studio studio;
    private String selected_name;
    private String selected_property;
    public LaserFactory(Studio studio) {
        this.studio = studio;
        selected_name = getPossibleLasers().get(0);
    }
    
    public StrVector getPossibleLasers() {
        return studio.core().getLoadedDevices();
    }
    
    public void selectDevice(String name) {
        selected_name = name;
    }
    
    public String getSelectedDeviceName() {
        return selected_name;
    }
    
    public StrVector getSelectedDeviceProperties() throws Exception {
        return studio.core().getDevicePropertyNames(selected_name);
    }
    
    public void selectProperty(String property) {
        selected_property = property;
    }
    
    public Laser build() {
        return new MMLaser(studio, selected_name, selected_property, 0.0, 42.0);
    }
}
