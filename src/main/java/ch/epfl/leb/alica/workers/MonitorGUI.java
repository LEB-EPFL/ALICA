/* 
 * Copyright (C) 2017-2018 Laboratory of Experimental Biophysics
 * Ecole Polytechnique Federale de Lausanne
 * 
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
package ch.epfl.leb.alica.workers;

import ch.epfl.leb.alica.MainGUI;
import ch.epfl.leb.alica.interfaces.analyzers.AnalyzerStatusPanel;
import ch.epfl.leb.alica.interfaces.controllers.ControllerStatusPanel;
import ij.IJ;
import java.awt.Color;
import org.micromanager.internal.graph.GraphData;
import org.micromanager.internal.graph.GraphPanel;



/**
 * Display for monitoring the current Coordinator state.
 * 
 * This display is controlled by the Coordinator.
 * 
 * @author Marcel Stefko
 */
public class MonitorGUI extends javax.swing.JFrame {
    private double laser_power_max;
    private double plot_max = 0.0;
    private final Coordinator coordinator;
    private final GraphPanel realtime_graph;
    private final AnalyzerStatusPanel analyzer_status;
    private final ControllerStatusPanel controller_status;
    
    /**
     * Creates new form MonitorGUI
     * @param coordinator Coordinator parent
     * @param analyzer_name name of the used analyzer
     * @param controller_name name of the used controller
     * @param laser_name name of the used laser
     * @param analyzer_description A short description of the analyzer's units.
     * @param start_setpoint setpoint value to display at startup
     */
    public MonitorGUI(Coordinator coordinator, String analyzer_name, 
            String controller_name, String laser_name,
            String analyzer_description, double start_setpoint) {
        super();
        try {
            this.setLocation(MainGUI.getInstance().getLocationOnScreen());
        } catch (RuntimeException ex) {
            // center of screen fallback
            this.setLocationRelativeTo(null);
        }
        // check if coordinator is alive
        if (coordinator == null) {
            throw new NullPointerException();
        } else {
            this.coordinator = coordinator;
        }
        
        // initialize GUI
        initComponents();
        
        // set the labels
        l_analyzer.setText(analyzer_name);
        l_controller.setText(controller_name);
        l_laser.setText(laser_name);
        if (laser_name.startsWith("VIRTUAL")) {
            l_laser.setForeground(Color.red);
        }
        
        // initialize the GraphPanel
        realtime_graph = new GraphPanel();
        realtime_graph.setBounds(5,5,415,170);
        realtime_graph.revalidate();
        
        // place the GraphPanel into its container
        p_realtime_plot.add(realtime_graph);
        l_realtime_plot.setText(analyzer_description);
        
        // place analyzer and controller status panels
        analyzer_status = coordinator.getAnalyzerStatusPanel();
        if (analyzer_status != null) {
            analyzer_status.setBounds(5,5,200,150);
            analyzer_status.revalidate();
            p_analyzer_status.add(analyzer_status);
        }
        controller_status = coordinator.getControllerStatusPanel();
        if (controller_status != null) {
            controller_status.setBounds(5,5,200,150);
            controller_status.revalidate();
            p_controller_status.add(controller_status);
        }
        
        

        // set the setpoint display
        l_setpoint.setText(String.format("%5.2f", start_setpoint));
        e_new_setpoint.setText(l_setpoint.getText());
        
    }
    
    /**
     * Displays the STOPPED message in GUI.
     */
    public void setStopped() {
        l_running.setText("Stopped");
        l_running.setForeground(Color.RED);
    }
    
    
    
    /**
     * Adjust the displayed laser power maximal value and store it for
     * progressbar calculations.
     * @param value max laser power value
     */
    public void setLaserPowerDisplayMax(double value) {
        // update GUI display
        l_laser_power_max.setText(String.format("%5.2f",value));
        // update internal value
        laser_power_max = value;
    }
    
    /**
     * Update analyzer description.
     * 
     * @param description A short description of the analyzer's outputs.
     * 
     */
    public void updateAnalyzerDescription(String description) {
        l_realtime_plot.setText(description);
    }
    
    /**
     * Update displayed laser power to new value
     * @param value new value of laser power
     */
    public void updateLaserPowerDisplay(double value) {
        // update text
        String text = String.format("%6.4f", value);
        l_current_laser_power.setText(text);
        // calculate how full should the progressbar be
        double pctage = 100.0 * value / laser_power_max;
        // constrain the value between 0 and 100
        if (pctage>100.0)
            pctage = 100.0;
        if (pctage<0.0)
            pctage = 0.0;
        
        pb_laser_power.setValue((int) pctage);
    }

