package de.tu.darmstadt.seemoo.ansian.control.events;

public class BandwidthEvent {
	private int bandwidth;

	public BandwidthEvent(int bandwidth) {
		this.bandwidth = bandwidth;
	}

	public int getBandwidth() {
		return bandwidth;
	}

}
