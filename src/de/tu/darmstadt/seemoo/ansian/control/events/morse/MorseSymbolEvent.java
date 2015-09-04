package de.tu.darmstadt.seemoo.ansian.control.events.morse;

import de.tu.darmstadt.seemoo.ansian.tools.StringFormatter;

public class MorseSymbolEvent {

	private String currentSymbolCode;
	private String symbol;
	private boolean recognized;
	private float symbolSuccessRate;

	public MorseSymbolEvent(String currentSymbolCode, String symbol, boolean recognized, float symbolSuccessRate) {
		this.currentSymbolCode = currentSymbolCode;
		this.symbol = symbol;
		this.recognized = recognized;
		this.symbolSuccessRate = symbolSuccessRate;
	}

	public float getSuccessRate() {
		return symbolSuccessRate;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getCurrentSymbolCode() {
		return currentSymbolCode;
	}

	public boolean isRecognized() {
		return recognized;

	}

	public String getSuccessRateString() {
		return StringFormatter.formatPercent(symbolSuccessRate);
	}

}
