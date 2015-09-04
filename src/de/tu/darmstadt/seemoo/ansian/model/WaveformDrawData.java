package de.tu.darmstadt.seemoo.ansian.model;

import de.tu.darmstadt.seemoo.ansian.tools.ArrayHelper;

/**
 * 
 * @author Steffen Kreis
 *
 *         dedicated datatype to hold real and imaginary data for WaveformView
 *         and handle calculation request for representation
 *
 */

public class WaveformDrawData {

	private float[] re;
	private float[] im;
	private boolean isDemodulated = false;

	public WaveformDrawData(float[] re, float[] im, boolean isdemodulatedData) {
		this.re = re;
		this.im = im;
		this.isDemodulated = isdemodulatedData;
	}

	public float[] getRe() {
		return re;
	}

	public float[] getIm() {
		return im;
	}

	public boolean isDemodulatedData() {
		return isDemodulated;
	}

	/**
	 * 
	 * @param scaledAmount
	 *            desired amount of data
	 * @param xScale
	 * @param yScale
	 * @return
	 */
	public float[] getDrawData(int scaledAmount, float yScale) {
		ArrayHelper arrHelp = new ArrayHelper(re);
		float[] array = arrHelp.getScaledValues(scaledAmount, yScale, false);
		return array;
	}

}
