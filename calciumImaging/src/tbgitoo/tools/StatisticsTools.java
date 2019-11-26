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
## along with this program; if not, see <http://www.gnu.org/licenses/>
 */

package tbgitoo.tools;

import java.util.Arrays;
/**
 * Implements some common statistical functions on arrays
 * @author thomasbraschler
 *
 */
public class StatisticsTools {
	/** Histogram
	 * Get the relative histogram of an array, using 0 to 255 value bins (as for greyscale image)
	 * @param vals array of values to be counted (must be within 0 to 255)
	 * @return Array of length 256, with relative frequencies for integer values between 0 and 255 found in vals  
	 */
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

		// return normalized histogram
		return Dhist;

	}

	/**
	 * Estimate quantiles from observed values and a desired probability value. The quantile is such that
	 * a fraction of p realization are below the quantile. The function returns truncated quantiles in the 
	 * sense that regardless of the value of p, the minimum value of the quantile is the smallest value in vals,
	 * and the maximum is the largest value in vals
	 * @param val Array of observed values
	 * @param p Cumulative probability associated with the quantile
	 * @return Value of the quantile, with interpolation if not matching one of the values provided.
	 */

	public static double getQuantileFromValues(double [] val, double p)
	{
		// For quantile determination, we will sort the values. However, if work directly on the argument
		// val, this will change val, with unpredictable side effects regarding other functions. 
		// This is not what one would expect, so obtain a cloned copy of vals first
		double [] clonedVal = new double[val.length];

		for(int ind=0; ind>clonedVal.length; ind++)
		{
			clonedVal[ind]=val[ind];
		}
		// Sort the cloned copy, this leaves val unchanged
		Arrays.sort(clonedVal);

		double n_intervals = clonedVal.length+1; 

		double index_precise = p*n_intervals-1; 

		double index_round = Math.floor(index_precise);

		// Limit to values spanned by vals
		if(index_precise<0) { return clonedVal[0]; }

		if(index_precise>=clonedVal.length-1)
		{
			return clonedVal[clonedVal.length-1];
		}

		// Index precise is between 0 and val.length-1 (excluded), so index round will be from 0 to val.length-2
		// Do linear interpolation between adjacent intervals

		return (clonedVal[(int)index_round]+(clonedVal[(int)index_round+1]-clonedVal[(int)index_round])*(index_precise-index_round));





	}

	// Calculates the quantile from a histogram

	/**
	 * Estimate quantiles from a histogram, the x-values assumed to be 0 to length(histogram)-1 (i.e. array index).
	 *  The quantile is such that
	 * a fraction of p realization are below the quantile. The function returns truncated quantiles: if the p value
	 * is too small to be covered by the histogram midpoints, then -1 is return, if it is too large, length(hist) is returned
	 * @param hist Histogram of occurrence of values 0&lt;=x&lt;1, then 1&lt;=x&lt;2, ... (n-1)&lt;=x&lt;n
	 * @param p Cumulative probability associated with the quantile
	 * @return Value of the quantile, with interpolation
	 */

	public static double getQuantile(double [] hist, double p)
	{

		// Normalize (in case)
		// Also, calculate cumulative sums, with 1 element more than
		// the histogram to have 0 and 1 in it. So cumsum[0] is 0, cumsum[1] is h0, cumsum[2]=h0+h1
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



		// Look for the element in cumsum  where the cumulative sum is just bigger (or equal to) than the
		// desired p value

		int current_ind=0;
		while(cumsum[current_ind]<p && current_ind<=hist_normalized.length)
		{

			current_ind++;
		}

		

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


}
