package de.tu.darmstadt.seemoo.ansian.control.events.morse;

public class MorseDitDurationEvent {

	private int ditDuration;

	public MorseDitDurationEvent(int ditDuration) {
		this.ditDuration = ditDuration;
	}

	public int getDitDuration() {
		return ditDuration;
	}
}
