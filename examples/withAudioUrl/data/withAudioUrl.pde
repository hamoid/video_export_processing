import com.hamoid.*;

VideoExport videoExport;

// This sketch creates a movie with audio from a streaming internet
// radio station url.  It will run until the 'q' or 'x' keys are pressed.
// To find the url for a given station, look at the .m3u or .pls files
// on the site.

float movieFPS = 30;
boolean copyMode = false;
VideoExport videoExport = null;


void setup() {
  background(0);
  fill(255);
  frameRate(movieFPS);
  // video related setup
  videoExport = new VideoExport(this);
  // video and processing framerates do not have to exactly match,
  // since the audio seems to drive the frame creation -
  // but looks choppy if they are too different.
  videoExport.setFrameRate(movieFPS);
  videoExport.setQuality(70, 128);
  videoExport.setAudioUrl("http://ice.somafm.com/spacestation");
  videoExport.startMovie();
}

void draw() {
  // demo drawing
  spaceTravel();
  // save video frame
  videoExport.saveFrame();
}


void keyPressed() {
  if (key == 'q' || key == 'x') {
    noLoop();
    videoExport.endMovie();
    exit();
  }
}

void spaceTravel() {
  stroke(random(127), random(159), random(191), random(127));
  strokeWeight(random(2,10));
  ellipse(random(width), random(height), random(1,3),random(1,6));
  // used to use copy(1, 1, width-2, height-2, 0, 0, width, height);
  // processing 3 seemed to introduce artifacts, so this looks better
  if (copyMode) {
    copy(0, 1, width, height-2, 0, 0, width, height);
  }
  else {
    copy(1, 0, width-2, height, 0, 0, width, height);
  }
  copyMode = !copyMode;
}
