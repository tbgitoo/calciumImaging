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

## The FindPeaksTools class is in part a Java re-implementation
## of the Octave findPeaks method, by Juan Pablo Carbajal, 
## available at https://bitbucket.org/mtmiller/octave-signal
## (direct link: https://searchcode.com/codesearch/view/64213481/)
## The Octave source code is under a General Public License

## For array ordering, as need for the findPeaks algorithm, 
## I also invoke functionality form the ArrayIndexComparator class
## The ArrayIndexComparator class and also its invocation here is based
## on an answer to stackoverflow question
## https://stackoverflow.com/questions/4859261/get-the-indices-of-an-array-after-sorting
## The question was asked about string ordering by 
## Eng. Fouad, https://stackoverflow.com/users/597657/eng-fouad and the reply was given
## The answer used here was given by Jon Skeet, https://stackoverflow.com/users/22656/jon-skeet
## Here, I adapted the code a bit to the case of double numbers in an array.
## Feel free your-self to use and modify this code, but please beware that the stackoverflow terms of service
## require you to: 1) Mention the question and especially its link 2) both the user having asked the 
## question and the one having answered, by referring to their profile page (as above)
## and share any derived work under a Creative Commons ShareAlike license or compatible such as 
## GPL v3 as done here (i.e. see:
## https://creativecommons.org/share-your-work/licensing-considerations/compatible-licenses) 
## 

*/

package FindPeaks.accessory.classes;

import java.util.Arrays;
import java.util.Vector;

import ij.ImageStack;
import ij.process.ByteProcessor;


/**
 * Collection of static methods to enable peak identification in a one-dimensional array of values
 * @author thomasbraschler
 *
 */
public class FindPeaksTools {
	
	/** 
	 * 
	 * @param vals
	 * @param threshold
	 * @param minD
	 * @param doFiltering
	 * @param minW
	 * @param maxW
	 * @param minH
	 * @return
	 */
	public static int[] findPeaks(double[] vals, double threshold, double minD, boolean doFiltering, 
			double minW, double maxW, double minH)
	{

		int[] idx= identifyPeaksAtMinimalDistance(
				vals, threshold,  minD);
		
		if(doFiltering)
		{
			idx=filterPeaks(idx, vals, minD, minW, maxW, minH,0);
		}

		return idx;



	}


	// This function returns indexes such that they point to increasing elements
	public static int[] getIndexesOfSortedArray(double[] vals)
	{
		
		
		
		ArrayIndexComparator comparator = new ArrayIndexComparator(vals);
		Integer[] indexes = comparator.createIndexArray();
		
			
				
		
		int[] return_val = new int[indexes.length];
		for(int ind=0; ind<return_val.length; ind++)
		{
			return_val[ind]=indexes[ind];
		}
		
		

		return return_val;

	}

	// This function finds the indexes of the values at or above the threshold
	public static int[] getIndexesOfValuesExceedingThreshold(double[] vals, double threshold)
	{
		int[] idx = new int[vals.length];
		int index=0;

		for(int index_vals=0; index_vals<vals.length; index_vals++)
		{
			if(vals[index_vals]>=threshold)
			{
				idx[index]=index_vals;
				index++;
			}
		}

		int[] idx_final = new int[index];
		for(int newIndex=0; newIndex < index; newIndex++)
		{
			idx_final[newIndex]=idx[newIndex];
		}

		return idx_final;

	}

