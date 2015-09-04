package de.tu.darmstadt.seemoo.ansian.control.events;

public abstract class ScaleEvent {
	private float xScale;
	private float yScale;

	public ScaleEvent(float xScale, float yScale) {
		this.xScale = xScale;
		this.yScale = yScale;
	}

	public float getXScale() {
		return xScale;
	}

	public float getYScale() {
		return yScale;
	}
}
