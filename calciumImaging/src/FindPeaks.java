/* Copyright (c) 2019 Thomas Braschler <thomas.braschler@unige.ch>
##
## This program is free software; you can redistribute it and/or modify
## it under the terms of the GNU General Public License as published by
## the Free Software Foundation; either version 3 of the License, or
## (at your option) any later version.
##
## This program is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
## GNU General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with this program; if not, see <http://www.gnu.org/licenses/>. 

## This is an ImageJ plugin implementation for finding peaks along the 
## z-direction in a Stack, for each pixel. It makes use of the 
## FindPeaksTools class, in package "FindPeaks.accesory.classes", which provides
## the actual mathematical functionality. This class in turn is mostly a Java re-implementation
## of the Octave findPeaks method, available at https://bitbucket.org/mtmiller/octave-signal
## (direct link: https://searchcode.com/codesearch/view/64213481/)
## The Octave source code is under a General Public License

*/


import java.awt.AWTEvent;

import FindPeaks.accessory.classes.FindPeaksTools;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

// The basic idea of this plugin is that it identifies the peak location in the z-axis of the stack
// it is inspired by Octave findpeaks code.

/** ImageJ plugin to find peaks (maxima) in vertical sections of a z-stack*/
public class FindPeaks implements PlugInFilter,DialogListener {

	/** Holds a reference to the ImagePlus object associated with this plugin */
	protected ImagePlus imp;

	/** image processor at the time of starting the analysis */
	protected ImageProcessor ip;


	/** Expected fraction of pixels above the threshold to be set and thus to be
	 * eligible for being a peak */
	public static double peak_fraction = 0.5;

	/** Minimal distance, in pixels, in the z-direction. This is the minD argument to the 
	 * Octave function findPeaks */
	public static double minD=20;

	/** Minimal width, in pixels, in the z-direction. This is the minW argument to the 
	 * Octave function findPeaks */
	public static double minW =1;

	/** Maximum width, in pixels, in the z-direction. This is the maxW argument to the 
	 * Octave function findPeaks */
	public static double maxW = 100;

	/** Minimum height, in intensity units, above the background level. This is the minH 
	 * of the Octave function findPeaks, but is interpreted as being the difference to 
	 * background */
	public static double minH=5.5;
	
	/** Octave's findpeak function works in two parts: primary peak detection, and then filtering
	 * according to various criteria to reject local maxima that do not satisfy the quality criteria
	 * This variable indicates whether this filtering step should be done (true) or not (false)
	 */
	public static boolean doFiltering=true;


	/**
	 * @inheritDoc
	 *
	 * For this class, read the peak_fraction, minD, doFiltering, minW, maxW and minH parameters
	 * from the dialog
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {

		// Intermediate variable to accept numbers before filtering
		double n;

		// Get the first number from the dialog
		n = gd.getNextNumber();
		// Do basic checking, should be a valid number
		if (gd.invalidNumber())
			return false;
		// More checking, should be between 0 and 1

		if(n<0)
		{
			n=0;
		}
		if(n>1)
		{
			n=1;
		}
		peak_fraction=n;

		// Get the first number from the dialog
		n = gd.getNextNumber();
		// Do basic checking, should be a valid number
		if (gd.invalidNumber())
			return false;
		// More checking, should be between 0 and 1

		if(n<2)
		{
			n=2;
		}

		minD=n;
		
		doFiltering = gd.getNextBoolean();

		// Get the first number from the dialog
		n = gd.getNextNumber();
		// Do basic checking, should be a valid number
		if (gd.invalidNumber())
			return false;
		// More checking, should be between 0 and 1

		if(n<1)
		{
			n=1;
		}

		minW=n;

		// Get the first number from the dialog
		n = gd.getNextNumber();
		// Do basic checking, should be a valid number
		if (gd.invalidNumber())
			return false;
		// More checking, should be between 0 and 1

		if(n<minW)
		{
			n=minW;
		}

		maxW=n;

		// Get the first number from the dialog
		n = gd.getNextNumber();
		// Do basic checking, should be a valid number
		if (gd.invalidNumber())
			return false;
		// More checking, should be between 0 and 1

		if(n<0)
		{
			n=0;
		}

		minH=n;








		return true;
	}



	/**
	 * @inheritDoc
	 *
	 * For this class, indicate that we need greyscale images and also
	 * that the original image is not changed ( a new output image is generated instead)
	 */
	public int setup(String arg, ImagePlus imp) {

		// Store an internal reference to the assigned image
		this.imp = imp;

		// For now, restricted to 8bit greyscale images
		return DOES_8G+NO_CHANGES;
	}
	
