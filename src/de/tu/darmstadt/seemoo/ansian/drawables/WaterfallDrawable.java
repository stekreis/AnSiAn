package de.tu.darmstadt.seemoo.ansian.drawables;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import de.tu.darmstadt.seemoo.ansian.control.DataHandler;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.model.FFTDrawData;
import de.tu.darmstadt.seemoo.ansian.model.FFTSample;
import de.tu.darmstadt.seemoo.ansian.model.WaterfallColorMap;
import de.tu.darmstadt.seemoo.ansian.model.preferences.ColorPreference;
import de.tu.darmstadt.seemoo.ansian.model.preferences.GuiPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.MiscPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * Drawable which takes care of the waterfall drawing below the FFT spectrum
 *
 */

public class WaterfallDrawable extends MyDrawable {

	MiscPreferences preferences;
	GuiPreferences guiPreferences;
	Bitmap[] waterfallLines;
	private int waterfallLinesTopIndex;
	private int rowHeight = 1;
	private int yPos;
	private int width;
	private int height;

	private String LOGTAG = "WaterfallDrawable";
	private long lastFrequecy = Preferences.GUI_PREFERENCE.getFrequency();

	public WaterfallDrawable(int yPos, int width, int height) {
		preferences = Preferences.MISC_PREFERENCE;
		guiPreferences = Preferences.GUI_PREFERENCE;
		rowHeight = guiPreferences.getWaterfallRowHeight();
		this.yPos = yPos;
		this.width = width;
		this.height = height;
		createWaterfallLineBitmaps();
	}

	/**
	 * Will initialize the waterfallLines array for the given width and height
	 * of the waterfall plot. If the array is not null, it will be recycled
	 * first.
	 */

	public void createWaterfallLineBitmaps() {
		// Recycle bitmaps if not null:
		if (waterfallLines != null) {
			for (Bitmap b : this.waterfallLines)
				b.recycle();
		}

		// Create new array:
		waterfallLinesTopIndex = 0;
		waterfallLines = new Bitmap[height];
		for (int i = 0; i < waterfallLines.length; i++)
			waterfallLines[i] = Bitmap.createBitmap(width, 1, Bitmap.Config.ARGB_8888);
	}

	public void setDimensions(int yPos, int width, int height) {
		// remove old waterfall bitmaps if display width changed
		if (this.width != width || this.height != height || this.yPos != yPos) {
			waterfallLines = null;
			this.yPos = yPos;
			this.width = width;
			this.height = height;
			createWaterfallLineBitmaps();
		}

	}

	private boolean isChanged() {
		if (lastFrequecy != Preferences.GUI_PREFERENCE.getFrequency()) {
			lastFrequecy = Preferences.GUI_PREFERENCE.getFrequency();
			return true;
		} else
			return false;
	}

	@Override
	public void draw(Canvas canvas) {
		if (Preferences.GUI_PREFERENCE.isWaterfallPaused() && StateHandler.isPaused()) {
			if (isChanged()) {
				FFTSample[] ffts = DataHandler.getInstance().getSamples(height - yPos);
				for (FFTSample fft : ffts)
					if (fft != null)
						drawNewWaterfallLine(fft.getDrawData(width));
			} else
				drawOldWaterfallBitmaps(canvas);
		} else {
			drawOldWaterfallBitmaps(canvas);
			FFTDrawData fftDrawData;
			if (StateHandler.isScanning()) {
				fftDrawData = DataHandler.getInstance().getScannerDrawData(width);
			} else {
				fftDrawData = DataHandler.getInstance().getWaterfallDrawData(width);
			}
			drawNewWaterfallLine(fftDrawData);
		}

	}

	private void drawOldWaterfallBitmaps(Canvas canvas) {
		int tempYPos = yPos;
		// draw the bitmaps on the canvas:
		for (int i = 0; i < waterfallLines.length; i++) {
			int idx = (waterfallLinesTopIndex + i) % waterfallLines.length;
			canvas.drawBitmap(waterfallLines[idx], 0, tempYPos, ColorPreference.DEFAULT_PAINT);
			tempYPos += rowHeight;
		}
	}

	private void drawNewWaterfallLine(FFTDrawData fftDrawData) {
		int startX = 0;
		float[] magnitudes = null;
		if (fftDrawData != null) {
			startX = fftDrawData.getStart();
			magnitudes = fftDrawData.getValues();
		}
		if (magnitudes != null) {
			// Log.d(LOGTAG, "drawing waterfall. mag length: " +
			// magnitudes.length);
			Canvas newline = new Canvas(waterfallLines[waterfallLinesTopIndex]);
			newline.drawColor(Color.BLACK);

			// move the array index (note that we have to decrement in order to
			// do
			// it correctly)
			waterfallLinesTopIndex--;
			if (waterfallLinesTopIndex < 0) {
				waterfallLinesTopIndex += waterfallLines.length;
			}
			float minDB = Preferences.GUI_PREFERENCE.getCurMinDB();
			float maxDB = Preferences.GUI_PREFERENCE.getCurMaxDB();
			float dbDiff = maxDB - minDB;
			WaterfallColorMap colormap = Preferences.GUI_PREFERENCE.getWaterfallColorMap();
			float scale = colormap.getLength() / dbDiff;

			int px = startX;
			for (int magPos = 0; magPos < magnitudes.length; magPos++) {
				// Waterfall:
				if (magnitudes[magPos] <= minDB)
					ColorPreference.WATERFALL_LINE_PAINT.setColor(colormap.getColor(0));
				else if (magnitudes[magPos] >= maxDB)
					ColorPreference.WATERFALL_LINE_PAINT.setColor(colormap.getColor(colormap.getLength() - 1));
				else
					ColorPreference.WATERFALL_LINE_PAINT
							.setColor(colormap.getColor((int) ((magnitudes[magPos] - minDB) * scale)));
				newline.drawPoint(px++, 0, ColorPreference.WATERFALL_LINE_PAINT);
			}

		}

	}

}
