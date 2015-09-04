package de.tu.darmstadt.seemoo.ansian.model;

public class FFTDrawData {

	private int start;
	private float[] values;

	public FFTDrawData(float[] tempMagnitudes, int i) {
		values = tempMagnitudes;
		start = i;
	}

	public int getStart() {
		return start;
	}

	public float[] getValues() {
		return values;
	}

}
