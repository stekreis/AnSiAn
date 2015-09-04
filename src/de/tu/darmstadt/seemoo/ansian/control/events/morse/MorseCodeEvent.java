package de.tu.darmstadt.seemoo.ansian.control.events.morse;

import de.tu.darmstadt.seemoo.ansian.tools.StringFormatter;

public class MorseCodeEvent {

	private static StringBuffer completeCodeString = new StringBuffer();
	private String code;
	private long duration;
	private boolean high;
	private boolean inRange;
	private float successRate;
	private float threshold;

	public MorseCodeEvent(String code, boolean high, long duration, float successRate, float threshold) {
		this.threshold = threshold;
		this.code = code;
		this.high = high;
		this.successRate = successRate;
		this.duration = duration;
		inRange = code.length() == 1;
		if (inRange)
			completeCodeString.append(code);
	}

	public String getCode() {
		return code;
	}

	public boolean getHigh() {
		return high;
	}

	public long getDuration() {
		return duration;
	}

	public boolean isInRange() {
		return inRange;
	}

	public float getSuccessRate() {
		return successRate;
	}

	public String getSuccessRateString() {
		return StringFormatter.formatPercent(successRate);
	}

	public String getCompleteCodeString() {
		return completeCodeString.toString();
	}

	public float getThreshold() {
		return threshold;
	}

	public String getThresholdString() {
		return StringFormatter.formatThreshold(threshold);
	}
}
