package de.tu.darmstadt.seemoo.ansian.drawables;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Rect;
import de.tu.darmstadt.seemoo.ansian.model.preferences.ColorPreference;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * Drawable to show a scale matching the shown frequency spectrum
 *
 */

public class FrequencyGridDrawable extends MyDrawable {

	private int gridSize;
	private int fftWidth;
	private int fftHeight;
	private boolean relFreq;

	public void setFftWidth(int width) {
		this.fftWidth = width;
	}

	public void setFftHeight(int height) {
		this.fftHeight = height;
	}

	public void setDisplayRelativeFrequencies(boolean relFreq) {
		this.relFreq = relFreq;
	}

	public void init(int width, int height, boolean relfreq) {
		gridSize = Preferences.GUI_PREFERENCE.getGridSize();
		setFftHeight(height);
		setFftWidth(width);
		setDisplayRelativeFrequencies(relfreq);
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void draw(Canvas canvas) {
		String textStr;
		double MHZ = 1000000F;
		double tickFreqMHz;
		final float bandwidth = Preferences.GUI_PREFERENCE.getBandwidth();
		final long guiFrequency = Preferences.GUI_PREFERENCE.getFrequency();
		float lastTextEndPos = -99999; // will indicate the horizontal pixel pos
										// where the last text ended
		float textPos;

		Rect bounds = createRect();
		float minFreeSpaceBetweenText = bounds.width();

		// Calculate span of a minor tick (must be a power of 10KHz)
		int tickSize = 10; // we start with 10KHz
		float helperVar = bandwidth / 20f;
		while (helperVar > 100) {
			helperVar = helperVar / 10f;
			tickSize = tickSize * 10;
		}

		// Calculate pixel width of a minor tick
		float pixelPerMinorTick = fftWidth / (bandwidth / (float) tickSize);

		// Calculate the frequency at the left most point of the fft:
		long startFrequency;
		if (relFreq)
			startFrequency = (long) (-1 * (bandwidth / 2.0));
		else
			startFrequency = (long) (guiFrequency - (bandwidth / 2.0));

		// Calculate the frequency and position of the first Tick (ticks are
		// every <tickSize> KHz)
		long tickFreq = (long) (Math.ceil((double) startFrequency / (float) tickSize) * tickSize);
		float tickPos = pixelPerMinorTick / (float) tickSize * (tickFreq - startFrequency);

		// Draw the ticks
		for (int i = 0; i < bandwidth / (float) tickSize; i++) {
			float tickHeight;
			if (tickFreq % (tickSize * 10) == 0) {
				// Major Tick (10x <tickSize> KHz)
				tickHeight = (float) (gridSize / 2.0);

				// Draw Frequency Text (always in MHz)
				tickFreqMHz = tickFreq / MHZ;
				if (tickFreqMHz == (int) tickFreqMHz)
					textStr = String.format("%d", (int) tickFreqMHz);
				else
					textStr = String.format("%s", tickFreqMHz);
				ColorPreference.TEXT_PAINT.getTextBounds(textStr, 0, textStr.length(), bounds);
				textPos = tickPos - bounds.width() / 2;

				// ...only if not overlapping with the last text:
				if (lastTextEndPos + minFreeSpaceBetweenText < textPos) {
					canvas.drawText(textStr, textPos, fftHeight - tickHeight, ColorPreference.TEXT_PAINT);
					lastTextEndPos = textPos + bounds.width();
				}
			} else if (tickFreq % (tickSize * 5) == 0) {
				// Half major tick (5x <tickSize> KHz)
				tickHeight = (float) (gridSize / 3.0);

				// Draw Frequency Text (always in MHz)...
				tickFreqMHz = tickFreq / MHZ;
				if (tickFreqMHz == (int) tickFreqMHz)
					textStr = String.format("%d", (int) tickFreqMHz);
				else
					textStr = String.format("%s", tickFreqMHz);
				ColorPreference.TEXT_SMALL_PAINT.getTextBounds(textStr, 0, textStr.length(), bounds);
				textPos = tickPos - bounds.width() / 2;

				// ...only if not overlapping with the last text:
				if (lastTextEndPos + minFreeSpaceBetweenText < textPos) {
					// ... if enough space between the major ticks:
					if (bounds.width() < pixelPerMinorTick * 3) {
						canvas.drawText(textStr, textPos, fftHeight - tickHeight, ColorPreference.TEXT_SMALL_PAINT);
						lastTextEndPos = textPos + bounds.width();
					}
				}
			} else {
				// Minor tick (<tickSize> KHz)
				tickHeight = (float) (gridSize / 4.0);
			}

			// Draw the tick line:
			canvas.drawLine(tickPos, fftHeight, tickPos, fftHeight - tickHeight, ColorPreference.TEXT_PAINT);
			tickFreq += tickSize;
			tickPos += pixelPerMinorTick;
		}
	}

}
