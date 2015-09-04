package de.tu.darmstadt.seemoo.ansian.control.events;

import de.tu.darmstadt.seemoo.ansian.control.StateHandler.State;

public class RequestStateEvent {

	private State requestedState;

	public RequestStateEvent(State requestedState) {
		this.requestedState = requestedState;
	}

	public State getRequestState() {
		return requestedState;
	}
}
