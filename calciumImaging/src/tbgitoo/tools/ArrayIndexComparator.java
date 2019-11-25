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

package FindPeaks.accessory.classes;

import java.util.Comparator;

/** 
 * Modified comparator for Index array: 
 * compares the underlying values (i.e. arr[ind] rather than the integer values
 * We need this in the class FindPeaksTools to find the indexes of the largest values
 * @author thomasbraschler
 *
 */

public class ArrayIndexComparator implements Comparator<Integer>
{
	/**
	 * Array with the values for which we would like to get the ordered indices
	 */
    protected Double[] array;

    /**
     *  Load this class with the double array for which we'd like to do comparison
     * @param vals The values for wich we would like to get the indices indicating their orgering
     */
    public ArrayIndexComparator(double[] vals)
    {
        this.array = new Double[vals.length];
        for(int ind=0; ind<vals.length; ind++)
        {
        	this.array[ind]=vals[ind];
        }
    }
    /**
     * Convenience function. We want to get the indices sorted along the values, and so we need to start
     * out with the indices array (i.e. 0,1,2,3 ...). For reasons of Java typing, we need this not as primitive
     * int[] array, but as a class array using the Integer class
     * @return Returns an ascending array of indices (0 to length array-1)
     */
    public Integer[] createIndexArray()
    {
        Integer[] indexes = new Integer[array.length];
        for (int i = 0; i < array.length; i++)
        {
            indexes[i] = i; // This is necessary to be able to use the Integer classes
        }
        return indexes;
    }

    /** 
     * Value-based index comparision: An index value is "larger" than another index value if
     * it points to a larger array element
     */
    public int compare(Integer index1, Integer index2)
    {
         // Autounbox from Integer to int to use as array indexes
        return array[index1].compareTo(array[index2]);
    }
}