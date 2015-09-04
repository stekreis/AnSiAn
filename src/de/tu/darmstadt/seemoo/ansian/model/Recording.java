package de.tu.darmstadt.seemoo.ansian.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.control.events.RecordingEvent;
import de.tu.darmstadt.seemoo.ansian.gui.misc.MyToast;
import de.tu.darmstadt.seemoo.ansian.model.preferences.MiscPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

public class Recording {
	private static final String LOGTAG = "Recording";

	private boolean stopRecording = false;

	private BufferedOutputStream bufferedOutputStream = null; // Used for
																// recording

	private File recordingFile;

	private MiscPreferences miscPreference;

	public Recording(final File recordingFile) {
		this.recordingFile = recordingFile;
		try {
			this.bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(recordingFile));
		} catch (FileNotFoundException e) {
			Log.e(LOGTAG, "showRecordingDialog: File not found: " + recordingFile.getAbsolutePath());
		}

	}

	/**
	 * Will start writing the raw samples to the bufferedOutputStream. Stream
	 * will be closed on error, on stopRecording() and on stopSampling()
	 *
	 * @param bufferedOutputStream
	 *            stream to write the samples out.
	 * @param recordingFile
	 */
	public void startRecordingThread() {
		miscPreference = Preferences.MISC_PREFERENCE;
		// if stopAfter was selected, start thread to
		// supervise the recording:
		if (miscPreference.isRecordingStoppedAfterEnabled()) {
			Thread supervisorThread = new Thread() {
				@Override
				public void run() {
					Log.i(LOGTAG, "recording_superviser: Supervisor Thread started. (Thread: " + this.getName() + ")");
					try {
						long startTime = System.currentTimeMillis();
						boolean stop = false;

						// We check once per half a
						// second if the stop criteria
						// is met:
						Thread.sleep(500);
						Log.d(LOGTAG, "Unit: " + miscPreference.getRecordingStoppedAfterUnit() + " Value: "
								+ miscPreference.getRecordingStoppedAfterValue());
						Log.d(LOGTAG, "recordingFile: " + recordingFile + " stop: " + stop);
						while (recordingFile != null && !stop) {

							switch (miscPreference.getRecordingStoppedAfterUnit()) { // see
							// arrays.xml
							// -
							// recording_stopAfterUnit
							case 0: /* MB */
								if (recordingFile.length() / 1000000 >= miscPreference.getRecordingStoppedAfterValue())
									stop = true;
								break;
							case 1: /* GB */
								if (recordingFile.length() / 1000000000 >= miscPreference
										.getRecordingStoppedAfterValue())
									stop = true;
								break;
							case 2: /* sec */
								if (System.currentTimeMillis()
										- startTime >= miscPreference.getRecordingStoppedAfterValue() * 1000)
									stop = true;
								break;
							case 3: /* min */
								if (System.currentTimeMillis()
										- startTime >= miscPreference.getRecordingStoppedAfterValue() * 1000 * 60)
									stop = true;
								break;
							}
						}
						// stop recording:
						stopRecordingThread();
					} catch (InterruptedException e) {
						Log.e(LOGTAG, "recording_superviser: Interrupted!");
					} catch (NullPointerException e) {
						Log.e(LOGTAG, "recording_superviser: Recording file is null!");
					}
					Log.i(LOGTAG, "recording_superviser: Supervisor Thread stopped. (Thread: " + this.getName() + ")");
				}
			};
			supervisorThread.start();
		}

	}

	/**
	 * Will stop writing samples to the bufferedOutputStream and close it.
	 */
	public void stopRecordingThread() {
		if (isRecording()) {
			stopRecording = true;

			if (recordingFile != null) {
				final String filename = recordingFile.getAbsolutePath();
				final long filesize = recordingFile.length() / 1000000; // file
																		// size
																		// in MB
				MainActivity.instance.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						MyToast.makeText("Recording stopped: " + filename + " (" + filesize + " MB)",
								Toast.LENGTH_LONG);
					}
				});
				recordingFile = null;

			}

			EventBus.getDefault().post(new RecordingEvent(false));
			EventBus.getDefault().unregister(this);
		}
	}

	public void write(byte[] packet) {
		if (stopRecording) {
			try {
				bufferedOutputStream.close();
			} catch (IOException e) {
				Log.e(LOGTAG, "run: Error while closing output stream (recording): " + e.getMessage());
			}
			bufferedOutputStream = null;
			Log.i(LOGTAG, "run: Recording stopped.");
		} else
			try {
				bufferedOutputStream.write(packet);
			} catch (IOException e) {
				Log.e(LOGTAG, "run: Error while writing to output stream (recording): " + e.getMessage());
				this.stopRecordingThread();
			}

	}

	/**
	 * @return true if currently recording; false if not
	 */
	private boolean isRecording() {
		return bufferedOutputStream != null;
	}

}
