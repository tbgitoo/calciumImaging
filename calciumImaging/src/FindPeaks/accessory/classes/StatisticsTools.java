package FindPeaks.accessory.classes;

public class StatisticsTools {
	
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

}
