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
import tbgitoo.tools.ArraySortTools;
import tbgitoo.tools.FitParabola;
import tbgitoo.tools.VectorTools;


/**
 * Collection of static methods supporting the peak analysis of image stacks
 * A first group of functions enables peak detection in a one-dimensional array of values
 * There are also some very basic implementation of vecotr functions (mean, scalar product, sum and so on) used
 * repeatedly in the context of the peak analysis, and finally also image handling functions
 * @author thomasbraschler
 *
 */
public class FindPeaksTools {
	
	/** 
	 * Main function to identify peaks an array of double values. Parameters as defined for the
	 * corresponding findPeaks function in Octave (https://searchcode.com/codesearch/view/64213481/)
	 * However, currently, double-sided peak evaluation is not implemented, if you wish to so, follow
	 * the instructions for the Octave function (i.e. subtract the mean and take the absolute value before
	 * passing the array to this function, https://searchcode.com/codesearch/view/64213481/)
	 * @param vals Array of values in which we search for peaks
	 * @param threshold Threshold above which the values need to lie to be considered peak candidates
	 * @param minD Minimal distance between peaks (unit spacing between individual values is assumed) 
	 * @param doFiltering Should we do filtering for width and height or just accept the peaks found in the primary search?
	 * @param minW Minimal width (if filtering)
	 * @param maxW Maximal width (if filtering)
	 * @param minH Minimal height (above threshold for fitted parabola, effective only when filtering)
	 * @return Ordered array of indices of the peaks
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


	

	

	// This function identifies peaks above a threshold, and with a minimal distance between 
	// them. The function is a bit rought and therefore needs some cleanup afterwards, but it 
	// gives suitable primary candidates
	public static int[] identifyPeaksAtMinimalDistance(double[] vals, double threshold, double minD)
	{


		int[] idx = VectorTools.getIndexesOfValuesExceedingThreshold(vals,threshold);



		// Along the idx values, get the remaining associated values in the vals array
		double[] remaining_vals = new double[idx.length];

		for(int ind=0; ind<remaining_vals.length; ind++)
		{
			remaining_vals[ind]=vals[idx[ind]];
		}

		

		// This is the order that we have in the remaining values, sorted increasingly

		int[] order = ArraySortTools.getIndexesOfSortedArray(remaining_vals);
		
		
		

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

					if(!VectorTools.findInVector(ind, visited)) // Not visited yet, add to the neighbors
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

				idx_pruned=VectorTools.setDiff(idx_pruned,idx_neighs);
				node2visit=VectorTools.setDiff(node2visit, visited);
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
				pp=FitParabola.fitParabola(localVals);
				xm=-pp[1]/2/pp[2];
				H=pp[0]+pp[1]*xm+pp[2]*xm*xm;


			} else // Use fixed extremum fittin instead
			{
				xm=idx[ind]-lower_bound;
				pp=FitParabola.fitParabolaFixedExtremum(localVals, (int)Math.round(xm));
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
	

	



	

}
