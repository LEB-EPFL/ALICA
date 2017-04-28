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
package ch.epfl.leb.alica.workers;

import ch.epfl.leb.alica.Analyzer;
import com.google.common.eventbus.Subscribe;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.micromanager.Studio;
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import org.micromanager.data.NewImageEvent;
import org.micromanager.events.LiveModeEvent;

/**
 *
 * @author stefko
 */
public class AnalysisWorker extends Thread {
    private boolean stop_flag = false;
    
    private final Coordinator coordinator;
    private final Studio studio;
    private final Analyzer analyzer;
    private final boolean draw_from_core;
    
    private final NewImageWatcher new_image_watcher;
    private Coords last_image_coords = null;
    
    private long last_analysis_time_ms = 0;
    private int last_fps_count = 0;
    
    public AnalysisWorker(Coordinator coordinator, Studio studio, Analyzer analyzer, boolean draw_from_core) {
        this.setName("Continual analysis worker");
        
        this.coordinator = coordinator;
        this.studio = studio;
        this.analyzer = analyzer;
        this.draw_from_core = draw_from_core;
        
        
        this.new_image_watcher = new NewImageWatcher(this.analyzer, this);
    }
    
    public void setLastImageCoords(Coords coords) {
        this.last_image_coords = coords;
    }
    
    @Subscribe
    public void liveModeStarted(LiveModeEvent evt) {
        if (evt.getIsOn()) {
            this.studio.live().getDisplay().getDatastore().registerForEvents(this.new_image_watcher);
        }
    }
    
    @Override
    public void run() {
        long fps_time = coordinator.getTimeMillis();
        int fps_count = 0;
        while (!this.stop_flag) {
            Coords current_coords;
            // wait for image acquisition by NewImageWatcher
            synchronized(analyzer) {
                if (this.last_image_coords==null) {
                    try {
                        analyzer.wait();
                    } catch (InterruptedException ex) {
                        // if we get interrupted, exit disgracefully
                        Logger.getLogger(Coordinator.class.getName()).log(Level.SEVERE, null, ex);
                        throw new RuntimeException("AnalysisWorker was interrupted!");
                    }
                }
                current_coords = this.last_image_coords;
            }
            long image_acquisition_time = coordinator.getTimeMillis();
            // draw the next image from the core or from the display
            // we don't want the last_image_coords to change during this operation
            synchronized(analyzer) {
                try {
                    if (draw_from_core) {
                        analyzer.processImage(studio.core().getLastImage(), (int) studio.core().getImageWidth(), (int) studio.core().getImageHeight(), studio.core().getPixelSizeUm(), image_acquisition_time);
                    } else {
                        Image img = studio.live().getDisplay().getDatastore().getImage(last_image_coords);
                        analyzer.processImage(img.getRawPixels(), img.getWidth(), img.getHeight(), studio.core().getPixelSizeUm(), image_acquisition_time);
                    } 
                } catch (Exception ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
                // clear last image coords pointer
                this.last_image_coords = null;
            }
            last_analysis_time_ms = coordinator.getTimeMillis() - image_acquisition_time;
            fps_count++;
            
            if ((coordinator.getTimeMillis() - fps_time) > 1000) {
                last_fps_count = fps_count;
                fps_count = 0;
                fps_time = coordinator.getTimeMillis();
            }
        }
    }
    
    public double queryAnalyzerForIntermittentOutput() {
        synchronized(this.analyzer) {
            return this.analyzer.getIntermittentOutput();
        }
    }
    
    public double queryAnalyzerForBatchOutput() {
        synchronized(this.analyzer) {
            return this.analyzer.getBatchOutput();
        }
    }
    
    public long getLastAnalysisTime() {
        return last_analysis_time_ms;
    }
    
    public int getCurrentFPS() {
        return last_fps_count;
    }
    
    
    public void requestStop() {
        this.stop_flag = true;
    }
}

class NewImageWatcher {
    private final Object object_to_lock;
    private final AnalysisWorker thread_to_notify;
    
    public NewImageWatcher(Object object_to_lock, AnalysisWorker thread_to_notify) {
        this.object_to_lock = object_to_lock;
        this.thread_to_notify = thread_to_notify;
    }
    
    @Subscribe
    public void newImageAcquired(NewImageEvent evt) {
        synchronized(object_to_lock) {
            // notify thread that it can wake up
            object_to_lock.notify();
            // store the last image coords
            thread_to_notify.setLastImageCoords(evt.getCoords());
        }
    }
}

