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

/**
 * Possible ways for the plugin to grab images from micromanager.
 * @author Marcel Stefko
 */
public enum ImagingMode {

    /**
     * Query directly the MMCore getLastImage() method.
     */
    GRAB_FROM_CORE,

    /**
     * Get images from the Datastore associated with live() mode.
     */
    LIVE,

    /**
     * Get images from the Datastore which is associated with the next
     * acquisition that will be started.
     */
    NEXT_ACQUISITION
}
