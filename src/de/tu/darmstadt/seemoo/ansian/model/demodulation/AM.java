package de.tu.darmstadt.seemoo.ansian.model.demodulation;

import de.tu.darmstadt.seemoo.ansian.model.SamplePacket;

public class AM extends Demodulation {

	private float lastMax = 0;

	public AM() {
		MIN_USER_FILTER_WIDTH = 3000;
		MAX_USER_FILTER_WIDTH = 15000;
	}

	/**
	 * Will AM demodulate the samples in input. Demodulated samples are stored
	 * in the real array of output. Note: All samples in output will always be
	 * overwritten!
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
		float avg = 0;
		lastMax *= 0.95; // simplest AGC

		// Complex to magnitude
		for (int i = 0; i < input.size(); i++) {
			reOut[i] = (reIn[i] * reIn[i] + imIn[i] * imIn[i]);
			avg += reOut[i];
			if (reOut[i] > lastMax)
				lastMax = reOut[i];
		}
		avg = avg / input.size();

		// normalize values:
		float gain = 0.75f / lastMax;
		for (int i = 0; i < output.size(); i++)
			reOut[i] = (reOut[i] - avg) * gain;

		output.setSize(input.size());
		output.setSampleRate(quadratureRate);

	}

	@Override
	public DemoType getType() {
		return DemoType.AM;
	}

}
