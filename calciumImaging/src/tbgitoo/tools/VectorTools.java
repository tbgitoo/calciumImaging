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

import java.util.Vector;
/**
 * Some standard functions applied to vectors
 * @author thomasbraschler
 *
 */
public class VectorTools {
	
	/**
	 * Mean of the values in a vector
	 * @param x The vector of values
	 * @return The arithmetic mean of the values in the vector. 0 if vector length 0 is provided
	 */
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
	/**
	 * Pythagorean length of the vector: sqrt(x1^2+x2^2+..)
	 * @param x The vector of values
	 * @return The Pythogerean (Euclidian) norm of the vector
	 */
	
		public static double vector_norm(double [] x)
		{

			return Math.sqrt(scalar_product(x,x));
		}
		
		/**
		 * Scalar product of two vectors. If not of the same length, the shorter is length is considered only
		 * @param x First vector of values
		 * @param y Second vector of values
		 * @return Scalar product
		 */
		

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
		
		/**
		 * Element-wise multiplication of two vectors.
		 *  If not of the same length, multiplication is only carried out up to the end of the shorter vector
		 * @param x First vector of values
		 * @param y Second vector of values
		 * @return Vector of the same length as x and y (or the shorter of the two if different)
		 */

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
		
		/**
		 * Sum of the values in a vector
		 * @param x The vector of values
		 * @return The sum of values in the vector. 0 if vector length 0 is provided
		 */
		
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
		
		/**
		 * Sum of the values in a vector
		 * @param x The vector of values
		 * @return The sum of values in the vector. 0 if vector length 0 is provided
		 */
		
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
		
		 /**
		 * Is a given value in a vector?
		 * @param needle The value to be searched
		 * @param haystack The vector to be searched through
		 * @return True if needle was found in haystack, false otherwise
		 */
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
		
		/**
		 * Ensemble difference: Returns all the elements that are in a but could not be found in b
		 * @param a Vector with candidate elements to be tested
		 * @param b Vector with disallowed elements that should not be in output
		 * @return Vector of all values in a that could not be found in b
		 */
		
		
		public static Vector <Integer> setDiff(Vector <Integer> a, Vector <Integer> b)
		{
			Vector <Integer> ret = new Vector <Integer>(a.size());

			for(int ind=0; ind<a.size(); ind++)
			{
				if(!VectorTools.findInVector(a.get(ind).intValue(),b))
				{
					ret.add(a.get(ind).intValue());
				}
			}

			return(ret);

		}
		
		/**
		 * /Indexes of the values at or above the threshold
		 * @param vals Array of values
		 * @param threshold Minimal threshold required for elements to be eligible
		 * @return Array of indexes to the elements with values above the threshold
		 */
		
		
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


}