	// This function identifies peaks above a threshold, and with a minimal distance between 
	// them. The function is a bit rought and therefore needs some cleanup afterwards, but it 
	// gives suitable primary candidates
	public static int[] identifyPeaksAtMinimalDistance(double[] vals, double threshold, double minD)
	{


		int[] idx = getIndexesOfValuesExceedingThreshold(vals,threshold);



		// Along the idx values, get the remaining associated values in the vals array
		double[] remaining_vals = new double[idx.length];

		for(int ind=0; ind<remaining_vals.length; ind++)
		{
			remaining_vals[ind]=vals[idx[ind]];
		}

		

		// This is the order that we have in the remaining values, sorted increasingly

		int[] order = getIndexesOfSortedArray(remaining_vals);
		
		
		

		// We need to apply this order, but in reverse, to the idx values
		int[] idx_s=new int[idx.length];

		for(int ind=0; ind<idx.length; ind++)
		{
			idx_s[ind]=idx[order[idx.length-1-ind]];
		}
		
		
		
		
		
		



		// The nodes2visit hold the indexes to the elements in idx_s we still need to look at
		Vector<Integer> node2visit = new Vector <Integer>(idx_s.length);
		// The visited variable holds the indexes of the elements in idx_s which we already considered
		Vector<Integer> visited = new Vector <Integer>(idx_s.length);

		// Remaining idx elements which are standalone peaks
		Vector<Integer> idx_pruned = new Vector <Integer>(idx_s.length);

		// Initialize: all nodes are still to visit, and all corresponding indexes in idx_s are still in
		for(int ind=0; ind<idx_s.length; ind++)
		{
			node2visit.add(ind);
			idx_pruned.add(idx_s[ind]);
		}





		while (node2visit.size()>0)
		{
			// Set the current node as visited
			int current_node = node2visit.get(0);
			node2visit.remove(0);
			visited.add(current_node);

			// Get the new neighbors of this node. These are the points that were not
			// yet visited and that are closer than minD to the current node

			Vector<Integer> neighs = new Vector <Integer>(idx_s.length);

			for(int ind=0; ind<idx_s.length; ind++)
			{
				// We are looking at some other node that is close
				if(ind != current_node && Math.abs(idx_s[ind]-idx_s[current_node])<minD)
				{

					if(!FindPeaksTools.findInVector(ind, visited)) // Not visited yet, add to the neighbors
					{
						neighs.add(ind);
					}
				}
			}

			if(neighs.size()>0)
			{
				Vector<Integer> idx_neighs=new Vector<Integer>(neighs.size());

				for(int ind=0; ind<neighs.size(); ind++)
				{
					idx_neighs.add(idx_s[neighs.get(ind)]);
					visited.add(neighs.get(ind).intValue());
				}

				idx_pruned=FindPeaksTools.setDiff(idx_pruned,idx_neighs);
				node2visit=FindPeaksTools.setDiff(node2visit, visited);
			}


		}










		// So now we have all the indexes of the values that are larger than the threshold, in decreasing order
		idx=new int[idx_pruned.size()];

		for(int ind=0; ind<idx_pruned.size(); ind++)
		{
			idx[ind]=idx_pruned.get(ind);
		}






		return idx;


	}

	//Estimate widths of peaks and filter for:
	//width smaller than given.
	//wrong concavity.
	//not high enough
	//data at peak is lower than parabola by 1%
	//position of extrema minus center is bigger equal than minD/2
	// This function takes as a first input the argument (idx) the pre-filtered peak candidates,
	// which are typicaly obtained from "identifyPeaksAtMinimalDistance"
	// It also needs the underlying values from the complete dataset (vals) and the minimal
	// distance required between the peaks (minD)
	// further, it checks whether the fitted peak width fals between minW and maxW
	// and whether the height is more than minH

