package FindPeaks.accessory.classes;

import java.awt.Polygon;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.Thresholder;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.StackProcessor;

// This class provides generic (static) methods supporting the actual LocalPhase plugin
public class LocalPhaseTools {

	
	public static void applyThreshold(ByteProcessor bp, double minThreshold, double maxThreshold)
	{
		int[] lut = new int[256];
		for (int i=0; i<256; i++) {
			if (i>=minThreshold && i<=maxThreshold)
				lut[i] = 255;
			else {
				lut[i] = 0;
			}
		}
		bp.applyTable(lut);
		
		
		
		
	}
	
	
	public double[] getZAxisProfileRoi(Roi roi, ImagePlus imp) {
		ImageStack stack = imp.getStack();
		int size = stack.getSize();
		double[] values = new double[size];
		Calibration cal = imp.getCalibration();
		boolean isLine = roi!=null && roi.isLine();
		
		for (int i=1; i<=size; i++) {
				ImageProcessor ip = stack.getProcessor(i);
			ip.setRoi(roi);
			ImageStatistics stats = null;
			if (isLine)
				stats = getLineStatistics(roi, ip, Measurements.MEAN, cal);
			else
				stats = ImageStatistics.getStatistics(ip, Measurements.MEAN, cal);
			
			values[i-1] = (double)stats.mean;
		}
		
		return values;
	}
	
	public static ImageStatistics getLineStatistics(Roi roi, ImageProcessor ip, int measurements, Calibration cal) {
		ImagePlus imp = new ImagePlus("", ip);
		imp.setRoi(roi);
		ProfilePlot profile = new ProfilePlot(imp);
		double[] values = profile.getProfile();
		ImageProcessor ip2 = new FloatProcessor(values.length, 1, values);
		return ImageStatistics.getStatistics(ip2, measurements, cal);
	}
	
	
	
	
	// Helper function to get a new stack of the same dimensions as 
		// given stack for creating masks
		public static ImageStack getEmptyByteStack(ImageStack source)
		{
			ImageStack theStack = new ImageStack(source.getWidth(), 
					source.getHeight(), source.getSize());



			for(int ind=1; ind<=theStack.getSize(); ind++)
			{
				theStack.setProcessor(
						new ByteProcessor(theStack.getWidth(), 
								theStack.getHeight()), ind);
			}

			return(theStack);


		}
		
		public static ImageStack getEmptyByteStack(int width, int height, int size)
		{
			ImageStack theStack = new ImageStack(width, 
					height, size);



			for(int ind=1; ind<=theStack.getSize(); ind++)
			{
				theStack.setProcessor(
						new ByteProcessor(theStack.getWidth(), 
								theStack.getHeight()), ind);
			}

			return(theStack);


		}
		
		public static ImageStack getEmptyFloatStack(int width, int height, int size)
		{
			ImageStack theStack = new ImageStack(width, 
					height, size);



			for(int ind=1; ind<=theStack.getSize(); ind++)
			{
				theStack.setProcessor(
						new FloatProcessor(theStack.getWidth(), 
								theStack.getHeight()), ind);
			}

			return(theStack);


		}
	
	public static ImageStack getClonedByteStack(ImageStack source)
	{
		ImageStack theStack=getEmptyByteStack(source);
		for(int z=1; z<=theStack.getSize(); z++)
		{
			theStack.getProcessor(z).copyBits(source.getProcessor(z).convertToByte(false), 0, 0, Blitter.COPY);
		}
		return theStack;
	}

	
	// Gets the element with the highest numeric value in idx that is below (<) the target value
	// el
	// This function supposes that all the idx values are zero or positive, and so returns
	// -1 if no element below el can be found in the array idx

	public static int getMaxElementBelow(int [] idx, int el)
	{
		boolean lower_element_found = false;

		int max_lower_element = 0;

		for(int ind=0; ind<idx.length; ind++)
		{
			if(idx[ind]<el)
			{
				if(!lower_element_found)
				{
					lower_element_found=true;
					max_lower_element=idx[ind];
				} else
				{
					if(idx[ind]>max_lower_element)
					{
						max_lower_element=idx[ind];
					}
				}
			}
		}

		if(lower_element_found)
		{
			return max_lower_element;
		}
		return -1;


	}

	// Gets the element with the lowest numeric value in idx that is above 
	//( defined as >=) the target value el
	// This function supposes that all the idx values are zero or positive, and so returns
	// -1 if no element above el can be found in the array idx


	public static int getMinElementAbove(int [] idx, int el)
	{

		boolean upper_element_found = false;

		int min_upper_element = 0;

		for(int ind=0; ind<idx.length; ind++)
		{
			if(idx[ind]>=el)
			{
				if(!upper_element_found)
				{
					upper_element_found=true;
					min_upper_element=idx[ind];
				} else
				{
					if(idx[ind]<min_upper_element)
					{
						min_upper_element=idx[ind];
					}
				}
			}
		}

		if(upper_element_found)
		{
			return min_upper_element;
		}
		return -1;



	}


