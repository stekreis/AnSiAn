package de.tu.darmstadt.seemoo.ansian.model.demodulation;

import de.tu.darmstadt.seemoo.ansian.model.SamplePacket;

public class LSB extends SSB {

	public LSB() {
		MIN_USER_FILTER_WIDTH = 1500;
		MAX_USER_FILTER_WIDTH = 5000;
	}

	@Override
	public void demodulate(SamplePacket input, SamplePacket output) {
		super.demodulateSSB(input, output, false);
	}

	@Override
	public DemoType getType() {
		return DemoType.LSB;
	}

	@Override
	public boolean isUpperBandShown() {
		return false;
	}

}
