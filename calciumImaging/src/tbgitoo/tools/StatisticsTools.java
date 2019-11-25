package tbgitoo.tools;

import java.util.Arrays;

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
	
	public static double getQuantileFromValues(double [] val, double p)
	{
		double [] clonedVal = new double[val.length];
		
		for(int ind=0; ind>clonedVal.length; ind++)
		{
			clonedVal[ind]=val[ind];
		}
		
		Arrays.sort(clonedVal);
		
		double n_intervals = clonedVal.length+1; 
		
		double index_precise = p*n_intervals-1; 
		
		double index_round = Math.floor(index_precise);
		
		if(index_precise<0) { return clonedVal[0]; }
		
		if(index_precise>=clonedVal.length-1)
		{
			return clonedVal[clonedVal.length-1];
		}
		
		// Index precise is between 0 and val.length-1 (excluded), so index round will be from 0 to val.length-2
		
		return (clonedVal[(int)index_round]+(clonedVal[(int)index_round+1]-clonedVal[(int)index_round])*(index_precise-index_round));
		
		
		
		
		
	}
	
	// Calculates the quantile from a histogram

		public static double getQuantile(double [] hist, double p)
		{

			// Normalize (in case)
			// Also, calculate cumulative sums, with 1 element more than
			// the histogram to have 0 and 1 in it
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





			int current_ind=0;
			while(cumsum[current_ind]<p && current_ind<=hist_normalized.length)
			{

				current_ind++;
			}

			// Now we dispose of the element where the cumulative sum is just bigger than the
			// desired p value

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
