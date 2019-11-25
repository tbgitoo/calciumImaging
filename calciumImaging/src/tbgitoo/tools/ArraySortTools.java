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
