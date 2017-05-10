/** QuickPALM plugin developed by Ricardo Henriques @ Instituto de Medicina 
 *  Molecular (PT)/Institut Pasteur (FR).
 *  Adapted under GPL by Marcel Stefko, 2017.
 *
 * Main container to all dialogs within the plugin */

package ch.epfl.leb.alica.analyzers.quickpalm;

import ij.*;
import ij.io.*;
import ij.plugin.frame.*;
import ij.gui.*;

class MyDialogs
{
	
	ImagePlus imp;
	RoiManager rmanager;
	Roi [] rois;
	
	ij.Prefs prefs = new ij.Prefs(); 
	
	java.lang.String ptablefile;
	
	int width;
	int height;
	int nslices;
	String imtitle;
	int nrois;
	
	int snr;
	double pixelsize;
	
	double fwhm;
	//int roirad;
	double pthrsh = 0.2;
	boolean smartsnr;

	int buffer;
	double cal_z;
	int window;
	double magn;

	// advanced settings
	double minsize;
	double maxsize;
	double symmetry;
	int maxpart; //maximum particles
	int threads;

	// used on calib
	java.lang.String model;
	java.lang.String [] models = new java.lang.String[5]; // used for the calibration
	boolean part_divergence;
	boolean part_extrainfo;
	
	// used on is3d
	boolean is3d;
	java.lang.String calfile;
	
	// used on view
	boolean view = true;
	int viewer_accumulate;
	int viewer_update;
	double saturation;
	
	// used on attach
	boolean attach;
	java.lang.String imagedir;
	java.lang.String pattern;
	java.lang.String prefix;
	java.lang.String sufix;
	int nimchars;
	int nimstart;
	int waittime;
	
	// Reconstruct interface vars
	double viewer_tpixelsize;
	int viewer_owidth;
	int	viewer_oheight;
	//double saturation;
		
	java.lang.String [] view_modes = new java.lang.String[4];
	java.lang.String view_mode;
	boolean viewer_doConvolve;
	//boolean viewer_doBW;
	boolean viewer_do3d;
	boolean viewer_doMovie;
	//boolean viewer_doSave;
		
	double viewer_mergeabove;
	double viewer_mergebellow;
	
	double viewer_fwhm;
	boolean viewer_is8bit;
	double viewer_zstep;
	//int viewer_update;
	//int viewer_accumulate;
	

	public boolean analyseParticles(MyFunctions f) {	
		GenericDialog gd = new GenericDialog("Analyse PALM/STORM Particles");
		gd.addNumericField("Minimum SNR", prefs.get("QuickPALM.snr", 5), 2);
		gd.addNumericField("Maximum FWHM (in px)", prefs.get("QuickPALM.fwhm", 4), 0);
		gd.addNumericField("Image plane pixel size (nm)", prefs.get("QuickPALM.pixelsize", 106), 2);
		gd.addCheckbox("Smart SNR", prefs.get("QuickPALM.smartsnr", true));
		gd.addCheckbox("3D PALM (astigmatism) - will require calibration file", prefs.get("QuickPALM.is3d", false));
		gd.addCheckbox("Online rendering", prefs.get("QuickPALM.view", true));
		gd.addCheckbox("Attach to running acquisition", prefs.get("QuickPALM.attach", false));
		gd.addCheckbox("Stream particle info directly into file", prefs.get("QuickPALM.stream", true));
		gd.addMessage("\n");
		// -----------------------------------------
		gd.addMessage("-- Online rendering settings (used only if selected) --");
		gd.addMessage("\n");
		gd.addNumericField("Pixel size of rendered image (nm)", 30, 2);
		gd.addNumericField("Accumulate last (0 to accumulate all frames)", 0, 0);
		gd.addNumericField("Update every (frames)", 10, 0);
		//gd.addNumericField("Allow color saturation (%)", 50, 0);
		gd.addMessage("\n");
		// -----------------------------------------
		gd.addMessage("-- Attach to running acquisition settings (used only if selected) --");
		gd.addMessage("\n");
		gd.addStringField("_Image name pattern (NN...NN represents the numerical change)", prefs.get("QuickPALM.pattern", "imgNNNNNNNNN.tif"), 20);
		gd.addNumericField("Start NN...NN with", 0, 0);
		gd.addNumericField("In acquisition max. wait time for new image (ms)", 50, 0);
		gd.addMessage("\n");
		// -----------------------------------------
		gd.addMessage("-- Advanced settings (don't normally need to be changed) --");
		gd.addMessage("\n");
		gd.addNumericField("_Minimum symmetry (%)", prefs.get("QuickPALM.symmetry", 50), 0);
		gd.addNumericField("Local threshold (% maximum intensity)", prefs.get("QuickPALM.lthreshold", 20), 0);
		gd.addNumericField("_Maximum iterations per frame", prefs.get("QuickPALM.maxiter", 1000), 0);
		gd.addNumericField("Threads (each takes ~3*[frame size] in memory)", prefs.get("QuickPALM.nthreads", 50), 0);
		gd.addMessage("\n\nDon't forget to save the table in the end...");
		
		/*gd.showDialog();
		if (gd.wasCanceled())
                    return false;*/

		snr = (int) gd.getNextNumber();
		prefs.set("QuickPALM.snr", snr);
		fwhm = gd.getNextNumber();
		prefs.set("QuickPALM.fwhm", fwhm);
		pixelsize = gd.getNextNumber();
		prefs.set("QuickPALM.pixelsize", pixelsize);
		
		smartsnr = gd.getNextBoolean();
		prefs.set("QuickPALM.smartsnr", smartsnr);
		is3d = gd.getNextBoolean();
		prefs.set("QuickPALM.is3d", is3d);
		view = gd.getNextBoolean();
		prefs.set("QuickPALM.view", view);
		attach = gd.getNextBoolean();
		prefs.set("QuickPALM.attach", attach);
		
		if (gd.getNextBoolean())
		{
			prefs.set("QuickPALM.stream", true);
		}
		else prefs.set("QuickPALM.stream", false);
		//--
		
		magn = pixelsize/gd.getNextNumber();
		viewer_accumulate = (int) gd.getNextNumber();
		viewer_update = (int) gd.getNextNumber();
		
		//--
		pattern = gd.getNextString().trim();
		prefs.set("QuickPALM.pattern", pattern);
		prefix = pattern.substring(0, pattern.indexOf("N"));
		sufix = pattern.substring(pattern.lastIndexOf("N")+1, pattern.length());
		nimchars = pattern.split("N").length-1;
		nimstart = (int) gd.getNextNumber();
		waittime = (int) gd.getNextNumber();
		
		//--
		
		symmetry = gd.getNextNumber()/100;
		prefs.set("QuickPALM.symmetry", symmetry);
		pthrsh = gd.getNextNumber()/100;
		prefs.set("QuickPALM.lthreshold", pthrsh*100);
		maxpart = (int) gd.getNextNumber();
		prefs.set("QuickPALM.maxiter", maxpart);
		threads = (int) gd.getNextNumber();
		prefs.set("QuickPALM.nthreads", threads);
		
		return true;
	}
}