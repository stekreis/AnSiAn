package de.tu.darmstadt.seemoo.ansian.gui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.control.events.ChangeChannelWidthEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.DemodScaleEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.DemodValueChangeEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.DemodulationEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.FrequencyEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.ScrollEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.SpectrumScaleEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.StateEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.TapEvent;
import de.tu.darmstadt.seemoo.ansian.control.threads.Demodulator;
import de.tu.darmstadt.seemoo.ansian.drawables.DemodTunerDrawable;
import de.tu.darmstadt.seemoo.ansian.drawables.FftSpectrumDrawable;
import de.tu.darmstadt.seemoo.ansian.drawables.FrequencyGridDrawable;
import de.tu.darmstadt.seemoo.ansian.drawables.PerformanceInfoDrawable;
import de.tu.darmstadt.seemoo.ansian.drawables.PowerGridDrawable;
import de.tu.darmstadt.seemoo.ansian.drawables.WaterfallDrawable;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * <h1>AnSiAn - Analyzer Surface</h1>
 *
 * Module: AnalyzerSurface.java Description: This is a custom view extending the
 * SurfaceView. It will show the frequency spectrum and the waterfall diagram.
 *
 * @author Dennis Mantz
 * @author Markus Grau
 * @author Steffen Kreis
 *
 *         Copyright (C) 2014 Dennis Mantz License:
 *         http://www.gnu.org/licenses/gpl.html GPL version 2 or higher
 *
 *         This library is free software; you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation; either version 2 of the License, or (at
 *         your option) any later version.
 *
 *         This library is distributed in the hope that it will be useful, but
 *         WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *         General Public License for more details.
 *
 *         You should have received a copy of the GNU General Public License
 *         along with this library; if not, write to the Free Software
 *         Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *         02110-1301 USA
 */
public class AnalyzerSurface extends MySurfaceView {

	public AnalyzerSurface(Context context) {
		super(context);
	}

	// organizes all drawables
	private LayerDrawable layDraw;

	// Drawables for info and data
	private DemodTunerDrawable demodTunerDrwb;
	private PowerGridDrawable powerGridDrwb;
	private FrequencyGridDrawable freqGridDrwb;
	private PerformanceInfoDrawable perfInfoDrwb;
	private FftSpectrumDrawable freqSpectrumDrwb;
	private WaterfallDrawable waterfallDrwb;
	// private MorseDrawable morseDrwb;

	private static final String LOGTAG = "AnalyzerSurface";

	private boolean displayRelativeFrequencies = false; // indicates whether
														// frequencies on the
														// horizontal axis
														// should be
														// relative to the
														// center frequency
														// (true) or absolute
														// (false)

	private int demodWidth = -1; // (half) width of the channel filter of the
									// demodulator
	// based on the area in which a touch/scroll/scale event first occured,
	// different operations are needed
	private int touchtype = 0;
	private static final int TOUCHTYPE_NORMAL = 1;
	private static final int TOUCHTYPE_DEMOD = 2;

	private boolean init = false;

	/**
	 * @param channelWidth
	 *            new channel width (cut-off frequency - single sided) of the
	 *            channel filter in Hz
	 */
	public void setChannelWidth(int channelWidth) {
		this.demodWidth = channelWidth;
	}

	/**
	 * @return true if frequencies on the horizontal axis are displayed relative
	 *         to center freq; false if absolute
	 */
	public boolean isDisplayRelativeFrequencies() {
		return displayRelativeFrequencies;
	}

	/**
	 * @param displayRelativeFrequencies
	 *            true if frequencies on the horizontal axis should be displayed
	 *            relative to center freq; false if absolute
	 */
	public void setDisplayRelativeFrequencies(boolean displayRelativeFrequencies) {
		this.displayRelativeFrequencies = displayRelativeFrequencies;
	}

	/**
	 * 
	 * update GUI components (e.g. when orientation changed)
	 */
	public void updateGUI() {
		powerGridDrwb.setDimensions(getFftHeight());
		demodTunerDrwb.setDimensions(getWidth(), getFftHeight());
		waterfallDrwb.setDimensions(getFftHeight(), getWidth(), getHeight());
		perfInfoDrwb.setDimensions(getHeight(), getWidth());
		freqSpectrumDrwb.init(getFftHeight(), getWidth());
		freqGridDrwb.init(getWidth(), getFftHeight(), isDisplayRelativeFrequencies());
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		Log.d(LOGTAG, "analyzersurfaceScale called");
		if ((detector.getFocusX() > Preferences.GUI_PREFERENCE.getGridSize() * 1.5)) {
			float xScale = detector.getCurrentSpanX() / detector.getPreviousSpanX();
			float yScale = detector.getCurrentSpanY() / detector.getPreviousSpanY();
			switch (touchtype) {
			case TOUCHTYPE_DEMOD:
				EventBus.getDefault().post(new DemodScaleEvent(xScale, yScale));
				break;
			case TOUCHTYPE_NORMAL:
				EventBus.getDefault().post(new SpectrumScaleEvent(xScale, yScale));
				break;
			}

		}
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// check if scrolling concerns demodulation
		if (StateHandler.isDemodulating()) {
			// check if touch was in spectrum area
			if (e.getY() < getFftHeight()) {
				// check if touch was in demod area
				float xPos = e.getX();
				if (xPos > demodTunerDrwb.getLowerBWEnd() && xPos < demodTunerDrwb.getUpperBWEnd()) {
					touchtype = TOUCHTYPE_DEMOD;
					return true;
				}
			}
		}
		touchtype = TOUCHTYPE_NORMAL;
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// not used
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// Set the channel frequency to the tapped position
		EventBus.getDefault().post(new TapEvent(e.getX() / getWidth(), e.getY() / getFftHeight()));
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

		switch (touchtype) {
		case TOUCHTYPE_NORMAL:
			EventBus.getDefault().post(new ScrollEvent(distanceX / getWidth(), distanceY / getHeight(), false));
			break;
		case TOUCHTYPE_DEMOD:
			EventBus.getDefault().post(new ScrollEvent(distanceX / getWidth(), distanceY / getHeight(), true));
		}

		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// not used
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return true;
	}

