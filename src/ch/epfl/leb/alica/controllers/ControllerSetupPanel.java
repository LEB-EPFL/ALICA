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

import ch.epfl.leb.alica.Controller;

/**
 * Makes sure that the added setup panels know how to initialize Controllers.
 * @author Marcel Stefko
 */
public abstract class ControllerSetupPanel extends javax.swing.JPanel{

    /**
     * Initialize and return the controller based on GUI information.
     * @param max_controller_output maximal output value, this is passed from
     *  different part of the GUI
     * @return initialized controller
     */
    public abstract Controller initController(double max_controller_output);
   
    
}
