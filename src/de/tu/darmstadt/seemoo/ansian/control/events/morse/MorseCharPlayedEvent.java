package de.tu.darmstadt.seemoo.ansian.control.events.morse;

public class MorseCharPlayedEvent {
	private int c;

	public MorseCharPlayedEvent(int counter) {
		this.c = counter;
	}

	public int getCounter() {
		return c;
	}
}
