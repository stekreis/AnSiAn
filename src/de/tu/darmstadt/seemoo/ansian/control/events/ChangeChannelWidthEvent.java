package de.tu.darmstadt.seemoo.ansian.control.events;

public class ChangeChannelWidthEvent {

	private int channelWidth;

	public ChangeChannelWidthEvent(int channelWidth) {
		this.channelWidth = channelWidth;
	}

	public int getChannelWidth() {
		return channelWidth;
	}

}
