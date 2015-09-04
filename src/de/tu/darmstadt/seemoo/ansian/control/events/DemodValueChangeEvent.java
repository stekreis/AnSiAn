package de.tu.darmstadt.seemoo.ansian.control.events;

import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

public class DemodValueChangeEvent {
	private long frequency = -1;
	private long bandwidth = -1;

	public DemodValueChangeEvent(long frequency, long bandwidth) {
		this.frequency = frequency;
		this.bandwidth = bandwidth;
	}

	public DemodValueChangeEvent(long demodFrequency) {
		frequency = demodFrequency;
		if (bandwidth == -1) {
			bandwidth = Preferences.GUI_PREFERENCE.getDemodBandwidth();
		}
	}

	public long getFrequency() {
		return frequency;
	}

	public long getBandwidth() {
		return bandwidth;
	}

}
