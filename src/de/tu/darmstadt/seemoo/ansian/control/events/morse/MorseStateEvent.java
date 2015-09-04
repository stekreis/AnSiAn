package de.tu.darmstadt.seemoo.ansian.control.events.morse;

import de.tu.darmstadt.seemoo.ansian.model.demodulation.morse.Morse.State;

public class MorseStateEvent {

	private State state;

	public MorseStateEvent(State state) {
		this.state = state;
	}

	public State getState() {
		return state;
	}

}
