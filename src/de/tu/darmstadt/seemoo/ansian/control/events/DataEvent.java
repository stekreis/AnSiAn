package de.tu.darmstadt.seemoo.ansian.control.events;

import de.tu.darmstadt.seemoo.ansian.model.SamplePacket;

public class DataEvent {

	private SamplePacket sample;

	public DataEvent(SamplePacket sample) {
		this.sample = sample;
	}

	public SamplePacket getSample() {
		return sample;
	}
}
