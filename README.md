# calciumImaging

This is an imageJ plugin that can be used to evaluate temporal peaks in calcium imaging videos. To install the plugin, download the 
calciumImaging_.jar file and place it into the plugins folder of your ImageJ (or Fiji) installation. Restart ImageJ, and there should be a menu "CalciumImaging" available under "Plugins".

In the folder calciumImaging, the source code and Eclipse project used to generate the calciumImaging_.jar files can be found.

Regarding usage, the plugin for now works only on grey-scale stacks, so if you have RGB-color videos, split the color channels and use only the relevant channel for evaluation (typically the green channel). Also, consider downsampling for large videos to save calculation time.