	public static int[] filterPeaks(int[] idx, double[] vals, double minD, 
			double minW, double maxW, double minH, double fitMin)
	{

		// Remaining idx elements which are standalone peaks
		// To start with, identical to idx but Vector class so we can modify it
		Vector<Integer> idx_pruned = new Vector <Integer>(idx.length);


		for(int ind=0; ind<idx.length; ind++)
		{
			// get the local data around the peak. Check whether peak candidate is a local maximum
			int lower_bound = (int)Math.max(Math.floor(-minD/2+idx[ind]), 0);
			int upper_bound = (int)Math.min(Math.ceil(minD/2+idx[ind]), vals.length-1);



			while(lower_bound < idx[ind]-1 && vals[lower_bound]<fitMin)
			{
				lower_bound++;
			}

			while(upper_bound > idx[ind]+1 && vals[upper_bound]<fitMin)
			{
				upper_bound--;
			}


			double[] localVals = new double[upper_bound-lower_bound+1];



			boolean isMaximum=true;

			for(int local_ind=0; local_ind<localVals.length; local_ind++)
			{
				localVals[local_ind]=vals[lower_bound+local_ind];
				if(localVals[local_ind]>vals[idx[ind]])
				{
					isMaximum=false;
				}
			}



			double [] pp; // for fitting a parabola
			double xm; // local X-value at maximum
			double H; // height of peak
			// The current point is not the maximum, so use polynomial fitting
			if(!isMaximum)
			{
				pp=fitParabola(localVals);
				xm=-pp[1]/2/pp[2];
				H=pp[0]+pp[1]*xm+pp[2]*xm*xm;


			} else // Use fixed extremum fittin instead
			{
				xm=idx[ind]-lower_bound;
				pp=fitParabolaFixedExtremum(localVals, (int)Math.round(xm));
				H=pp[0]+pp[1]*xm+pp[2]*xm*xm;

			}



			// Based on the fitting, keep or reject peek

			boolean keep=true;

			// No quadratic or concave peak => remove
			if(pp[2]>=0)
			{
				keep=false;

			}



			if(keep)
			{

				if(H<minH)
				{
					keep = false;

				} else
				{
					double width=Math.sqrt(-(H-minH)/pp[2]);

					if(width<minW || width>maxW)
					{

						keep=false;
					}
				}

			}




			if(keep)
			{

				if(Math.abs(idx[ind]-xm-lower_bound)>minD/2)
				{
					keep=false;
				}


			}

			if(!keep)
			{


			}

			if(keep)
			{
				idx_pruned.add(idx[ind]);
			}





		}

		int [] new_idx = new int[idx_pruned.size()];

		for(int ind=0; ind<idx_pruned.size(); ind++)
		{
			new_idx[ind]=idx_pruned.get(ind).intValue();
		}

		return new_idx;

	}
	// Fits a parabola (a + bx + cx^2) to series of values (vals). The 
	// values are regularly spaced, and the x coordinate is assumed to be
	// 0,1,2,3...
	// that is as in standard array indexing in Java

	public static double[] fitParabola(double [] vals)
	{
		// The constants a,b,c
		double [] params = new double[3];
		// No values provided, return a=b=c=0
		if(vals.length==0)
		{
			params[0]=0;
			params[1]=0;
			params[2]=0;
			return params;
		}
		// only 1 value provided, store this in constant
		// also, negative value in the quadratic term to make peak at x=0
		if(vals.length==1)
		{
			params[0]=vals[0];
			params[1]=0;
			params[2]=-1;
			return params;
		}
		// two values provided, place apex of parabola on 1
		if(vals.length==2)
		{
			// Now we have:
			// v0=a
			// v1=a+b+c
			// dv/dx(1)=2c+b=0
			// and so b=-2c
			// and v1=a-2c+c=a-c => c=a-v1=v0-v1

			// a=v0
			// b=2*v1-2*v0
			// c=v0-v1

			// Check:
			// v0=a+0*b+0*c
			// v1=a+b+c=v0+2*v1-2*v0+v0-v1=v1
			// dv/dx(1)=2*(v0-v1)+2*v1-2*v0=0

			params[0]=vals[0];
			params[1]=2*vals[1]-2*vals[0];
			params[2]=vals[0]-vals[1];

			return params;
		}

		// General case, we have at least three values



		double xbar=((double)vals.length-1)/2.0; // Average x value for the regularly spaced 
		// x values

		double x_regression[] = new double[vals.length]; // Regressor for the linear regression

		for(int ind=0; ind<x_regression.length; ind++)
		{
			x_regression[ind]=(double)ind-xbar;
		}


		// Normalize to length 1
		double n_x_regression = vector_norm(x_regression);
		for(int ind=0; ind<x_regression.length; ind++)
		{
			x_regression[ind]=x_regression[ind]/n_x_regression;
		}

		double b=scalar_product(vals,x_regression)/n_x_regression;

		double x2_regression[] = new double[vals.length]; // Regressor for the linear regression

		for(int ind=0; ind<x_regression.length; ind++)
		{
			x2_regression[ind]=((double)ind-xbar)*((double)ind-xbar);
		}

		// Subtract the average value

		double x2_regression_mean = mean(x2_regression);
		for(int ind=0; ind<x_regression.length; ind++)
		{
			x2_regression[ind]=x2_regression[ind]-x2_regression_mean;
		}

		// Normalize to length 1
		double n_x2_regression = vector_norm(x2_regression);
		for(int ind=0; ind<x_regression.length; ind++)
		{
			x2_regression[ind]=x2_regression[ind]/n_x2_regression;
		}




		double c=scalar_product(vals,x2_regression)/n_x2_regression;


		// Since we have b and c now, we can calculate the offset from the mean values

		double [] vals_reduced = new double[vals.length];

		for(int ind=0; ind<vals.length; ind++)
		{
			vals_reduced[ind] = vals[ind]-b*((double)ind-xbar)-c*((double)ind-xbar)*((double)ind-xbar);
		}

		double a = mean(vals_reduced);



		// Now we have:

		// y = a + b*(x-xbar)+c*((x-xbar)^2)
		// y = a + b*x -b*xbar+c*x^2-2*c*x*xbar+c*xbar^2

		// so the zero-order coefficient in terms of the unshifted x is:
		// p0 = a -b*xbar+c*(xbar^2)
		params[0]=a-b*xbar+c*xbar*xbar;

		// and the first order coefficient
		// p1= b-2*c*xbar
		params[1]=b-2*c*xbar;

		// and the second order
		// p2 = c
		params[2]=c;



		return params;

	}

