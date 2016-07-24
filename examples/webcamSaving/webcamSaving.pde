import processing.video.*;
import com.hamoid.*;

Capture cam;
VideoExport videoExport;

int FPS = 30;

void setup() {
  size(600, 600);
  
  // Important: set the output-framerate (sketch) 
  // equal to the input-framerate (webcam).
  // By default Processing run at 60 fps. If the webcam runs at
  // 30 fps, we save each frame twice and the result plays back 
  // at half the speed.
  frameRate(FPS);

  cam = new Capture(this, 640, 480, FPS);
  cam.start();
  videoExport = new VideoExport(this, "camera.mp4", cam);
}
void draw() {
  if (cam.available()) {
    cam.read();
  }

  background(#224488);
  text("recording camera input", 100, 100);
  // Here we don't save what we see on the display,
  // but the webcam input.
  videoExport.saveFrame();
}