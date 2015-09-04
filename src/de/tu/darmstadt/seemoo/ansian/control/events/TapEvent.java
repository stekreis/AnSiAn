package de.tu.darmstadt.seemoo.ansian.control.events;

public class TapEvent {
	private float xVal;
	private float yVal;

	public TapEvent(float xVal, float yVal) {
		this.xVal = xVal;
		this.yVal = yVal;
	}

	public float getxVal() {
		return xVal;
	}

	public float getyVal() {
		return yVal;
	}

}
