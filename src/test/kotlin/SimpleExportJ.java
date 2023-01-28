import processing.core.PApplet;
import com.hamoid.VideoExport;

public class SimpleExportJ extends PApplet {
    VideoExport videoExport;

    public static void main(String[] args) {
        PApplet.main(new String[]{SimpleExportJ.class.getName()});
    }

    @Override
    public void settings() {
        size(600, 600);
    }

    @Override
    public void setup() {
        videoExport = new VideoExport(this);
        videoExport.startMovie();
    }

    @Override
    public void draw() {
        background(200f, 0f, 200f);
        rect(frameCount * frameCount % width, 0, 40, height);

        videoExport.saveFrame();
    }

    @Override
    public void keyPressed() {
        if (key == 'q') {
            videoExport.endMovie();
            exit();
        }
    }
}