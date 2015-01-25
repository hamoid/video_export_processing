import com.hamoid.*;

VideoExport videoExport;

void setup() {
  size(600, 600);
  noStroke();
  
  videoExport = new VideoExport(this, "internetVideo.mp4");

  // The standard frame rate for internet videos is about 
  // 30 frames per second 
  videoExport.setFrameRate(30);
  
  // By default Processing tries to play at 60 frames
  // per second. That means that your exported videos
  // may feel slow, as they are played at half the
  // speed of the original. You may want to play your
  // sketch at 30 fps too, so the sketch and the video
  // run at similar frame rates. To compensate for the 
  // lower frame rate you may have to adjust your sketch
  // to make your objects move faster.
  frameRate(30);   
}
void draw() {
  background(33);
  textSize(50);
  text(frameCount, 50, 50);
  videoExport.saveFrame();
}
