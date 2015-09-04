package de.tu.darmstadt.seemoo.ansian.control.events;

import de.tu.darmstadt.seemoo.ansian.model.SamplePacket;

public class DemodDataEvent {
	private SamplePacket sample;

	public DemodDataEvent(SamplePacket sample) {
		this.sample = sample;
	}

	public SamplePacket getSample() {
		return sample;
	}
}
