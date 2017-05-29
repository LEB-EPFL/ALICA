# ALICA
Automated Laser Illumination Control Algorithm

MicroManager2.0 plugin for real-time image analysis and automated laser illumination control.

## Installation
Copy `ALICA.jar` into the `Micro-Manager2.0/mmplugins` folder.

## Usage
 1. Select appropriate `Analyzer`, `Controller` and `Laser`, and adjust parameters.
 2. `Start` ALICA. Images will now be continually analyzed from given source, and controller will adjust its output accordingly.
 3. You can preview current output of the `Analyzer`, modify the ROI, and set a desired setpoint for the signal.
 4. Once finished, use the `Stop` button to stop the analysis. You can use the `Save last run log` button to export a `.csv` file with data from the last run (e.g. analyzer output, setpoint, controller output for each image).

### Parameter description

#### Image source
ALICA offers 3 different options of acquiring images from MicroManager:
 - MM Core: Images are drawn directly from the circular buffer. This method is the fastest, and recommended in most cases, since it can smoothly function whenever the camera is acquiring images.
 - Live mode: Images are drawn from the Datastore associated with the current live mode view. Use this if you wish to do some on-the-fly processing using the MicroManager pipeline, before passing the image to `Analyzer`.
 - Next acquisition: Images will be drawn from the Datastore associated with the first acquisition that is started afterwards.
 
#### ROI
Using the live view, you can select a region of interest to constrain the analyzed area (for example if the density of fluorophores is uneven, or the analysis of full image takes too long).

#### Controller tick rate
This value in milliseconds defines how often the `Controller` queries the `Analyzer`, and adjusts the `Laser` output.

#### Laser
 - Max Power: Maximal output. Requests for larger output are constrained to allowed value.
 - Deadzone [%]: Requests for adjustment of output, which differ less than this value from current output, are ignored (so the laser is not unnecessarily adjusted too often.
 - Virtual: If checked, the output is not passed to the device. Useful for debugging or preview of parameters.

## Internal Workflow
The plugin's workflow can be separated into multiple steps:
 1. Acquiring images from MicroManager
 2. Analyzing the images using an instance of `Analyzer`
 3. Channeling `Analyzer`'s output into `Controller`, which processes it and controls the `Laser` to achieve a desired value of `Analyzer`'s output.

### Acquiring images
Described in [Image source]
 
### Analyzing images
Whichever `Analyzer` is selected, it waits for new image acquisition, and analyzes it. If analysis is slower than image acquisition, images are skipped, and if it is faster, the same image is not analyzed twice. Each analyzed image adjusts the `Analyzer`'s internal state. (For example, SpotCounter records the number of detected spots of each image, and waits for a query from `Controller`.

### Controlling the laser
Periodically, the `Analyzer` is queried by the `Controller` to produce a `double` value which is a measure of the controlled property of the sample (e.g. fluorophore density). For example, SpotCounter outputs the average density of spots per frame since last query by `Controller`. The `Controller` then processes this value, and adjusts the `Laser`'s output, to achieve a desired value of `Analyzer`'s output. The `Laser` can be any Device Property from MicroManager's core (e.g. the device is a 405nm Laser, and property is Power Setpoint).

## Acknowledgements
ALICA uses adapted code and algorithms under GPL from following projects:
 - [AutoLase](https://micro-manager.org/wiki/AutoLase) by Thomas Pengo and Seamus Holden
 - [QuickPalm](http://imagej.net/QuickPALM) by Ricardo Henriques
 - [SpotCounter](http://imagej.net/SpotCounter) by Nico Stuurman
 
 Many thanks to Dr. Kyle M. Douglass for guidance during this project.
