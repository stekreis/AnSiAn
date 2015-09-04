package de.tu.darmstadt.seemoo.ansian.control.events.morse;

import android.widget.Toast;
import de.tu.darmstadt.seemoo.ansian.gui.misc.MyToast;

public class MorseDitEvent {

	private int dit;

	public MorseDitEvent(int dit) {
		this.dit = dit;
		MyToast.makeText("Timings initialized, one dit is about " + dit + " ms.", Toast.LENGTH_LONG);
	}

	public int getDit() {
		return dit;
	}
}
