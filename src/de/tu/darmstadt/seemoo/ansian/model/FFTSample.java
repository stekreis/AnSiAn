package de.tu.darmstadt.seemoo.ansian.model;

import java.util.Arrays;

import de.tu.darmstadt.seemoo.ansian.tools.ArrayHelper;

public class FFTSample {

	private int samplerate;

	private long centerFrequency;

	private int length;
	private int center;
	private float[] magnitude;
	private long timestamp;
	private GuiFFTDataAdapter fftDataAdapter;

	private final String LOGTAG = "FFTSample";

	private boolean morseBit;

	public FFTSample(SamplePacket samples, float[] mag) {
		this.timestamp = samples.getTimestamp();
		centerFrequency = samples.getFrequency();
		this.samplerate = samples.getSampleRate();
		this.setMagnitudes(mag);
		length = magnitude.length;
		center = length / 2;
	}

	public float[] getMagnitudes(int bandwidth) {
		if (bandwidth == samplerate)
			return Arrays.copyOfRange(magnitude, 0, length);
		else {
			// Log.d(LOGTAG,"bandwidth: "+bandwidth+"samplerate: "+samplerate);
			float factor = (float) bandwidth / samplerate;
			// Log.d(LOGTAG,"offset: "+factor);
			int offset = (int) (center * factor);
			// Log.d(LOGTAG,"offset: "+offset);
			return Arrays.copyOfRange(magnitude, center - offset, center + offset + 1);
		}
	}

	public float[] getMagnitudes() {
		return magnitude;
	}

	public int getSize() {
		return magnitude.length;
	}

	public void setMagnitudes(float[] values) {
		this.magnitude = values;
	}

	public long getCenterFrequency() {
		return centerFrequency;
	}

	public FFTDrawData getDrawData(int pixel) {
		if (fftDataAdapter == null) {
			fftDataAdapter = new GuiFFTDataAdapter(this, pixel);
		}
		return fftDataAdapter.getFftDrawData(pixel);
	}

	public long getTimestamp() {
		return timestamp;

	}

	public int getBandwidth() {
		return samplerate;
	}

	public float getAverage(int bandwidth) {
		float[] cutMags = getMagnitudes(bandwidth);
		// Log.d(LOGTAG,"cutMags: "+Arrays.toString(cutMags));
		return new ArrayHelper(cutMags).getAverage();
	}

	public float getAverage(long frequency, int bandwidth) {
		float[] cutMags = getMagnitudes(frequency, bandwidth);
		// Log.d(LOGTAG,"cutMags: "+Arrays.toString(cutMags));
		return new ArrayHelper(cutMags).getAverage();
	}

	private float[] getMagnitudes(long frequency, int bandwidth) {
		if (bandwidth == samplerate && frequency == centerFrequency)
			return Arrays.copyOfRange(magnitude, 0, length);
		else {
			float frequencyOffset = centerFrequency - frequency;

			float newCenter = (float) (center - length * frequencyOffset / samplerate);
			float factor = (float) bandwidth / samplerate;
			float offset = (float) (newCenter * factor);
			int newLow = Math.max((int) (newCenter - offset), 0);
			int newHigh = Math.min((int) (newCenter + offset + 1), length - 1);
			return Arrays.copyOfRange(magnitude, newLow, newHigh);
		}
	}

	public float getAverage() {
		return new ArrayHelper(magnitude).getAverage();
	}

	public void setMorseBit(boolean estimateValue) {
		this.morseBit = estimateValue;
	}

	public boolean getMorseBit() {
		return morseBit;
	}

}
