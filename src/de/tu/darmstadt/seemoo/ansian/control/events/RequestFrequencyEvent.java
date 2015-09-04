package de.tu.darmstadt.seemoo.ansian.control.events;

public class RequestFrequencyEvent {

	private long previousFrequency;
	private long requestedFrequency;
	private boolean checkPrevious = false;

	public RequestFrequencyEvent(long requestedFrequency, long previousFrequency) {
		this.requestedFrequency = requestedFrequency;
		this.previousFrequency = previousFrequency;
		checkPrevious = true;
	}

	public RequestFrequencyEvent(long requestedFrequency) {
		this.requestedFrequency = requestedFrequency;
		checkPrevious = false;
	}

	public boolean isCheckPrevious() {
		return checkPrevious;
	}

	public long getPreviousFrequency() {
		return previousFrequency;
	}

	public long getRequestedFrequency() {
		return requestedFrequency;
	}
}
