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

## This code is in part a Java re-implementation (sometimes physically, sometimes in spirit)
## of the Octave findPeaks method, by Juan Pablo Carbajal, 
## available at https://bitbucket.org/mtmiller/octave-signal
## (direct link: https://searchcode.com/codesearch/view/64213481/)
## The Octave source code is under a General Public License
## 

 */


package tbgitoo.tools;

/**
 * Fit a parabolic profile to unit-spaced values
 * 
 * @author thomasbraschler
 */

public class FitParabola {

	/**
	 * Fits a parabola (a + bx + cx^2) to series of values (vals). The 
	 * values are regularly spaced, and the x coordinate is assumed to be
	 * 0,1,2,3...
	 * that is as in standard array indexing in Java
	 * @param vals Array of values defining the parabola to be fitted
	 * @return array of three values, representing the coefficients a, b, c for the parabolic fit. Order (offset, linear, squared)
	 */


	public static double[] fitParabola(double [] vals)
	{
		// The constants a,b,c for cx^2 + bx + a
		double [] params = new double[3];
		// No values provided, return a=b=c=0
		if(vals.length==0)
		{
			// no values provided, return all coefficients 0
			params[0]=0; // a as offset
			params[1]=0; // b in of x terms
			params[2]=0; // c in front of x^2 terms
			return params;
		}
		// only 1 value provided, store this in constant
		// also, negative value in the quadratic term to make peak at x=0
		if(vals.length==1)
		{

			params[0]=vals[0]; // a as offset, this is the actual value
			params[1]=0; // b: no linear contribution
			params[2]=-1; // c negative to have a maximum, about 1 unit wide
			return params;
		}
		// two values provided, place apex of parabola on the second element (index 1)
		if(vals.length==2)
		{
			// Now we have:
			// v=cx^2 + bx + a = val[x], for x=0 and x=1
			// v0=a // from x=0
			// v1=a+b+c // from x=1
			// dv/dx(1)=2c+b=0 // to have the apex at x=1, the first derivative must be zero at x=1 
			// and so b=-2c
			// and v1=a-2c+c=a-c => c=a-v1=v0-v1

			// Wrapping up:
			// a=v0
			// b=2*v1-2*v0
			// c=v0-v1

			// Check agin by plugin back in
			// v0=a+0*b+0*c
			// v1=a+b+c=v0+2*v1-2*v0+v0-v1=v1
			// dv/dx(1)=2*(v0-v1)+2*v1-2*v0=0

			// Transcripte to the params (params[0]=a, params[1]=b, params[2]=c)
			params[0]=vals[0];
			params[1]=2*vals[1]-2*vals[0];
			params[2]=vals[0]-vals[1];
			// return the fitted coefficients
			return params;
		}

		// General case, we have at least three values
		// We will do a least squares regression according to constant, linear, and square contributions:
		// y = a + b*(x-xbar)+c*((x-xbar)^2 - mean((x-xbar)^2))
		// where the regressors (x-xbar), (x-xbar^2 - mean((x-xbar)^2)) are orthogoanl among them, so that we can use them
		// independently; once the linear and square regression performed, we get a as the arithmetic mean
		// of the residuals
		// from a, b and c we will then calculate the usual coefficients for the fit parabola such that:
		// y = p0 + p1*x + p2*x^2
		// and return (p0,p1,p2) as the output of the function


		// To start with, we need the mean x value. We assume regular x-spacing of the values 
		// vals provided to this function, and so this is:
		double xbar=((double)vals.length-1)/2.0; // Average x value for the regularly spaced 
		// x values

		// Define the regressor for the linear contribution: x-xbar
		double x_regression[] = new double[vals.length]; 

		for(int ind=0; ind<x_regression.length; ind++)
		{
			x_regression[ind]=(double)ind-xbar; // This is the linear regressor, x-mean(x) for regularly 
			// spaced x values 
		}


		// Normalize to length 1
		double n_x_regression = VectorTools.vector_norm(x_regression);
		for(int ind=0; ind<x_regression.length; ind++)
		{
			x_regression[ind]=x_regression[ind]/n_x_regression;
		}

		// Linear regression formula
		double b=VectorTools.scalar_product(vals,x_regression)/n_x_regression;

		// Same approach with the squared term, for obtaining the coefficient c
		// First, define the regressor ((x-xbar)^2 - mean((x-xbar)^2)

		double x2_regression[] = new double[vals.length]; // Initialize

		// Center on the xbar value such as to have the square regressor orthogonal to the linear regressor
		// meaning 0 scalar product between the two regressors
		for(int ind=0; ind<x_regression.length; ind++)
		{
			x2_regression[ind]=((double)ind-xbar)*((double)ind-xbar); // This is (x-xbar)^2
		}

		// Subtract the average value to get the final regressor ((x-xbar)^2 - mean((x-xbar)^2)
		// This is again to have the square regressor orthogonal to the linear regression
		double x2_regression_mean = VectorTools.mean(x2_regression);
		for(int ind=0; ind<x_regression.length; ind++)
		{
			x2_regression[ind]=x2_regression[ind]-x2_regression_mean;
		}

		// Normalize to length 1
		double n_x2_regression = VectorTools.vector_norm(x2_regression);
		for(int ind=0; ind<x_regression.length; ind++)
		{
			x2_regression[ind]=x2_regression[ind]/n_x2_regression;
		}




		double c=VectorTools.scalar_product(vals,x2_regression)/n_x2_regression;


		// Since we have b and c now, we can calculate the offset from the mean values

		double [] vals_reduced = new double[vals.length];

		for(int ind=0; ind<vals.length; ind++)
		{
			vals_reduced[ind] = vals[ind]-b*((double)ind-xbar)-c*((double)ind-xbar)*((double)ind-xbar);
		}

		double a = VectorTools.mean(vals_reduced); // Since the linear and square regressor both 
		// have an arithmetic mean of zero, the offset is directly from the mean of the values to be fitted



		// Due to the calculation of a from the reduced residuals, we now directly have:
		// y = a + b*(x-xbar)+c*((x-xbar)^2)
		// y = a + b*x -b*xbar+c*x^2-2*c*x*xbar+c*xbar^2

		// so the zero-order coefficient in terms of the unshifted x is:
		// p0 = a -b*xbar+c*(xbar^2)
		params[0]=a-b*xbar+c*xbar*xbar;

		// and the first order coefficient
		// p1= b-2*c*xbar
		params[1]=b-2*c*xbar;

		// and the second order
		// p2 = c
		params[2]=c;


		// Return the coefficients
		return params;

	}

