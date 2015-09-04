package de.tu.darmstadt.seemoo.ansian.model.demodulation;

import android.util.Log;
import de.tu.darmstadt.seemoo.ansian.model.SamplePacket;
import de.tu.darmstadt.seemoo.ansian.model.filter.ComplexFirFilter;

public abstract class SSB extends Demodulation {

	private ComplexFirFilter bandPassFilter = null; // used for SSB demodulation
	private static final int BAND_PASS_ATTENUATION = 40;
	private static int userFilterCutOff = 0;
	// DEMODULATION
	private float lastMax = 0; // used for gain control in AM / SSB demodulation
	private String LOGTAG = "SSB";

	/**
	 * Will SSB demodulate the samples in input. Demodulated samples are stored
	 * in the real array of output. Note: All samples in output will always be
	 * overwritten!
	 *
	 * @param input
	 *            incoming (modulated) samples
	 * @param output
	 *            outgoing (demodulated) samples
	 * @param upperBand
	 *            if true: USB; if false: LSB
	 */
	protected void demodulateSSB(SamplePacket input, SamplePacket output, boolean upperBand) {
		float[] reOut = output.getRe();

		// complex band pass:
		if (bandPassFilter == null
				|| (upperBand && (((int) bandPassFilter.getHighCutOffFrequency()) != userFilterCutOff))
				|| (!upperBand && (((int) bandPassFilter.getLowCutOffFrequency()) != -userFilterCutOff))) {
			// We have to (re-)create the band pass filter:
			this.bandPassFilter = ComplexFirFilter.createBandPass(2, // Decimate
																		// by 2;
																		// =>
																		// AUDIO_RATE
					1, input.getSampleRate(), upperBand ? 200f : -userFilterCutOff,
					upperBand ? userFilterCutOff : -200f, input.getSampleRate() * 0.01f, BAND_PASS_ATTENUATION);
			if (bandPassFilter == null)
				return; // This may happen if input samples changed rate or
						// demodulation was turned off. Just skip the filtering.
			Log.d(LOGTAG, "demodulateSSB: created new band pass filter with " + bandPassFilter.getNumberOfTaps()
					+ " taps. Decimation=" + bandPassFilter.getDecimation() + " Low-Cut-Off="
					+ bandPassFilter.getLowCutOffFrequency() + " High-Cut-Off="
					+ bandPassFilter.getHighCutOffFrequency() + " transition=" + bandPassFilter.getTransitionWidth());
		}
		output.setSize(0); // mark buffer as empty
		if (bandPassFilter.filter(input, output, 0, input.size()) < input.size()) {
			Log.e(LOGTAG, "demodulateSSB: could not filter all samples from input packet.");
		}

		// gain control: searching for max:
		lastMax *= 0.95; // simplest AGC
		for (int i = 0; i < output.size(); i++) {
			if (reOut[i] > lastMax)
				lastMax = reOut[i];
		}
		// normalize values:
		float gain = 0.75f / lastMax;
		for (int i = 0; i < output.size(); i++)
			reOut[i] *= gain;
	}

}
