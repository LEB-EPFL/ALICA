Parameter Explanations
======================

Image Source
++++++++++++

ALICA offers 3 different options of acquiring images from MicroManager:

- **MM Core** Images are drawn directly from the circular buffer. This
  method is the fastest, and recommended in most cases, since it can
  smoothly function whenever the camera is acquiring images.
- **Live mode** Images are drawn from the Datastore associated with
  the current live mode view. Use this if you wish to do some
  on-the-fly processing using the MicroManager pipeline, before
  passing the image to Analyzer.
- **Next acquisition** Images will be drawn from the Datastore
  associated with the first acquisition that is started afterwards.

ROI
+++

Using the live view, you can select a region of interest to constrain
the analyzed area (for example if the density of fluorophores is
uneven, or the analysis of full image takes too long).

Controller tick rate
++++++++++++++++++++

This value in milliseconds defines how often the Controller queries
the Analyzer, and adjusts the laser output.

Laser
+++++

- **Max Power** Maximal power setpoint of the laser. ALICA will not
  adjust the laser power above this value.
- **Deadzone [%]** The minimum adjustment to the power setpoint that
  the controller may make as a percentage of the current
  value. Adjustments to the laser by an amount less than this are not
  permitted, which prevents unnecessary fine-tuning of the laser.
- **Virtual** If checked, the output is not passed to the
  device. Useful for debugging or preview of parameters.


