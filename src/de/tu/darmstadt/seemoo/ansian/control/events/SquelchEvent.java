package de.tu.darmstadt.seemoo.ansian.control.events;

public class SquelchEvent {

	private boolean squelchSatisfied;

	public SquelchEvent(boolean squelchSatisfied) {
		this.squelchSatisfied = squelchSatisfied;
	}

	public boolean getSquelchSatisfied() {
		return squelchSatisfied;
	}

}
