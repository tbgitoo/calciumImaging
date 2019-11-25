import java.awt.AWTEvent;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.ZProjector;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class LocalFrequency implements PlugInFilter,DialogListener  {
	
	

	
	protected static GenericDialog gd;
	
	protected ImagePlus imp;

	// image processor at the time of starting the analysis
	protected ImageProcessor ip;
	
	public static double frame_rate=24;

	/**
	 * @inheritDoc
	 *
	 * For this class, read frame rate
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		// Intermediate variable to accept numbers before filtering
		
		
		double n;

		// Get the first number from the dialog
		n = gd.getNextNumber();
		// Do basic checking, should be a valid number
		if (gd.invalidNumber())
			return false;
		// Must be within the image width

		

		if(n<0)
		{
			n=0;
		}
		
		frame_rate=n;
		

		// Get the first number from the dialog
		
		

		return true;

	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		

		// Store an internal reference to the assigned image
		this.imp = imp;

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

		
		ImageStack theStack = new ImageStack(imp.getWidth(), 
				imp.getHeight(), 1);

		FloatProcessor fp=new FloatProcessor(theStack.getWidth(), 
				theStack.getHeight());
		
		theStack.setProcessor(fp, 1);

		
		
		ZProjector zp = new ZProjector();
		
		zp.setImage(imp);
		zp.setMethod(ZProjector.SUM_METHOD);
		zp.setStartSlice(1);
		zp.setStopSlice(imp.getStackSize());
		zp.doProjection();
		
		ImagePlus output = zp.getProjection();
		
		FloatProcessor flp = (FloatProcessor) output.getProcessor();
		
		for(int x=0; x<flp.getWidth(); x++)
		{
			for(int y=0; y<flp.getHeight(); y++)
			{
				// We suppose here that the peak image was a 255 vs. 0 image, so that the sum is 255*n
				// Then, calculate beat rate from the relative proportion of active pixels, 
				// and the known framerate
				// Finally, convert to beats per minute
				double newVal = (double)flp.getPixelValue(x, y)/((double)imp.getStackSize())*frame_rate/255.0*60.0;
				flp.putPixelValue(x, y, newVal);
			}
		}
		
		output.setTitle("Frequency mean - "+imp.getTitle());
		
		output.show();
		
		
		


	}
	

	
	
	
	
	


	


	



	public boolean doDialog()
	{

		// Open a dialog to get the use variables
		// As a particular feature of ImageJ, does not open in macro mode but
		// is substituted with macro parameter values instead
		gd = new GenericDialog("Local Phase Evaluation (LocalPhase)");


		// Add the fields


		gd.addNumericField("Framerate [per second]", frame_rate, 1);

		
		

		// We need to follow the dialog to update the class variables
		gd.addDialogListener(this);
		// Show the dialog



		gd.showDialog();                    // input by the user (or macro) happens here
		// Do not proceed when the use pushes cancel



		return (!gd.wasCanceled());

	}
	
	

	
	
	
	

	
	
	

}
