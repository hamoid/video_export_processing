import com.hamoid.*;
import ddf.minim.*;
import ddf.minim.analysis.*;
import ddf.minim.spi.*;

VideoExport videoExport;

String audioFilePath = "jingle.mp3";

String SEP = "|";
float movieFPS = 30;
float frameDuration = 1 / movieFPS;
BufferedReader reader;

/*
   Example to visualize sound frequencies from
   an audio file.
    
   Producing a file with audio and video in sync
   is tricky. It gets easily out of sync.
    
   One approach, used in this example, is:
   
   Pass 1. Analyze the sound in a Processing sketch 
           and output a text file including the FFT 
           analysis data.
   Pass 2. Load the data from pass 1 and use it to 
           output frames for a video file, including 
           the right frames to match the sound 
           precisely at any given time.
            
   Using this technique it does not matter how fast
   or slow your second program is, and you know that
   no frames will be dropped (as may happen when
   recording live).
   
   The difficulty of recording live graphics with
   sound is that the frame rate is not always stable.
   We may request 60 frames per second, but once in
   a while a frame is not ready on time. So the
   "speed of frames" (the frameRate) is not constant
   while frames are produced, but they are probably
   constant when played back. The "speed of audio",
   on the other hand, is often constant. If audio
   is constant but video is not, they get out of 
   sync.
*/

void setup() {
  size(600, 600);

  // Produce the video as fast as possible
  frameRate(1000);

  // Read a sound file and output a txt file
  // with the FFT analysis.
  // It uses Minim, because the standard
  // Sound library did not work in my system.

  // You could comment out the next line once you
  // have produced the txt file to speed up
  // experimentation. Otherwise every time you
  // run this program it re-generates the FFT
  // analysis.
  audioToTextFile(audioFilePath);

  // Now open the text file we just created for reading
  reader = createReader(audioFilePath + ".txt");

  // Set up the video exporting
  videoExport = new VideoExport(this);
  videoExport.setFrameRate(movieFPS);
  videoExport.setAudioFileName(audioFilePath);
  videoExport.startMovie();
}
void draw() {
  String line;
  try {
    line = reader.readLine();
  }
  catch (IOException e) {
    e.printStackTrace();
    line = null;
  }
  if (line == null) {
    // Done reading the file.
    // Close the video file.
    videoExport.endMovie();
    exit();
  } else {
    String[] p = split(line, SEP);
    // The first column indicates 
    // the sound time in seconds.
    float soundTime = float(p[0]);

    // Our movie will have 30 frames per second.
    // Our FFT analysis probably produces 
    // 43 rows per second (44100 / fftSize) or 
    // 46.875 rows per second (48000 / fftSize).
    // We have two different data rates: 30fps vs 43rps.
    // How to deal with that? We render frames as
    // long as the movie time is less than the latest
    // data (sound) time. 
    // I added an offset of half frame duration, 
    // but I'm not sure if it's useful nor what 
    // would be the ideal value. Please experiment :)
    while (videoExport.getCurrentTime() < soundTime + frameDuration * 0.5) {
      background(0);
      noStroke();
      // Iterate over all our data points (different
      // audio frequencies. First bass, then hihats)
      for (int i=1; i<p.length; i++) {
        float value = float(p[i]);
        // do something with value (set positions,
        // sizes, colors, angles, etc)
        pushMatrix();
        translate(width/2, height/2);
        if(i%2 == 1) {
          // Left channel value
          fill(255, 50, 20);
          rotate(i * 0.05);
          translate(50, 0);
          rect(value * 5, -5, value * 4, 10);
        } else {
          // Right channel value
          fill(20, 100, 250);
          rotate(-i * 0.05);
          translate(50, 0);
          rect(value * 5, -5, value * 4, 10);
        }
        popMatrix();
      }
      videoExport.saveFrame();
    }
  }
}

// Minim based audio FFT to data text file conversion.
// Non real-time, so you don't wait 5 minutes for a 5 minute song :)
// You can look at the produced txt file in the data folder
// after running this program to see how it looks like.
void audioToTextFile(String fileName) {
  PrintWriter output;

  Minim minim = new Minim(this);
  output = createWriter(dataPath(fileName + ".txt"));

  AudioSample track = minim.loadSample(fileName, 2048);

  int fftSize = 1024;
  float sampleRate = track.sampleRate();

  float[] fftSamplesL = new float[fftSize];
  float[] fftSamplesR = new float[fftSize];

  float[] samplesL = track.getChannel(AudioSample.LEFT);
  float[] samplesR = track.getChannel(AudioSample.RIGHT);  

  FFT fftL = new FFT(fftSize, sampleRate);
  FFT fftR = new FFT(fftSize, sampleRate);

  fftL.logAverages(22, 3);
  fftR.logAverages(22, 3);

  int totalChunks = (samplesL.length / fftSize) + 1;
  int fftSlices = fftL.avgSize();

  for (int ci = 0; ci < totalChunks; ++ci) {
    int chunkStartIndex = ci * fftSize;   
    int chunkSize = min( samplesL.length - chunkStartIndex, fftSize );

    System.arraycopy( samplesL, chunkStartIndex, fftSamplesL, 0, chunkSize);      
    System.arraycopy( samplesR, chunkStartIndex, fftSamplesR, 0, chunkSize);      
    if ( chunkSize < fftSize ) {
      java.util.Arrays.fill( fftSamplesL, chunkSize, fftSamplesL.length - 1, 0.0 );
      java.util.Arrays.fill( fftSamplesR, chunkSize, fftSamplesR.length - 1, 0.0 );
    }

    fftL.forward( fftSamplesL );
    fftR.forward( fftSamplesL );

    // The format of the saved txt file.
    // The file contains many rows. Each row looks like this:
    // T|L|R|L|R|L|R|... etc
    // where T is the time in seconds
    // Then we alternate left and right channel FFT values
    // The first L and R values in each row are low frequencies (bass)
    // and they go towards high frequency as we advance towards
    // the end of the line.
    StringBuilder msg = new StringBuilder(nf(chunkStartIndex/sampleRate, 0, 3));
    for (int i=0; i<fftSlices; ++i) {
      msg.append(SEP + nf(fftL.getAvg(i), 0, 4));
      msg.append(SEP + nf(fftR.getAvg(i), 0, 4));
    }
    output.println(msg.toString());
  }
  track.close();
  output.flush();
  output.close();
  println("Sound analysis done");
}