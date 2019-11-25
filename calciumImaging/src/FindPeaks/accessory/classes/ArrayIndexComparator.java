package FindPeaks.accessory.classes;

import java.util.Comparator;

// Modified compararor for Index array: compares the underlying values (i.e. arr[ind] rather than the integer values
// themselves
public class ArrayIndexComparator implements Comparator<Integer>
{
    protected Double[] array;

    // Load this class with the double array for which we'd like to do comparison
    public ArrayIndexComparator(double[] vals)
    {
        this.array = new Double[vals.length];
        for(int ind=0; ind<vals.length; ind++)
        {
        	this.array[ind]=vals[ind];
        }
    }

    public Integer[] createIndexArray()
    {
        Integer[] indexes = new Integer[array.length];
        for (int i = 0; i < array.length; i++)
        {
            indexes[i] = i; // This is necessary to be able to use the Integer classes
        }
        return indexes;
    }

    @Override
    public int compare(Integer index1, Integer index2)
    {
         // Autounbox from Integer to int to use as array indexes
        return array[index1].compareTo(array[index2]);
    }
}