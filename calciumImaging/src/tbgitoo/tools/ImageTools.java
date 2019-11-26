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

import ij.ImageStack;
import ij.process.ByteProcessor;


/**
 * Class with convenience and utility functions for handling images in ImageJ
 * 
 * @author thomasbraschler
 *
 */
public class ImageTools {
	
		/**
		 * Get a new stack, byte format, of the same dimensions as given stack
		 * Typically useful for creating mask or greyscale images for the output of some operation
		 * @param source Source stack, to get the dimensions
		 * @return 0-initialized stack of the same x,y and z dimensions as the source stack
		 */
		public static ImageStack getEmptyByteStack(ImageStack source)
		{
			ImageStack theStack = new ImageStack(source.getWidth(), 
					source.getHeight(), source.getSize());



			for(int ind=1; ind<=theStack.getSize(); ind++)
			{
				theStack.setProcessor(
						new ByteProcessor(theStack.getWidth(), 
								theStack.getHeight()), ind);
			}

			return(theStack);


		}

}
