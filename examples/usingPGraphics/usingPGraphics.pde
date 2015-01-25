import com.hamoid.*;

VideoExport videoExport;
PGraphics pg;

void setup() {
  size(600, 600);
  pg = createGraphics(640, 480);
  
  videoExport = new VideoExport(this, "pgraphics.mp4", pg);
}
void draw() {
  background(0);
  text("exporting video " + frameCount, 50, 50);
  
  pg.beginDraw();
  pg.background(#224488);
  pg.rect(pg.width * noise(frameCount * 0.01), 0, 40, pg.height);
  pg.endDraw();

  videoExport.saveFrame();
}