	// ------------------- </OnGestureListener>
	// ----------------------------------//

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (init) {
			boolean retVal = this.scaleGestureDetector.onTouchEvent(event);
			retVal = this.gestureDetector.onTouchEvent(event) || retVal;
			return retVal;
		}
		return false;
	}

	/**
	 * Returns the height of the fft plot in px (y coordinate of the bottom line
	 * of the fft spectrum)
	 *
	 * @return height (in px) of the fft
	 */
	public int getFftHeight() {
		return (int) (getHeight() * Preferences.GUI_PREFERENCE.getFFTRatio());
	}

	/**
	 * Returns the height of the waterfall plot in px
	 *
	 * @return heigth (in px) of the waterfall
	 */
	public int getWaterfallHeight() {
		return (int) (getHeight() * (1 - Preferences.GUI_PREFERENCE.getFFTRatio()));

	}

	/**
	 * Will (re-)draw the given data set on the surface. Note that it actually
	 * only draws a sub set of the fft data depending on the current settings of
	 * virtual frequency and sample rate.
	 *
	 * @param mag
	 *            array of magnitude values that represent the fft
	 * @param frequency
	 *            center frequency
	 * @param sampleRate
	 *            sample rate
	 * @param frameRate
	 *            current frame rate (FPS)
	 */

	public void draw() {
		if (layDraw == null)
			init();

		// Draw:
		Canvas dataCanvas = null;
		try {
			dataCanvas = this.getHolder().lockCanvas();
			synchronized (this.getHolder()) {
				if (dataCanvas != null) {
					dataCanvas.drawColor(Color.BLACK);
					updateGUI();
					layDraw.draw(dataCanvas);
				} else
					Log.d(LOGTAG, "draw: Canvas is null.");
			}
		} catch (Exception e) {
			Log.e(LOGTAG, "draw: Error while drawing on the canvas. Stop!");
			e.printStackTrace();
		} finally {
			if (dataCanvas != null) {
				this.getHolder().unlockCanvasAndPost(dataCanvas);
			}
		}
	}

	@Subscribe
	public void onEvent(DemodulationEvent event) {
		// DemoType demodulation = event.getDemodulation();

		// MorseDrawable highly experimental, not built in
		// if (DemoType.MORSE == event.getDemodulation()) {
		// if (morseDrwb == null) {
		// morseDrwb = new MorseDrawable(getWidth() / 2, getFftHeight(),
		// getWidth() / 2, 100);
		// }
		// morseDrwb.setState(true);
		// } else {
		// // morseDrwb.setState(false);
		// }

		EventBus.getDefault().post(new ChangeChannelWidthEvent(demodWidth));

	}

	private void init() {
		powerGridDrwb = new PowerGridDrawable(getFftHeight());
		freqGridDrwb = new FrequencyGridDrawable();
		perfInfoDrwb = new PerformanceInfoDrawable(getHeight(), getWidth());
		freqSpectrumDrwb = new FftSpectrumDrawable();
		waterfallDrwb = new WaterfallDrawable(getFftHeight(), getWidth(), getWaterfallHeight());
		demodTunerDrwb = new DemodTunerDrawable(getWidth(), getFftHeight());
		// if (morseDrwb == null) {
		// morseDrwb = new MorseDrawable(getWidth() / 2, getFftHeight() / 2,
		// getWidth() / 2, 100);
		// }
		layDraw = new LayerDrawable(new Drawable[] { waterfallDrwb, freqSpectrumDrwb, powerGridDrwb, freqGridDrwb,
				perfInfoDrwb, demodTunerDrwb });// , morseDrwb });
		init = true;
	}

	@Subscribe
	public void onEvent(StateEvent event) {
		if (init) {
			updateGUI();
			switch (event.getState()) {
			case SCANNING:
				if (Preferences.GUI_PREFERENCE.isScannerWaterfall())
					layDraw = new LayerDrawable(new Drawable[] { waterfallDrwb, freqSpectrumDrwb, powerGridDrwb,
							freqGridDrwb, perfInfoDrwb });
				else
					layDraw = new LayerDrawable(
							new Drawable[] { freqSpectrumDrwb, powerGridDrwb, freqGridDrwb, perfInfoDrwb });
				break;
			case MONITORING:
				layDraw = new LayerDrawable(new Drawable[] { waterfallDrwb, freqSpectrumDrwb, powerGridDrwb,
						freqGridDrwb, perfInfoDrwb, demodTunerDrwb });

			default:
				break;
			}
		}
	}

	@Subscribe
	public void onEvent(FrequencyEvent event) {
		freqSpectrumDrwb.resetPeaks();
	}

	@Subscribe
	public void onEvent(DemodValueChangeEvent event) {
		updateGUI();
	}

}
