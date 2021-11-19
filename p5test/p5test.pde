import videoExport.*;

VideoExport ve;
PGraphics pg;

void settings() {
  size(600, 600);
}

void setup() {
  pg = createGraphics(600, 600);
  ve = new VideoExport(this, "movie.mp4", pg);
  ve.setSaveAlphaVideo(true);
  ve.startMovie();
}

void draw() {
  pg.beginDraw();
  pg.clear();
  pg.noStroke();
  pg.translate(300, 300);
  pg.rotate(frameCount * 0.01);
  pg.fill(200, 100, 30);
  pg.rect(0, 0, 200, 50);
  pg.endDraw();
  ve.saveFrame();
}

void keyPressed() {
  if (key == 'q') {
    ve.dispose();
    exit();
  }
}
