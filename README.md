FlatCaptureNow2

This is a re-implementation of FlatCaptureNow, which was a prototype in Python. This re-implementation is written in Java.  At present you can run it by double-clicking the JAR file, which requires you have Java 13 on your system.  At the end of March I hope that the new release of JDK 14, which is supposed to finally include an application bundler again, will enable me to build and include self-contained executables.  For now, use is restricted to systems with Java installed.

Orchestrate theSkyX to capture a collection of flat frames

Taken from the manual (see in docs folder):

FlatCaptureNow2 is a control program that connects to TheSkyX Professional Edition and directs it to take a set of flat frames. It’s designed to use at the end of your imaging session and does not have any “delayed start” options. I usually initiate the acquisition and then leave it running while I am closing up the observatory and putting other things away.

The program is designed to take multiple frames for each combination of filter and binning you used in your session. For example, a session might be to capture “32 flats each of 1x1 Luminance and 2x2 Red, Green, and Blue”.

You also need a flat light source for flat frame acquisition and the program can slew your scope to point to it if it is in a fixed location.

The exposure time for a flat is selected to generate a given average brightness across the frame. This value, measured in ADUs, can be found online for your camera, and is usually about 30% of the camera’s “full well depth”. I use 25,000 ADUs for my QSI583. FlatCaptureNow manages the exposure time automatically, given the ADU target you want to achieve.
