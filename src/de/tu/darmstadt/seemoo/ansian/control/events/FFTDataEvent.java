package de.tu.darmstadt.seemoo.ansian.control.events;

import de.tu.darmstadt.seemoo.ansian.model.FFTSample;

public class FFTDataEvent {

	private FFTSample sample;

	public FFTDataEvent(FFTSample sample) {
		this.sample = sample;
	}

	public FFTSample getSample() {
		return sample;
	}
}
