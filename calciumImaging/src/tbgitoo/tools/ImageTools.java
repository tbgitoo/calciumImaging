package FindPeaks.accessory.classes;

import ij.ImageStack;
import ij.process.ByteProcessor;

public class ImageTools {
	// Helper function to get a new stack of the same dimensions as 
		// given stack for creating masks
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
