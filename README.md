# openAF
This repository is for the openAF AutofocusPlugin for Micro-Manager 2.0

## Software setup
You will need a functioning Python environment with the appropriate dependencies satisfied in order to run the Python code, but no extra software is needed (other than µManager) for the Java side.

## Setting up the software environment (Python)
We recommend the use of a virtual environment for Python in order to prevent dependancy issues. We have used Anaconda for this purpose, and these instructions assume that you have Anaconda installed, will make a virtual environment called openAF, and will use Spyder to run the python script.

To create a suitable Python environment using Anaconda, an environment file (openAF.yml) is available in the _openAF_Python_ folder of this repository. This can be used via the Anaconda prompt to create the environment. 

You should also ensure that you have installed the appropriate Spinnaker SDK with Python support (version 2.3.0.77) if you are using the Chameleon camera as in the original openAF implementation before trying to use the openAF code. You will also need to pip-install the Spinnaker wheel (version 2.3.0.77) into the openAF environment.

The Python part of the code is best run from Spyder (in the openAF environment).

## Setting up software (Java)
We have used NetBeans to create the AutofocusPlugin for µManager. If you simply wish to use the plugin, just navigate to the _openAF_Java_ folder, then to the _dist_ subfolder, where you will find a file called [Open_AF.jar](https://github.com/ImperialCollegeLondon/openAF/blob/main/openAF_Java/dist/Open_AF.jar) - simply copy this to your Micro-Manager 2.0 plugins directory, and it should be available the next time µManager is started.

### Setting up the software environment (Java)
If you wish to modify the code for the Java side, you can do so by installing NetBeans and setting up a project for openAF. To do this, you can simply download the _openAF_Java_ folder and load the project it contains into NetBeans. You may need to update the project properties to reflect the exact version of µManager that is installed on your computer, as some plugins and resources will have different version numbers.

# Running openAF
In order to setup the openAF system in closed loop operation, the following steps should be taken (the instructions here assume the use of an oil immersion objective):

1.	The python script for openAF (DefocusCalc) is run and the AF light source is turned off
2.	µManager is started, and a socket connection is accepted and established
3.	The sample to be imaged is placed in the system and the interface of the coverslip and the sample mounting medium is focused on using images from the standard imaging camera.
4.	OpenAF-Noise Background is set to “Set Noise BG”, and a dark image is recorded
5.	The AF light source is turned on
6.	OpenAF-Range is set to a large distance which is approximately half the coverslip thickness (e.g. 100µm)
7.	OpenAF-Step size is set to double the value you have just put in the OpenAF-Range box
8.	OpenAF-Noise Background is set to “Set BG”, and a pair of background images will be recorded
9.	OpenAF-Range is set to a value expected to cover the variations in focus across the sample (typically 70µm) and OpenAF-Stepsize is set to a value small enough to ensure that focus can be maintained within the objective depth of field (Typically 0.1µm)
10.	OpenAF-Calibration is set to “On”. This will result in a z-stack of images being acquired on the autofocus camera (not shown other than outputs in the Python UI). Once this is done, a graph showing the values obtained for the AF metric will be shown. If smooth curves with peaks slightly offset from each other are shown for the blue and red curves, then this has been done successfully.
11.	The OpenAF-Threshold value should be set to a metric value (y-axis) above where the sharper of the red and blue curves flattens off.
12.	The OpenAF-Intensity Threshold value should be set by finding the point on the x-axis (Defocus) where the less sharp of the red and blue curves flattens off to near baseline. Go upwards along the y-axis to the point at which the green line on the graph (Mean intensity) is reached, and use the value on the secondary y-axis (Mean intensity) as the Intensity Threshold value.
13.	The sample should then be brought to the desired focus position and OpenAD-DefineFocus should be set to “On”
14.	Finally, closed-loop operation can be enabled/disabled by setting OpenAF-Continuous Focus to “On” or “Off respectively”
15.	If a different focus position is needed, OpenAF-Continous Focus should be set to “Off”, and steps 11 and 12 repeated
