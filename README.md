# Video Export for Processing

This library interfaces with ffmpeg and makes it easy to export video files out
of Processing.

## Example

```java
import com.hamoid.*;

VideoExport videoExport;

void setup() {
  size(600, 600);
  videoExport = new VideoExport(this, "basic.mp4");
}

void draw() {
  background(#224488);
  rect(frameCount * frameCount % width, 0, 40, height);
  videoExport.saveFrame();
}
```

## ffmpeg

You need to download and install [ffmpeg](http://ffmpeg.org/) on your system before you can use this library.
Note that you might already have it installed! You can find out by typing ffmpeg or ffmpeg.exe
in the terminal. If the program is not found:

* GNU/Linux systems: use your favorite package manager.
* Windows: get a [static 32bit or 64bit binary](http://ffmpeg.zeranoe.com/builds/)
* Mac: get a [static 64bit binary](http://evermeet.cx/ffmpeg/)

For more details and download links, check the official ffmpeg website: [http://ffmpeg.org](http://ffmpeg.org/)

When you start a Processing sketch that uses this library you may be asked to indicate the location
of your ffmpeg executable. This may happen once per sketch.

## faq

### I changed the location of ffmpeg and the library stopped working. What can I do?

Add ``` videoExport.forgetFfmpegPath()``` to your setup(), run your program once, then remove that line. This will trigger the library asking for the location of ffmpeg again, so you can point to the new location of ffmpeg.

### Sometimes the resulting mp4 video files are not working. Why?

mp4 files contain essential metadata at the end of the file. The video export library saves this metadata when shutting down your sketch, inside the dispose() method. In theory, Processing calls dispose() automatically when stopping the sketch. Unfortunately dispose() is not always called. 

There's at least 3 ways to stop your program: pressing ESC, pressing STOP on the IDE, and closing the sketch window. Try those three ways to find out what works for you (until [this bug](https://github.com/processing/processing/issues/4445) is resolved in Processing). You can also try calling ```videoExport.dispose()``` manually (for instance when pressing a key to stop exporting video frames).

## change log

* v0.1.3 - 24.07.2016
  * Add webcam saving example.
  * Add getFfmpegPath() public method (requested by [@ffd8](https://github.com/ffd8)).
  * Replace PGraphics with PImage, enables webcam/movie saving (requested by [@transfluxus](https://github.com/transfluxus)).
* v0.1.1 - 15.06.2016
  * Use .waitFor() to reduce chances of video corruption.
* ...
* v0.0.1 - 25.01.2015

## Download

http://funprogramming.org/VideoExport-for-Processing/