	// Fits a parabola (a + bx + cx^2) to series of values (vals).
	// xm designates an element through at which the extremum has to occur

	public static double[] fitParabolaFixedExtremum(double [] vals, int xm)
	{
		if(xm<0)
		{
			xm=0;
		}
		if(xm>=vals.length)
		{
			xm=vals.length-1;
		}
		// The constants a,b,c
		double [] params = new double[3];
		// No values provided, return a=b=c=0
		if(vals.length==0)
		{
			params[0]=0;
			params[1]=0;
			params[2]=0;
			return params;
		}
		// only 1 value provided, store this in constant. Also, make quadratic term negative to 
		// have peak at x=0
		if(vals.length==1)
		{
			params[0]=vals[0];
			params[1]=0;
			params[2]=-1;
			return params;
		}
		// two values provided, place apex of parabola on 1
		if(vals.length==2)
		{
			// The apex is the first element, step is 1
			if(xm==0)
			{
				params[0]=vals[0];
				params[1]=0;
				params[2]=vals[1]-vals[0];
			} else
			{
				// This is
				// y=v1 + (x-1)^2*(v0-v1)
				// y=v1 + x^2*(v0-v1) -2*x*(v0-v1) + (v0-v1)
				// y=v0 -2*x(v0-v1)+x^2(v0-v1)

				params[0]=vals[0];
				params[1]=2*(vals[1]-vals[0]);
				params[2]=-vals[1]-vals[0];
			}


			return params;
		}

		// General case, we have at least three values

		double H     = vals[xm];


		double [] x_rel = new double[vals.length];
		double []relative_height = new double[vals.length];

		for(int x_ind=0; x_ind<vals.length; x_ind++)
		{
			x_rel[x_ind]=(double)x_ind-xm;
			relative_height[x_ind]=vals[x_ind]-H;

		}



		// Here the idea is basically to find the best coefficients such that

		// relative_height =  x_rel*b + x_rel^2*c

		// For this, we need to optimize the sum of squares

		// sum (relative_height-x_rel*b-x_rel^2*c)^2

		// Partial derivative with regard to b

		// sum (relative_height-x_rel*b-x_rel^2*c)*x_rel=0
		// and to c
		// sum (relative_height-x_rel*b-x_rel^2*c)*x_rel^2=0

		// b and c are constants that can written in front of the sums

		// sum relative_height*x_rel-b*sum x_rel^2-c*x_rel^3=0
		// and
		// sum relative_height*x_rel^2-b*sum x_rel^3-c*x_rel^4=0

		// We need some definitions because this is getting lengthy
		// A=sum(relative_height*x_rel)
		// B=sum(x_rel^2)
		// C=sum(x_rel^3)
		// D=sum(relative_height*x_rel^2)
		// E=sum(x_rel^4)

		// So we have:
		// A-b*B-c*C=0
		// D-b*C-c*E=0

		// and so
		// A*C-b*B*C-c*C*C=0
		// -D*B+b*B*C+c*E*B=0
		// A*C-D*B+c*(E*B-C*C)=0


		// That makes for c
		// c=(D*B-A*C)/(E*B-C*C)

		// and for B
		// A-b*B-c*C =>
		// b=(c*C-A)/B

		double [] x_rel2 = element_wise_multiplication(x_rel,x_rel);
		double [] x_rel3 = element_wise_multiplication(x_rel2,x_rel);
		double [] x_rel4 = element_wise_multiplication(x_rel3,x_rel);

		double A = scalar_product(relative_height,x_rel);
		double B = sum(x_rel2);
		double C = sum(x_rel3);
		double D = scalar_product(relative_height,x_rel2);
		double E = sum(x_rel4);

		double c=(D*B-A*C)/(E*B-C*C);
		double b=(A-c*C)/B;


		double a=H;

		// This makes that we have
		// y = a + b*(x-xm)+c*(x-xm)^2
		// y = a-b*xm+c*xm^2 + b*x-x*2*c*xm+c*x^2 

		params[0]=a-b*xm+c*xm*xm;
		params[1]=b-2*c*xm;
		params[2]=c;

		return params;




	}




