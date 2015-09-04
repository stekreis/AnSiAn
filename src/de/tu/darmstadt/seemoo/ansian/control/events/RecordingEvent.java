package de.tu.darmstadt.seemoo.ansian.control.events;

import android.widget.Toast;
import de.tu.darmstadt.seemoo.ansian.gui.misc.MyToast;
import de.tu.darmstadt.seemoo.ansian.model.Recording;

public class RecordingEvent {
	private boolean isRecording = false;
	private Recording recording;
	private String stateString;

	public RecordingEvent(boolean b) {
		isRecording = b;
		if (b)
			stateString = "Start recording.";
		else
			stateString = "Stop recording.";
	}

	public RecordingEvent(RequestRecordingEvent recording) {
		this.recording = recording.getRecording();
		isRecording = true;
	}

	public RecordingEvent(String string) {
		this.stateString = string;
		isRecording = false;
		MyToast.makeText(stateString, Toast.LENGTH_LONG);
	}

	public boolean isRecording() {
		return isRecording;
	}

	public Recording getRecording() {
		return recording;
	}

	public String getStateString() {
		return stateString;
	}
}
