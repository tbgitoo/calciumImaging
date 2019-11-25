package FindPeaks.accessory.classes;

import java.util.Vector;

public class VectorTools {
	
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
	
	// Pythagorean length of the vector: sqrt(x1^2+x2^2+..)
		public static double vector_norm(double [] x)
		{

			return Math.sqrt(scalar_product(x,x));
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
				if(!VectorTools.findInVector(a.get(ind).intValue(),b))
				{
					ret.add(a.get(ind).intValue());
				}
			}

			return(ret);

		}


}
