# New version of the video export library

Written in Kotlin and uses Gradle as build system.

Note: Not throroughly tested nor polished. May break. Use with a fire
extinguisher near by.

## Using the library

I placed a copy of the library to test under the `p5Library` folder so you don't
need to build it yourself. To install it:

- Open the Preferences in Processing and at the top note down the `Sketchbook location`.
- Close Processing
- Make sure there's a `libraries` folder inside the `Sketchbook location`.
  Create it if it's not there.
- Click the green `Code` button in [this page](https://github.com/hamoid/video_export_processing/tree/kotlinGradle) and choose `Download ZIP`.
- From inside the zip file, drag the folder `p5Library/videoExport` into the `libraries` folder, so you end up with
  `libraries/videoExport/settings.json` and
  `libraries/videoExport/library/videoExport.jar`
- Launch Processing and open the Examples menu.
- In the examples window, go to Contributed Libraries > videoExport and try to run the `basic` example.

## Bulding the library

The project can be opened in IntelliJ Idea community edition.

Wait for indexing (takes a minute).

On the Gradle tab on the right side of the IDE, double click on:

    videoExport > Tasks > shadow > shadowJar

This should update the `p5library` folder which you can use as described under **Using the library**.


Also by running the 

    videoExport > Tasks > ocumentation > dokkaHtml 

one can create the docs, that end up in 

    build/dokka/html/


The previous version of the library uses Ant to build, an older build system.


I tested the library in Processing 3 and 4 and it does seem to run, and it can
now generate a video pair (original video plus alpha channel video). The idea
behind this was to be able to compose videos in a video editor (as layers).


Good luck :)

## To do

- https://medium.com/@shanemyrick/publishing-to-github-packages-with-gradle-and-github-actions-4ad842634c4e