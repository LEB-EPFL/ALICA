//Class:         SpotCounterCore
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nico Stuurman (adapted 2017 by Marcel Stefko)
//
// COPYRIGHT:    University of California, San Francisco 2015
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

package ch.epfl.leb.alica.analyzers.spotcounter;

///////////////////////////////////////////////////////////////////////////////

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.sqrt;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Core of the SpotCounter algorithm
 * @author Nico Stuurman
 */
public class SpotCounterCore {
    private final ImagePlus live_view;
    private final int nPasses_ = 1;
    private int pasN_ = 0;
    private final String preFilterChoice_ = "";
    private boolean start_ = true;
    private ResultsTable res_;
    private ResultsTable res2_;
    private static final boolean outputAllSpots_ = false;
    private static final FindLocalMaxima.FilterType filter_
            = FindLocalMaxima.FilterType.NONE;
    private int boxSize_;
    private int noiseTolerance_;
    private Roi roi;
    
    /**
     *
     * @param noiseTolerance minimum peak value
     * @param boxSize size of scanning box
     * @param live_view if true, live preview is shown
     */
    public SpotCounterCore(int noiseTolerance, int boxSize, boolean live_view) {
        boxSize_ = boxSize;
        noiseTolerance_ = noiseTolerance;
        
        if (live_view) {
            this.live_view = new ImagePlus("SpotCounter live view");
            this.live_view.show();
        } else {
            this.live_view = null;
        }
    }
    
    /**
     * Hide live view window if it exists.
     */
    public void dispose() {
        if (this.live_view != null) {
            this.live_view.hide();
        }
    }
    
    /**
     * Set new parameters for the analysis
     * @param noiseTolerance minimum peak value
     * @param boxSize size of scanning box
     */
    public void setParams(int noiseTolerance, int boxSize) {
        boxSize_ = boxSize;
        noiseTolerance_ = noiseTolerance;
    }

    /**
     * Analyzes the image and returns information about current state.
     *
     * @param ip - image to be analyzed
     * @return ResultsTable which contains information about analysis results.
     */
    public HashMap<String,Double> analyze(ImageProcessor ip) {
        Overlay ov = getSpotOverlay(ip);
        if (live_view != null) {
            live_view.setProcessor(ip);
            live_view.setOverlay(ov);
            live_view.updateAndDraw();
            live_view.show();
        }
        return getFrameStats(ov);

    }
    
    /**
     * Analyzes the overlay and returns statistics about spot positions.
     *
     * @param ov - Overlay which contains spot position information
     * @return HashMap with spot position statistics.
     */
    private HashMap<String, Double> getFrameStats(Overlay ov) {
        HashMap<String, Double> map = new LinkedHashMap<String, Double>();
        
        double dist2;
        double[] min_distances = new double[ov.size()];
        for (int i=0; i<ov.size(); i++) {
            Rectangle rect_i = ov.get(i).getBounds();
            double min_dist2 = 1000000000.0;
            for (int j=0; j<ov.size(); j++) {
                if (i==j)
                    continue;
                Rectangle rect_j = ov.get(j).getBounds();
                dist2 = ((rect_i.getCenterX() - rect_j.getCenterX())* 
                         (rect_i.getCenterX() - rect_j.getCenterX()) ) +
                        ((rect_i.getCenterY() - rect_j.getCenterY())* 
                         (rect_i.getCenterY() - rect_j.getCenterY()) );
                if (dist2<min_dist2)
                    min_dist2 = dist2;
            }
            min_distances[i] = sqrt(min_dist2);
        }
        
        Arrays.sort(min_distances);
        double mean = 0.0;
        for (double val: min_distances)
            mean += val;
        mean /= ov.size();
        
        // if no spots found
        if (min_distances.length == 0) {
            min_distances = new double[1];
            min_distances[0] = 0.0;
        }
        
        map.put("min-distance", min_distances[0]);
        map.put("mean-distance", mean);
        int p10 = (int) floor((double) ov.size() / 10.0);
        map.put("p10-distance", min_distances[p10]);
        map.put("spot-count", (double)ov.size());
        return map;
    }
    
    
    /**
     * Finds local maxima and returns them as an collection of Rois (an overlay)
     *
     * @param ip - ImageProcessor to be analyzed
     * @return overlay with local maxima
     */
    private Overlay getSpotOverlay(ImageProcessor ip) {
        Polygon pol = FindLocalMaxima.FindMax(
                ip, roi, boxSize_, noiseTolerance_, filter_);
        int halfSize = boxSize_ / 2;
        Overlay ov = new Overlay();
        for (int i = 0; i < pol.npoints; i++) {
            int x = pol.xpoints[i];
            int y = pol.ypoints[i];
            boolean use = true;
            if (use) {
                Roi roi = new Roi(x - halfSize, y - halfSize, boxSize_, boxSize_);
                roi.setStrokeColor(Color.RED);
                ov.add(roi);
            }
        }
        return ov;
    }
    
    /**
     * Constrain analysis to given ROI.
     * @param roi ROI to constrain analysis to
     */
    public void setROI(Roi roi) {
        this.roi = roi;
    }
}
