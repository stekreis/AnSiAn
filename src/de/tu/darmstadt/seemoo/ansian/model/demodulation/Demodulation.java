package de.tu.darmstadt.seemoo.ansian.model.demodulation;

import de.tu.darmstadt.seemoo.ansian.control.threads.Demodulator;
import de.tu.darmstadt.seemoo.ansian.model.SamplePacket;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.morse.Morse;

/**
 * Superclass for the various Demodulations
 * 
 * @author Markus Grau and Steffen Kreis
 *
 */

public abstract class Demodulation {

	public static enum DemoType {
		OFF, AM, NFM, WFM, LSB, USB, MORSE;
	}

	private static Demodulation[] demodulations;

	protected static int MIN_USER_FILTER_WIDTH;
	protected static int MAX_USER_FILTER_WIDTH;

	// The quadrature rate is the sample rate that is used for the demodulation:
	protected int quadratureRate = 2 * Demodulator.AUDIO_RATE;
	// off; this value is not
	// 0 to avoid divide by
	// zero errors!

	private int userFilterCutOff = -1;

	public abstract void demodulate(SamplePacket input, SamplePacket output);

	public static Demodulation getDemodulation(DemoType type) {
		if (demodulations == null)
			demodulations = new Demodulation[DemoType.values().length];

		// if (demodulations[type.ordinal()]==null)
		demodulations[type.ordinal()] = createDemodulation(type);

		return demodulations[type.ordinal()];
	}

	private static Demodulation createDemodulation(DemoType type) {
		switch (type) {
		case OFF:
			return new OFF();

		case AM:
			return new AM();

		case NFM:
			return new FM(DemoType.NFM);

		case WFM:
			return new FM(DemoType.WFM);

		case LSB:
			return new LSB();

		case USB:
			return new USB();

		case MORSE:
			return new Morse();

		default:
			return new OFF();
		}
	}

	public int getQuadratureRate() {
		return quadratureRate;
	}

	public int getUserFilterCutOff() {
		if (userFilterCutOff == -1)
			userFilterCutOff = MAX_USER_FILTER_WIDTH + MIN_USER_FILTER_WIDTH / 2;
		return userFilterCutOff;
	}

	public int getMinUserFilterWidth() {
		return MIN_USER_FILTER_WIDTH;
	}

	public int getMaxUserFilterWidth() {
		return MAX_USER_FILTER_WIDTH;
	}

	public void setUserFilterCutOff(int channelWidth) {
		userFilterCutOff = channelWidth;

	}

	public boolean isLowerBandShown() {
		return true;
	}

	public boolean isUpperBandShown() {
		return true;
	}

	public abstract DemoType getType();
}
