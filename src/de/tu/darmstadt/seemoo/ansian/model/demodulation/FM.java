package de.tu.darmstadt.seemoo.ansian.model.demodulation;

import de.tu.darmstadt.seemoo.ansian.control.threads.Demodulator;
import de.tu.darmstadt.seemoo.ansian.model.SamplePacket;

public class FM extends Demodulation {

	private int deviation;
	private SamplePacket demodulatorHistory;
	private DemoType type;

	public FM(DemoType type) {
		this.type = type;
		switch (type) {
		case WFM:
			MIN_USER_FILTER_WIDTH = 50000;
			MAX_USER_FILTER_WIDTH = 120000;
			deviation = 75000;
			quadratureRate = 8 * Demodulator.AUDIO_RATE;
			break;
		case NFM:
			deviation = 5000;
			MIN_USER_FILTER_WIDTH = 3000;
			MAX_USER_FILTER_WIDTH = 15000;
			break;
		default:
			// throw new WrongDemodulationTypeException();

		}

	}

	/**
	 * Will FM demodulate the samples in input. Use ~75000 deviation for wide
	 * band FM and ~3000 deviation for narrow band FM. Demodulated samples are
	 * stored in the real array of output. Note: All samples in output will
	 * always be overwritten!
	 *
	 * @param input
	 *            incoming (modulated) samples
	 * @param output
	 *            outgoing (demodulated) samples
	 */
	@Override
	public void demodulate(SamplePacket input, SamplePacket output) {
		float[] reIn = input.getRe();
		float[] imIn = input.getIm();
		float[] reOut = output.getRe();
		float[] imOut = output.getIm();
		int inputSize = input.size();
		float quadratureGain = quadratureRate / (2 * (float) Math.PI * deviation);

		if (demodulatorHistory == null) {
			demodulatorHistory = new SamplePacket(1);
			demodulatorHistory.getRe()[0] = reIn[0];
			demodulatorHistory.getIm()[0] = reOut[0];
		}

		// Quadrature demodulation:
		reOut[0] = reIn[0] * demodulatorHistory.re(0) + imIn[0] * demodulatorHistory.im(0);
		imOut[0] = imIn[0] * demodulatorHistory.re(0) - reIn[0] * demodulatorHistory.im(0);
		reOut[0] = quadratureGain * (float) Math.atan2(imOut[0], reOut[0]);
		for (int i = 1; i < inputSize; i++) {
			reOut[i] = reIn[i] * reIn[i - 1] + imIn[i] * imIn[i - 1];
			imOut[i] = imIn[i] * reIn[i - 1] - reIn[i] * imIn[i - 1];
			reOut[i] = quadratureGain * (float) Math.atan2(imOut[i], reOut[i]);
		}
		demodulatorHistory.getRe()[0] = reIn[inputSize - 1];
		demodulatorHistory.getIm()[0] = imIn[inputSize - 1];
		output.setSize(inputSize);
		output.setSampleRate(quadratureRate);

	}

	@Override
	public DemoType getType() {
		return type;
	}

}
