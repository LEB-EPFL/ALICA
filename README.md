# mALICA

After loading the project in NetBeans for the first time on your local machine,
you need to load the files in `external-jars/` folder to your local Maven
repository:
 1. Go to `Projects` tab, `Dependencies` folder.
 2. For each entry with a warning (e.g. `mmacqengine-1.0.jar`), right click,
 select `Manually install artifact`, and guide Netbeans to the corresponding
 jar in `external-jars/` folder.


### Known issues
 - MicroManager does not have an online Maven repository and thus the 3 local JARs have 
 to be included in the git repository, and then installed into the local Maven repository
 on each development machine.
 - In order to get NetBeans debugger to find jars in the Micro-Manager folder,
 I manually hardcoded the debug classpath as
 `-classpath "C:\Program Files\Micro-Manager-2.0beta\plugins\Micro-Manager\*;C:\Program Files\Micro-Manager-2.0beta\plugins\*.jar;C:\Program Files\Micro-Manager-2.0beta\ij.jar"`
 for the Debug action in `nbactions.xml`. This will obviously not fly on Linux.