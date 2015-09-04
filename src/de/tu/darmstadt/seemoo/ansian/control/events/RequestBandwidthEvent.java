package de.tu.darmstadt.seemoo.ansian.control.events;

public class RequestBandwidthEvent {
	private int bandwidth;

	public RequestBandwidthEvent(int bandwidth) {
		this.bandwidth = bandwidth;
	}

	public int getBandwidth() {
		return bandwidth;
	}

}
