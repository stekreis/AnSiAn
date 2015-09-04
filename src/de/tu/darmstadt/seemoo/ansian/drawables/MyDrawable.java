package de.tu.darmstadt.seemoo.ansian.drawables;

import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import de.tu.darmstadt.seemoo.ansian.model.preferences.ColorPreference;

/**
 * General Drawable class
 *
 */

public abstract class MyDrawable extends Drawable {
	private int alpha;

	protected Rect createRect() {
		// Calculate the min space (in px) between text if we want it separated
		// by at least
		// the same space as two dashes would consume.
		Rect bounds = new Rect();
		ColorPreference.TEXT_PAINT.getTextBounds("--", 0, 2, bounds);
		return bounds;
	}

	@Override
	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	@Override
	public int getAlpha() {
		return alpha;
	}

	@Override
	public void setColorFilter(ColorFilter cf) {

	}

	@Override
	public int getOpacity() {
		return 0;
	}

}
