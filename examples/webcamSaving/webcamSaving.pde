import processing.video.*;
import com.hamoid.*;

Capture cam;
VideoExport videoExport;

void setup() {
  size(600, 600);

  String[] cameras = Capture.list();
  // Show the camera IDs in the console
  printArray(cameras);

  // 1. Run the program once.
  // 2. On the console, note the camera ID you want to use.
  // 3. Enter the ID on the next line inside the square brackets.
  // 4. Run the program again.
  cam = new Capture(this, cameras[2]);
  cam.start();
}
void draw() {
  if (cam.available()) {
    cam.read();
  }

  // If videoExport has been initialized, we can use it.
  if (videoExport != null) {
    background(#224488);
    text("recording camera input", 100, 100);
    // Here we don't save what we see on the display,
    // but the webcam input.
    videoExport.saveFrame();
  } else {
    // videoExport was not initialized. Try doing that now.
    initVideoExport();
  }
}
void initVideoExport() {
  // Make sure the webcam is giving us the right width.
  // It may return 0 right after starting the program.
  if (cam.width > 0) {
    videoExport = new VideoExport(this, "basic.mp4", cam);
  }
}