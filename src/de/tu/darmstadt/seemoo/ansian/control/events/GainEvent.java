package de.tu.darmstadt.seemoo.ansian.control.events;

public class GainEvent {

	private boolean manualGain;
	private int ifGain;
	private int gain;

	public void setManualGain(boolean isChecked) {
		manualGain = isChecked;
	}

	public void setGain(int i) {
		gain = i;
	}

	public void setIFGain(int i) {
		ifGain = i;
	}

	public int getGain() {
		return gain;
	}

	public int getIfGain() {
		return ifGain;
	}

	public boolean isManualGain() {
		return manualGain;
	}

}
