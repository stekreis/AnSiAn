package de.tu.darmstadt.seemoo.ansian.control.events;

public class ScanAreaUpdateEvent {
	private long lowerfrequency;
	private long upperfrequency;
	private long samplerate;
	private int scanCropDataFactor;

	public ScanAreaUpdateEvent(long lowerFrequency, long upperFrequency, long samplerate, int scanCropDataFactor) {
		this.lowerfrequency = lowerFrequency;
		this.upperfrequency = upperFrequency;
		this.samplerate = samplerate;
		this.scanCropDataFactor = scanCropDataFactor;
	}

	public long getLowerFrequency() {
		return lowerfrequency;
	}

	public long getUpperFrequency() {
		return upperfrequency;
	}

	public long getSamplerate() {
		return samplerate;
	}

	public int getScanCropDataFactor() {
		return scanCropDataFactor;
	}
}
