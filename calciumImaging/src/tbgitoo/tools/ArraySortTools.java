/*
## Copyright (c) 2019 Thomas Braschler <thomas.braschler@unige.ch>
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

## The basis of this code is from an answer to stackoverflow question
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

package tbgitoo.tools;

public class ArraySortTools {
	
	/**
	 *  This function returns indexes such that they point to increasing elements
	 *  This function is based on the stackoverflow question 
	 *  https://stackoverflow.com/questions/4859261/get-the-indices-of-an-array-after-sorting
	 *  see license section at the beginning of the file
	 * @param vals Values for which the ordering indices should be obtained
	 * @return array of indices pointing to progressively increasing array elements
	 */
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

}
