# ALICA

- [![Build Status](https://travis-ci.org/LEB-EPFL/ALICA.svg?branch=master)](https://travis-ci.org/LEB-EPFL/ALICA)
- [![Join the chat at https://gitter.im/leb_epfl/ALICA](https://badges.gitter.im/leb_epfl/ALICA.svg)](https://gitter.im/leb_epfl/ALICA?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

A platform for real-time image analysis and automated laser
illumination control in super-resolution microscopy. Also a plugin for
[Micro-Manager 2.0](https://micro-manager.org/).

## Installation
0. Download the latest `ALICA*.jar` from the [releases
page](https://github.com/MStefko/ALICA/releases) and place it in the
`mmplugins` folder inside the Micro-Manager installation directory.
1. Download the latest `ALICA_ACPack*.jar` from the [ALICA_ACPack
   releases page](https://github.com/LEB-EPFL/ALICA_ACPack) and place
   it in the same `mmplugins` folder.

## Quickstart

0. Start Micro-Manager with a camera and light source already
   configured.
1. In the Micro-Manager menu bar, navigate to *Plugins > Device
   Control > ALICA*.
2. Select `Live mode` as the image source, `SpotCounter` as the
   Analyzer, `Manual` as the Controller, and your light source from
   drop down box next to *Laser*.
3. Click *Start* to bring up the ALICA monitor window.
4. Click *Live* in the Micro-Manager window to start a live
   acquisition. The ALICA monitor graph should now change to reflect
   the number of fluorescence spots that are found in the images over
   time.
5. Click the *Live view* checkbox under the spot counter control panel
   to bring up a live view of the Spot Counter's output.
6. Click *Stop Live* in the Micro-Manager control window and *Stop* in
   the ALICA control window to return to the ALICA configuration. You
   can use the `Save last run log` button to export a `.csv` file with
   data from the last run (e.g. analyzer output, setpoint, controller
   output for each image).
   
## Documentation

http://alica.readthedocs.io/en/latest

### Parameter description

#### Image source
ALICA offers 3 different options of acquiring images from
Micro-Manager:
- MM Core: Images are drawn directly from the circular buffer. This
  method is the fastest, and recommended in most cases, since it can
  smoothly function whenever the camera is acquiring images.
- Live mode: Images are drawn from the Datastore associated with the
  current live mode view. Use this if you wish to do some on-the-fly
  processing using the Micro-Manager pipeline, before passing the
  image to `Analyzer`.
- Next acquisition: Images will be drawn from the Datastore associated
  with the first acquisition that is started afterwards.
 
#### ROI
Using the live view, you can select a region of interest to constrain
the analyzed area (for example if the density of fluorophores is
uneven, or the analysis of full image takes too long).

#### Controller tick rate
This value in milliseconds defines how often the `Controller` queries
the `Analyzer`, and adjusts the `Laser` output.

#### Laser
- Max Power: Maximal output. Requests for larger output are
  constrained to allowed value.
- Deadzone [%]: Requests for adjustment of output, which differ less
  than this value from current output, are ignored (so the laser is
  not unnecessarily adjusted too often.
- Virtual: If checked, the output is not passed to the device. Useful
  for debugging or preview of parameters.

## Internal Workflow
The plugin's workflow can be separated into multiple steps:
1. Acquiring images from MicroManager
2. Analyzing the images using an instance of `Analyzer`
3. Channeling `Analyzer`'s output into `Controller`, which processes
   it and controls the `Laser` to achieve a desired value of
   `Analyzer`'s output.

### Acquiring images
Described in [Image source]
 
### Analyzing images
Whichever `Analyzer` is selected, it waits for new image acquisition,
and analyzes it. If analysis is slower than image acquisition, images
are skipped, and if it is faster, the same image is not analyzed
twice. Each analyzed image adjusts the `Analyzer`'s internal
state. (For example, SpotCounter records the number of detected spots
of each image, and waits for a query from `Controller`.

### Controlling the light source
Periodically, the `Analyzer` is queried by the `Controller` to produce
a `double` value which is a measure of the controlled property of the
sample (e.g. fluorophore density). For example, SpotCounter outputs
the average density of spots per frame since last query by
`Controller`. The `Controller` then processes this value, and adjusts
the `Laser`'s output, to achieve a desired value of `Analyzer`'s
output. The `Laser` can be any Device Property from MicroManager's
core (e.g. the device is a 405nm Laser, and property is Power
Setpoint).

## Developing custom functionality
It is possible to implement your own `Analyzer` or `Controller`.  More
info can be found in the [documentation](http://alica.readthedocs.io).

## Getting help

- How to use ALICA: https://gitter.im/leb_epfl/ALICA
- Bug reports: https://github.com/LEB-EPFL/ALICA/issues
- Feature requests: https://github.com/LEB-EPFL/ALICA/issues
- Developer questions: https://gitter.im/leb_epfl/ALICA

## Acknowledgements
ALICA uses adapted code and algorithms under GPL from following
projects:
- [AutoLase](https://micro-manager.org/wiki/AutoLase) by Thomas Pengo
  and Seamus Holden
- [QuickPalm](http://imagej.net/QuickPALM) by Ricardo Henriques
- [SpotCounter](http://imagej.net/SpotCounter) by Nico Stuurman

In addition, ALICA relies on the following projects:
- [Micro-Manager](https://micro-manager.org/)
- [ImageJ](https://imagej.net/Welcome)
- [Fiji](https://fiji.sc/)
- [SciJava](http://scijava.org/)
- [Maven](https://maven.apache.org/)
 
Contributors:
- [Marcel Stefko](https://github.com/MStefko): ALICA framework and
  GUI, and adaptation of algorithms into Analyzers and Controllers
- [Kyle M. Douglass](https://github.com/kmdouglass): Documentation and
  beta-testing
- [Baptiste Ottino](https://github.com/bottino): The
  [DEFCoN](https://github.com/LEB-EPFL/DEFCoN-ImageJ) analyzer

...and the many, many people behind *all* the software that we
rely on.

