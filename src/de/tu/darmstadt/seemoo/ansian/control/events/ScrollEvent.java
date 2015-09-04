package de.tu.darmstadt.seemoo.ansian.control.events;

public class ScrollEvent {
	private float xScrollRatio;
	private float yScrollRatio;
	private boolean isDemodScroll = false;

	public ScrollEvent(float xScrollRatio, float yScrollRatio, boolean isDemodScroll) {
		this.xScrollRatio = xScrollRatio;
		this.yScrollRatio = yScrollRatio;
		this.isDemodScroll = isDemodScroll;
	}

	public float getXScroll() {
		return xScrollRatio;
	}

	public float getYScroll() {
		return yScrollRatio;
	}

	public boolean isDemodScroll() {
		return isDemodScroll;
	}
}
