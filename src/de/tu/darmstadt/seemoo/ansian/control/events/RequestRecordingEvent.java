package de.tu.darmstadt.seemoo.ansian.control.events;

import de.tu.darmstadt.seemoo.ansian.model.Recording;

public class RequestRecordingEvent {
	private Recording recording;

	public RequestRecordingEvent(Recording recording) {
		this.recording = recording;

	}

	public Recording getRecording() {
		return recording;
	}
}
