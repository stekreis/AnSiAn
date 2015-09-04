package de.tu.darmstadt.seemoo.ansian.control.events;

import de.tu.darmstadt.seemoo.ansian.model.sources.IQSourceInterface;

public class SourceEvent {
	private IQSourceInterface source;

	public SourceEvent(IQSourceInterface source) {
		this.source = source;
	}

	public IQSourceInterface getSource() {
		return source;
	}
}
