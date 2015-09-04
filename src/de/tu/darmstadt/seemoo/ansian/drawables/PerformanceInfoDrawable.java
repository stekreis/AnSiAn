package de.tu.darmstadt.seemoo.ansian.drawables;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import de.tu.darmstadt.seemoo.ansian.control.DataHandler;
import de.tu.darmstadt.seemoo.ansian.control.SourceControl;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.control.threads.SurfaceUpdateThread;
import de.tu.darmstadt.seemoo.ansian.model.preferences.ColorPreference;
import de.tu.darmstadt.seemoo.ansian.model.preferences.GuiPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;
import de.tu.darmstadt.seemoo.ansian.model.sources.HackrfSource;
import de.tu.darmstadt.seemoo.ansian.model.sources.IQSourceInterface;
import de.tu.darmstadt.seemoo.ansian.model.sources.RtlsdrSource;

/**
 * Drawable which shows performance and frequency information in AnalyzerSurface
 *
 */

public class PerformanceInfoDrawable extends MyDrawable {

	GuiPreferences guiPreferences = Preferences.GUI_PREFERENCE;
	private int height;
	private int width;

	public PerformanceInfoDrawable(int height, int width) {
		this.height = height;
		this.width = width;
	}

	public void setDimensions(int height, int width) {
		this.height = height;
		this.width = width;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void draw(Canvas canvas) {
		final float minDB = guiPreferences.getCurMinDB();
		final float maxDB = guiPreferences.getCurMaxDB();
		final long guiFrequency = guiPreferences.getFrequency();
		final long demodFrequency = guiPreferences.getDemodFrequency();
		final float squelch = guiPreferences.getSquelch();
		final boolean showDebugInformation = guiPreferences.isShowDebugInformation();
		// final boolean demodulationEnabled = StateHandler.isDemodulating();
		final boolean displayRelativeFrequencies = guiPreferences.isDisplayRelativeFrequencies();
		final int frameRate = SurfaceUpdateThread.getFrameRate();
		final double load = SurfaceUpdateThread.getLoad();
		Rect bounds = new Rect();
		String text;
		float yPos = height * 0.01f;
		float rightBorder = width * 0.99f;
		IQSourceInterface source = SourceControl.getSource();

		// Source name and information
		if (source != null) {
			// Name
			text = source.getName();
			ColorPreference.TEXT_SMALL_PAINT.getTextBounds(text, 0, text.length(), bounds);
			canvas.drawText(text, rightBorder - bounds.width(), yPos + bounds.height(),
					ColorPreference.TEXT_SMALL_PAINT);
			yPos += bounds.height() * 1.1f;

			// Frequency
			text = String.format("tuned to %4.6f MHz", guiFrequency / 1000000f);
			ColorPreference.TEXT_SMALL_PAINT.getTextBounds(text, 0, text.length(), bounds);
			canvas.drawText(text, rightBorder - bounds.width(), yPos + bounds.height(),
					ColorPreference.TEXT_SMALL_PAINT);
			yPos += bounds.height() * 1.1f;

			// Center Frequency
			if (displayRelativeFrequencies) {
				text = String.format("centered at %4.6f MHz", guiFrequency / 1000000f);
				ColorPreference.TEXT_SMALL_PAINT.getTextBounds(text, 0, text.length(), bounds);
				canvas.drawText(text, rightBorder - bounds.width(), yPos + bounds.height(),
						ColorPreference.TEXT_SMALL_PAINT);
				yPos += bounds.height() * 1.1f;
			}

			// HackRF specific stuff:
			if (source instanceof HackrfSource) {
				text = String.format("shift=%4.6f MHz",
						Preferences.MISC_PREFERENCE.getHackrfFrequencyShift() / 1000000f);
				ColorPreference.TEXT_SMALL_PAINT.getTextBounds(text, 0, text.length(), bounds);
				canvas.drawText(text, rightBorder - bounds.width(), yPos + bounds.height(),
						ColorPreference.TEXT_SMALL_PAINT);
				yPos += bounds.height() * 1.1f;
			}
			// RTLSDR specific stuff:
			if (source instanceof RtlsdrSource) {
				text = String.format("shift=%4.6f MHz",
						Preferences.MISC_PREFERENCE.getRtlsdrFrequencyShift() / 1000000f);
				ColorPreference.TEXT_SMALL_PAINT.getTextBounds(text, 0, text.length(), bounds);
				canvas.drawText(text, rightBorder - bounds.width(), yPos + bounds.height(),
						ColorPreference.TEXT_SMALL_PAINT);
				yPos += bounds.height() * 1.1f;

				text = "sps=" + SourceControl.getSource().getSampleRate();
				ColorPreference.TEXT_SMALL_PAINT.getTextBounds(text, 0, text.length(), bounds);
				canvas.drawText(text, rightBorder - bounds.width(), yPos + bounds.height(),
						ColorPreference.TEXT_SMALL_PAINT);
				yPos += bounds.height() * 1.1f;
			}
		}

		// Draw the channel frequency if demodulation is enabled:
		if (StateHandler.isDemodulating()) {
			text = String.format("demod at %4.6f MHz", demodFrequency / 1000000f);
			ColorPreference.TEXT_SMALL_PAINT.getTextBounds(text, 0, text.length(), bounds);
			canvas.drawText(text, rightBorder - bounds.width(), yPos + bounds.height(),
					ColorPreference.TEXT_SMALL_PAINT);

			// increase yPos:
			yPos += bounds.height() * 1.1f;

			// Draw the average signal strength indicator if demodulation is
			// enabled

			float averageSignalStrength = DataHandler.getInstance().getLastFFTSample().getAverage(demodFrequency,
					Preferences.GUI_PREFERENCE.getDemodBandwidth());
			text = String.format("%2.1f dB", averageSignalStrength);
			ColorPreference.TEXT_SMALL_PAINT.getTextBounds(text, 0, text.length(), bounds);

			float indicatorWidth = width / 10;
			float indicatorPosX = rightBorder - indicatorWidth;
			float indicatorPosY = yPos + bounds.height();
			float squelchTickPos = (squelch - minDB) / (maxDB - minDB) * indicatorWidth;

			float signalWidth = (averageSignalStrength - minDB) / (maxDB - minDB) * indicatorWidth;
			if (signalWidth < 0)
				signalWidth = 0;
			if (signalWidth > indicatorWidth)
				signalWidth = indicatorWidth;

			// draw signal rectangle:
			guiPreferences.setSquelchSatisfied(squelch < averageSignalStrength);
			canvas.drawRect(indicatorPosX, yPos + bounds.height() * 0.1f, indicatorPosX + signalWidth, indicatorPosY,
					guiPreferences.isSquelchSatisfied() ? ColorPreference.SQUELCH_PAINT_SATISFIED
							: ColorPreference.SQUELCH_PAINT);

			// draw left border, right border, bottom line and squelch tick:
			canvas.drawLine(indicatorPosX, indicatorPosY, indicatorPosX, yPos, ColorPreference.TEXT_PAINT);
			canvas.drawLine(rightBorder, indicatorPosY, rightBorder, yPos, ColorPreference.TEXT_PAINT);
			canvas.drawLine(indicatorPosX, indicatorPosY, rightBorder, indicatorPosY, ColorPreference.TEXT_PAINT);
			canvas.drawLine(indicatorPosX + squelchTickPos, indicatorPosY + 2, indicatorPosX + squelchTickPos,
					yPos + bounds.height() * 0.5f, ColorPreference.TEXT_PAINT);

			// draw text:
			canvas.drawText(text, indicatorPosX - bounds.width() * 1.1f, indicatorPosY,
					ColorPreference.TEXT_SMALL_PAINT);

			// increase yPos:
			yPos += bounds.height() * 1.1f;
		}

		// Draw recording information
		if (StateHandler.isRecording()) {
			text = String.format("%4.6f MHz @ %2.3f MSps", source.getFrequency() / 1000000f,
					source.getSampleRate() / 1000000f);
			ColorPreference.TEXT_SMALL_PAINT.getTextBounds(text, 0, text.length(), bounds);
			canvas.drawText(text, rightBorder - bounds.width(), yPos + bounds.height(),
					ColorPreference.TEXT_SMALL_PAINT);
			ColorPreference.DEFAULT_PAINT.setColor(Color.RED);
			canvas.drawCircle(rightBorder - bounds.width() - (bounds.height() / 2) * 1.3f, yPos + bounds.height() / 2,
					bounds.height() / 2, ColorPreference.DEFAULT_PAINT);

			// increase yPos:
			yPos += bounds.height() * 1.1f;
		}

		if (showDebugInformation) {
			// Draw the FFT/s rate
			text = frameRate + " FPS";
			ColorPreference.TEXT_SMALL_PAINT.getTextBounds(text, 0, text.length(), bounds);
			canvas.drawText(text, rightBorder - bounds.width(), yPos + bounds.height(),
					ColorPreference.TEXT_SMALL_PAINT);
			yPos += bounds.height() * 1.1f;

			// Draw the load
			text = String.format("%3.1f %%", load * 100);
			ColorPreference.TEXT_SMALL_PAINT.getTextBounds(text, 0, text.length(), bounds);
			canvas.drawText(text, rightBorder - bounds.width(), yPos + bounds.height(),
					ColorPreference.TEXT_SMALL_PAINT);
			yPos += bounds.height() * 1.1f;
		}

	}

}
