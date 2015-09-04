package de.tu.darmstadt.seemoo.ansian.control.events.morse;

import de.tu.darmstadt.seemoo.ansian.model.demodulation.morse.Morse.State;

public class RequestMorseStateEvent {

	private State state;

	public RequestMorseStateEvent(State state) {
		this.state = state;
	}

	public State getState() {
		return state;
	}
}
