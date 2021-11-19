# New version of the video export library

Written in Kotlin and uses Gradle as build system.

The project can be opened in IntelliJ Idea community edition.

Wait for indexing (takes a minute).

On the Gradle tab on the right side of the IDE, double click on:

    videoExport > Tasks > shadow > shadowJar

This should create the .jar library placing it into

    build/libs/videoExport-all.jar

It needs to be copied into

libraries/videoExport/library/videoExport.jar



This copying should be automated but I didn't have time to do it.

Also by running the 

    videoExport > Tasks > ocumentation > dokkaHtml 

one can create the docs, that end up in 

    build/dokka/html/


I tested the library in Processing 3 and 4 and it does seem to run, and it can
now generate a video pair (original video plus alpha channel video). The idea
behind this was to be able to compose videos in a video editor (as layers).


Good luck :)