    /**
     * Update displayed FPS to new value
     * @param value new value of FPS
     */
    public void updateFPS(int value) {
        l_fps.setText(String.format("%d", value));
    }
    
    /**
     * Update last analysis duration to new value
     * @param duration_ms last analysis duration in ms
     */
    public void updateLastAnalysisDuration(int duration_ms) {
        l_last_analysis_duration.setText(String.format("%d", duration_ms));
    }
    
    /**
     * Update the GUI display of ROI status
     * @param is_set true if ROI is set
     */
    public void setRoiStatus(boolean is_set) {
        if (is_set)
            l_roi_isset.setText("ROI: Set");
        else
            l_roi_isset.setText("ROI: Not set");
    }
    
    /**
     * Update the plow with new data
     * @param data data to be plotted
     */
    public void updatePlot(GraphData data) {
        // set data to the graph
        realtime_graph.setData(data);
        
        // rescale y-bounds from 0 to 10% above historical max value
        GraphData.Bounds bounds = data.getBounds();
        plot_max = (1.1*bounds.yMax > plot_max) ? 1.1*bounds.yMax : plot_max;
        realtime_graph.setBounds(bounds.xMin, bounds.xMax, 0.0, plot_max);
        
        realtime_graph.repaint();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        pb_laser_power = new javax.swing.JProgressBar();
        jLabel7 = new javax.swing.JLabel();
        l_laser_power_max = new javax.swing.JLabel();
        l_current_laser_power = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        l_setpoint = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        e_new_setpoint = new javax.swing.JTextField();
        b_set_setpoint = new javax.swing.JButton();
        l_roi_isset = new javax.swing.JLabel();
        b_set_ROI = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        l_analyzer = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        l_controller = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        l_laser = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        l_fps = new javax.swing.JLabel();
        l_last_analysis_duration = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        l_running = new javax.swing.JLabel();
        b_stop = new javax.swing.JButton();
        p_realtime_plot_parent = new javax.swing.JPanel();
        l_realtime_plot = new javax.swing.JLabel();
        p_realtime_plot = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        p_analyzer_status = new javax.swing.JPanel();
        p_controller_status = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("ALICA Monitor");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        pb_laser_power.setOrientation(1);
        pb_laser_power.setValue(90);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel7.setText("0.0");

        l_laser_power_max.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        l_laser_power_max.setText("50.0");

        l_current_laser_power.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        l_current_laser_power.setText("0.0");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(l_current_laser_power, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(l_laser_power_max, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pb_laser_power, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(19, 19, 19))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pb_laser_power, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(l_laser_power_max)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel7)))
                .addGap(14, 14, 14)
                .addComponent(l_current_laser_power)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel4.setText("Current setpoint:");

        l_setpoint.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        l_setpoint.setText("1.0");

        jLabel5.setText("New setpoint:");

        e_new_setpoint.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        e_new_setpoint.setText("1.0");

        b_set_setpoint.setText("Set");
        b_set_setpoint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_set_setpointActionPerformed(evt);
            }
        });

        l_roi_isset.setText("ROI: Not set");

        b_set_ROI.setText("Set ROI");
        b_set_ROI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_set_ROIActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(l_setpoint, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(e_new_setpoint, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(b_set_setpoint))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(l_roi_isset)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(b_set_ROI)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(l_setpoint))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(e_new_setpoint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(b_set_setpoint)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(b_set_ROI)
                    .addComponent(l_roi_isset))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Analyzer:");

        l_analyzer.setText("Analyzer name...");
        l_analyzer.setMaximumSize(new java.awt.Dimension(85, 14));

        jLabel2.setText("Controller:");

        l_controller.setText("jLabel4");
        l_controller.setMaximumSize(new java.awt.Dimension(95, 14));

        jLabel3.setText("Laser:");

        l_laser.setText("jLabel4");
        l_laser.setMaximumSize(new java.awt.Dimension(85, 14));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(l_analyzer, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(l_controller, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(l_laser, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(l_analyzer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(l_controller, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(l_laser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel8.setText("Frames per second:");

        jLabel9.setText("Last analysis:");

        l_fps.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        l_fps.setText("0.0");

        l_last_analysis_duration.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        l_last_analysis_duration.setText("0");

        jLabel12.setText("ms");

        l_running.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        l_running.setForeground(new java.awt.Color(0, 120, 0));
        l_running.setText("Running...");

        b_stop.setText("STOP");
        b_stop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_stopActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(l_fps, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(l_last_analysis_duration, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(l_running)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(b_stop)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(l_fps))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(l_last_analysis_duration)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(l_running)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(b_stop)
                .addContainerGap())
        );

        p_realtime_plot_parent.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        p_realtime_plot_parent.setPreferredSize(new java.awt.Dimension(380, 180));

        l_realtime_plot.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout p_realtime_plotLayout = new javax.swing.GroupLayout(p_realtime_plot);
        p_realtime_plot.setLayout(p_realtime_plotLayout);
        p_realtime_plotLayout.setHorizontalGroup(
            p_realtime_plotLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        p_realtime_plotLayout.setVerticalGroup(
            p_realtime_plotLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 129, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout p_realtime_plot_parentLayout = new javax.swing.GroupLayout(p_realtime_plot_parent);
        p_realtime_plot_parent.setLayout(p_realtime_plot_parentLayout);
        p_realtime_plot_parentLayout.setHorizontalGroup(
            p_realtime_plot_parentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, p_realtime_plot_parentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(p_realtime_plot_parentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(p_realtime_plot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(l_realtime_plot, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE))
                .addContainerGap())
        );
        p_realtime_plot_parentLayout.setVerticalGroup(
            p_realtime_plot_parentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, p_realtime_plot_parentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(p_realtime_plot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(l_realtime_plot, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        p_analyzer_status.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        p_analyzer_status.setPreferredSize(new java.awt.Dimension(210, 160));

        javax.swing.GroupLayout p_analyzer_statusLayout = new javax.swing.GroupLayout(p_analyzer_status);
        p_analyzer_status.setLayout(p_analyzer_statusLayout);
        p_analyzer_statusLayout.setHorizontalGroup(
            p_analyzer_statusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 206, Short.MAX_VALUE)
        );
        p_analyzer_statusLayout.setVerticalGroup(
            p_analyzer_statusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        p_controller_status.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        p_controller_status.setPreferredSize(new java.awt.Dimension(200, 150));

        javax.swing.GroupLayout p_controller_statusLayout = new javax.swing.GroupLayout(p_controller_status);
        p_controller_status.setLayout(p_controller_statusLayout);
        p_controller_statusLayout.setHorizontalGroup(
            p_controller_statusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 204, Short.MAX_VALUE)
        );
        p_controller_statusLayout.setVerticalGroup(
            p_controller_statusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 156, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(p_analyzer_status, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(p_controller_status, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(p_controller_status, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                    .addComponent(p_analyzer_status, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(p_realtime_plot_parent, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(p_realtime_plot_parent, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void b_set_setpointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_set_setpointActionPerformed
        double setpoint;
        // sanitize input
        try {
            setpoint = Double.parseDouble(e_new_setpoint.getText());
        } catch (NumberFormatException ex) {
            IJ.showMessage("Malformed setpoint format string.");
            return;
        }
        if (setpoint<0.0) {
            IJ.showMessage("Setpoint can't be negative!");
            return;
        }
        
        // notify the coordinator and update display text
        coordinator.setSetpoint(setpoint);
        l_setpoint.setText(e_new_setpoint.getText());
    }//GEN-LAST:event_b_set_setpointActionPerformed

    private void b_set_ROIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_set_ROIActionPerformed
        boolean is_set = coordinator.setCurrentROI();
        setRoiStatus(is_set);
    }//GEN-LAST:event_b_set_ROIActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (coordinator.isRunning())
            return;
        else
            this.dispose();
    }//GEN-LAST:event_formWindowClosing

    private void b_stopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b_stopActionPerformed
        coordinator.requestStop();
    }//GEN-LAST:event_b_stopActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton b_set_ROI;
    private javax.swing.JButton b_set_setpoint;
    private javax.swing.JButton b_stop;
    private javax.swing.JTextField e_new_setpoint;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel l_analyzer;
    private javax.swing.JLabel l_controller;
    private javax.swing.JLabel l_current_laser_power;
    public javax.swing.JLabel l_fps;
    private javax.swing.JLabel l_laser;
    public javax.swing.JLabel l_laser_power_max;
    public javax.swing.JLabel l_last_analysis_duration;
    private javax.swing.JLabel l_realtime_plot;
    private javax.swing.JLabel l_roi_isset;
    private javax.swing.JLabel l_running;
    private javax.swing.JLabel l_setpoint;
    private javax.swing.JPanel p_analyzer_status;
    private javax.swing.JPanel p_controller_status;
    private javax.swing.JPanel p_realtime_plot;
    public javax.swing.JPanel p_realtime_plot_parent;
    public javax.swing.JProgressBar pb_laser_power;
    // End of variables declaration//GEN-END:variables
}
