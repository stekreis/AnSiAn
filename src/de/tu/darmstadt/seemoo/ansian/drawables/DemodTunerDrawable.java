package de.tu.darmstadt.seemoo.ansian.drawables;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Rect;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.control.events.SquelchChangeEvent;
import de.tu.darmstadt.seemoo.ansian.control.threads.Demodulator;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.Demodulation;
import de.tu.darmstadt.seemoo.ansian.model.preferences.ColorPreference;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * 
 * @author Steffen Kreis
 * 
 *         Drawable which shows the tuned demodulation frequency, demod
 *         bandwidth and squelch
 *
 */
public class DemodTunerDrawable extends MyDrawable {

	private int fftWidth;
	private int fftHeight;
	private float squelch;
	private float leftBorder;
	private float rightBorder;
	private String LOGTAG = "DemodTunerDrawable";

	public DemodTunerDrawable(int width, int height) {
		this.fftWidth = width;
		this.fftHeight = height;
		squelch = Preferences.GUI_PREFERENCE.getSquelch();
		EventBus.getDefault().register(this);
	}

	public void setDimensions(int width, int height) {
		this.fftWidth = width;
		this.fftHeight = height;
	}

	public float getLowerBWEnd() {
		return leftBorder;
	}

	public float getUpperBWEnd() {
		return rightBorder;
	}

	@SuppressLint("DefaultLocale")
	public void draw(Canvas c) {
		if (StateHandler.isDemodulating()) {
			Demodulation demodulation = Demodulator.getDemodulation();
			final long bandwidth = Preferences.GUI_PREFERENCE.getBandwidth();
			final long demodBandwidth = Math.max(
					Math.min(Preferences.GUI_PREFERENCE.getDemodBandwidth(), demodulation.getMaxUserFilterWidth()),
					demodulation.getMinUserFilterWidth());
			final long demodFrequency = Preferences.GUI_PREFERENCE.getDemodFrequency();
			final long centerFrequency = Preferences.GUI_PREFERENCE.getFrequency();
			float minDB = Preferences.GUI_PREFERENCE.getCurMinDB();
			float maxDB = Preferences.GUI_PREFERENCE.getCurMaxDB();
			String textStr;
			Rect bounds = createRect();
			float pxPerHz = fftWidth / (float) bandwidth;

			float channelPosition = fftWidth / 2 - pxPerHz * (centerFrequency - demodFrequency);

			leftBorder = channelPosition - pxPerHz * demodBandwidth;
			rightBorder = channelPosition + pxPerHz * demodBandwidth;

			float dbWidth = fftHeight / (maxDB - minDB);
			float squelchPosition = fftHeight - (squelch - minDB) * dbWidth;

			boolean showLowerBand = demodulation.isLowerBandShown();
			boolean showUpperBand = demodulation.isUpperBandShown();

			// draw half transparent channel area:
			ColorPreference.DEMOD_SELECTOR_PAINT.setAlpha(0x7f);
			if (showLowerBand)
				c.drawRect(leftBorder, squelchPosition, channelPosition, fftHeight,
						ColorPreference.DEMOD_SELECTOR_PAINT);
			if (showUpperBand)
				c.drawRect(channelPosition, squelchPosition, rightBorder, fftHeight,
						ColorPreference.DEMOD_SELECTOR_PAINT);

			// draw center and borders:
			ColorPreference.DEMOD_SELECTOR_PAINT.setAlpha(0xff);
			c.drawLine(channelPosition, fftHeight, channelPosition, 0, ColorPreference.DEMOD_SELECTOR_PAINT);
			if (showLowerBand) {
				c.drawLine(leftBorder, fftHeight, leftBorder, 0, ColorPreference.DEMOD_SELECTOR_PAINT);
				c.drawLine(leftBorder, squelchPosition, channelPosition, squelchPosition,
						ColorPreference.SQUELCH_PAINT);
			}
			if (showUpperBand) {
				c.drawLine(rightBorder, fftHeight, rightBorder, 0, ColorPreference.DEMOD_SELECTOR_PAINT);
				c.drawLine(channelPosition, squelchPosition, rightBorder, squelchPosition,
						ColorPreference.SQUELCH_PAINT);
			}

			// draw squelch text above the squelch selector:
			textStr = String.format("%2.1f dB", squelch);
			ColorPreference.TEXT_SMALL_PAINT.getTextBounds(textStr, 0, textStr.length(), bounds);
			c.drawText(textStr, channelPosition - bounds.width() / 2f, squelchPosition - bounds.height() * 0.1f,
					ColorPreference.TEXT_SMALL_PAINT);

			// draw channel width text below the squelch selector:
			int shownChannelWidth = 0;
			if (showLowerBand)
				shownChannelWidth += demodBandwidth;
			if (showUpperBand)
				shownChannelWidth += demodBandwidth;
			textStr = String.format("%d kHz", shownChannelWidth / 1000);
			ColorPreference.TEXT_SMALL_PAINT.getTextBounds(textStr, 0, textStr.length(), bounds);
			c.drawText(textStr, channelPosition - bounds.width() / 2f, squelchPosition + bounds.height() * 1.1f,
					ColorPreference.TEXT_SMALL_PAINT);
		}
	}

	@Subscribe
	public void onEvent(SquelchChangeEvent event) {
		squelch = event.getSquelch();
	}

}
