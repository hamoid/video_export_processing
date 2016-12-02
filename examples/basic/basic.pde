import com.hamoid.*;

VideoExport videoExport;

// Press 'q' to finish saving the movie and exit.

// In some systems, if you close your sketch by pressing ESC, 
// by closing the window, or by pressing STOP, the resulting 
// movie might be corrupted. If that happens to you, use
// videoExport.endMovie() like you see in this example.

// In some systems pressing ESC produces correct movies
// and .endMovie() is not necessary.

void setup() {
  size(600, 600);

  videoExport = new VideoExport(this);
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