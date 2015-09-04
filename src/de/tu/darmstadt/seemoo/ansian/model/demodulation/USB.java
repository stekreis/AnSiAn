package de.tu.darmstadt.seemoo.ansian.model.demodulation;

import de.tu.darmstadt.seemoo.ansian.model.SamplePacket;

public class USB extends SSB {

	public USB() {
		MIN_USER_FILTER_WIDTH = 1500;
		MAX_USER_FILTER_WIDTH = 5000;
	}

	@Override
	public void demodulate(SamplePacket input, SamplePacket output) {
		super.demodulateSSB(input, output, true);

	}

	@Override
	public DemoType getType() {
		return DemoType.USB;
	}

	@Override
	public boolean isLowerBandShown() {
		return false;
	}
}
