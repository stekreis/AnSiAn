package de.tu.darmstadt.seemoo.ansian.drawables;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import de.tu.darmstadt.seemoo.ansian.model.preferences.ColorPreference;
import de.tu.darmstadt.seemoo.ansian.model.preferences.MiscPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * Drawable for the dB scale in AnalyzerSurface
 *
 */
public class PowerGridDrawable extends MyDrawable {

	MiscPreferences preferences;

	private int gridWidth = 100;
	private int gridHeight = 200;

	public PowerGridDrawable(int gridHeight) {
		preferences = Preferences.MISC_PREFERENCE;
		this.gridWidth = Preferences.GUI_PREFERENCE.getGridSize();
		this.gridHeight = gridHeight;
	}

	public void setDimensions(int height) {
		this.gridWidth = Preferences.GUI_PREFERENCE.getGridSize();
		this.gridHeight = height;
	}

	@Override
	public void draw(Canvas canvas) {
		final float minDB = Preferences.GUI_PREFERENCE.getCurMinDB();
		final float maxDB = Preferences.GUI_PREFERENCE.getCurMaxDB();
		// final int gridSize = analyzerSurface.getGridSize();
		// final int fftHeight = analyzerSurface.getFftHeight();
		// Calculate pixel height of a minor tick (1dB)
		float pixelPerMinorTick = (float) (gridHeight / (maxDB - minDB));

		// Draw the ticks from the top to the bottom. Stop as soon as we
		// interfere with the frequency scale
		int tickDB = (int) maxDB;
		float tickPos = (maxDB - tickDB) * pixelPerMinorTick;
		for (; tickDB > minDB; tickDB--) {
			float tickWidth;
			if (tickDB % 10 == 0) {
				// Major Tick (10dB)
				tickWidth = (float) ((gridWidth) / 3.0);
				// Draw Frequency Text:
				canvas.drawText("" + tickDB, (float) (gridWidth / 2.9), tickPos, ColorPreference.TEXT_PAINT);
			} else if (tickDB % 5 == 0) {
				// 5 dB tick
				tickWidth = (float) (gridWidth / 3.5);
			} else {
				// Minor tick
				tickWidth = (float) (gridWidth / 5.0);
			}
			canvas.drawLine(0, tickPos, tickWidth, tickPos, ColorPreference.TEXT_PAINT);
			tickPos += pixelPerMinorTick;

			// stop if we interfere with the frequency grid:
			if (tickPos > gridHeight - gridWidth)
				break;
		}
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return 0;
	}

}
