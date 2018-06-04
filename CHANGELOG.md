# Change Log
All notable changes to this project will be documented in this file.

## [Unreleased]

### Changed
- Bumped ImageJ version to 1.51s.
- The real-time plot in the monitor window now displays a short
  description of the analyzer's output.

### Fixed
- The `AlicaLogger` no longer overwrites values that have already been
  written to the same frame number in the log.

## [v0.3.1]
### Changed
- The API was ported for compatibility with Micro-Manager 2.0 gamma.

## [v0.3.0]

### Changed 
- The build system has been ported from Ant to Maven.

## [v0.2.5]

### Added
- Analyzers now return a short description of their return values for
  y-axis labels in plots.

### Modified
- The Integrator no longer implicitly performs a background
  subtraction. This means that its outputs now match the result of
  running the **Measure** command on an image in ImageJ.
- All analyzers that return densities (SpotCounter, QuickPALM, and the
  Integrator) return events per 100 µm^2. Previously this was per
  10000 µm^2.

### Fixed
- A bug in the spot counter has been fixed that resulted missed spots
  in a narrow band of pixels on the right and bottom edges of an
  image.
- Fixed a typo in the `getName()` method for the `ManualController`.
  The method now returns `"Manual"` instead of `"Manuals"`.

## [v0.2.4]
### Modified
 - Added StatusPanel and blocking capabilities to the PI controller.

## [v0.2.3]
### Fixed
 - Last analysis time reporting now works for MM Core mode.

## [v0.2.2]
### Added
 - Function handles for use via BeanShell scripts

### Modified
 - SpotCounter live view is now toggled during ALICA run
 - SelfTuningPI's output can be suppressed temporarily

### Fixed
 - SelfTuningPI's reported P and I values in GUI

## [v0.2.1]
### Added
 - `ALICA_dev.jar` release
   - This release contains necessary MicroManager and ImageJ
     classes for extending ALICA and inclusion into SASS. 
   - This replaces the `ALICA_SASS.jar` release.
 - Dynamic class loading now supported in SASS.
 - [LEB](http://leb.epfl.ch/) logo

### Modified
 - Dynamic loaders of Analyzers and Controllers now scan the 
   root directory and one level of subdirectories. This allows 
   compatibility with SASS. 


## [v0.2.0]
### Added
 - Extensibility support:
    - Analyzers and Controllers can now be loaded from external jar files.
    - The jar file has to be placed in `mmplugins/` folder, and begin with 
`ALICA_`.
    - Example valid filename: `ALICA_MyOwnAnalyzer.jar`


### Modified
 - Self-tuning PI controller's calibration is now triggered manually after 
starting ALICA. 
 - The build process now compiles a "lightweight" jar file for use with
MicroManager, and also a "standalone" jar for inclusion into SASS.

### TODO
 - Make dynamic loading compatible with SASS.

## [v0.1.2]
### Added
- Colorized warning labels
- MainGUI and MonitorGUI now switch between each other

## [v0.1.1]
### Added
- Integrator analyzer (computes average pixel brightness in frame)

## [v0.1.0]
### Added
- Optional Analyzer and Controller StatusPanels

## [v0.0.2]
### Added
- Added self-tuning PI controller
- Resolved compatibility issues which prevented adaptation to STEADIER-SAILOR
- JAR file now contains all dependencies (downside - larger file size)

### Removed
- miniPID controller

## [v0.0.1]
### Added
- CHANGELOG.md was created for tracking changes to the project.

[Unreleased]: https://github.com/LEB-EPFL/ALICA/compare/v0.3.1...HEAD
[v0.3.1]: https://github.com/LEB-EPFL/ALICA/releases/tag/v0.3.0
[v0.3.0]: https://github.com/LEB-EPFL/ALICA/releases/tag/v0.3.0
[v0.2.5]: https://github.com/LEB-EPFL/ALICA/releases/tag/v0.2.5
[v0.2.4]: https://github.com/LEB-EPFL/ALICA/releases/tag/v0.2.4
[v0.2.3]: https://github.com/LEB-EPFL/ALICA/releases/tag/v0.2.3
[v0.2.2]: https://github.com/LEB-EPFL/ALICA/releases/tag/v0.2.2
[v0.2.1]: https://github.com/LEB-EPFL/ALICA/releases/tag/v0.2.1
[v0.2.0]: https://github.com/LEB-EPFL/ALICA/releases/tag/v0.2.0
[v0.1.2]: https://github.com/LEB-EPFL/ALICA/releases/tag/v0.1.2
[v0.1.1]: https://github.com/LEB-EPFL/ALICA/releases/tag/v0.1.1
[v0.1.0]: https://github.com/LEB-EPFL/ALICA/releases/tag/v0.1.0
[v0.0.2]: https://github.com/LEB-EPFL/ALICA/releases/tag/v0.0.2
