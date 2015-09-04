package de.tu.darmstadt.seemoo.ansian.gui.views.morse;

public class MorseSaveEvent {

	private String text;

	public MorseSaveEvent(String text) {
		this.setText(text);

	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
