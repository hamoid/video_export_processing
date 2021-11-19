package videoExport

import processing.core.PApplet
import processing.core.PImage
import processing.data.JSONArray
import processing.data.JSONObject
import processing.data.StringList
import videoExport.Settings.ASK_FOR_FFMPEG_DIALOG_TEXT
import videoExport.Settings.ASK_FOR_FFMPEG_INPUT
import videoExport.Settings.CMD_ENCODE_AUDIO_DEFAULT
import videoExport.Settings.CMD_ENCODE_GRAY_DEFAULT
import videoExport.Settings.CMD_ENCODE_RGB_DEFAULT
import videoExport.Settings.FFMPEG_PATH_UNSET
import videoExport.Settings.SETTINGS_CMD_ENCODE_ALPHA
import videoExport.Settings.SETTINGS_CMD_ENCODE_AUDIO
import videoExport.Settings.SETTINGS_CMD_ENCODE_VIDEO
import videoExport.Settings.SETTINGS_FFMPEG_PATH
import videoExport.Settings.ffmpegAudioBitRate
import videoExport.Settings.ffmpegCrfQuality
import videoExport.Settings.ffmpegExecutable
import videoExport.Settings.ffmpegFrameRate
import videoExport.Settings.ffmpegMetadataComment
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import javax.swing.JOptionPane
import kotlin.system.exitProcess

// look at
// https://github.com/enkatsu/processing-library-template-gradle/blob/master/build.gradle
// https://discourse.processing.org/t/processing-kotlin-syphon-gradle/18927/4
// https://mvnrepository.com/search?q=org.processing&d=org.processing
// https://docs.gradle.org/current/samples/sample_building_kotlin_libraries.html#header

// https://imperceptiblethoughts.com/shadow/introduction/

// Current state: builds and works from inside intellij, but not from Processing
// 3 or 4.

// Solved with this
// https://discuss.kotlinlang.org/t/kotlin-1-4-21-where-is-kotlin-runtime-jar/20655
// for Processing 3, but it embeds the PDE, need to adjust deps

// look at /home/funpro/OR/openrndr-template/src/main/kotlin/apps/Subdivide.kt
// for using gson and a serializable data class. Maybe someday.

/*
// PDE example
import videoExport.*;
VideoExport v = new VideoExport();
void setup() {
    println(v.isAvailable());
    v.openDialog();
}
*/

/**
 * A Processing library using ffmpeg to easily export video files.
 *
 * @param parent         Parent PApplet, normally "this" when called from setup()
 * @param outputFileName The name of the video file to produce
 * @param img            PImage object to export as video (can be a PGraphics, Movie,
 * Capture...)
 */
