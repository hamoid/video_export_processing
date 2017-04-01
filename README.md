# Video Export for Processing

This library interfaces with ffmpeg and makes it easy to export video files out
of Processing.

## Example

```java
import com.hamoid.*;

VideoExport videoExport;

void setup() {
  size(600, 600);
  videoExport = new VideoExport(this, "hello.mp4");
  videoExport.startMovie();
}
void draw() {
  background(#224488);
  rect(frameCount * frameCount % width, 0, 40, height);
  videoExport.saveFrame();
}
void keyPressed() {
  if (key == 'q') {
    videoExport.endMovie();
    exit();
  }
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

## Frequent issues and questions

### What if I change the location of ffmpeg?

The library should notice and ask you for its location again. The location
information is saved in a json file which you can find in the library location.
If you delete this json file the library will ask you for the location of ffmpeg
and create the file again with the updated information.

### Sometimes the resulting mp4 video files are not working. Why?

mp4 files contain essential metadata at the end of the file.
The video export library saves this metadata when you call the
`endMovie()` method. If you don't call it, the movie may be
incomplete. The endMovie() method was added in version 0.1.5.

### I see an ffmpeg error related to "crf". Why?

This happens if your copy of ffmpeg does not include the h264 encoder.
Not all ffmpeg binaries are equal, some include more features than others.
Try downloading a different or more recent binary. Let me know if that
doesn't work.

### Odd widths and heights not allowed

The exported video is compressed using the h264 codec. This codec does not allow odd image sizes like 111 x 113. Some computer screens actually have odd sizes like 1920 x 1059. If you use fullScreen() with such a screen, exporting will fail with an error like `height not divisible by 2 (1920x1059)`. This will be fixed in future versions by auto correcting the requested size to an even number and notifying the user.

## change log

* 0.1.7 - April 1st, 2017
  * Setting are now saved in the libray folder. Using the Java Preferences was
    giving errors on some Windows versions.
  * New example added to produce a video based on FFT data and making sure audio
    and video stay in sync.
  * In Windows, when ending a video, CTRL+C is sent to ffmpeg to terminate
    properly. This seems to fix corrupted videos on Windows.
  * The library now notices if ffmpeg is not found (maybe because it was moved)
    and asks again for its location.
* 0.1.6 - December 8th, 2016
  * Fix for high dpi screens (Thanks to @LodenRietveld)
* 0.1.5 - December 2nd, 2016
  * Refactoring. Clean up code.
  * Add .startMovie() and .endMovie() to prevent possible "missing-end-of-movie corruption".
  * Allow attaching a sound file to the produced movie.
  * Allow exporting multiple video files using the same videoExport object.
* 0.1.4 - August 4th, 2016
  * Attempt to fix randomly corrupted videos on Windows 
* 0.1.3 - July 24th, 2016
  * Add webcam saving example.
  * Add getFfmpegPath() public method (requested by [@ffd8](https://github.com/ffd8)).
  * Replace PGraphics with PImage, enables webcam/movie saving (requested by [@transfluxus](https://github.com/transfluxus)).
* 0.1.1 - June 15th, 2016
  * Use .waitFor() to reduce chances of video corruption.
* ...
* 0.0.1 - January 25th, 2015

## Download

http://funprogramming.org/VideoExport-for-Processing/
