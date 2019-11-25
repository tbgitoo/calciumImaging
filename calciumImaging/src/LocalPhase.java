import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import FindPeaks.accessory.classes.LocalPhaseTools;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/** 
 * ImageJ plugin to calculate the local phase from a temporal peak image 
 * */
public class LocalPhase implements PlugInFilter,DialogListener, ActionListener {

	/** 
	 * Mask for enhancing calculation speed or defining region of interest	
	 */
	public ImagePlus mask=null;
	
	/**
	 * The title of the mask used in the last run (to remember the user choice)
	 */
	public static String lastMaskTitle=null;
	
	/**
	 * Should we use a mask to speed up calculation by analyzing only the non-zero points in the mask?
	 */
	protected static boolean do_masking=false;
	
	/**
	 * Internal lock, to avoid infinite loops when programmatically updating dialog fields
	 */
	protected boolean locked = false;
	
	/**
	 * The imageJ dialog
	 */
	protected static GenericDialog gd;
	
	/** 
	 * Image stack to analyze
	 * */ 
	protected ImagePlus imp;

	/** 
	 * image processor at the time of starting the analysis
	 */
	protected ImageProcessor ip;

	/** 
	 * Internal flag, to set reference secton to image center at first run
	 */
	public static boolean first=true;

	/** 
	 * x-coordinate of the reference section
	 */
	public static int reference_x=0;
	/**
	 * y-coordinate of the reference section
	 */
	public static int reference_y=0;
	
	/**
	 * Read the position (xy) of the reference section and masking preferences from 
	 * the user dialog
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		// Intermediate variable to accept numbers before filtering
		
		// Possibility to lock to avoid concurrent action of automated change and manual change
		if(locked) { return true;}
		
		double n;

		// Get the first number from the dialog
		n = gd.getNextNumber();
		// Do basic checking, should be a valid number
		if (gd.invalidNumber())
			return false;
		// Must be within the image width

		int x = (int)Math.round(n);

		if(x<0)
		{
			x=0;
		}
		if(x>=imp.getWidth())
		{
			x=imp.getWidth()-1;
		}
		reference_x=x;
		
		

		// Get the first number from the dialog
		n = gd.getNextNumber();
		// Do basic checking, should be a valid number
		if (gd.invalidNumber())
			return false;
		// Must be within the image width

		int y = (int)Math.round(n);

		if(y<0)
		{
			y=0;
		}
		if(y>=imp.getHeight())
		{
			y=imp.getHeight()-1;
		}
		reference_y=y;
		
		do_masking=gd.getNextBoolean();
		
		int mask_ind = gd.getNextChoiceIndex();
		
		mask=WindowManager.getImage(WindowManager.getIDList()[mask_ind]);
		
		lastMaskTitle=mask.getTitle();

		return true;

	}

	/**
	 * Indicate that we need greyscale images and also
	 * that the original image is not changed ( a new output image is generated instead)
	 * If run for the first time, set reference section to center of image
	 */
	public int setup(String arg, ImagePlus imp) {
		if(first)
		{
			reference_x = (int)Math.round((double)imp.getWidth()/2.0);
			reference_y = (int)Math.round((double)imp.getHeight()/2.0);
			first=false;
		}


		// Store an internal reference to the assigned image
		this.imp = imp;
		
		// Display the other Windows for potential masking
		
		

		// For now, restricted to 8bit greyscale images
		return DOES_8G+NO_CHANGES;
	}

	/**
	 * Run the plugin: Start the dialog and recover user preferences; 
	 * Check whether the reference section contains at least two peaks (identifie by non-zero values)
	 * Calculate and show the phase image
	 */
	public void run(ImageProcessor theIp) {
		// TODO Auto-generated method stub

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

		if(!checkSection(reference_x, reference_y))
		{
			IJ.error("At reference point, at least 2 peaks need to be detected\n "
					+ "throughout the measurement time" );
			return;
		}
		
		
		


		
		ImageStack theStack = null;
		
		if(do_masking)
		{
			
			theStack = getPhaseImage(imp, reference_x, reference_y, mask,true);
		} else
		{
			theStack = getPhaseImage(imp, reference_x, reference_y, true);
		}
		
		

		ImagePlus output=new ImagePlus("Result Local Phase");
		
		output.setStack("Result local Phase - "+imp.getTitle(),theStack);
		
		
		
		output.show();
		
		
		//IJ.showMessage("Phase angle="+(getPhaseAtxy( x, y, idx_ref)/Math.PI*180.0));



	}
	
