import com.hamoid.*;

/*
  This sketch shows how you can output multiple movies.
 
 To create a movie, hold down the mouse button for a few seconds.
 
 Do this multiple times to export multiple movies.
 */

VideoExport videoExport;
boolean recording = false;
color bgcolor = #FFAA22;

void setup() {
  size(600, 600);
  noStroke();
  frameRate(30);

  videoExport = new VideoExport(this);
}
void draw() {
  background(bgcolor);
  
  // draw something
  float t = frameCount * 0.01;
  for(float a=0; a<TAU; a+=0.1) {
    pushMatrix();
    translate(width/2, height/2);
    rotate(a);
    fill(30);
    blendMode(ADD);
    rectMode(CENTER);
    rect(0, 0, noise(t, a, noise(t+0.3))*800, 20);
    popMatrix();
  }

  if (recording) {
    videoExport.saveFrame();
  }
}

void mousePressed() {
  recording = true;
  videoExport.setMovieFileName(frameCount + ".mp4");
  bgcolor = color(random(255), random(255), random(255));
  println("Start movie.");
}
void mouseReleased() {
  recording = false;
  videoExport.endMovie();
  println("End movie.");
}