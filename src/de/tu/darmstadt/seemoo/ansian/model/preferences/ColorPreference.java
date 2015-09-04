package de.tu.darmstadt.seemoo.ansian.model.preferences;

import android.graphics.Color;
import android.graphics.Paint;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.R;

public class ColorPreference extends MySharedPreferences {

	public ColorPreference(MainActivity activity) {
		super(activity);
	}

	public static Paint DEFAULT_PAINT = null; // Paint object to draw bitmaps on
												// the canvas
	public static Paint BLACK_PAINT = null; // Paint object to draw black
											// (erase)
	public static Paint FFT_PAINT = null; // Paint object to draw the fft lines
	public static Paint PEAK_HOLD_PAINT = null; // Paint object to draw the fft
												// peak hold points
	public static Paint WATERFALL_LINE_PAINT = null;// Paint object to draw one
													// waterfall pixel
	public static Paint TEXT_PAINT = null; // Paint object to draw text on the
											// canvas
	public static Paint TEXT_SMALL_PAINT = null; // Paint object to draw small
													// text on the canvas
	public static Paint DEMOD_SELECTOR_PAINT = null;// Paint object to draw the
													// area of the channel
	public static Paint SQUELCH_PAINT = null; // Paint object to draw the
												// squelch selector
	public static Paint SQUELCH_PAINT_SATISFIED = null; // Paint object to draw
														// the
	// squelch selector

	@Override
	public void loadPreference() {
		DEFAULT_PAINT = new Paint();
		BLACK_PAINT = new Paint();
		BLACK_PAINT.setColor(Color.BLACK);
		WATERFALL_LINE_PAINT = new Paint();
		FFT_PAINT = new Paint();
		FFT_PAINT.setColor(getInt("color_fft", Color.BLUE));
		FFT_PAINT.setStyle(Paint.Style.FILL);
		PEAK_HOLD_PAINT = new Paint();
		PEAK_HOLD_PAINT.setColor(getInt("color_peak_hold", Color.YELLOW));
		TEXT_PAINT = new Paint();
		TEXT_PAINT.setColor(getInt("color_text", Color.WHITE));
		TEXT_PAINT.setAntiAlias(true);
		TEXT_SMALL_PAINT = new Paint();
		TEXT_SMALL_PAINT.setColor(getInt("color_text_small", Color.WHITE));
		TEXT_SMALL_PAINT.setAntiAlias(true);
		DEMOD_SELECTOR_PAINT = new Paint();
		DEMOD_SELECTOR_PAINT.setColor(getInt("color_demod_selector", Color.WHITE));
		SQUELCH_PAINT = new Paint();
		SQUELCH_PAINT.setColor(getInt("color_squelch", Color.RED));
		SQUELCH_PAINT_SATISFIED = new Paint();
		SQUELCH_PAINT_SATISFIED.setColor(getInt("color_squelch", Color.GREEN));
	}

	@Override
	public void savePreference() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {

		return "color";
	}

	@Override
	public int getResID() {

		return R.xml.color_preferences;
	}

}
