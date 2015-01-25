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
