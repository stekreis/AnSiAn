package de.tu.darmstadt.seemoo.ansian.control.events.morse;

import android.text.Editable;

public class MorseEditTextEvent {

	private Editable text;
	private String morseCodeTemp;

	public MorseEditTextEvent(Editable text, String morseCodeTemp) {
		this.text = text;
		this.morseCodeTemp = morseCodeTemp;
	}

	public Editable getText() {
		return text;
	}

	public String getMorseCodeTemp() {
		return morseCodeTemp;
	}
}
