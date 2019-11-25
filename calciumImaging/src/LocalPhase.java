import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import FindPeaks.accessory.classes.FindPeaksTools;
import FindPeaks.accessory.classes.LocalPhaseTools;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class LocalPhase implements PlugInFilter,DialogListener, ActionListener {

		
	public ImagePlus mask=null;
	
	public static String lastMaskTitle=null;
	
	protected static boolean do_masking=false;
	
	protected boolean locked = false;
	
	protected static GenericDialog gd;
	
	protected ImagePlus imp;

	// image processor at the time of starting the analysis
	protected ImageProcessor ip;

	public static boolean first=true;

	public static int reference_x=0;
	public static int reference_y=0;
	@Override
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

	@Override
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


		int [] idx_ref = non_zero_indices(reference_x, reference_y);

		IJ.showMessage("Reference section : "+idx_ref.length+" peaks detected");

		ImageStack theStack = new ImageStack(imp.getWidth(), 
				imp.getHeight(), 1);

		FloatProcessor fp=new FloatProcessor(theStack.getWidth(), 
				theStack.getHeight());
		
		theStack.setProcessor(fp, 1);

		
		
		for(int x=0; x<imp.getWidth(); x++)
		{
			for(int y=0; y<imp.getHeight(); y++)
			{
				
					fp.putPixelValue(x, y, doEvaluationAtxy( x, y, idx_ref));
					
					
				
			}
			
			IJ.showProgress(((double) x)/((double) imp.getWidth()));
		}
		
		

		ImagePlus output=new ImagePlus("Result Local Phase");
		
		output.setStack("Result local Phase - "+imp.getTitle(),theStack);
		
		
		
		output.show();
		
		
		//IJ.showMessage("Phase angle="+(getPhaseAtxy( x, y, idx_ref)/Math.PI*180.0));



	}
	
	public static FloatProcessor getPhaseImage(ImagePlus inputImage, int ref_x, int ref_y, ImagePlus theMask)
	{
	 return getPhaseImage(inputImage, ref_x, ref_y, theMask, false);	
	}
	
	public static FloatProcessor getPhaseImage(ImagePlus inputImage, int ref_x, int ref_y, boolean showOutput)
	{
		return getPhaseImage(inputImage, ref_x, ref_y, null, showOutput);
		
	}
	
	public static FloatProcessor getPhaseImage(ImagePlus inputImage, int ref_x, int ref_y)
	{
		return getPhaseImage(inputImage, ref_x, ref_y, null, false);
		
	}
	
	
	public static FloatProcessor getPhaseImage(ImagePlus inputImage, int ref_x, 
			int ref_y, ImagePlus theMask, boolean showOutput)
	{
		
		int [] idx_ref = non_zero_indices(inputImage, ref_x, ref_y);

		if(showOutput)
		{
			IJ.showMessage("Reference section : "+idx_ref.length+" peaks detected");
		}

		ImageStack theStack = new ImageStack(inputImage.getWidth(), 
				inputImage.getHeight(), 1);

		FloatProcessor fp=new FloatProcessor(theStack.getWidth(), 
				theStack.getHeight());
		
		theStack.setProcessor(fp, 1);

		
		
		for(int x=0; x<inputImage.getWidth(); x++)
		{
			for(int y=0; y<inputImage.getHeight(); y++)
			{
					
					fp.putPixelValue(x, y, doEvaluationAtxy(inputImage, x, y, idx_ref, theMask));
					
					
					
				
			}
			
			IJ.showProgress(((double) x)/((double) inputImage.getWidth()));
		}
		
		
		
		return fp;
		
	}
	
	public static double doEvaluationAtxy(ImagePlus inputImage, int x, int y, 
			int[] idx_ref, ImagePlus theMask)
	{
		
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
		
		return getPhaseAtxy(inputImage,x, y, idx_ref)/Math.PI*180.0;
		
		
		
	}
	
	public static double doEvaluationAtxy(ImagePlus inputImage, int x, int y, int[] idx_ref)
	{
		return doEvaluationAtxy( inputImage,  x,  y, 
				 idx_ref, null);
	}
	
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
