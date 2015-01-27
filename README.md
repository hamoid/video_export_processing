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