	public static ImageStack getPhaseImage(ImagePlus inputImage, int ref_x, int ref_y, ImagePlus theMask)
	{
	 return getPhaseImage(inputImage, ref_x, ref_y, theMask, false);	
	}
	
	public static ImageStack getPhaseImage(ImagePlus inputImage, int ref_x, int ref_y, boolean showOutput)
	{
		return getPhaseImage(inputImage, ref_x, ref_y, null, showOutput);
		
	}
	
	public static ImageStack getPhaseImage(ImagePlus inputImage, int ref_x, int ref_y)
	{
		return getPhaseImage(inputImage, ref_x, ref_y, null, false);
		
	}
	
	/** Calculate phase image
	 * 
	 * @param inputImage The image indicating peak locations (pixel value larger than 0) in the z-profiles. 
	 * @param ref_x X-coordinate of the reference section
	 * @param ref_y Y-coordinate of the reference section
	 * @param theMask Mask image to limit analysis, provide null to not use this option
	 * @param showOutput Be verbose about output (ImageJ message boxes)
	 * @return New ImageStack, with a single slice, shows the local phase (or NaN if none could be evaluated)
	 */
	
	public static ImageStack getPhaseImage(ImagePlus inputImage, int ref_x, 
			int ref_y, ImagePlus theMask, boolean showOutput)
	{
		// Get the non-zero indices at the reference section
		int [] idx_ref = non_zero_indices(inputImage, ref_x, ref_y);

		if(showOutput)
		{
			IJ.showMessage("Reference section : "+idx_ref.length+" peaks detected");
		}
		// Intialize the output stack, single slice
		ImageStack theStack = new ImageStack(inputImage.getWidth(), 
				inputImage.getHeight(), 1);
		// Processor to put the values
		FloatProcessor fp=new FloatProcessor(theStack.getWidth(), 
				theStack.getHeight());
		// Assign the processor to the one slice in the output stack
		theStack.setProcessor(fp, 1);

		
		// Run through all the pixels
		for(int x=0; x<inputImage.getWidth(); x++)
		{
			for(int y=0; y<inputImage.getHeight(); y++)
			{
					// Calculate and put the local phase value
					fp.putPixelValue(x, y, doEvaluationAtxy(inputImage, x, y, idx_ref, theMask));
					
					
					
				
			}
			// This can take a while, so show the progress bar
			if(showOutput)
			{
			IJ.showProgress(((double) x)/((double) inputImage.getWidth()));
			}
		}
		
		
		// Return the single-slice stack containing the phase image
		return theStack;
		
	}
	
	/**
	 * Calculate the phase at a fixed xy position by comparing the local z-profile to the reference z-profile
	 * @param inputImage The temporal peak image to be analyzed (the z-profile will be taken from this)
	 * @param x The x value where the phase should be determined
	 * @param y The y value where the phase should be determined
	 * @param idx_ref The indices (z-positions) where the peaks are in the reference section
	 * @param theMask Mask to only evaluate pixels positive in the mask (provide null if not needed)
	 * @return Local phase, in degrees.
	 */
	public static double doEvaluationAtxy(ImagePlus inputImage, int x, int y, 
			int[] idx_ref, ImagePlus theMask)
	{
		// If we fall outside the mask or if the pixel value at xy in the mask is zero, do not do
		// any analysis but return NaN
		if(theMask != null)
		{
			if(x>=theMask.getWidth() || y>=theMask.getHeight())
			{
				return Double.NaN;
			}
			if(theMask.getImageStack().getProcessor(1).getPixelValue(x, y)<=0)
			{
				return Double.NaN;
			}
		}
		
		// Otherwise, or if no mask is provided, calculate the local phase, convert to degreees, and return
		return getPhaseAtxy(inputImage,x, y, idx_ref)/Math.PI*180.0;
		
		
		
	}
	
	/**
	 * Calculate the phase at a fixed xy position by comparing the local z-profile to the reference z-profile
	 * @param inputImage The temporal peak image to be analyzed (the z-profile will be taken from this)
	 * @param x The x value where the phase should be determined
	 * @param y The y value where the phase should be determined
	 * @param idx_ref The indices (z-positions) where the peaks are in the reference section
	 * @return Local phase, in degrees.
	 */
	