	// Scalar product of two vectors. If not of the same length, the shorter is length is considered only

	public static double scalar_product(double [] x, double [] y)
	{
		int l = Math.min(x.length, y.length);
		double sum=0;
		for(int ind=0; ind<l; ind++)
		{
			sum=sum+x[ind]*y[ind];
		}
		return sum;
	}

	public static double[] element_wise_multiplication(double [] x, double [] y)
	{
		int l = Math.min(x.length, y.length);
		double[] res=new double[l];
		for(int ind=0; ind<l; ind++)
		{
			res[ind]=x[ind]*y[ind];
		}
		return res;
	}

	// Pythagorean length of the vector: sqrt(x1^2+x2^2+..)
	public static double vector_norm(double [] x)
	{

		return Math.sqrt(scalar_product(x,x));
	}

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

	public static double [] getHistogram(int [] vals)
	{


		// Initialize
		long[] hist = new long[256];
		double[] Dhist = new double[256];

		for(int ind=0; ind<hist.length; ind++)
		{
			hist[ind]=0;
			Dhist[ind]=0;
		}

		double total=0;




		for(int ind=0; ind<vals.length; ind++)
		{
			hist[vals[ind]]++;
			total++;

		}

		// Normalize


		for(int ind=0; ind<hist.length; ind++)
		{
			Dhist[ind]=((double)hist[ind])/total;
		}


		return Dhist;




	}
	
	public static double getQuantileFromValues(double [] val, double p)
	{
		double [] clonedVal = new double[val.length];
		
		for(int ind=0; ind>clonedVal.length; ind++)
		{
			clonedVal[ind]=val[ind];
		}
		
		Arrays.sort(clonedVal);
		
		double n_intervals = clonedVal.length+1; 
		
		double index_precise = p*n_intervals-1; 
		
		double index_round = Math.floor(index_precise);
		
		if(index_precise<0) { return clonedVal[0]; }
		
		if(index_precise>=clonedVal.length-1)
		{
			return clonedVal[clonedVal.length-1];
		}
		
		// Index precise is between 0 and val.length-1 (excluded), so index round will be from 0 to val.length-2
		
		return (clonedVal[(int)index_round]+(clonedVal[(int)index_round+1]-clonedVal[(int)index_round])*(index_precise-index_round));
		
		
		
		
		
	}
	
	
	public static int[][] getEnvironment(ByteProcessor ip, int x, int y, double r)
	{

		int x_upper = (int)Math.ceil(x+r);
		if(x_upper > ip.getWidth()-1)
		{
			x_upper = ip.getWidth()-1;
		}
		
		int y_upper = (int)Math.ceil(y+r);
		if(y_upper > ip.getHeight()-1)
		{
			y_upper = ip.getHeight()-1;
		}
		
		int x_lower = (int)Math.floor(x-r);
		if(x_lower <0 )
		{
			x_lower = 0;
		}
		
		int y_lower = (int)Math.floor(y-r);
		if(y_lower <0)
		{
			y_lower = 0;
		}
		
		
		int width=x_upper-x_lower+1;
		int height=y_upper-y_lower+1;
		
		int [][] ret = new int[width][height];
		
		for(int x_ind=x_lower; x_ind<=x_upper; x_ind++ )
		{
			for(int y_ind=y_lower; y_ind<=y_upper; y_ind++)
			{
				
				ret[x_ind-x_lower][y_ind-y_lower]=
						ip.getPixel(x_ind, y_ind);
			}
		}
		
		
		
		
		return ret;

	}
	
