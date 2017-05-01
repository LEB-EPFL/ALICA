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
import ch.epfl.leb.alica.ImagingMode;
import com.google.common.eventbus.Subscribe;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.micromanager.Studio;
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import org.micromanager.data.NewImageEvent;
import org.micromanager.events.LiveModeEvent;

/**
 * This thread continuously queries either the MMCore, or the processing pipeline
 * of the live mode for new images, and calls the analyzer's processImage() method
 * on them as fast as it can. Always the latest image is taken for analysis, so
 * it is possible for images to be skipped. It also gathers some statistics for
 * display by the GUI.
 * @author Marcel Stefko
 */
public class AnalysisWorker extends Thread {
    private boolean stop_flag = false;
    
    private final Coordinator coordinator;
    private final Studio studio;
    private final Analyzer analyzer;
    private final ImagingMode imaging_mode;
    
    // recieves signals from the live view Datastore, and passes the latest
    // Coords to last_live_image_coords
    private final NewLiveImageWatcher new_image_watcher;
    private Coords last_live_image_coords = null;
    // for comparison with newly acquired images, to see if the image has
    // changed
    private Object last_core_image = null;
    
    // for GUI output
    private long last_analysis_time_ms = 0;
    private int last_fps_count = 0;
    
    /**
     * Initialize the worker.
     * @param coordinator parent Coordinator
     * @param studio for logging and image queries
     * @param analyzer this Analyzer's processImage() method is called on gathered images
     * @param imaging_mode
     */
    public AnalysisWorker(Coordinator coordinator, Studio studio, Analyzer analyzer, ImagingMode imaging_mode) {
        this.setName("Analysis Worker");
        
        this.coordinator = coordinator;
        this.studio = studio;
        this.analyzer = analyzer;
        this.imaging_mode = imaging_mode;
        
        this.new_image_watcher = new NewLiveImageWatcher(this.analyzer, this);
    }
    
    /**
     * Called by the NewImageWatcher to update last coords
     * @param coords new Coords
     */
    void setLastImageCoords(Coords coords) {
        this.last_live_image_coords = coords;
    }
    
    /**
     * Called by the MMCore to signalize there is a new live mode, so that we
     * can registerForEvents the NewImageWatcher.
     * @param evt new live mode event
     */
    @Subscribe
    public void liveModeStarted(LiveModeEvent evt) {
        if (evt.getIsOn() && imaging_mode.equals(ImagingMode.LIVE)) {
            this.studio.live().getDisplay().getDatastore().registerForEvents(this.new_image_watcher);
        }
    }
    
    
    
    @Override
    public void run() {
        // FPS counters
        long fps_time = coordinator.getTimeMillis();
        int fps_count = 0;
        // loop while asked to stop
        while (!this.stop_flag) {
            // either draw from core or live mode datastore
            if (imaging_mode.equals(ImagingMode.GRAB_FROM_CORE))
                getNewImageFromCoreAndAnalyze();
            else
                getNewImageFromLiveAndAnalyze();
            // increment fps counter after each image
            fps_count++;
            
            // if a second has passed, store value and reset FPS counters
            if ((coordinator.getTimeMillis() - fps_time) > 1000) {
                last_fps_count = fps_count;
                fps_count = 0;
                fps_time = coordinator.getTimeMillis();
            }
        }
    }
    
    /**
     * Grabs new images from the Datastore associated with Live mode, analyzes
     * it.
     */
    public void getNewImageFromLiveAndAnalyze() {
        Coords current_coords;
        // wait for image acquisition by NewLiveImageWatcher
        synchronized(analyzer) {
            if (this.last_live_image_coords==null) {
                try {
                    analyzer.wait();
                } catch (InterruptedException ex) {
                    // if we get interrupted, exit disgracefully
                    throw new RuntimeException("AnalysisWorker was interrupted!");
                }
            }
            current_coords = this.last_live_image_coords;
        }
        long image_acquisition_time = coordinator.getTimeMillis();
        // draw the next image from the core or from the display
        // we don't want the last_live_image_coords to change during this operation
        synchronized(analyzer) {
            try {
                Image img = studio.live().getDisplay().getDatastore().getImage(last_live_image_coords);
                analyzer.processImage(img.getRawPixels(), img.getWidth(), img.getHeight(), studio.core().getPixelSizeUm(), image_acquisition_time);
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
            // clear last image coords pointer
            this.last_live_image_coords = null;
        }
        last_analysis_time_ms = coordinator.getTimeMillis() - image_acquisition_time;
    }
    
    /**
     * Acquire the new image directly from MMCore and send for analysis.
     */
    public void getNewImageFromCoreAndAnalyze() {
        long image_acquisition_time = coordinator.getTimeMillis();
        Object new_image = null;
        // query core for new image, if failed, just log it and enter loop
        try {
            new_image = studio.core().getLastImage();
        } catch (Exception ex) {
            studio.core().logMessage("Failure by AnalysisWorker to recieve image from MMCore.", true);
        }
        // if no image was detected, or is the same as last analyzed, wait 1ms and query again
        while (new_image==null || new_image.equals(this.last_core_image)) {
            try {
                sleep(1);
            } catch (InterruptedException ex) {
                throw new RuntimeException("Analysis worker interrupted while waiting for new core image.");
            }
            try {
                // also update acq time value
                image_acquisition_time = coordinator.getTimeMillis();
                new_image = studio.core().getLastImage();
            } catch (Exception ex) {
                studio.core().logMessage("Failure by AnalysisWorker to recieve image from MMCore.", true);
            }
        }
        // so we have a new image, now we process it and store for later comparison
        synchronized(analyzer) {
            analyzer.processImage(new_image, (int) studio.core().getImageWidth(), (int) studio.core().getImageHeight(), studio.core().getPixelSizeUm(), image_acquisition_time);
            this.last_core_image = new_image;
        }
    }
    
    /**
     * Used for GUI rendering.
     * @return intermittent output of the analyzer
     */
    public double queryAnalyzerForIntermittentOutput() {
        synchronized(this.analyzer) {
            return this.analyzer.getIntermittentOutput();
        }
    }
    
    /**
     * Analyzer's internal state might change, and the output is passed on
     * to the controller.
     * @return batched output of analyzer
     */
    public double queryAnalyzerForBatchOutput() {
        synchronized(this.analyzer) {
            return this.analyzer.getBatchOutput();
        }
    }
    
    /**
     * 
     * @return duration of last analysis in milliseconds
     */
    public long getLastAnalysisTime() {
        return last_analysis_time_ms;
    }
    
    /**
     *
     * @return number of analyzed frames in the last second
     */
    public int getCurrentFPS() {
        return last_fps_count;
    }
    
    /**
     * Stops the analyzer after finalizing the current analysis.
     */
    public void requestStop() {
        this.stop_flag = true;
    }
}

/**
 * The watcher is subscribed to a Datastore by the AnalysisWorker, and then it
 * informs the AnalysisWorker of any new images in the Datastore.
 * @author Marcel Stefko
 */
class NewLiveImageWatcher {
    private final Object object_to_lock;
    private final AnalysisWorker thread_to_notify;
    
    public NewLiveImageWatcher(Object object_to_lock, AnalysisWorker thread_to_notify) {
        this.object_to_lock = object_to_lock;
        this.thread_to_notify = thread_to_notify;
    }
    
    /**
     * Notify the thread that new image is available and send it the coords.
     * @param evt event containing coords
     */
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

