package de.tu.darmstadt.seemoo.ansian.control.events.morse;

public class MorseSendEvent {

	private boolean sending;

	public MorseSendEvent(boolean b) {
		sending = b;
	}

	public boolean isSending() {
		return sending;
	}
}
