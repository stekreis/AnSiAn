package de.tu.darmstadt.seemoo.ansian.control.events;

public class FrequencyEvent {

	private long frequency;

	public FrequencyEvent(long frequency) {
		this.frequency = frequency;
	}

	public long getFrequency() {
		return frequency;
	}
}
