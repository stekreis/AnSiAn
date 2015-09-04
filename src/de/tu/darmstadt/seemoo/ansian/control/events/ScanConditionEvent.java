package de.tu.darmstadt.seemoo.ansian.control.events;

public class ScanConditionEvent {
	boolean isReady = false;

	public ScanConditionEvent(boolean isReady) {
		this.isReady = isReady;
	}

	public boolean getCondition() {
		return isReady;
	}
}
