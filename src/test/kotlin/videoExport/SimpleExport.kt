package videoExport

import processing.core.PApplet
import processing.core.PGraphics

fun main() = PApplet.main(SimpleExport::class.java.name)

class SimpleExport : PApplet() {
    lateinit var videoExport : VideoExport
    lateinit var pg : PGraphics
    override fun settings() {
        size(600, 600)
    }

    override fun setup() {
        pg = createGraphics(600, 600)
        videoExport = VideoExport(this, "movie.mp4", pg)
        videoExport.setSaveAlphaVideo(true)
        videoExport.startMovie()
    }

    override fun draw() {
        pg.beginDraw()
        pg.clear()
        pg.translate(300f, 300f)
        pg.noStroke()
        pg.fill(200f, 100f, 0f)
        pg.rotate(frameCount * 0.01f)
        pg.rect(0f, 0f, 200f, 50f)
        pg.endDraw()

        background(255)
        image(pg, 0f, 0f)

        videoExport.saveFrame();
    }

    override fun keyPressed() {
        if(key == 'q') {
            videoExport.dispose()
            exit()
        }
    }
}