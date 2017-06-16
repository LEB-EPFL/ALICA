Quickstart
==========

This page describes a brief tutorial on how to install and begin
working with ALICA. It necessarily avoids any details on how ALICA
works; instead, its focus is on helping you become acquainted with
working with ALICA.

Installation
++++++++++++

Micro-Manager 2
---------------

If `Micro-Manager`_ 2.0 or greater is not already installed on your
machine, then follow the steps in this section.

1. Navigate to
   https://www.micro-manager.org/wiki/Version_2.0#Downloads and
   download the latest nightly build for your system.
2. Install Micro-Manager by following the directions provided on the
   previously mentioned website. Make note of the installation
   directory, which on Windows is usually something like *C:\\Program
   Files\\Micro-Manager-2.0*.

.. _Micro-Manager: https://www.micro-manager.org/wiki/Version_2.0

ALICA
-----

ALICA is distributed as a .jar file and is easily installed by copying
the file into the Micro-Manager plugins folder.

1. Navigate to https://github.com/MStefko/ALICA/releases and download
   the ALICA.jar file corresponding to the latest release.
2. Copy ALICA.jar to the *MM2ROOT/mmplugins* directory, where
   *MM2ROOT/* is the Micro-Manager installation directory.
3. Verify that ALICA was installed and recognized by starting
   Micro-Manager and selecting *Plugins > Device Control > ALICA* in
   the Micro-Manager menu bar. (ALICA will not be located in the
   ImageJ menu bar.) The ALICA Setup window should appear, which will
   verify that ALICA is properly installed.

.. image:: _images/alica_setup_default.png
   :alt: An example of the ALICA setup window that will appear if
         ALICA was properly installed.
   :align: center
   :scale: 75%

Using ALICA
+++++++++++

ALICA reads an image stream from Micro-Manager and uses these images
to estimate the real-time density of fluorescence emitting molecules
within the microscope's field of view. As the estimated density of
emitting fluorophores changes (due to bleaching or changes in the
sample, for example), ALICA will automatically adjust the laser power
to maintain a set emitter density.

Step 1: Select an image source
------------------------------

First, select a source for the image stream that ALICA will
analyzer. Your options include

1. the Micro-Manager core, which contains unprocessed images from the
   camera;
2. the Micro-Manager Live mode, which contains the images that appear
   in Micro-Manager's Snap/Live View window. These images may be
   preprocssed by Micro-Manager's On-The-Fly Image Processors;
3. the next Multi-Dimensional Acquisition.

We suggest choosing the **Live mode** option when you are just
starting to use ALICA because it is the most interactive option.

.. image:: _images/alica_setup_imagesource.png
   :alt: Select the source of the image stream.
   :align: center
   :scale: 40%

Step 2: Select and configure the analyzer
-----------------------------------------

An analyzer is an algorithm that estimates the density of fluorophores
that are visible in an image. At the time of this writing, ALICA
included the following analyzers

1. a spot counter, which counts the number of fluorescent spots in the
   images;
2. AutoLase, an algorithm which estimates fluorophore densities by
   identifying the single pixel within the field of view that has been
   above a given threshold for the longest time;
3. `QuickPALM`_, a tool which identifies fluorescent spots and then
   performs a subpixel localization of each spot;
4. an integrator, which simply computes the integrated intensity of an
   image.

The **spot counter** performs well for many samples and also offers a
live view which provides real-time visual feedback of which spots it
identifies.

.. image:: _images/alica_setup_analyzer.png
   :alt: The region of the setup window for selecting and configuring
         the analyzer.
   :align: center
   :scale: 40%

Step 3: Select and configure the controller
-------------------------------------------

A controller is a feedback loop that adjusts the laser power so that
the estimated density of emitters remains as close as possible to a
previously determined set point. The difference between the current
estimate and the set point is called the error signal. The choice of
controllers includes

1. a proportional-integral (PI) controller, which responds both
   proportionately to the error signal and to the time integral of the
   error signal;
2. a manual controller, which gives control over the laser to the
   microscopist;
3. an inverter **TODO: BRIEF EXPLANATION OF THE INVERTER**;
4. a self-tuning (PI) controller, which uses a pulse of laser light to
   estimate the optimum values for the P and I parameters.

We recommend starting with **manual** control to first learn how the
analyzer responds to changes in your sample. Once you understand a
little bit about this, you can try a **self-tuning PI
controller**. The self-tuning PI controller can only tune itself when
the sample is already under STORM or PALM imaging conditions. For
direct STORM, this means that the fluorophores should already be
blinking.

.. image:: _images/alica_setup_controller.png
   :alt: The region of the setup window for selecting and configuring
         the controller.
   :align: center
   :scale: 40%

Step 4: Select the device to be controlled
------------------------------------------

A device and its property that corresponds to output power needs to be
specified for the controller to actually do something. In most STORM
and PALM experiments, the density of emitters is typically controlled
using an ultraviolet laser. To be able select this laser, it needs to
be added to the current Micro-Manager hardware configuration. Once the
laser is selected, choose its power setting from the next drop-down
menu.

To prevent a run-away laser illumination, you can set the maximum
power for the controller. We typically do not set this above a few
tens of milliWatts, but the actual value depends on the sample.

If you are testing ALICA and do not want to select a device, then
check the *Virtual* checkbox. This will instruct the controller that
it should not affect the state of any hardware devices. Checking it
will allow you to test ALICA's analyzers without performing any
hardware control.

.. image:: _images/alica_setup_device.png
   :alt: The region of the setup window for selecting and configuring
         the laser device.
   :align: center
   :scale: 40%

Step 5: Start the monitor
-------------------------

**TODO** Describe the ALICA monitor window

.. _QuickPALM: http://imagej.net/QuickPALM