	// Calculates the quantile from a histogram

	public static double getQuantile(double [] hist, double p)
	{

		// Normalize (in case)
		// Also, calculate cumulative sums, with 1 element more than
		// the histogram to have 0 and 1 in it
		double total = 0;
		double[] cumsum = new double[hist.length+1];
		for(int ind=0; ind<hist.length; ind++)
		{
			total=total+hist[ind];
			cumsum[ind]=0;
		}

		double [] hist_normalized=new double [hist.length];




		for(int ind=0; ind<hist_normalized.length; ind++)
		{
			hist_normalized[ind]=hist[ind]/total;
			cumsum[ind+1]=cumsum[ind]+hist_normalized[ind];
		}
		// To be sure it's really exactly 1 and some close value due to
		// rounding errors
		cumsum[hist_normalized.length]=1;

		// limiting cases and non-treatable values
		if(p<=0) { return -1; }
		if(p>=1) { return hist_normalized.length; }

		// nominal case





		int current_ind=0;
		while(cumsum[current_ind]<p && current_ind<=hist_normalized.length)
		{

			current_ind++;
		}

		// Now we dispose of the element where the cumulative sum is just bigger than the
		// desired p value

		// Do interpolation to get a finer estimate

		double p_upper = cumsum[current_ind];
		double p_lower = cumsum[current_ind-1];

		double q_upper = current_ind;
		double q_lower = current_ind-1;

		// Degenerate case where the is no entry into the hist_normalizedogram here
		if(p_upper==p_lower)
		{
			int index_to_lower = current_ind-1;
			while(index_to_lower>0 && p_lower==p_upper)
			{
				index_to_lower--;
				p_lower = cumsum[index_to_lower];
				q_lower = index_to_lower;
			}

		}

		// This still hasn't helped, return the mean of the associated quantiles
		if(p_lower==p_upper)
		{
			return (q_lower+q_upper)/2;
		}

		double linear_inter_q = q_lower + (q_upper-q_lower)/(p_upper-p_lower)*(p-p_lower);

		return(linear_inter_q);




	}

	public static boolean findInVector(int needle, Vector <Integer> haystack)
	{
		boolean found=false;
		for(int ind=0; ind<haystack.size(); ind++)
		{
			if(haystack.get(ind).intValue()==needle)
			{
				found=true;
			}
		}
		return found;
	}

	// Returns all the elements that are in a but could not be found in b
	public static Vector <Integer> setDiff(Vector <Integer> a, Vector <Integer> b)
	{
		Vector <Integer> ret = new Vector <Integer>(a.size());

		for(int ind=0; ind<a.size(); ind++)
		{
			if(!findInVector(a.get(ind).intValue(),b))
			{
				ret.add(a.get(ind).intValue());
			}
		}

		return(ret);

	}

	public static double  sum(double [] x)
	{
		double s = 0;
		for(int ind=0; ind<x.length; ind++)
		{
			s = s + x[ind];
		}
		if(x.length>0)
		{
			return (s);
		}
		return 0;

	}

	public static int  sum(int [] x)
	{
		int s = 0;
		for(int ind=0; ind<x.length; ind++)
		{
			s = s + x[ind];
		}
		if(x.length>0)
		{
			return (s);
		}
		return 0;

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

}