	/**
	 * Fits a parabola (a + bx + cx^2) to series of values (vals), but under the restriction that
	 * the appex has to occur at the x=xm 
	 * For the peak fitting purpose intended, xm should be in the interval 0 .. length(vals)-1, 
	 * and this condition is reinforced
	 * @param Array of values defining the parabola to be fitted
	 * @param xm Force location of the apex of the parabola. 
	 * @return array of three values, representing the coefficients a, b, c for the parabolic fit. Order (offset, linear, squared)
	 */

	public static double[] fitParabolaFixedExtremum(double [] vals, int xm)
	{
		return fitParabolaFixedExtremum(vals, xm, true);
	}

	/**
	 * Fits a parabola (a + bx + cx^2) to series of values (vals), but under the restriction that
	 * the appex has to occur at the x=xm 
	 * @param Array of values defining the parabola to be fitted
	 * @param xm Force location of the apex of the parabola.
	 * @param force_xm_in_array_domain Force xm to be in the interval 0 .. length(vals)-1 ? 
	 * @return array of three values, representing the coefficients a, b, c for the parabolic fit. Order (offset, linear, squared)
	 */

	public static double[] fitParabolaFixedExtremum(double [] vals, int xm, boolean force_xm_in_array_domain)
	{
		if(force_xm_in_array_domain)
		{
			if(xm<0)
			{
				xm=0;
			}

			if(xm>=vals.length)
			{
				xm=vals.length-1;
			}
		}
		
		// Initialize the coefficient vector p0,p1,p2. This will hold the coefficients for the 
		// best fit y = p0 + p1*x + p2*x^2
		
		double [] params = new double[3];
		
		// No values provided, return a=b=c=0
		if(vals.length==0)
		{
			params[0]=0;
			params[1]=0;
			params[2]=0;
			return params;
		}
		// only 1 value provided, store this in constant. Also, make quadratic term negative to 
		// have peak at x=0
		if(vals.length==1)
		{
			params[0]=vals[0];
			params[1]=0;
			params[2]=-1;
			return params;
		}
		// two values provided, xm can be either on the first (xm=0) or second element (xm=1)
		if(vals.length==2)
		{
			// The apex is the first element, step is 1
			if(xm==0)
			{
				params[0]=vals[0];
				params[1]=0; // p1=0 guarantees apex at origin x=0
				params[2]=vals[1]-vals[0]; // to match the second value, x^2=1^2=1 here. 
			} else // the desired apex is on the second element
			{
				// Again, no fitting needed, but arithmetic considerations only
				// To have the apex at x=1, express using (x-1)^2 to create the parabola shape
				// A bit of algebra to have the right form:
				// y=v1 + (x-1)^2*(v0-v1)
				// y=v1 + x^2*(v0-v1) -2*x*(v0-v1) + (v0-v1)
				// y=v0 -2*x(v0-v1)+x^2(v0-v1)

				params[0]=vals[0];
				params[1]=2*(vals[1]-vals[0]);
				params[2]=-vals[1]-vals[0];
			}


			return params;
		}

		// General case, we have at least three values

		// For simplification, express x relative to xm and y relative to the value at xm
		double H     = vals[xm];


		double [] x_rel = new double[vals.length];
		double []relative_height = new double[vals.length];

		for(int x_ind=0; x_ind<vals.length; x_ind++)
		{
			x_rel[x_ind]=(double)x_ind-xm;
			relative_height[x_ind]=vals[x_ind]-H;

		}



		// Here the idea is basically to find the best coefficients (in the least squares sense) such that
		// relative_height =  x_rel*b + x_rel^2*c

		// For this, we need to optimize the sum of squares
		// sum (relative_height-x_rel*b-x_rel^2*c)^2

		// Partial derivative with regard to b
		// sum (relative_height-x_rel*b-x_rel^2*c)*x_rel=0
		// and to c
		// sum (relative_height-x_rel*b-x_rel^2*c)*x_rel^2=0

		// b and c are constants that can written in front of the sums

		// sum relative_height*x_rel-b*sum x_rel^2-c*x_rel^3=0
		// and
		// sum relative_height*x_rel^2-b*sum x_rel^3-c*x_rel^4=0

		// We need some definitions because this is getting lengthy
		// A=sum(relative_height*x_rel)
		// B=sum(x_rel^2)
		// C=sum(x_rel^3)
		// D=sum(relative_height*x_rel^2)
		// E=sum(x_rel^4)

		// So we have:
		// A-b*B-c*C=0
		// D-b*C-c*E=0

		// and so
		// A*C-b*B*C-c*C*C=0
		// -D*B+b*B*C+c*E*B=0
		// A*C-D*B+c*(E*B-C*C)=0


		// That makes for c
		// c=(D*B-A*C)/(E*B-C*C)

		// and for b
		// A-b*B-c*C =>
		// b=(c*C-A)/B

		// Since we now have the formula, Java implementation:
		double [] x_rel2 = VectorTools.element_wise_multiplication(x_rel,x_rel);
		double [] x_rel3 = VectorTools.element_wise_multiplication(x_rel2,x_rel);
		double [] x_rel4 = VectorTools.element_wise_multiplication(x_rel3,x_rel);

		double A = VectorTools.scalar_product(relative_height,x_rel);
		double B = VectorTools.sum(x_rel2);
		double C = VectorTools.sum(x_rel3);
		double D = VectorTools.scalar_product(relative_height,x_rel2);
		double E = VectorTools.sum(x_rel4);

		double c=(D*B-A*C)/(E*B-C*C);
		double b=(A-c*C)/B;


		double a=H;

		// This makes that we have
		// y = a + b*(x-xm)+c*(x-xm)^2
		// and after regrouping to have the offset, linear and square coefficients
		// y = a-b*xm+c*xm^2 + b*x-x*2*c*xm+c*x^2 

		params[0]=a-b*xm+c*xm*xm;
		params[1]=b-2*c*xm;
		params[2]=c;

		// return the coefficients
		return params;




	}

}
