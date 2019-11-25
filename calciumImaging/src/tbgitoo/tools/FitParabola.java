package tbgitoo.tools;

public class FitParabola {
	
	// Fits a parabola (a + bx + cx^2) to series of values (vals). The 
		// values are regularly spaced, and the x coordinate is assumed to be
		// 0,1,2,3...
		// that is as in standard array indexing in Java

		public static double[] fitParabola(double [] vals)
		{
			// The constants a,b,c
			double [] params = new double[3];
			// No values provided, return a=b=c=0
			if(vals.length==0)
			{
				params[0]=0;
				params[1]=0;
				params[2]=0;
				return params;
			}
			// only 1 value provided, store this in constant
			// also, negative value in the quadratic term to make peak at x=0
			if(vals.length==1)
			{
				params[0]=vals[0];
				params[1]=0;
				params[2]=-1;
				return params;
			}
			// two values provided, place apex of parabola on 1
			if(vals.length==2)
			{
				// Now we have:
				// v0=a
				// v1=a+b+c
				// dv/dx(1)=2c+b=0
				// and so b=-2c
				// and v1=a-2c+c=a-c => c=a-v1=v0-v1

				// a=v0
				// b=2*v1-2*v0
				// c=v0-v1

				// Check:
				// v0=a+0*b+0*c
				// v1=a+b+c=v0+2*v1-2*v0+v0-v1=v1
				// dv/dx(1)=2*(v0-v1)+2*v1-2*v0=0

				params[0]=vals[0];
				params[1]=2*vals[1]-2*vals[0];
				params[2]=vals[0]-vals[1];

				return params;
			}

			// General case, we have at least three values



			double xbar=((double)vals.length-1)/2.0; // Average x value for the regularly spaced 
			// x values

			double x_regression[] = new double[vals.length]; // Regressor for the linear regression

			for(int ind=0; ind<x_regression.length; ind++)
			{
				x_regression[ind]=(double)ind-xbar;
			}


			// Normalize to length 1
			double n_x_regression = VectorTools.vector_norm(x_regression);
			for(int ind=0; ind<x_regression.length; ind++)
			{
				x_regression[ind]=x_regression[ind]/n_x_regression;
			}

			double b=VectorTools.scalar_product(vals,x_regression)/n_x_regression;

			double x2_regression[] = new double[vals.length]; // Regressor for the linear regression

			for(int ind=0; ind<x_regression.length; ind++)
			{
				x2_regression[ind]=((double)ind-xbar)*((double)ind-xbar);
			}

			// Subtract the average value

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

			double a = VectorTools.mean(vals_reduced);



			// Now we have:

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



			return params;

		}
		
		// Fits a parabola (a + bx + cx^2) to series of values (vals).
		// xm designates an element through at which the extremum has to occur

		public static double[] fitParabolaFixedExtremum(double [] vals, int xm)
		{
			if(xm<0)
			{
				xm=0;
			}
			if(xm>=vals.length)
			{
				xm=vals.length-1;
			}
			// The constants a,b,c
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
			// two values provided, place apex of parabola on 1
			if(vals.length==2)
			{
				// The apex is the first element, step is 1
				if(xm==0)
				{
					params[0]=vals[0];
					params[1]=0;
					params[2]=vals[1]-vals[0];
				} else
				{
					// This is
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

			double H     = vals[xm];


			double [] x_rel = new double[vals.length];
			double []relative_height = new double[vals.length];

			for(int x_ind=0; x_ind<vals.length; x_ind++)
			{
				x_rel[x_ind]=(double)x_ind-xm;
				relative_height[x_ind]=vals[x_ind]-H;

			}



			// Here the idea is basically to find the best coefficients such that

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

			// and for B
			// A-b*B-c*C =>
			// b=(c*C-A)/B

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
			// y = a-b*xm+c*xm^2 + b*x-x*2*c*xm+c*x^2 

			params[0]=a-b*xm+c*xm*xm;
			params[1]=b-2*c*xm;
			params[2]=c;

			return params;




		}

}
