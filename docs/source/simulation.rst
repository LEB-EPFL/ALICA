Photophysical Simulations with ALICA and SASS
=============================================

Extensibility is a core design principle of ALICA. If the builtin
components do not suit the needs of your application, then you can
write your own set of tools using the frameworks of ALICA and
`Micro-Manager`_. Alternatively, you may find that ALICA already suits
your needs but you need to do some testing in a controlled environment
prior to using it in your measurements. We developed the **STORM
Acquistion Simulation Software** (`SASS`_) to assist in both of these
situations.

This document explains how to setup SASS to test ALICA in a fully
controlled simulation environment.

Install SASS and ALICA
======================

SASS and ALICA are both distributed as Java .jar files. To install
these files, you simply download the latest .jar from the Releases
page of the respective projects and copy the files into the
appropriate directories.

Micro-Manager 2
---------------

Before starting, you need the latest nightly build of Micro-Manager
2.0 (or higher). 
1. Go to https://www.micro-manager.org/wiki/Version_2.0 and download
   the latest nightly build for your system.
2. Install Micro-Manager. Make note of the installation directory
   since you will need it later to install the .jar files.

ALICA
-----

1. Navigate to https://github.com/MStefko/ALICA/releases and download
   ALICA.jar from the latest release.
2. Copy ALICA.jar to the *MM2ROOT/mmplugins* directory, where *MM2ROOT*
   refers to the installation directory of Micro-Manager.

SASS
----

1. Navigate to https://github.com/MStefko/SASS/releases and download
   SASS_VERSION.jar from the latest release. VERSION will vary
   depending on the latest release.
2. Copy the SASS .jar file to *MM2ROOT/plugins* directory. This will
   install SASS as an ImageJ plugin.

Image Injector Plugin
---------------------

**TODO**

Simulation Workflow
===================

The workflow goes as follows:

1. Use SASS to simulate a time series image stack of a PALM or STORM
   experiment.
2. Save the image stack to a .tif file.
3. Use the Image Injector Plugin to feed the images in the stack into
   the Micro-Manager live window.
4. Run ALICA in virtual mode and observe how it responds to the
   simulated conditions in the image stack.

.. _Micro-Manager: https://www.micro-manager.org/
.. _SASS: https://github.com/MStefko/SASS