	/**
	 * Get the pixel values along the z direction in a stack, at a horizontal location
	 * specified by x and y
	 * @param x x-position in pixel units, 0-based (ImageJ standard)
	 * @param y y-position in pixel units, 0-based (ImageJ standard)
	 * @return Pixel values along the z-profile
	 */

	public int[] getStackSection(int x, int y)
	{

		int[] vals=new int[imp.getStackSize()];


		for(int z=1; z<=imp.getStackSize(); z++)
		{
			ImageProcessor p=imp.getStack().getProcessor(z);

			vals[z-1]=(int)p.getPixelValue(x, y);


		}

		return vals;

	}




	
	

	/** 
	 * @inheritDoc
	 * 
	 *  For this class, displays the dialog for setting the custom options.
	 *  Then finds the temporal peaks with these options for each xy pixel position.
	 *  Output: creates and shows a new stack with identical dimensions to the stack analyzed,
	 *  where non-peak pixels are black (0) and peak pixels white (255)
	 */
	public void run(ImageProcessor theIp) {

		// store the ImageProcessor internall
		ip = theIp;

		// Register this class to avoid garbage collection
		// usually done in ImageJ plugins, not of necessarily of
		// demonstrated usefulness
		IJ.register(this.getClass()); 



		// Show the dialog for choosing the options
		if(!doDialog())
		{
			return;
		}


		ImageStack outputStack = FindPeaksTools.getEmptyByteStack(imp.getStack());
		
		
		for(int x=0; x<imp.getWidth(); x++)
		{
			for(int y=0; y<imp.getHeight(); y++)
			{
				int[] idx = findPeaksInSection(x,y);
				for(int ind=0; ind<idx.length; ind++)
				{
					outputStack.getProcessor(idx[ind]+1).set(x, y, 255);
				}
			}
			
			IJ.showProgress(((double) x)/((double) imp.getWidth()));
		}
		
		
		ImagePlus outputPlus = new ImagePlus("Result findpeaks");
		
		outputPlus.setStack("Result findpeaks - "+imp.getTitle(), outputStack);
		
		outputPlus.show();
		
		


	}
	
	
	
	/**
	 * Finds the indexes of the peaks in the z-Profile at a given xy position
	 * @param x x-position in pixel units, 0-based (ImageJ standard)
	 * @param y y-position in pixel units, 0-based (ImageJ standard)
	 * @return Array of peak positions (pixel units, 0-based) in the z-profile
	 */
	public int[] findPeaksInSection(int x, int y)
	{
		
		int[] intSection = getStackSection(x,y);

		double[] theSection = new double [intSection.length];

		for(int ind=0; ind<theSection.length; ind++)
		{
			theSection[ind]=(double)intSection[ind];
		}

		double threshold = FindPeaksTools.getQuantile(
				FindPeaksTools.getHistogram(intSection)
				, 1-peak_fraction);
		
		
		
		for(int ind=0; ind<theSection.length;ind++)
		{
			theSection[ind]=theSection[ind]-threshold;
		}
		
		int[] idx=FindPeaksTools.findPeaks(theSection, 0, minD, doFiltering, 
				minW,  maxW,  minH);

		
		return idx;

		
	}




	/** 
	 * @inheritDoc
	 * 
	 *  For this class, displays the dialog with the various findPeaks options
	 */

	public boolean doDialog()
	{

		// Open a dialog to get the use variables
		// As a particular feature of ImageJ, does not open in macro mode but
		// is substituted with macro parameter values instead
		GenericDialog gd = new GenericDialog("Temporal Peak plugin (FindPeaks)");


		// Add the fields


		gd.addNumericField("Target fraction of time activated", peak_fraction, 2);

		gd.addNumericField("Minimal distance between peaks in pixels (z-direction)", minD, 1);
		
		gd.addCheckbox("Do Filtering with options below", doFiltering);

		gd.addNumericField("Minimal width of peak in pixels (z-direction)", minW, 1);

		gd.addNumericField("Maximum width of peak in pixels (z-direction)", maxW, 1);

		gd.addNumericField("Minimum peak intensity above background", minH, 1);

		// We need to follow the dialog to update the class variables
		gd.addDialogListener(this);
		// Show the dialog



		gd.showDialog();                    // input by the user (or macro) happens here
		// Do not proceed when the use pushes cancel



		return (!gd.wasCanceled());



	}




}
