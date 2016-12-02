import com.hamoid.*;

VideoExport videoExport;

void setup() {
  size(600, 600);
  noStroke();
  
  videoExport = new VideoExport(this, "options.mp4");
  
  // Set video and audio quality.
  // Video quality: 100 is best, lossless video (but big file size)
  //   Set it to 0 (worst) if you enjoy poor quality videos :)
  //   70 is the default.
  // Audio quality: 128 is the default, 192 very good,
  //   256 is near lossless but big file size.
  videoExport.setQuality(70, 128);
  
  // This sets the frame rate of the resulting video file. I has nothing to do
  // with the current Processing frame rate. For instance you could have a 
  // Processing sketch that does heavy computation and renders only one frame 
  // every 5 seconds, but here you could still set that the resulting video should 
  // play at 30 frames per second.  
  videoExport.setFrameRate(10);  
  
  // If your sketch already calls loadPixels(), you can tell videoExport to 
  // not do that again. It's not necessary, but your sketch may perform a 
  // bit better if you avoid calling it twice.
  videoExport.dontCallLoadPixels();
  
  // If video is being exported correctly, you can call this function to avoid
  // creating .txt files containing debug information.
  videoExport.dontSaveDebugInfo();

  // Use the next line once if you have change the
  // location of the ffmpeg tool. This will make
  // the library ask for it's location again.
  //videoExport.forgetFfmpegPath();

  // Start exporting after adjusting the settings.
  videoExport.startMovie();
}
void draw() {
  loadPixels();
  for(int i=0; i<pixels.length; i++) {
    pixels[i] = (int)random(-99999, -33333);
  }
  videoExport.saveFrame();
  updatePixels();
}
void keyPressed() {
  if (key == 'q') {
    videoExport.endMovie();
    exit();
  }
}