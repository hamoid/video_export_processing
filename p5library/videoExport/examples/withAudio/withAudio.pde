import com.hamoid.*;

VideoExport videoExport;

// This sketch ends automatically.
// Run and wait for 10 seconds.

float movieFPS = 30;
float soundDuration = 10.03; // in seconds

void setup() {
  size(600, 600);

  videoExport = new VideoExport(this);
  videoExport.setFrameRate(movieFPS);
  videoExport.setAudioFileName("test-sound.mp3");
  videoExport.startMovie();  
}
void draw() {
  background(#888888);
  rect(frameCount * frameCount % width, 0, 40, height);

  videoExport.saveFrame();
  
  // End when we have exported enough frames 
  // to match the sound duration.
  if(frameCount > round(movieFPS * soundDuration)) {
    videoExport.endMovie();
    exit();
  }  
}

/*
   Note: if you want to visualize sound and want to
   match the sound precisely see the "withAudioViz"
   example.
*/
