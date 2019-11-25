# calciumImaging

This is an imageJ plugin that can be used to evaluate temporal peaks in calcium imaging videos. To install the plugin, download the 
calciumImaging_.jar file and place it into the plugins folder of your ImageJ (or Fiji) installation. Restart ImageJ, and there should be a menu "CalciumImaging" available under "Plugins".

In the folder calciumImaging, the source code and Eclipse project used to generate the calciumImaging_.jar files can be found.

Regarding usage, the plugin for now works only on grey-scale stacks, so if you have RGB-color videos, split the color channels and use only the relevant channel for evaluation (typically the green channel). Also, consider downsampling for large videos to save calculation time.

A typical workflow would involve:
1) detection of temporal peaks (loading of the relevant greyscale stack representing a calcium imaging video, then >Plugins>CalciumImaging>Locate temporal peaks). 
2) On the result obtained (a new image stack indicating the location of the temporal peaks in both space xy and time z), local frequency can be evaluated from the mean temporal (z) distance between peaks (>Plugins>CalciumImaging>Local frequency (from peaks, mean time per peak), or 
3) Also on the result obtained in step 1, local frequency can be evaluated from the median temporal (z) distance between peaks (>Plugins>CalciumImaging>Local frequency (from peaks, median peak-peak time).
4) Local phase can be evaluated from the result of step 1 (>Plugins>CalciumImaging>Local Phase(from peaks))
