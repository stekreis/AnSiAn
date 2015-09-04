package de.tu.darmstadt.seemoo.ansian.model.demodulation.morse;

import java.util.BitSet;

import de.tu.darmstadt.seemoo.ansian.model.FFTSample;

public class MorseBitSet extends java.util.BitSet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5572966622427907284L;
	private BitSet bits;
	private int index;
	private MorseStats stats;

	public MorseBitSet(FFTSample[] fftSamples, MorseStats morseStats) {
		stats = morseStats;
		index = 0;
		bits = new BitSet();
		for (int i = 0; i < fftSamples.length; i++) {
			setBit(stats.estimateValue(fftSamples[i]));
		}
	}

	private int getLowBits(int i) {
		int nextHigh = bits.nextSetBit(i);
		int bitsLow = 0;

		if (nextHigh != -1)
			;
		{
			bitsLow = nextHigh - i;
		}

		return bitsLow;

	}

	private int getHighBits(int i) {
		int nextLow = bits.nextClearBit(i);
		int bitsHigh = 0;

		if (nextLow != -1)
			;
		{
			bitsHigh = nextLow - i;
		}

		return bitsHigh;

	}

	void setBit(boolean b) {
		if (b)
			bits.set(index);
		index++;
	}

	public int getLength() {
		return index - 1;
	}

	public int getNextBits(int i) {
		int num;
		if (bits.get(i)) {
			num = getHighBits(i);
		} else {
			num = -getLowBits(i);

		}

		return num;
	}

}
