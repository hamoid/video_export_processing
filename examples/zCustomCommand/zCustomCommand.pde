import com.hamoid.*;

VideoExport videoExport;

// This advanced example shows how to customize the ffmpeg command that gets
// executed by this library.
// This allows you to access the full power of ffmpeg and select custom
// codecs, apply audio or video filters and even do video streaming!

void setup() {
  size(600, 600);

  videoExport = new VideoExport(this);

  // Everything as by default except -vf (video filter)
  videoExport.setFfmpegVideoSettings(
    new String[]{
    "[ffmpeg]",                       // ffmpeg executable
    "-y",                             // overwrite old file
    "-f",        "rawvideo",          // format rgb raw
    "-vcodec",   "rawvideo",          // in codec rgb raw
    "-s",        "[width]x[height]",  // size
    "-pix_fmt",  "rgb24",             // pix format rgb24
    "-r",        "[fps]",             // frame rate
    "-i",        "-",                 // pipe input

                                      // video filter with vignette, blur,
                                      // noise and text. font commented out
    "-vf", "vignette,gblur=sigma=1,noise=alls=10:allf=t+u," +
    "drawtext=text='Made with Processing':x=50:y=(h-text_h-50):fontsize=24:fontcolor=white@0.8",
    // drawtext=fontfile=/path/to/a/font/myfont.ttf:text='Made...

    "-an",                            // no audio
    "-vcodec",   "h264",              // out codec h264
    "-pix_fmt",  "yuv420p",           // color space yuv420p
    "-crf",      "[crf]",             // quality
    "-metadata", "comment=[comment]", // comment
    "[output]"                        // output file
    });

  // Everything as by default. Unused: no audio in this example.
  videoExport.setFfmpegAudioSettings(new String[]{
    "[ffmpeg]",                       // ffmpeg executable
    "-y",                             // overwrite old file
    "-i",        "[inputvideo]",      // video file path
    "-i",        "[inputaudio]",      // audio file path
    "-filter_complex", "[1:0]apad",   // pad with silence
    "-shortest",                      // match shortest file
    "-vcodec",   "copy",              // don't reencode vid
    "-acodec",   "aac",               // aac audio encoding
    "-b:a",      "[bitrate]k",        // bit rate (quality)
    "-metadata", "comment=[comment]", // comment
    // https://stackoverflow.com/questions/28586397/ffmpeg-error-while-re-encoding-video#28587897
    "-strict",   "-2",                // enable aac
    "[output]"                        // output file
    });

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
