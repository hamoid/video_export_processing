package videoExport

import processing.data.JSONArray
import processing.data.StringList

object Settings {
    const val SETTINGS_FFMPEG_PATH = "ffmpeg_path"
    const val SETTINGS_CMD_ENCODE_VIDEO = "encode_video"
    const val SETTINGS_CMD_ENCODE_ALPHA = "encode_alpha"
    const val SETTINGS_CMD_ENCODE_AUDIO = "encode_audio"
    const val FFMPEG_PATH_UNSET = "ffmpeg_path_unset"

    const val ffmpegMetadataComment = "Made with " +
            "Video Export for Processing - https://git.io/vAXLk"

    val CMD_ENCODE_AUDIO_DEFAULT = JSONArray(
        StringList(
            arrayOf(
                "[ffmpeg]",  // ffmpeg executable
                "-y",  // overwrite old file
                "-i", "[inputvideo]",  // video file path
                "-i", "[inputaudio]",  // audio file path
                "-filter_complex", "[1:0]apad",  // pad with silence
                "-shortest",  // match shortest file
                "-vcodec", "copy",  // don't reencode vid
                "-acodec", "aac",  // aac audio encoding
                "-b:a", "[bitrate]k",  // bit rate (quality)
                "-metadata", "comment=[comment]",  // comment
                // https://stackoverflow.com/questions/28586397/ffmpeg-error-while-re-encoding-video#28587897
                "-strict", "-2",  // enable aac
                "[output]" // output file
            )
        )
    )
    val CMD_ENCODE_RGB_DEFAULT = JSONArray(
        StringList(
            arrayOf(
                "[ffmpeg]",  // ffmpeg executable
                "-y",  // overwrite old file
                "-f", "rawvideo",  // format rgb raw
                "-vcodec", "rawvideo",  // in codec rgb raw
                "-s", "[width]x[height]",  // size
                "-pix_fmt", "rgb24",  // pix format rgb24
                "-r", "[fps]",  // frame rate
                "-i", "-",  // pipe input
                "-an",  // no audio
                "-vcodec", "h264",  // out codec h264
                "-pix_fmt", "yuv420p",  // color space yuv420p
                "-crf", "[crf]",  // quality
                "-metadata", "comment=[comment]",  // comment
                "[output]" // output file
            )
        )
    )
    val CMD_ENCODE_GRAY_DEFAULT = JSONArray(
        StringList(
            arrayOf(
                "[ffmpeg]",  // ffmpeg executable
                "-y",  // overwrite old file
                "-f", "rawvideo",  // format rgb raw
                "-vcodec", "rawvideo",  // in codec rgb raw
                "-s", "[width]x[height]",  // size
                "-pix_fmt", "gray",  // pix format rgb24
                "-r", "[fps]",  // frame rate
                "-i", "-",  // pipe input
                "-an",  // no audio
                "-vcodec", "h264",  // out codec h264
                "-pix_fmt", "yuv420p",  // color space yuv420p
                "-crf", "[crf]",  // quality
                "-metadata", "comment=[comment]",  // comment
                "[output]" // output file
            )
        )
    )
    val ASK_FOR_FFMPEG_DIALOG_TEXT = """
        The VideoExport library requires ffmpeg,
        a free command line tool.
        
        If you don't have ffmpeg yet:
        
        -- Windows / Mac --
        1. Download a static build from http://ffmpeg.org
        2. Unzip it
        
        -- Linux --
        1. Install ffmpeg using your package manager
        
        -- After installing ffmpeg --
        Click OK and select the ffmpeg or ffmpeg.exe program
    """.trimIndent()

    const val ASK_FOR_FFMPEG_INPUT =
        "Please select the previously downloaded ffmpeg or ffmpeg.exe executable"

    var ffmpegExecutable = FFMPEG_PATH_UNSET
    var ffmpegCrfQuality = 15
    var ffmpegAudioBitRate = 128
    var ffmpegWidth = 640
    var ffmpegHeight = 480
    var ffmpegFrameRate = 30f
    var loadPixelsEnabled = true
    var saveDebugInfo = true
    var saveAlphaVideo = false
}