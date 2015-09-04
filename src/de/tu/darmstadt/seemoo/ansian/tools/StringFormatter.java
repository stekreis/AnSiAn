package de.tu.darmstadt.seemoo.ansian.tools;

import java.text.MessageFormat;

public class StringFormatter {

	public static String formatPercent(float f) {
		return MessageFormat.format("{0,number,#.##%}", f);
	}

	public static String formatThreshold(float threshold) {
		return MessageFormat.format("{0,number,#.##}", threshold);
	}

}
