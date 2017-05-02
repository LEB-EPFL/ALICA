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

import ch.epfl.leb.alica.AlicaLogger;
import ch.epfl.leb.alica.Analyzer;
import ch.epfl.leb.alica.ImagingMode;
import com.google.common.eventbus.Subscribe;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.micromanager.Studio;
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.Image;
import org.micromanager.data.NewImageEvent;
import org.micromanager.events.AcquisitionEndedEvent;
import org.micromanager.events.AcquisitionStartedEvent;
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
    private final NewImageWatcher new_image_watcher;
    private Coords last_live_image_coords = null;
    // for comparison with newly acquired images, to see if the image has
    // changed
    private Object last_core_image = null;
    
    // for GUI output
    private long last_analysis_time_ms = 0;
    private int last_fps_count = 0;
    
    // for logging
    private int image_counter = 0;
    
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
        
        this.new_image_watcher = new NewImageWatcher(this.analyzer, this);
    }
    
    /**
     * Called by the NewImageWatcher to update last coords
     * @param coords new Coords
     */
    void setLastImageCoords(Coords coords) {
        this.last_live_image_coords = coords;
    }
    
    /**
     * Called by the MMCore to signalize there is a new live mode. 
     * If the imaging mode is LIVE, the NewImageWatcher will be informed.
     * @param evt new live mode event
     */
    @Subscribe
    public void liveModeStarted(LiveModeEvent evt) {
        if (evt.getIsOn() && imaging_mode.equals(ImagingMode.LIVE)) {
            this.new_image_watcher.setLatestDatastore(this.studio.live().getDisplay().getDatastore());
            this.image_counter = 0;
            AlicaLogger.getInstance().clear();
        }
    }
    
    /**
     * If the imaging mode is NEXT_ACQUISITION, the NewImageWatcher will be informed.
     * @param evt new acquisition started event
     */
    @Subscribe
    public void acquisitionStarted(AcquisitionStartedEvent evt) {
        if (imaging_mode.equals(ImagingMode.NEXT_ACQUISITION)) {
            this.new_image_watcher.setLatestDatastore(evt.getDatastore());
            this.image_counter = 0;
            AlicaLogger.getInstance().clear();
        }
    }
    
    /**
     * If the imaging mode is NEXT_ACQUISITION, the coordinator will 
     * asked to stop.
     * @param evt acquisition stopped
     */
    @Subscribe
    public void acquisitionEnded(AcquisitionEndedEvent evt) {
        if (imaging_mode.equals(ImagingMode.NEXT_ACQUISITION)) {
            this.new_image_watcher.setLatestDatastore(null);
            this.coordinator.requestStop();
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
                getNewImageFromWatcherAndAnalyze();
            // increment fps counter after each image
            fps_count++;
            image_counter++;
            // log the intermittent output
            AlicaLogger.getInstance().addIntermittentOutput(image_counter, this.queryAnalyzerForIntermittentOutput());
            
            // if a second has passed, store value and reset FPS counters
            if ((coordinator.getTimeMillis() - fps_time) > 1000) {
                last_fps_count = fps_count;
                fps_count = 0;
                fps_time = coordinator.getTimeMillis();
            }
        }
    }
    
    /**
     * Grabs new images from the Datastore associated with the NewImageWatcher, analyzes
     * it.
     */
    public void getNewImageFromWatcherAndAnalyze() {
        Coords current_coords;
        // wait for image acquisition by NewImageWatcher
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
                Image img = this.new_image_watcher.getLatestDatastore().getImage(current_coords);
                analyzer.processImage(img.getRawPixels(), img.getWidth(), img.getHeight(), studio.core().getPixelSizeUm(), image_acquisition_time);
            } catch (Exception ex) {
                studio.logs().logError(ex, "Error in image retrieval from datastore or processing by analyzer.");
            }
            // log coords of the image
            AlicaLogger.getInstance().addToLog(image_counter+1, "Coords", current_coords.toString());
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
            studio.logs().logDebugMessage("Failure by AnalysisWorker to recieve image from MMCore.");
        }
        // if no image was detected, or is the same as last analyzed, wait 1ms and query again
        while (new_image==null || areTwoImagesEqual((short[]) new_image, (short[]) this.last_core_image)) {
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
                studio.logs().logDebugMessage("Failure by AnalysisWorker to recieve image from MMCore.");
            }
        }
        // so we have a new image, now we process it and store for later comparison
        synchronized(analyzer) {
            analyzer.processImage(new_image, (int) studio.core().getImageWidth(), (int) studio.core().getImageHeight(), studio.core().getPixelSizeUm(), image_acquisition_time);
            this.last_core_image = new_image;
        }
    }
    
    private boolean areTwoImagesEqual(short[] img1, short[] img2) {
        // if any of these images is null, they are marked as not equal
        if ((img1==null) || (img2==null)) {
            return false;
        }
        for (int i=0; i<10; i++) {
            try {
                if (img1[i]!=img2[i]) {
                    return false;
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                // we reached the end of the array without finding a difference
                studio.logs().logError(ex);
                return true;
            }
        }
        return true;
    }
    
    /**
     * Used for GUI rendering.
     * @return intermittent output of the analyzer
     */
    public double queryAnalyzerForIntermittentOutput() {
        synchronized(this.analyzer) {
            double out = this.analyzer.getIntermittentOutput();
            return out;
        }
    }
    
    /**
     * Analyzer's internal state might change, and the output is passed on
     * to the controller.
     * @return batched output of analyzer
     */
    public double queryAnalyzerForBatchOutput() {
        synchronized(this.analyzer) {
            double out = this.analyzer.getBatchOutput();
            AlicaLogger.getInstance().addBatchedOutput(image_counter, out);
            return out;
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
     * 
     * @return number of analyzed frames since last counter reset,
     *  which could be either caused by live mode start, or acquisition start.
     */
    public int getCurrentImageCount() {
        return this.image_counter;
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
class NewImageWatcher {
    private final Object object_to_lock;
    private final AnalysisWorker thread_to_notify;
    private Datastore latest_datastore;
    
    public NewImageWatcher(Object object_to_lock, AnalysisWorker thread_to_notify) {
        this.object_to_lock = object_to_lock;
        this.thread_to_notify = thread_to_notify;
        this.latest_datastore = null;
    }
    
    public Datastore getLatestDatastore() {
        if (latest_datastore==null) {
            throw new NullPointerException("No datastore associated with watcher!");
        }
        return latest_datastore;
    }
    
    /**
     * Sets the latest datastore, and registers for its events.
     * @param store 
     */
    public void setLatestDatastore(Datastore store) {
        // try unregistering from previous datastore
        if (latest_datastore != null) {
            try {
                latest_datastore.unregisterForEvents(this);
            } catch (Exception ex) {
                org.micromanager.internal.MMStudio.getInstance().logs().logError(ex, "Failure in unsubscribing NewImageWatcher from events.");
            }
        }
        store.registerForEvents(this);
        this.latest_datastore = store;
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

