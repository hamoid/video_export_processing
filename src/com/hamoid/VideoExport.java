/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 * @author ##author##
 * @modified ##date##
 * @version ##library.prettyVersion## (##library.version##)
 */

package com.hamoid;

import java.io.File;
import java.io.OutputStream;
import java.util.prefs.Preferences;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 *
 * @example basic
 *
 */

public class VideoExport {

	private ProcessBuilder processBuilder;
	private Process process;
	private boolean initialized = false;

	private final byte[] pixelsByte;

	private boolean loadPixelsEnabled = true;
	private final String outputFilePath;

	private final PGraphics pg;
	private final PApplet parent;

	private final String ffmpegMetadataComment = "Exported using VideoExport for Processing - https://github.com/hamoid/VideoExport-for-Processing";
	private int ffmpegCrfQuality;
	private float ffmpegFrameRate;
	private boolean ffmpegFound = false;
	private File ffmpegOutputMsg;
	private OutputStream ffmpeg;

	private final Preferences settings;
	private final static String SETTINGS_FFMPEG_PATH = "settings_ffmpeg_path";
	private final static String FFMPEG_PATH_UNSET = "ffmpeg_path_unset";

	public final static String VERSION = "##library.prettyVersion##";

	/**
	 * Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 *
	 * @example basic
	 * @param parent
	 *            Parent PApplet, normally "this" when called from setup()
	 * @param outputFileName
	 *            The name of the video file to produce, for instance
	 *            "beauty.mp4"
	 */
	public VideoExport(PApplet parent, String outputFileName) {
		this(parent, outputFileName, parent.g);
	}

	/**
	 *
	 * Constructor that allows to set a PGraphics to export as video (advanced)
	 *
	 * @example usingPGraphics
	 * @param parent
	 *            Parent PApplet, normally "this" when called from setup()
	 * @param outputFileName
	 *            The name of the video file to produce, for instance
	 *            "beauty.mp4"
	 * @param pg
	 *            PGraphics object to export as video
	 */
	public VideoExport(PApplet parent, String outputFileName, PGraphics pg) {
		parent.registerMethod("dispose", this);

		this.parent = parent;
		this.pg = pg;

		settings = Preferences.userRoot().node(this.getClass().getName());

		outputFilePath = parent.sketchPath(outputFileName);
		ffmpegFrameRate = 30f;
		ffmpegCrfQuality = 15;

		if (pg == null) {
			pixelsByte = null;
			err("Did you initialize your PGraphics?");
		} else {
			pixelsByte = new byte[pg.width * pg.height * 3];
		}
	}

	/**
	 * Set the quality of the produced video file. Optional.
	 *
	 * @param crf
	 *            A value between 0 (high compression) and 100 (high quality,
	 *            lossless). Default is 70.
	 */
	public void setQuality(int crf) {
		if (ffmpeg != null) {
			System.err.println("Can't setQuality() after saveFrame()!");
			return;
		}
		if (crf > 100) {
			crf = 100;
		} else if (crf < 0) {
			crf = 0;
		}
		ffmpegCrfQuality = (100 - crf) / 2;
	}

	/**
	 * Set the frame rate of the produced video file. Optional.
	 *
	 * @param frameRate
	 *            The frame rate at which the resulting video file should be
	 *            played. The default is 30, which is the recommended for online
	 *            video.
	 */
	public void setFrameRate(float frameRate) {
		if (ffmpeg != null) {
			System.err.println("Can't setFrameRate() after saveFrame()!");
			return;
		}
		ffmpegFrameRate = frameRate;
	}

	/**
	 * Tells VideoExport not to call loadPixels(). Use it only if you
	 * already call loadPixels() in your program. Useful to avoid calling it
	 * twice, which might hurt the performance a bit. Optional.
	 */
	public void dontCallLoadPixels() {
		loadPixelsEnabled = false;
	}

	/**
	 * Adds one frame to the video file. The frame will be the content of the
	 * display, or the content of a PGraphics if you specified one in the
	 * constructor.
	 */
	public void saveFrame() {
		if (!initialized) {
			initialize();
			initialized = true;
		}
		if (!ffmpegFound) {
			return;
		}
		if (loadPixelsEnabled) {
			pg.loadPixels();
		}
		int byteNum = 0;
		for (final int px : pg.pixels) {
			pixelsByte[byteNum++] = (byte) (px >> 16);
			pixelsByte[byteNum++] = (byte) (px >> 8);
			pixelsByte[byteNum++] = (byte) (px);
		}
		try {
			ffmpeg.write(pixelsByte);
		} catch (Exception e) {
			e.printStackTrace();
			err();
		}
	}

	/**
	 * Called automatically by Processing to clean up before shut down
	 */
	public void dispose() {
		if (ffmpeg != null) {
			try {
				ffmpeg.flush();
				ffmpeg.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (process != null) {
			process.destroy();
		}
	}

	/**
	 * Return the version of the library.
	 *
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}

	private void initialize() {
		String ffmpeg_path = settings.get(SETTINGS_FFMPEG_PATH,
				FFMPEG_PATH_UNSET);
		if (ffmpeg_path.equals(FFMPEG_PATH_UNSET)) {
			String[] guess_path = { "/usr/local/bin/ffmpeg", "/usr/bin/ffmpeg" };
			for (String guess : guess_path) {
				if ((new File(guess)).isFile()) {
					ffmpeg_path = guess;
					settings.put(SETTINGS_FFMPEG_PATH, ffmpeg_path);
					break;
				}
			}
		}
		if (ffmpeg_path.equals(FFMPEG_PATH_UNSET)) {
			parent.selectInput(
					"Please select the ffmpeg or ffmpeg.exe executable",
					"onFfmpegSelected", new File("/"), this);
		} else {
			startFfmpeg(ffmpeg_path);
		}
	}

	/**
	 * Called internally by the file selector when the user chooses
	 * the location of ffmpeg on the disk.
	 *
	 * @param selection
	 */
	public void onFfmpegSelected(File selection) {
		if (selection == null) {
			System.err.println("Ffmpeg not found.");
		} else {
			String ffmpeg_path = selection.getAbsolutePath();
			settings.put(SETTINGS_FFMPEG_PATH, ffmpeg_path);
			startFfmpeg(ffmpeg_path);
		}
	}

	private void startFfmpeg(String executable) {
		// -y = overwrite, otherwise it fails the second time you run
		// -an = no audio
		// "-b:v", "3000k" = video bit rate
		// "-i", "-" = pipe:0
		processBuilder = new ProcessBuilder(executable, "-y", "-f", "rawvideo",
				"-vcodec", "rawvideo", "-s", pg.width + "x" + pg.height,
				"-pix_fmt", "rgb24", "-r", "" + ffmpegFrameRate, "-i", "-",
				"-an", "-vcodec", "h264", "-crf", "" + ffmpegCrfQuality,
				"-metadata", "comment=" + ffmpegMetadataComment,
				outputFilePath);

		processBuilder.redirectErrorStream(true);
		ffmpegOutputMsg = new File(outputFilePath + ".txt");
		processBuilder.redirectOutput(ffmpegOutputMsg);
		processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
		try {
			process = processBuilder.start();
		} catch (Exception e) {
			e.printStackTrace();
			err();
		}

		ffmpeg = process.getOutputStream();

		ffmpegFound = true;
	}

	private void err(String msg) {
		System.err.println("VideoExport error: " + msg);
		System.exit(1);
	}

	private void err() {
		err("Ffmpeg failed. Study " + ffmpegOutputMsg + " for more details.");
	}

}
