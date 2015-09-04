package de.tu.darmstadt.seemoo.ansian.control.events;

public class SquelchChangeEvent {
	private float squelch;

	public SquelchChangeEvent(float squelch) {
		this.squelch = squelch;
	}

	public float getSquelch() {
		return squelch;
	}

}
