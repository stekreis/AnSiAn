package de.tu.darmstadt.seemoo.ansian.model;

import java.util.BitSet;

import android.util.Log;

public class ErrorBitSet extends BitSet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5148101260177507175L;

	private BitSet bits;
	private int index;
	private boolean filled;
	private int size;
	private float threshold = 0.5f;

	public ErrorBitSet(int size) {
		filled = false;
		index = 0;
		this.size = size;
		bits = new BitSet(size);
	}

	public void setBit(boolean b) {
		if (index == size) {
			index = 0;
			filled = true;
		}
		bits.set(index, b);
		index++;
	}

	public float getSuccessRate() {
		Log.d("ERROR", "c: " + bits.cardinality() + " size: " + size + " index: " + index);
		if (filled)
			return (float) bits.cardinality() / size;
		else
			return (float) bits.cardinality() / index;
	}

	public float getErrorRate() {
		return 1f - getSuccessRate();
	}

	public boolean checkStats() {
		if (filled)
			if (getSuccessRate() < threshold)
				return false;
		return true;
	}

}
