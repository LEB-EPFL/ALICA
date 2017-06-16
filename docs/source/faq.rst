Frequently Asked Questions
==========================

What version of Micro-Manager should I use?
-------------------------------------------

ALICA was designed to work with Micro-Manager 2.0 or greater. See the
`Micro-Manager 2.0`_ website for more information.

.. _`Micro-Manager 2.0`:
   https://www.micro-manager.org/wiki/Version_2.0

Why doesn't ALICA work properly when SASS is installed?
-------------------------------------------------------

SASS is a Fiji plugin providing a simulation environment that is used
to develop and test ALICA. Because of this, the SASS .jar file
contains a completely independent copy of ALICA which competes with
Micro-Manager's copy, producing unexpected behavior.

For this reason, we highly recommend installing SASS with an
installation of Fiji that is independent of the copy of ImageJ used by
Micro-Manager and ALICA.