class VideoExport @JvmOverloads constructor(
    private val parent: PApplet,
    outputFileName: String = "processing-movie.mp4",
    img: PImage = parent.g
) {
    val VERSION = "0.2.4"
    protected var pixelsByteRGB: ByteArray? = null
    protected var pixelsByteAlpha: ByteArray? = null
    protected var frameCount = 0
    protected var audioFilePath: String? = null
    protected var img: PImage? = null
    protected var ffmpegFound = false
    protected var ffmpegRGB: Ffmpeg
    protected var ffmpegAlpha: Ffmpeg
    protected lateinit var settings: JSONObject
    protected lateinit var settingsPath: String

    init {
        parent.registerMethod("dispose", this)

        setGraphics(img)

        val filePathRGB = parent.sketchPath(outputFileName)
        val filePathAlpha =
            filePathRGB.replace("(\\.\\w+)$".toRegex(), "-alpha$1")
        ffmpegRGB = Ffmpeg(parent, filePathRGB)
        ffmpegAlpha = Ffmpeg(parent, filePathAlpha)

        // The `Preferences` object does not work on Windows 10:
        // it requires fiddling with the registry to add a missing key.
        // Therefore, I decided to find the location of VideoExport.jar in the
        // disk, and create a settings file two folders above (which is the root
        // folder of this library)
        try {
            val thisJar = File(
                VideoExport::class.java.protectionDomain
                    .codeSource.location.toURI().path
            ).parentFile.parentFile

            settingsPath =
                "${thisJar.absolutePath}${File.separator}settings.json"

            val settingsFile = File(settingsPath)
            if (settingsFile.isFile) {
                println(settingsPath)
                settings = parent.loadJSONObject(settingsPath)

                // Update config files from v0.2.2
                val keysToUpdate = arrayOf(
                    SETTINGS_CMD_ENCODE_VIDEO,
                    SETTINGS_CMD_ENCODE_ALPHA,
                    SETTINGS_CMD_ENCODE_AUDIO
                )
                for (key in keysToUpdate) {
                    // If String, make it JSONArray
                    val o = settings.get(key)
                    if (o is String) {
                        settings.setJSONArray(key, toJSONArray(o))
                    }
                }
            } else {
                settings = JSONObject()
                settings.setJSONArray(
                    SETTINGS_CMD_ENCODE_VIDEO, CMD_ENCODE_RGB_DEFAULT
                )
                settings.setJSONArray(
                    SETTINGS_CMD_ENCODE_AUDIO, CMD_ENCODE_AUDIO_DEFAULT
                )
                settings.setJSONArray(
                    SETTINGS_CMD_ENCODE_ALPHA, CMD_ENCODE_GRAY_DEFAULT
                )
            }
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            System.err.println("Error loading settings.json")
        }
    }


    /**
     * Return the version of the library.
     *
     * @return String
     */
    fun version() = VERSION

    private fun toJSONArray(s: String) =
        JSONArray(StringList(s.split(" ").toTypedArray()))


    /**
     * Allow setting a new movie name, in case we want to export several movies,
     * one after the other.
     *
     * @param newMovieFileName String with file name of the new movie to create
     */
    fun setMovieFileName(newMovieFileName: String) {
        val filePathRGB = parent.sketchPath(newMovieFileName)
        val filePathAlpha =
            filePathRGB.replace("(\\.\\w+)$".toRegex(), "-alpha$1")
        ffmpegRGB.outputFilePath = filePathRGB
        ffmpegAlpha.outputFilePath = filePathAlpha
    }

    fun setAudioFileName(audioFileName: String) {
        audioFilePath = parent.dataPath(audioFileName)
    }

    /**
     * Set the PImage element. Advanced use only. Optional.
     *
     * @param img A PImage object. Probably used for off-screen exporting..
     */
    fun setGraphics(img: PImage) {
        this.img = img
        Settings.ffmpegWidth = img.pixelWidth
        Settings.ffmpegHeight = img.pixelHeight
    }


    /**
     * Set the quality of the produced video file. Optional.
     *
     * @param crf Video quality. A value between 0 (high compression) and 100
     * (high quality, lossless). Default is 70.
     * @param audioBitRate Audio quality (bit rate in kbps).
     * 128 is the default. 192 is very good.
     * More than 256 does not make sense.
     * Higher numbers produce heavier files.
     */
    fun setQuality(crf: Int, audioBitRate: Int) {
        ffmpegCrfQuality = (100 - crf.coerceIn(0, 100)) / 2
        ffmpegAudioBitRate = audioBitRate
    }

    /**
     * Set the frame rate of the produced video file. Optional.
     *
     * @param frameRate The frame rate at which the resulting video file should be
     * played. The default is 30, which is the recommended for online
     * video.
     */
    fun setFrameRate(frameRate: Float) {
        if (ffmpegRGB.isStarted) {
            System.err.println(
                "setFrameRate() has no effect after calling start()"
            )
        }
        ffmpegFrameRate = frameRate
    }


    /**
     * You can tell VideoExport not to call loadPixels() internally.
     * Use it only if you already call loadPixels() in your program.
     * Useful to avoid calling it twice, which might hurt the
     * performance a bit. Optional.
     *
     * @param doLoadPixels Set to false disables the internal loadPixels() call.
     */
    fun setLoadPixels(doLoadPixels: Boolean) {
        Settings.loadPixelsEnabled = doLoadPixels
    }

    /**
     * Advanced. Set to true if you want to save an accompanying grayscale
     * video with the alpha channel from a PGraphics. This is useful if you
     * want to layer the video on top of other videos in a video editing
     * software while a having transparent background in your Processing output.
     *
     * @param doSaveAlpha Specify if an alpha channel video is desired
     */
    fun setSaveAlphaVideo(doSaveAlpha: Boolean) {
        Settings.saveAlphaVideo = doSaveAlpha
    }

    /**
     * Call this method to specify if you want a debug text file saved
     * together with the video file. The text file normally contains the
     * output messages from ffmpeg, which may be useful for diagnosing
     * problems. If video is being exported correctly you may want to
     * call videoExport.setDebugging(false) to avoid creating unnecessary
     * files. Optional.
     *
     * @param saveDebugFile Set to false disables saving the ffmpeg output in a
     * text file
     */
    fun setDebugging(saveDebugFile: Boolean) {
        Settings.saveDebugInfo = saveDebugFile
    }


    /**
     * Advanced. You can use this if you want to change how ffmpeg behaves,
     * for example to add filters, custom encodings, or anything else you
     * could do with ffmpeg in the command line. See
     * https://forum.processing.org/two/discussion/22139/video-export-library-0-1-9
     * To find the default values find CMD_ENCODE_VIDEO_DEFAULT in
     * https://github.com/hamoid/video_export_processing/blob/master/src/com/hamoid/VideoExport.java
     *
     * @param newSettings An array with all the command line
     * arguments to call to produce a video.
     */
    fun setFfmpegVideoSettings(newSettings: Array<String?>?) {
        settings.setJSONArray(
            SETTINGS_CMD_ENCODE_VIDEO,
            JSONArray(StringList(newSettings))
        )
    }

    /**
     * Advanced. You can use this if you want to change how ffmpeg behaves,
     * for example to add filters, custom encodings, or anything else you
     * could do with ffmpeg in the command line. See
     * https://forum.processing.org/two/discussion/22139/video-export-library-0-1-9
     * To find the default values find CMD_ENCODE_AUDIO_DEFAULT in
     * https://github.com/hamoid/video_export_processing/blob/master/src/com/hamoid/VideoExport.java
     *
     * @param newSettings An array with all the command line
     * arguments to call to produce a video.
     */
    fun setFfmpegAudioSettings(newSettings: Array<String?>?) {
        settings.setJSONArray(
            SETTINGS_CMD_ENCODE_AUDIO,
            JSONArray(StringList(newSettings))
        )
    }


    /**
     * Adds one frame to the video file. The frame will be the content of the
     * display, or the content of a PImage if you specified one in the
     * constructor.
     */
    fun saveFrame() {
        if (img != null && img!!.width > 0) {
            if (!ffmpegFound) {
                return
            }
            if (pixelsByteRGB == null) {
                pixelsByteRGB =
                    ByteArray(img!!.pixelWidth * img!!.pixelHeight * 3)
                if (Settings.saveAlphaVideo) {
                    pixelsByteAlpha = ByteArray(pixelsByteRGB!!.size / 3)
                }
            }
            if (Settings.loadPixelsEnabled) {
                img!!.loadPixels()
            }
            run {
                var byteNum = 0
                for (px in img!!.pixels) {
                    pixelsByteRGB!![byteNum++] = (px shr 16).toByte()
                    pixelsByteRGB!![byteNum++] = (px shr 8).toByte()
                    pixelsByteRGB!![byteNum++] = px.toByte()
                }
                ffmpegRGB.write(pixelsByteRGB!!)
            }
            if (Settings.saveAlphaVideo) {
                var byteNum = 0
                for (px in img!!.pixels) {
                    pixelsByteAlpha!![byteNum++] = (px shr 24).toByte()
                }
                ffmpegAlpha.write(pixelsByteAlpha!!)
            }
            frameCount++
        }
    }


    /**
     * Make sure ffmpeg is found, then create a process
     * to run it.
     */
    protected fun initialize() {
        // Get the saved ffmpeg path from the settings file
        // which maybe does not exist.
        ffmpegExecutable = getFfmpegPath()
        // If it did not exist, try to guess where it is
        if (ffmpegExecutable == FFMPEG_PATH_UNSET) {
            val guessPaths = arrayOf(
                "/usr/local/bin/ffmpeg",
                "/usr/bin/ffmpeg"
            )
            for (guess_path in guessPaths) {
                if (File(guess_path).isFile) {
                    ffmpegExecutable = guess_path
                    settings.setString(SETTINGS_FFMPEG_PATH, ffmpegExecutable)
                    parent.saveJSONObject(settings, settingsPath)
                    break
                }
            }
        } else {
            // If it did exist in the settings file,
            // check if the path is still valid
            // (maybe the user moved ffmpeg to a different folder)
            val ffmpegFile = File(ffmpegExecutable)
            if (!ffmpegFile.isFile) {
                ffmpegExecutable = FFMPEG_PATH_UNSET
            }
        }
        // If it was not set, or if it was moved, ask the user where
        // to find ffmpeg. We will try to start after the user makes
        // a decision and onFfmpegSelected() is called.
        if (ffmpegExecutable == FFMPEG_PATH_UNSET) {
            JOptionPane.showMessageDialog(null, ASK_FOR_FFMPEG_DIALOG_TEXT)

            // Show "select file" dialog
            parent.selectInput(
                ASK_FOR_FFMPEG_INPUT,
                "onFfmpegSelected", File("/"), this
            )
        } else {
            // If it was found, all good. Start.
            startFfmpeg()
        }
    }


    /**
     * Call this function to figure out how many frames your movie has so far.
     *
     * @return the number of frames added to the movie so far
     */
    fun getCurrentFrame() = frameCount

    /**
     * You could use the returned value to display a time counter, a progress
     * bar or to create periodic motion, for instance by feeding
     * the returned value into the sin() function, and using the result to drive
     * the position of an object.
     *
     * @return the duration of the movie (so far) in seconds
     */
    fun getCurrentTime() = frameCount / ffmpegFrameRate

    /**
     * Call this if you need to figure out the path to the ffmpeg program
     * (advanced).
     *
     * @return the path to the ffmpeg program as a String
     */
    fun getFfmpegPath(): String =
        settings.getString(SETTINGS_FFMPEG_PATH, FFMPEG_PATH_UNSET)

    /**
     * Call this method if you know the path to ffmpeg on your computer
     * (advanced).
     *
     * @param path Specify the location of the ffmpeg executable
     */
    fun setFfmpegPath(path: String?) {
        settings.setString(SETTINGS_FFMPEG_PATH, path)
        parent.saveJSONObject(settings, settingsPath)
    }

    /**
     * Makes the library forget about where the ffmpeg binary was located.
     * Useful if you moved ffmpeg to a different location. After calling this
     * function the library will ask you again for the location of ffmpeg.
     * Optional.
     */
    fun forgetFfmpegPath() {
        settings.setString(SETTINGS_FFMPEG_PATH, FFMPEG_PATH_UNSET)
        parent.saveJSONObject(settings, settingsPath)
    }


    /**
     * Called internally by the file selector when the user chooses
     * the location of ffmpeg on the disk.
     *
     * @param selection (internal)
     */
    fun onFfmpegSelected(selection: File?) {
        if (selection == null) {
            System.err.println(
                """
                The VideoExport library requires ffmpeg but it was not found. 
                Please try again or read the library documentation.
                """
            )
        } else {
            ffmpegExecutable = selection.absolutePath
            println("ffmpeg selected at $ffmpegExecutable")
            settings.setString(SETTINGS_FFMPEG_PATH, ffmpegExecutable)
            parent.saveJSONObject(settings, settingsPath)
            startFfmpeg()
        }
    }

    // https://trac.ffmpeg.org/wiki/Encode/H.264#Compatibility
    protected fun startFfmpeg() {
        if (img!!.pixelWidth == 0 || img!!.pixelHeight == 0) {
            err("The export image size is 0!")
        }
        if (img!!.pixelWidth % 2 == 1 || img!!.pixelHeight % 2 == 1) {
            err(
                """
                Width and height can only be even numbers when using the h264 encoder
                but the requested image size is ${img!!.pixelWidth}x${img!!.pixelHeight}
                """.trimIndent()
            )
        }
        var encodeSettings: JSONArray?
        encodeSettings = try {
            settings.getJSONArray(SETTINGS_CMD_ENCODE_VIDEO)
        } catch (e: RuntimeException) {
            CMD_ENCODE_RGB_DEFAULT
        }
        ffmpegRGB.start(encodeSettings!!)
        if (Settings.saveAlphaVideo) {
            encodeSettings = try {
                settings.getJSONArray(SETTINGS_CMD_ENCODE_ALPHA)
            } catch (e: RuntimeException) {
                CMD_ENCODE_GRAY_DEFAULT
            }
            ffmpegAlpha.start(encodeSettings!!)
        }
        ffmpegFound = true
        frameCount = 0
    }

    fun startMovie() = initialize()


    /**
     * Called automatically by Processing to clean up before shut down
     */
    fun dispose() {
        ffmpegRGB.endMovie()
        if (audioFilePath != null && audioFilePath!!.isNotEmpty()) {
            attachSound()
            audioFilePath = null
        }
        if (ffmpegAlpha != null) {
            ffmpegAlpha.endMovie()
        }
    }

    protected fun attachSound() {
        // Add sound to outputFilePath (crop at shortest)
        // ffmpeg -i video.avi -i audio.mp3 -codec copy -shortest output.avi

        // if sound is shorter, pad with silence
        // ffmpeg -i videofile.mp4 -i audiofile.wav -filter_complex " [1:0] apad
        // " -shortest output.mp4

        // Fade in/out. Works with ffmpeg 2.5.2.
        // Fade in and fade out both for the duration of 3 seconds.
        // ffmpeg -i audio.mp3 -af 'afade=t=in:ss=0:d=3,afade=t=out:st=27:d=3'
        // out.mp3

        // Specify length in frames (-t for seconds)
        // If source audio is mp3 or aac, use acodec copy
        // ffmpeg -i videofile.mp4 -i audiofile.wav -vframes 260 -vcodec copy
        // -acodec copy output.mp4

        // Check if the sound file exists and is a regular file (not a
        // directory)
        val audioPath = File(audioFilePath!!)
        if (!audioPath.exists() || !audioPath.isFile) {
            System.err.println(
                "The file $audioFilePath was not found or is not a regular file."
            )
            return
        }

        // Get command as JSONArray
        val cmd = try {
            settings.getJSONArray(SETTINGS_CMD_ENCODE_AUDIO)
        } catch (e: RuntimeException) {
            CMD_ENCODE_AUDIO_DEFAULT
        }
        val tmpAudioFile = "temp-with-audio.mp4"
        val cmdArgs = cmd.stringArray.map {
            if (it.contains("[")) {
                it.replace("[ffmpeg]", ffmpegExecutable)
                    .replace("[inputvideo]", ffmpegRGB.outputFilePath)
                    .replace("[inputaudio]", audioFilePath!!)
                    .replace("[bitrate]", ffmpegAudioBitRate.toString())
                    .replace("[comment]", ffmpegMetadataComment)
                    .replace(
                        "[output]", parent.sketchFile(tmpAudioFile).absolutePath
                    )
            } else {
                it
            }
        }

        val processBuilder = ProcessBuilder(cmdArgs)
        processBuilder.redirectErrorStream(true)
        val ffmpegOutputLogAudio = File(
            parent.sketchPath("ffmpeg-audio.txt")
        )
        processBuilder.redirectOutput(ffmpegOutputLogAudio)
        var process: Process? = null
        try {
            process = processBuilder.start()
        } catch (e: IOException) {
            e.printStackTrace()
            err(ffmpegOutputLogAudio)
        }
        if (process != null) {
            try {
                // wait until done
                process.waitFor()
                val deleted: Boolean = File(ffmpegRGB.outputFilePath).delete()
                val renamed = parent.sketchFile(tmpAudioFile)
                    .renameTo(File(ffmpegRGB.outputFilePath))
            } catch (e: InterruptedException) {
                PApplet.println(
                    "Waiting for ffmpeg while adding audio timed out!"
                )
                e.printStackTrace()
            }
        }
        if (!Settings.saveDebugInfo && ffmpegOutputLogAudio.isFile) {
            val deleted = ffmpegOutputLogAudio.delete()
        }
    }

    protected fun err(msg: String) {
        System.err.println("\nVideoExport error: $msg\n")
        exitProcess(1)
    }

    protected fun err(f: File) {
        System.err.println(
            "\nVideoExport error: Ffmpeg failed. Study $f for more details."
        )
    }

    fun openDialog() {
        val r = JOptionPane.showInputDialog(
            null, "What?", "Hmm..", JOptionPane.QUESTION_MESSAGE
        )
        println("Text entered:")
        println(r)
    }
}
