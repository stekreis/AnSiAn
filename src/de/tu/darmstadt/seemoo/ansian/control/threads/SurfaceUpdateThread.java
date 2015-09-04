package de.tu.darmstadt.seemoo.ansian.control.threads;

import java.util.concurrent.CopyOnWriteArrayList;

import android.util.Log;
import de.tu.darmstadt.seemoo.ansian.gui.views.MySurfaceView;
import de.tu.darmstadt.seemoo.ansian.model.preferences.MiscPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * <h1>AnSiAn - SurfaceUpdateThread</h1>
 *
 * Module: SurfaceUpdateThread.java Description: This thread takes care of
 * updating the GUI components
 *
 * @author Dennis Mantz
 * @author Markus Grau
 * @author Steffen Kreis
 *
 *         Copyright (C) 2014 Dennis Mantz License:
 *         http://www.gnu.org/licenses/gpl.html GPL version 2 or higher
 *
 *         This library is free software; you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation; either version 2 of the License, or (at
 *         your option) any later version.
 *
 *         This library is distributed in the hope that it will be useful, but
 *         WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *         General Public License for more details.
 *
 *         You should have received a copy of the GNU General Public License
 *         along with this library; if not, write to the Free Software
 *         Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *         02110-1301 USA
 */
public class SurfaceUpdateThread extends Thread {

	private int fftSize = 0; // Size of the FFT
	private static int frameRate = 50; // Frames per Second
	private static double load = 0; // Time_for_processing_and_drawing /
									// Time_per_Frame
	private boolean dynamicFrameRate = false; // Turns on and off the automatic
												// frame rate control
	private boolean stopRequested = true; // Will stop the thread when set to
											// true
	private static CopyOnWriteArrayList<MySurfaceView> views = new CopyOnWriteArrayList<MySurfaceView>();

	private boolean running = true;
	private int drawCounter = 0;

	private static final String LOGTAG = "SurfaceUpdateThread";
	private static final int MAX_FRAMERATE = 30; // Upper limit for the
													// automatic frame rate
													// control
	// at every load value below this threshold we increase the frame rate
	private static final double LOW_THRESHOLD = 0.65;
	// at every load value above this threshold we decrease the frame rate
	private static final double HIGH_THRESHOLD = 0.85;

	/**
	 * Constructor. Will initialize the member attributes.
	 *
	 * @param view
	 *            reference to the AnalyzerSurface for drawing
	 * @param fftSize
	 *            Size of the FFT
	 * @param arrayBlockingQueue
	 *            queue that delivers sample packets
	 */

	public SurfaceUpdateThread() {
		setPriority(MIN_PRIORITY);
		MiscPreferences preferences = Preferences.MISC_PREFERENCE;
		this.fftSize = preferences.getFFTSize();
		frameRate = Preferences.GUI_PREFERENCE.getFramerate();
		dynamicFrameRate = Preferences.GUI_PREFERENCE.isDynamicFrameRate();
		// Check if fftSize is a power of 2
		int order = (int) (Math.log(fftSize) / Math.log(2));
		if (fftSize != (1 << order))
			throw new IllegalArgumentException("FFT size must be power of 2");

	}

	public int getFftSize() {
		return fftSize;
	}

	/**
	 * Will start the processing loop
	 */
	@Override
	public void start() {
		this.stopRequested = false;
		super.start();
	}

	/**
	 * Will set the stopRequested flag so that the processing loop will
	 * terminate
	 */
	public void stopLoop() {
		this.stopRequested = true;
		clearGui();
	}

	/**
	 * @return true if loop is running; false if not.
	 */
	public boolean isRunning() {
		return !stopRequested;
	}

	@Override
	public void run() {
		Log.i(LOGTAG, "Processing loop started. (Thread: " + this.getName() + ")");
		long startTime; // timestamp when signal processing is started
		long sleepTime; // time (in ms) to sleep before the next run to meet the
						// frame rate

		while (!stopRequested) {
			if (running) {
				// store the current timestamp
				startTime = System.currentTimeMillis();

				sleepTime = (1000 / frameRate) - (System.currentTimeMillis() - startTime);

				drawGui();

				try {
					if (sleepTime > 0) {
						// load = processing_time / frame_duration
						load = (System.currentTimeMillis() - startTime) / (1000.0 / frameRate);
						// Log.d(LOGTAG, "drawing framerate: " + frameRate + "/
						// load: " + load);
						// Automatic frame rate control:
						if (dynamicFrameRate && load < LOW_THRESHOLD && frameRate < MAX_FRAMERATE)
							frameRate++;
						if (dynamicFrameRate && load > HIGH_THRESHOLD && frameRate > 1)
							frameRate--;

						sleep(sleepTime);

					} else {
						// Automatic frame rate control:
						if (dynamicFrameRate && frameRate > 1)
							frameRate--;

						// Log.d(LOGTAG, "Couldn't meet requested frame rate!");
						load = 1;
					}
				} catch (Exception e) {
					Log.e(LOGTAG, "Error while calling sleep()");
				}
			}
		}
		this.stopRequested = true;
		Log.i(LOGTAG, "Processing loop stopped. (Thread: " + this.getName() + ")");
	}

	public void drawGui() {
		drawCounter++;
		for (MySurfaceView view : views) {
			if (view.isShown() && drawCounter % view.getDrawDivisor() == 0)
				view.draw();
		}
	}

	public void clearGui() {
		for (MySurfaceView view : views) {
			view.clear();
		}
	}

	public static double getLoad() {
		return load;
	}

	public static int getFrameRate() {
		return frameRate;
	}

	public static void registerView(MySurfaceView mySurfaceView) {
		views.add(mySurfaceView);
	}

	public static void unregisterView(MySurfaceView mySurfaceView) {
		views.remove(mySurfaceView);
	}

}