	public static double doEvaluationAtxy(ImagePlus inputImage, int x, int y, int[] idx_ref)
	{
		return doEvaluationAtxy( inputImage,  x,  y, 
				 idx_ref, null);
	}
	
	/**
	 * Calculate the phase at a fixed xy position by comparing the local z-profile to the reference z-profile
	 * non-static method, uses image passed to plugin
	 * @param x The x value where the phase should be determined
	 * @param y The y value where the phase should be determined
	 * @param idx_ref The indices (z-positions) where the peaks are in the reference section
	 * @return Local phase, in degrees.
	 */
	
	public double doEvaluationAtxy(int x, int y, int[] idx_ref)
	{
		
		
		if(do_masking)
		{
			return doEvaluationAtxy(imp,x,y,idx_ref,mask);
		}
		
		return doEvaluationAtxy(imp,x,y,idx_ref);
		
		
	}
	
	
	public static double getPhaseAtxy(ImagePlus theImage, int x, int y, int[] idx_ref)
	{
		return LocalPhaseTools.getPhase(non_zero_indices(theImage,x,y),idx_ref);
		
		
	}
	
	public double getPhaseAtxy(int x, int y, int[] idx_ref)
	{
		return LocalPhaseTools.getPhase(non_zero_indices(x,y),idx_ref);
		
		
	}
	

	public static int[] non_zero_indices(ImagePlus theImage, int x, int y)
	{
		int [] theSection = getStackSection(theImage,x,y);
		
		return LocalPhaseTools.indices_to_positive_elements(theSection);
		

	}
	
	
	
	
	

	public int[] non_zero_indices(int x, int y)
	{
		int [] theSection = getStackSection(x,y);
		
		return LocalPhaseTools.indices_to_positive_elements(theSection);
		

	}

	// Checks whether at least 2 points are non-zero, otherwise no time can be defined
	public boolean checkSection(int x, int y)
	{
		int [] theSection = getStackSection( x,  y);
		int n_found = LocalPhaseTools.n_positive_elements(theSection);
		if(n_found >= 2)
		{
			return true;
		} else
		{
			return false;
		}

	}
	
	public static int[] getStackSection(ImagePlus theImage, int x, int y)
	{

		int[] vals=new int[theImage.getStackSize()];


		for(int z=1; z<=theImage.getStackSize(); z++)
		{
			ImageProcessor p=theImage.getStack().getProcessor(z);

			vals[z-1]=(int)p.getPixelValue(x, y);


		}

		return vals;

	}
	

	public int[] getStackSection(int x, int y)
	{

		return getStackSection(imp,x,y);

	}
	
	// For inheriting classes: hook to allow to add more things to the dialog
	public void extendDialog()
	{
		
	}


	public boolean doDialog()
	{

		// Open a dialog to get the use variables
		// As a particular feature of ImageJ, does not open in macro mode but
		// is substituted with macro parameter values instead
		gd = new GenericDialog("Local Phase Evaluation (LocalPhase)");


		// Add the fields


		gd.addNumericField("Reference pixel position x", reference_x, 0);

		gd.addNumericField("Reference pixel position y", reference_y, 0);
		
		Button bt = new Button("Get image center position for x and y");
		
		bt.addActionListener(this);
		
		gd.add(bt);
		
		gd.addCheckbox("Use mask (>0 means evaluate)", do_masking);
		
		String[] window_titles=WindowManager.getImageTitles();
		
		
		
		gd.addChoice("Mask", window_titles, lastMaskTitle);
		
		
		extendDialog();

		// We need to follow the dialog to update the class variables
		gd.addDialogListener(this);
		// Show the dialog



		gd.showDialog();                    // input by the user (or macro) happens here
		// Do not proceed when the use pushes cancel



		return (!gd.wasCanceled());

	}
	
	public void update_text_fields_reference_x_y()
	{
		locked = true;
		
		
		TextField t1 = (TextField) gd.getNumericFields().get(0);
		t1.setText(""+reference_x);
		
		TextField t2 = (TextField) gd.getNumericFields().get(1);
		t2.setText(""+reference_y);
		
		locked = false;
		
	}

	
	public void actionPerformed(ActionEvent e) {
		
		reference_x = (int)Math.round((double)imp.getWidth()/2.0);
		reference_y = (int)Math.round((double)imp.getHeight()/2.0);
		
		update_text_fields_reference_x_y();
		
	}





}
