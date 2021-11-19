package videoExport

import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Wincon
import processing.core.PApplet
import processing.core.PConstants
import processing.data.JSONArray
import videoExport.Settings.ffmpegCrfQuality
import videoExport.Settings.ffmpegExecutable
import videoExport.Settings.ffmpegFrameRate
import videoExport.Settings.ffmpegHeight
import videoExport.Settings.ffmpegMetadataComment
import videoExport.Settings.ffmpegWidth
import videoExport.Settings.saveDebugInfo
import java.io.*
import java.util.regex.Pattern

class Ffmpeg(private val parent: PApplet, var outputFilePath: String) {
    private var process: Process? = null
    private var processBuilder: ProcessBuilder? = null
    private var ffmpegOutputLog: File? = null
    private var ffmpeg: OutputStream? = null

    fun start(encodeSettings: JSONArray) {
        val args = getProcessArguments(encodeSettings)
        processBuilder = ProcessBuilder(args)
        processBuilder!!.redirectErrorStream(true)
        ffmpegOutputLog = File(parent.sketchPath("ffmpeg.txt"))
        processBuilder!!.redirectOutput(ffmpegOutputLog)
        processBuilder!!.redirectInput(ProcessBuilder.Redirect.PIPE)
        try {
            process = processBuilder!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
            err()
        }
        ffmpeg = process!!.outputStream
    }

    val isStarted: Boolean
        get() = ffmpeg != null

    private fun getProcessArguments(cmd: JSONArray) = cmd.stringArray.map {
        if (it.contains("[")) {
            it.replace("[ffmpeg]", ffmpegExecutable)
                .replace("[width]", "" + ffmpegWidth)
                .replace("[height]", "" + ffmpegHeight)
                .replace("[fps]", "" + ffmpegFrameRate)
                .replace("[crf]", "" + ffmpegCrfQuality)
                .replace("[comment]", ffmpegMetadataComment)
                .replace("[output]", outputFilePath)
        } else {
            it
        }
    }

    /**
     * Called to end exporting a movie before exiting our program, or before
     * exporting a new movie.
     */
    fun endMovie() {
        if (ffmpeg != null) {
            try {
                ffmpeg!!.flush()
                ffmpeg!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            ffmpeg = null
        }
        if (process != null) {
            try {
                // To avoid creating corrupted video files on Windows
                // send the CTRL+C keys to ffmpeg to stop it.
                if (PApplet.platform == PConstants.WINDOWS) {
                    stopFfmpegOnWindows()
                } else {
                    // In Linux and Mac tell the process to end
                    process!!.destroy()
                }
                process!!.waitFor()
                if (!saveDebugInfo && ffmpegOutputLog!!.isFile) {
                    ffmpegOutputLog!!.delete()
                    ffmpegOutputLog = null
                }
                println("$outputFilePath saved.")
            } catch (e: InterruptedException) {
                println("Waiting for ffmpeg timed out!")
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            processBuilder = null
            process = null
        }
    }

    @Throws(IOException::class)
    private fun stopFfmpegOnWindows() {
        // Launch `tasklist`
        val ps = ProcessBuilder("tasklist")
        val pr = ps.start()

        // Get all processes from `tasklist`
        val allProcesses = BufferedReader(InputStreamReader(pr.inputStream))
        // Regex to find the word "ffmpeg.exe"
        val isFfmpeg = Pattern.compile("ffmpeg\\.exe.*?([0-9]+)")
        var processDetails: String
        // Iterate over all processes
        while (allProcesses.readLine().also { processDetails = it } != null) {
            val m = isFfmpeg.matcher(processDetails)
            // Check if this process is ffmpeg.exe
            if (m.find()) {
                // If it is, send it CTRL+C to stop it
                Kernel32.INSTANCE.GenerateConsoleCtrlEvent(
                    Wincon.CTRL_C_EVENT,
                    m.group(1).toInt()
                )
                break
            }
        }
    }

    fun write(bytes: ByteArray) {
        try {
            ffmpeg!!.write(bytes)
        } catch (e: IOException) {
            e.printStackTrace()
            err()
        }
    }

    private fun err() {
        System.err.println(
            "\nVideoExport error: Ffmpeg failed. Study $ffmpegOutputLog for more details."
        )
    }
}