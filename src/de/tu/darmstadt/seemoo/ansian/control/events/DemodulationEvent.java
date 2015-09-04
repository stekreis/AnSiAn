package de.tu.darmstadt.seemoo.ansian.control.events;

import de.tu.darmstadt.seemoo.ansian.model.demodulation.Demodulation.DemoType;

public class DemodulationEvent {
	private DemoType demodulation;

	public DemodulationEvent(DemoType demodulation) {
		this.demodulation = demodulation;
	}

	public DemoType getDemodulation() {
		return demodulation;
	}

	public boolean isOff() {
		return demodulation == DemoType.OFF;
	}
}
