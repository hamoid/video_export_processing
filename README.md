# Video Export for Processing

This library interfaces with FFmpeg and makes it easy to export video files out
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

## FFmpeg

You need to download and install [FFmpeg](http://ffmpeg.org/) on your system before you can use this library.
Note that you might already have it installed! You can find out by typing ffmpeg or ffmpeg.exe
in the terminal. If the program is not found:

* GNU/Linux systems: use your favorite package manager.
* Windows: get a [static 32bit or 64bit binary](http://ffmpeg.zeranoe.com/builds/)
* Mac: get a [static 64bit binary](http://evermeet.cx/ffmpeg/)

For more details and download links, check the official FFmpeg website: [http://ffmpeg.org](http://ffmpeg.org/)

When you start a Processing sketch that uses this library you may be asked to indicate the location
of your FFmpeg executable.

## Frequent issues and questions

### What if I change the location of FFmpeg?

The library should notice and ask you for its location again. The location
information is saved in a json file which you can find in the library location.
If you delete this json file the library will ask you for the location of FFmpeg
and create the file again with the updated information.

### Sometimes the resulting mp4 video files are not working. Why?

mp4 files contain essential metadata at the end of the file.
The video export library saves this metadata when you call the
`endMovie()` method. If you don't call it, the movie may be
incomplete. The endMovie() method was added in version 0.1.5.

### I see an FFmpeg error related to "crf". Why?

This happens if your copy of FFmpeg does not include the h264 encoder.
Not all FFmpeg binaries are equal, some include more features than others.
Try downloading a different or more recent binary. Let me know if that
doesn't work.

### Odd widths and heights not allowed

The exported video is compressed using the h264 codec. This codec does not allow odd image sizes like 111 x 113. Some computer screens actually have odd sizes like 1920 x 1059. If you use fullScreen() with such a screen, exporting will fail with an error like `height not divisible by 2 (1920x1059)`. This will be fixed in future versions by auto correcting the requested size to an even number and notifying the user.

### How can I tweak the FFmpeg command the library runs?

The first time the library runs it will produce a settings.json file, which will
be placed in the library folder. This file can be carefully edited to adjust the
FFmpeg parameters used during the video creation. Why would you do this? FFmpeg
accepts hundreds of parametrs to define which codec to use, how the codec should
behave, etc. FFmpeg also includes hundreds of audio and video filters that can
be used and configured. It would not make sense for me to create hundreds of
methods in the video export library to let you access all those features. 
If you are and advanced user, you can tweak those settings yourself by editing
settings.json to change the codec settings or apply special effects to the 
resulting video files. See [this example](https://forum.processing.org/two/discussion/comment/95710/#Comment_95710).

### NoSuchMethodError when calling endMovie()

There seems to be a conflict between the Video Export library and the Video library. Both use different versions of the JNA library. See this discussion: https://github.com/hamoid/video_export_processing/issues/38

## change log

* 0.2.2 - February 11th, 2018
  * Re-add jna.jar and jna-platform.jar, fixing broken exported applications
    (issue [47](https://github.com/hamoid/video_export_processing/issues/47) ) 
    and the Windows version.
* 0.2.1 - November 8th, 2017
  * Add .setFfmpegPath() and fix [issue 41](https://github.com/hamoid/video_export_processing/issues/41)
* 0.2.0 - October 22nd, 2017
  * Minor change: when calling nf(), replace ',' with '.'. Some systems may use
    ',' due to localization, which fails later when parsing the csv file.
* 0.1.9 - April 22nd, 2017
  * https URL is now known by the IDE, so it's possible to install again without
    leaving the IDE.
  * Solve issue when attaching sound. In Ubuntu, the AAC codec inside FFmpeg is
    experimental, and had to be enabled in the command line.
  * Allow user customization of the FFmpeg commands that the library runs by including
    them inside settings.json, found in the library folder. This enables the user
    to tweak the commands to enable filters like blur, vignette, add noise,
    crop, etc. In future versions space characters should be allowed in those
    filter arguments.
* 0.1.8 - April 17th, 2017
  * Switch server url to https (attempt to solve broken installation inside the IDE)
* 0.1.7 - April 1st, 2017
  * Setting are now saved in the libray folder. Using the Java Preferences was
    giving errors on some Windows versions.
  * New example added to produce a video based on FFT data and making sure audio
    and video stay in sync.
  * In Windows, when ending a video, CTRL+C is sent to FFmpeg to terminate
    properly. This seems to fix corrupted videos on Windows.
  * The library now notices if FFmpeg is not found (maybe because it was moved)
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
