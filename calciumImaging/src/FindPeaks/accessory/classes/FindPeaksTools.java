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

import java.util.Vector;

import tbgitoo.tools.ArraySortTools;
import tbgitoo.tools.FitParabola;
import tbgitoo.tools.VectorTools;


/**
 * Generic methods for peak detection in a one-dimensional array of values
 * Based on the Octave findPeak method (https://searchcode.com/codesearch/view/64213481/)
 * Also, invokes methods from the ArraySortTools class, which is based on snippets 
 * from https://stackoverflow.com/questions/4859261/get-the-indices-of-an-array-after-sorting
 * See license at the beginning of this file
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


	

	/** 
	 * This function identifies peaks above a threshold, and with a minimal distance between 
	 * them. The function is a bit rought and therefore needs some cleanup afterwards, but it 
	 * gives suitable primary candidates. This is the first part of the findPeak implementation
	 * in Octave (https://searchcode.com/codesearch/view/64213481/)
	 * @param vals Values of the function for which peaks should be found. Unit spacing between sequential values is assumed
	 * @param threshold Threshold above which a value need to lie to be considered as a candidate for being peak
	 * @param minD Minimal distance between neighboring peaks
	 * @return Array of indices indicating the peaks
	 */

	
	public static int[] identifyPeaksAtMinimalDistance(double[] vals, double threshold, double minD)
	{

		// Only values exceeding the threshold are peak canditates
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
		// After this, we should have an array of indices, the first element
		// pointing to the highest value, then the second to second-highest value, and so
		// forth
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

		// While we still have nodes to visit left
		while (node2visit.size()>0)
		{
			// Set the current node as the node with the highest value that is still avaible
			int current_node = node2visit.get(0);
			// This node is now visited, remove from candidates and add to visited array
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
			// We got the neighbors, and there are indeed some neighbors
			if(neighs.size()>0)
			{
				Vector<Integer> idx_neighs=new Vector<Integer>(neighs.size());

				for(int ind=0; ind<neighs.size(); ind++)
				{
					// listing the neighbors
					idx_neighs.add(idx_s[neighs.get(ind)]);
					// all those neighbors should be counted as visited in the next round
					visited.add(neighs.get(ind).intValue());
				}

				// Remove the neighbors from the available candidates
				idx_pruned=VectorTools.setDiff(idx_pruned,idx_neighs);
				// Make sure no more visited nodes figure in the array with the nodes to visit
				node2visit=VectorTools.setDiff(node2visit, visited);
			}


		}



		// So now we have all the indexes of the values that are larger than the threshold, in decreasing order
		// with sequential removal of all the neighbors too close to the peaks as identified by the maxima
		
		// Format for return
		idx=new int[idx_pruned.size()];

		for(int ind=0; ind<idx_pruned.size(); ind++)
		{
			idx[ind]=idx_pruned.get(ind);
		}


		// And return
		return idx;


	}
	
	/**
	 * This is the second part of the Java implementation of the Octave findPeaks function
	 * (see https://searchcode.com/codesearch/view/64213481/)
	 * We estimate widths of peaks remove peaks that have quality issues:
	 * width smaller than given minal width or larger than maximal width.
	 * wrong concavity.
	 * not high enough
	 * data at peak is lower than parabola by 1%
	 * position of extrema minus center is bigger equal than minD/2
     * @param idx the pre-filtered peak candidates,typicaly obtained from the method identifyPeaksAtMinimalDistance
	 * @param vals Underlying values (complete dataset)
	 * @param minD distance required between the peaks (minD)
	 * @param minW Minimal fitting width
	 * @param maxW Maximal fitting width
	 * @param minH Minimal fitting height
	 * @param fitMin Minimal environment around peak for parabola fitting
	 * @return Indices point to peaks passing quality criteria
	 */

	
	public static int[] filterPeaks(int[] idx, double[] vals, double minD, 
			double minW, double maxW, double minH, double fitMin)
	{

		// Remaining idx elements which are standalone peaks
		// To start with, identical to idx but Vector class so we can modify it
		Vector<Integer> idx_pruned = new Vector <Integer>(idx.length);


		for(int ind=0; ind<idx.length; ind++)
		{
			// get the local data around the peak. Check whether peak candidate is a local maximum
			// The lower bound should not be below the putative peak position - minD/2
			// and the upper bound not higher than the putative peak position + minD/2
			// in addition, lower and upper bounds must cover actual array elements in vals
			int lower_bound = (int)Math.max(Math.floor(-minD/2+idx[ind]), 0);
			int upper_bound = (int)Math.min(Math.ceil(minD/2+idx[ind]), vals.length-1);


			// Approach the lower bound more if permitted by fitMin
			while(lower_bound < idx[ind]-1 && vals[lower_bound]<fitMin)
			{
				lower_bound++;
			}
			// Approach the upper bound more if permitted by fitMin
			while(upper_bound > idx[ind]+1 && vals[upper_bound]<fitMin)
			{
				upper_bound--;
			}

			// Isolate the locally relevant values
			double[] localVals = new double[upper_bound-lower_bound+1];



			boolean isMaximum=true;
			// For parabola fitting, check whether the original peak candidate is also a local
			// maximum or whether in the environment, there are some higher values
			for(int local_ind=0; local_ind<localVals.length; local_ind++)
			{
				localVals[local_ind]=vals[lower_bound+local_ind];
				if(localVals[local_ind]>vals[idx[ind]])
				{
					isMaximum=false;
				}
			}


			// Parabola coefficients (3 values), for fitting a parabola
			double [] pp; 
			double xm; // local X-value at maximum
			double H; // height of peak
			// The current point is not the maximum, so use polynomial fitting
			if(!isMaximum)
			{
				pp=FitParabola.fitParabola(localVals);
				xm=-pp[1]/2/pp[2]; // The maximum of a parabola has zero derivative
				// so d/dx pp[2]*x^2+pp[1]*x+pp[0]=0 => 2*pp[2]*x+pp[1]=0
				H=pp[0]+pp[1]*xm+pp[2]*xm*xm; // apex height


			} else // Use fixed extremum fittin instead, with the maximum supposed at the actual position
				
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
				// Not high enough
				if(H<minH)
				{
					keep = false;

				} else
				{
					// Estimate width from height above minimum. This is symmetry, so 
					// its delta H = x^2 * pp[2]
					double width=Math.sqrt(-(H-minH)/pp[2]);
					// The width fitted in this way should be between the imposed bounds
					if(width<minW || width>maxW)
					{

						keep=false;
					}
				}

			}




			if(keep)
			{
				// The actual maximum should not be further than minD/2 from the fitted one (if different)
				if(Math.abs(idx[ind]-xm-lower_bound)>minD/2)
				{
					keep=false;
				}


			}

			
			// All tests passed so add to list to keep
			if(keep)
			{
				idx_pruned.add(idx[ind]);
			}





		}

		// Format for return 
		int [] new_idx = new int[idx_pruned.size()];

		for(int ind=0; ind<idx_pruned.size(); ind++)
		{
			new_idx[ind]=idx_pruned.get(ind).intValue();
		}

		// Return
		return new_idx;

	}
	

	



	

}