	// Gets the period at the desired value (desired_position) 
	// The period is the difference in the value of the element just below and just above
	// the target desired_position (determined via getMaxElementBelow and 
	// getMinElementAbove)
	// If the desired_position is outside the domain covered by idx, then
	// the period is the difference between the idx values at the closest end covered

	public static int getPeriod(int [] idx, int desired_position)
	{
		// if we have zero or 1 point only, we can't get any period
		if(idx.length<=1)
		{
			return 0;
		}

		boolean upper_element_found = true;
		int min_upper_element = getMinElementAbove(idx, desired_position);



		if(min_upper_element<0)
		{
			upper_element_found = false;
		}

		boolean lower_element_found = true;
		int max_lower_element = getMaxElementBelow(idx, desired_position);

		if(max_lower_element<0)
		{
			lower_element_found = false;
		}



		if(lower_element_found && upper_element_found)
		{
			return min_upper_element-max_lower_element;
		}
		// only a lower element found but not an upper, meaning that we are above
		// the domain covered by idx. Get the interval from the lower_element
		// an element lower still
		if(lower_element_found)
		{
			return max_lower_element - getMaxElementBelow(idx, max_lower_element);
		}


		// We are below the elements in idx
		return getMinElementAbove(idx,min_upper_element+1)-min_upper_element;

	}

	// Get the element in the array idx that is closest to the target element el
	public static int getNearestElement(int [] idx, int el)
	{
		if(idx.length<1)
		{
			return -1;
		}

		boolean started=false;

		int current_delta=0;

		int element_identified=-1;

		for(int ind=0; ind<idx.length; ind++)
		{
			if(!started)
			{
				started=true;
				current_delta = Math.abs(el-idx[ind]);
				element_identified=idx[ind];
			} else
			{
				int new_delta = Math.abs(el-idx[ind]);
				if(new_delta<current_delta)
				{
					element_identified=idx[ind];
					current_delta=new_delta;
				}

			}
		}
		return element_identified;

	}

	// Analyzes timing correlation between the peak location vector idx and a reference
	// peak location vector idx_ref
	// returns a vector of 3 elements: The in-phase component, the out-of-phase component, 
	// and the number of peaks analyzed in idx

	public static double[] getCorrelationInformation(int [] idx, int [] idx_ref)
	{
		double n=0;

		double cos_phase=0;

		double sin_phase=0;

		for(int ind=0; ind<idx.length; ind++)
		{
			int nearest = getNearestElement(idx_ref, idx[ind]);
			int period = getPeriod(idx_ref, idx[ind]);

			double phase = 2.0*Math.PI*((double)(nearest-idx[ind]))/((double) period);

			cos_phase+=Math.cos(phase);

			sin_phase+=Math.sin(phase);

			n=n+1;
		}

		double [] ret = new double [3];

		ret[0]=cos_phase;
		ret[1]=sin_phase;
		ret[2]=n;

		return ret;




	}

	public static double getCorrelationStrength(int [] idx, int [] idx_ref )
	{
		double [] info = getCorrelationInformation(idx,  idx_ref);

		double n=info[2];

		if(n==0)
		{
			return 0;
		}

		double cos_phase=info[0];

		double sin_phase=info[1];

		double vector_length = Math.sqrt(cos_phase*cos_phase+sin_phase*sin_phase);

		return vector_length/n;

	}

	public static double getPhase(int [] idx, int [] idx_ref )
	{

		double [] info = getCorrelationInformation(idx,  idx_ref);

		double n=info[2];

		double cos_phase=info[0];

		double sin_phase=info[1];

		cos_phase=cos_phase/n;
		sin_phase=sin_phase/n;

		return Math.atan2(sin_phase, cos_phase);


	}

	// Compiles the indices to elements that have positive values

	public static int[] indices_to_positive_elements(int [] theSection)
	{

		Vector <Integer> idx_v = new Vector <Integer> (theSection.length);

		for(int ind=0; ind<theSection.length; ind++)
		{
			if(theSection[ind]>0)
			{
				idx_v.add(ind);
			}
		}

		int [] idx = new int[idx_v.size()];
		for(int ind=0; ind<idx.length; ind++)
		{
			idx[ind]=idx_v.get(ind);
		}

		return (idx);


	}


	// Counts the number of positive elements in the array theSection
	public static int n_positive_elements(int [] theSection)
	{

		int n_found=0;
		for(int ind=0; ind<theSection.length; ind++)
		{
			if(theSection[ind]>0)
			{
				n_found=n_found+1;
			}
		}
		return n_found;

	}

	public static double getMeanPeriod(int[] idx) {
		// TODO Auto-generated method stub
		Arrays.sort(idx);

		double sum_T=0;
		double n=0;

		for(int ind=0; ind<idx.length-1; ind++)
		{
			sum_T = sum_T + (idx[ind+1]-idx[ind]);
			n++;
		}

		if(n==0)
		{
			return 0;
		}
		return sum_T/n;
	}
	
	


	// Helper function: Calculates the arithmetic mean of the values in the list
	public static double  mean(double [] x)
	{
		double s = 0;
		for(int ind=0; ind<x.length; ind++)
		{
			s = s + x[ind];
		}
		if(x.length>0)
		{
			return (s/(double)x.length);
		}
		return 0;

	}

	





}
