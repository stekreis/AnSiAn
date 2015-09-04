package de.tu.darmstadt.seemoo.ansian.model.demodulation;

import de.tu.darmstadt.seemoo.ansian.model.SamplePacket;

public class OFF extends Demodulation {
	public OFF() {
		MIN_USER_FILTER_WIDTH = 0;
		MAX_USER_FILTER_WIDTH = 0;
	}

	@Override
	public void demodulate(SamplePacket input, SamplePacket output) {
		// TODO Auto-generated method stub

	}

	@Override
	public DemoType getType() {
		return DemoType.OFF;
	}

}
