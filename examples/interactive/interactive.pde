import com.hamoid.*;

/*
  This sketch shows how you can record different takes.
  
  This can be useful when you have randomized generative
  graphics and you want to save only the "good parts".
  
  For instance, try to record the ball only moving down.
  (press R to start when the ball is high, press R again
  to stop when the ball is low).
  
  Do this multiple times to get a video in which the
  ball only goes down.
*/

VideoExport videoExport;
boolean recording = false;

void setup() {
  size(600, 600);
  noStroke();
  frameRate(30);
  
  println("Press R to toggle recording");
  
  videoExport = new VideoExport(this, "interactive.mp4");
}
void draw() {
  background(0);
  float t = frameCount * 0.03;
  float sz = 100 + 50 * cos(t*1.33) * cos(t*1.84);
  ellipse(300 + 200 * cos(t*1.13) * cos(t*0.21),
          300 + 200 * cos(t*1.71) * cos(t*0.47),
          sz, sz);
  
  if(recording) {
    videoExport.saveFrame();
  }
}

void keyPressed() {
  if(key == 'r' || key == 'R') {
    recording = !recording;
    println("Recording is " + (recording ? "ON" : "OFF"));
  }
}
