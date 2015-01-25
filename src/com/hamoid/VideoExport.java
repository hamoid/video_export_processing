package com.hamoid;

import java.io.File;
import java.io.OutputStream;

import processing.core.PApplet;
import processing.core.PGraphics;

/*
 * Video Export for Processing
 * Dependency: ffmpeg
 * Author: Abe Pazos
 * First version: 25.01.2015
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
	private boolean ffmpegFound = true;
	private File ffmpegOutputMsg;
	private OutputStream ffmpeg;
	private final File videoExportSettings;

	public VideoExport(PApplet parent, String outputFileName) {
		this(parent, outputFileName, parent.g);
	}

	public VideoExport(PApplet parent, String outputFileName, PGraphics pg) {
		parent.registerMethod("dispose", this);

		this.parent = parent;
		this.pg = pg;

		videoExportSettings = parent.sketchFile("ffmpegPath.txt");
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

	/*
	 * Set quality 0 - 100 (100 means lossless)
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

	public void setFrameRate(float frameRate) {
		if (ffmpeg != null) {
			System.err.println("Can't setFrameRate() after saveFrame()!");
			return;
		}
		ffmpegFrameRate = frameRate;
	}

	public void dontCallLoadPixels() {
		loadPixelsEnabled = false;
	}

	public void saveFrame() {
		if (!ffmpegFound) {
			return;
		}
		if (!initialized) {
			init();
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

	// Called automatically by Processing
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

	// ----------- PRIVATE ------------

	private void checkFfmpegExecutable() {
		ffmpegFound = false;

		String[] settings = new String[1];
		if (videoExportSettings.isFile()) {
			settings = PApplet.loadStrings(videoExportSettings);
		} else if ((new File("/usr/local/bin/ffmpeg")).isFile()) {
			settings[0] = "/usr/local/bin/ffmpeg";
			PApplet.saveStrings(videoExportSettings, settings);
		} else if ((new File("/usr/bin/ffmpeg")).isFile()) {
			settings[0] = "/usr/bin/ffmpeg";
			PApplet.saveStrings(videoExportSettings, settings);
		}
		if (settings[0] != null) {
			startFfmpeg(settings[0]);
		} else {
			parent.selectInput(
					"Please select the ffmpeg or ffmpeg.exe executable",
					"onFfmpegSelected", new File("/"), this);
		}
	}

	public void onFfmpegSelected(File selection) {
		if (selection == null) {
			System.err.println("Ffmpeg not found.");
		} else {
			String[] settings = new String[1];
			settings[0] = selection.getAbsolutePath();
			PApplet.saveStrings(videoExportSettings, settings);
			startFfmpeg(settings[0]);
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
				"-metadata", "comment=\"" + ffmpegMetadataComment + "\"",
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
		initialized = true;
		ffmpegFound = true;
	}

	/*
	 * The start function is not part of the constructor to allow
	 * changing the settings before the ProcessBuilder is created.
	 */
	private void init() {
		checkFfmpegExecutable();
	}

	private void err(String msg) {
		System.err.println("VideoExport error: " + msg);
		System.exit(1);
	}

	private void err() {
		err("Ffmpeg failed. Study " + ffmpegOutputMsg + " for more details.");
	}

}
