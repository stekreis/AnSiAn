package de.tu.darmstadt.seemoo.ansian.control.events;

public class ChannelFrequencyEvent {

	private long channelFrequency;

	public ChannelFrequencyEvent(long channelFrequency) {
		this.channelFrequency = channelFrequency;
	}

	public long getChannelFrequency() {
		return channelFrequency;
	}

}
