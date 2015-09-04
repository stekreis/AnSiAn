package de.tu.darmstadt.seemoo.ansian.control.events;

import de.tu.darmstadt.seemoo.ansian.control.StateHandler;

public class StateEvent {
	private StateHandler.State state;

	public StateEvent(StateHandler.State state) {
		this.state = state;
	}

	public StateHandler.State getState() {
		return state;
	}
}
