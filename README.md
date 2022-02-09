# New version of the video export library

Written in Kotlin and uses Gradle as build system.

Note: Not throroughly tested nor polished. May break. Use with a fire
extinguisher near by.

## Using the library

I placed a copy of the library to test under the `p5Library` folder so you don't
need to build it yourself. To install it:

- Open the Preferences in Processind and at the top note down the `Sketchbook
  location`.
- Close Processing
- Make sure there's a `libraries` folder inside the `Sketchbook location`.
  Create it if it's not there.
- Click the green `Code` button in [this page](https://github.com/hamoid/video_export_processing/tree/kotlinGradle) and choose `Download ZIP`.
- From inside the zip file, drag the folder *inside* `p5Library` (the folder called
  `videoExport`) into the `libraries` folder, so you have
  `libraries/videoExport/settings.json` and
  `libraries/videoExport/library/videoExport.jar`
- Open Processing and run [this example](https://github.com/hamoid/video_export_processing/blob/kotlinGradle/p5test/p5test.pde) to see if it works.
- In the [master branch](https://github.com/hamoid/video_export_processing/tree/master/examples) there's a bunch of examples but I haven't checked yet if they need to be made compatible with this newer version of the video export library. If someone can try those examples and give me feedback it would be very useful.

## Bulding the library

The project can be opened in IntelliJ Idea community edition.

Wait for indexing (takes a minute).

On the Gradle tab on the right side of the IDE, double click on:

    videoExport > Tasks > shadow > shadowJar

This should create the .jar library placing it into

    build/libs/videoExport-all.jar

It needs to be copied into your Processing libraries folder, something like

    libraries/videoExport/library/videoExport.jar



This copying should be automated but I didn't have time to do it.

Also by running the 

    videoExport > Tasks > ocumentation > dokkaHtml 

one can create the docs, that end up in 

    build/dokka/html/


The previous version of the library uses Ant to build, an older build system.


I tested the library in Processing 3 and 4 and it does seem to run, and it can
now generate a video pair (original video plus alpha channel video). The idea
behind this was to be able to compose videos in a video editor (as layers).


Good luck :)


