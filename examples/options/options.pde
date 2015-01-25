import com.hamoid.*;

VideoExport videoExport;

void setup() {
  size(600, 600);
  noStroke();
  
  videoExport = new VideoExport(this, "options.mp4");
  
  // Set quality to 100 (best) for lossless video (high quality, big size)
  // Set it to 0 (worst) if you enjoy poor quality videos :)
  // 70 might be a good balance.
  videoExport.setQuality(70);
  
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
}
void draw() {
  loadPixels();
  for(int i=0; i<pixels.length; i++) {
    pixels[i] = (int)random(-99999, -33333);
  }
  videoExport.saveFrame();
  updatePixels();
}
