package de.tu.darmstadt.seemoo.ansian.model.preferences;

import android.content.pm.ActivityInfo;
import android.util.Log;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.R;
import de.tu.darmstadt.seemoo.ansian.control.SourceControl;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler.State;
import de.tu.darmstadt.seemoo.ansian.control.events.BandwidthEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.ChangeChannelWidthEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.DemodScaleEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.DemodValueChangeEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.DemodulationEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.FrequencyEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.RequestBandwidthEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.RequestFrequencyEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.RequestStateEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.ScaleEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.ScanAreaUpdateEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.ScrollEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.SpectrumScaleEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.SquelchChangeEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.StateEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.TapEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.WaterfallScaleEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.WaveFormScaleEvent;
import de.tu.darmstadt.seemoo.ansian.control.threads.Demodulator;
import de.tu.darmstadt.seemoo.ansian.model.WaterfallColorMap;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.Demodulation;
import de.tu.darmstadt.seemoo.ansian.model.sources.IQSourceInterface;

/**
 * Preferences concerning GUI and appropriate data representation
 * 
 * @author Markus Grau
 * @author Steffen Kreis
 *
 */
public class GuiPreferences extends MySharedPreferences {

	private final String LOGTAG = "GuiPreferences";
	private int bandwidth;
	private long centerFrequency;
	private long demodFrequency;
	private int maxBandwidth;
	private int minBandwidth = 1000;
	private int demodBandwidth;

	private float fftWaterfallRatio = 0.5f; // percentage of the height the fft
											// consumes
	// on the surface

	private static int waterfallRowHeight = 1;
	private static final float minDB = -100;
	private static final float maxDB = 10;
	private static final float mindBdiff = 10f;

	private static float curMinDB = -50;
	private static float curMaxDB = -5;
	private static int fftDrawingType = 2;
	private float squelch = -20;
	private boolean relativeFrequencies;
	private boolean debugInformation;
	private int fontSize; // Indicates the font size of the
	// grid labels
	private int frameRate;
	private boolean dynamicFramerate;
	private int colormapType;
	private WaterfallColorMap colormap;
	private boolean peakHold;
	private boolean pauseWaterfall;
	private boolean scannerWaterfall;
	private boolean autoscale;
	private boolean squelchSatisfied = false;
	private boolean hideTabs;

	private static enum FONT_SIZE {
		NULL, SMALL, MEDIUM, LARGE

	}

	public GuiPreferences(MainActivity activity) {
		super(activity);
		EventBus.getDefault().register(this);
	}

	public void loadPreference() {
		bandwidth = getInt("bandwidth", 2000000);
		maxBandwidth = getInt("max_bandwidth", 40000000);
		centerFrequency = getLong("center_frequency", 97000000);
		demodFrequency = getLong("demod_frequency", 97000000);
		demodBandwidth = getInt("demod_Bandwidth", 100000);
		relativeFrequencies = getBoolean("relative_frequencies", false);
		debugInformation = getBoolean("debug_information", false);
		setFontSize(getInt("font_size", 2));
		frameRate = getInt("framerate", 30);
		dynamicFramerate = getBoolean("dynamic_framerate", false);
		pauseWaterfall = getBoolean("pause_waterfall", false);
		scannerWaterfall = getBoolean("scanner_waterfall", false);
		autoscale = getBoolean("autoscale", false);
		hideTabs = getBoolean("hide_tabs", false);

		// FFT
		fftDrawingType = getInt("fft_drawing_type", 1);
		colormapType = getInt("colormap_type", 0);
		colormap = new WaterfallColorMap(colormapType);
		peakHold = getBoolean("peak_hold", false);
		fftWaterfallRatio = getFloat("fft_waterfall_ratio", 0.5f);

		// Screen Orientation:
		setScreenOrientation(getString("screen_orientation", "auto"));
	}

	private void setScreenOrientation(String screenOrientation) {
		if (screenOrientation.equals("auto"))
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		else if (screenOrientation.equals("landscape"))
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		else if (screenOrientation.equals("portrait"))
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		else if (screenOrientation.equals("reverse_landscape"))
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		else if (screenOrientation.equals("reverse_portrait"))
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);

	}

	public void savePreference() {
		// create editor
		MyEditor editor = edit();

		editor.putInt("bandwidth", bandwidth);
		editor.putInt("max_bandwidth", maxBandwidth);
		editor.putLong("center_frequency", centerFrequency);
		editor.putLong("demod_frequency", demodFrequency);
		editor.putLong("demod_bandwidth", demodBandwidth);
		editor.putBoolean("relative_frequencies", relativeFrequencies);
		editor.putBoolean("debug_information", debugInformation);
		editor.putString("font_size", "" + fontSize);
		editor.putString("framerate", "" + frameRate);
		editor.putBoolean("dynamic_framerate", dynamicFramerate);

		// FFT
		editor.putString("fft_drawing_type", "" + fftDrawingType);
		editor.putString("colormap_type", "" + colormapType);
		editor.putBoolean("peak_hold", peakHold);
		editor.putString("fft_waterfall_ratio", "" + fftWaterfallRatio);

		Log.d(LOGTAG, LOGTAG + " saved: " + editor.commit());
	}

	@Subscribe
	public void onEvent(ScaleEvent event) {
		float xScale = event.getXScale();
		float yScale = event.getYScale();
		if (event instanceof WaveFormScaleEvent) {
			// TODO implement
			return;
		}
		if (event instanceof DemodScaleEvent) {
			Demodulation demodulation = Demodulator.getDemodulation();
			int maxBW = demodulation.getMaxUserFilterWidth();
			int minBW = demodulation.getMinUserFilterWidth();
			Log.d(LOGTAG, "db for scale: max " + maxBW + " mmin " + minBW);
			int finalBW = (int) Math.max(minBW, Math.min(maxBW, demodBandwidth * xScale));
			setDemodBandwidth(finalBW);
			EventBus.getDefault().post(new ChangeChannelWidthEvent(finalBW));
			return;
		}
		if ((xScale > yScale && bandwidth > minBandwidth && xScale > 1) || (bandwidth < maxBandwidth && xScale < 1)) {
			bandwidth /= xScale;
			EventBus.getDefault().post(new BandwidthEvent(bandwidth));
			return;
		}

		if (event instanceof SpectrumScaleEvent) {
			if (yScale != 1f) {
				float oldDiff = (curMaxDB - curMinDB);
				float newDiff = oldDiff;// / 1.5f;
				float centerDb = curMaxDB - oldDiff / 2;

				if (yScale > 1) {
					newDiff = newDiff - newDiff * (yScale - 1);
				} else {
					newDiff = newDiff + newDiff * (1 / yScale - 1);
				}
				Log.d(LOGTAG, "yscale: " + yScale);
				Log.d(LOGTAG, "newdiff: " + newDiff);
				newDiff = Math.max(newDiff, mindBdiff) / 2;
				curMinDB = Math.max(minDB, centerDb - newDiff);
				curMaxDB = Math.min(maxDB, centerDb + newDiff);
			}
		}

		if (event instanceof WaterfallScaleEvent) {

		}

	}

	public void updateFFTWaterfallRatio() {
		fftWaterfallRatio = getFloat("fft_waterfall_ratio", fftWaterfallRatio);
	}

	@Subscribe
	public void onEvent(TapEvent event) {
		IQSourceInterface source = SourceControl.getSource();
		if (StateHandler.isDemodulating()) {
			long newDemodFrequency = (long) ((centerFrequency - bandwidth / 2) + (event.getxVal()) * bandwidth);
			if (newDemodFrequency > source.getMinFrequency() && newDemodFrequency < source.getMaxFrequency()) {
				demodFrequency = newDemodFrequency;
				EventBus.getDefault().post(new DemodValueChangeEvent(demodFrequency));
			}
		} else {
			long newBandwidth = SourceControl.getSource().getMaxSampleRate();
			long newCenterFrequency = (long) ((centerFrequency - bandwidth / 2) + (event.getxVal()) * bandwidth);
			EventBus.getDefault().post(new RequestFrequencyEvent(newCenterFrequency));
			EventBus.getDefault().post(new BandwidthEvent(bandwidth = (int) newBandwidth));
			EventBus.getDefault().post(new RequestStateEvent(State.MONITORING));
		}
	}

	@Subscribe
	public void onEvent(FrequencyEvent event) {
		centerFrequency = event.getFrequency();
	}

	@Subscribe
	public void onEvent(DemodulationEvent event) {
		if (demodFrequency < centerFrequency - bandwidth / 2 || demodFrequency > centerFrequency + bandwidth / 2) {
			demodFrequency = centerFrequency;
		}

	}

	private void correctSquelch() {
		if (squelch < curMinDB || squelch > curMaxDB) {
			squelch = curMinDB + (curMaxDB - curMinDB) / 4;
		}
		EventBus.getDefault().post(new SquelchChangeEvent(squelch));
	}

	@Subscribe
	public void onEvent(ScrollEvent event) {

		float xScrollVal = event.getXScroll();
		float yScrollVal = event.getYScroll();
		if (event.isDemodScroll()) {
			if (Math.abs(xScrollVal) > Math.abs(yScrollVal)) {
				long newDemodFrequency = (long) (getDemodFrequency() - xScrollVal * bandwidth);
				setDemodFrequency(newDemodFrequency);
				EventBus.getDefault().post(new DemodValueChangeEvent(newDemodFrequency, demodBandwidth));
			} else {
				squelch = Math.max(Math.min(curMaxDB, squelch += yScrollVal * (curMaxDB - curMinDB)), curMinDB);
				EventBus.getDefault().post(new SquelchChangeEvent(squelch));
			}
		} else {
			if (Math.abs(xScrollVal) > Math.abs(yScrollVal)) {

				// horizontal scrolling
				long relFreqChange = (long) (xScrollVal * bandwidth);
				long newFrequency = getFrequency() + relFreqChange;

				if (StateHandler.isScanning()) {
					EventBus.getDefault().post(new ScanAreaUpdateEvent(centerFrequency - bandwidth / 2,
							centerFrequency + bandwidth / 2, 2000000, 1));
					centerFrequency = newFrequency;
				} else {
					IQSourceInterface source = SourceControl.getSource();
					long freqIndicator = newFrequency - centerFrequency;
					if (newFrequency > source.getMinFrequency() && newFrequency < source.getMaxFrequency()) {
						EventBus.getDefault().post(new RequestFrequencyEvent(newFrequency, centerFrequency));
						long newDemodFrequency = demodFrequency + relFreqChange;
						setDemodFrequency(newDemodFrequency);
						EventBus.getDefault().post(new DemodValueChangeEvent(newDemodFrequency, demodBandwidth));
					} else if ((source.getMinFrequency() > newFrequency && freqIndicator > 0)
							|| (newFrequency > source.getMaxFrequency() && freqIndicator < 0)) {
						EventBus.getDefault().post(new RequestFrequencyEvent(newFrequency, centerFrequency));
					}
				}

			} else {
				// vertical scrolling
				float oldDiff = curMaxDB - curMinDB;
				float diff = oldDiff;

				diff = oldDiff * yScrollVal / fftWaterfallRatio;
				Log.d(LOGTAG, "diff:" + diff);
				// diff = Math.max(diff, mindBdiff);
				// Log.d(LOGTAG, "diff:" + diff);
				// Make sure we stay in the boundaries:
				if (curMaxDB - diff > maxDB)
					oldDiff = maxDB - curMaxDB;
				if (curMinDB - diff < minDB)
					oldDiff = minDB - curMinDB;
				curMinDB = Math.max(curMinDB - diff, minDB);
				curMaxDB = Math.min(curMaxDB - diff, maxDB);
				correctSquelch();
			}
		}

	}

	public int getBandwidth() {
		return bandwidth;
	}

	public long getFrequency() {
		return centerFrequency;
	}

	public void setDemodBandwidth(int bandwidth) {
		this.demodBandwidth = bandwidth;
	}

	public int getDemodBandwidth() {
		return demodBandwidth;
	}

	public long getDemodFrequency() {
		return demodFrequency;
	}

	public void setDemodFrequency(long frequency) {
		EventBus.getDefault().post(new DemodFrequencyEvent(frequency));
		demodFrequency = frequency;
	}

	public float getAbsMinDBLevel() {
		return minDB;
	}

	public float getAbsMaxDBLevel() {
		return maxDB;
	}

	public float getCurMinDB() {
		return curMinDB;
	}

	public void setMinDBLevelGUI(float minLevel) {
		curMinDB = minLevel;
	}

	public float getCurMaxDB() {
		return curMaxDB;
	}

	public void setMaxDBLevelGUI(float maxDBLevel) {
		curMaxDB = maxDBLevel;
	}

	public void setSquelch(float squelch) {
		this.squelch = squelch;
	}

	public int getFftDrawingType() {
		return fftDrawingType;
	}

	public void setMinDb(float minDb) {
		curMinDB = minDb;
	}

	public void setMaxDb(float maxDb) {
		curMaxDB = maxDb;
	}

	public void setFftDrawingType(int drawingType) {
		fftDrawingType = drawingType;
	}

	public int getWaterfallRowHeight() {
		return waterfallRowHeight;

	}

	public float getSquelch() {
		return squelch;
	}

	public boolean isDisplayRelativeFrequencies() {
		return relativeFrequencies;
	}

	public boolean isShowDebugInformation() {
		return debugInformation;
	}

	/**
	 * @return current font size: FONT_SIZE_SMALL, *_MEDIUM, *_LARGE
	 */
	public int getFontSize() {
		return fontSize;
	}

	/**
	 * Returns the height/width of the frequency/power grid in px
	 *
	 * @return size of the grid (frequency grid height / power grid width) in px
	 */
	public int getGridSize() {
		return (int) (75 * activity.getResources().getDisplayMetrics().xdpi / 200);
	}

	/**
	 * Set the font size
	 *
	 * @param fontSize
	 *            FONT_SIZE_SMALL, *_MEDIUM or *_LARGE
	 */
	private void setFontSize(int fontSize) {
		int normalTextSize;
		int smallTextSize;
		switch (FONT_SIZE.values()[fontSize]) {
		case SMALL:
			normalTextSize = (int) (getGridSize() * 0.3);
			smallTextSize = (int) (getGridSize() * 0.2);
			break;
		case MEDIUM:
			normalTextSize = (int) (getGridSize() * 0.476);
			smallTextSize = (int) (getGridSize() * 0.25);
			break;
		case LARGE:
			normalTextSize = (int) (getGridSize() * 0.7);
			smallTextSize = (int) (getGridSize() * 0.35);
			break;
		default:
			Log.e(LOGTAG, "setFontSize: Invalid font size: " + fontSize);
			return;
		}
		this.fontSize = fontSize;
		ColorPreference.TEXT_PAINT.setTextSize(normalTextSize);
		ColorPreference.TEXT_SMALL_PAINT.setTextSize(smallTextSize);
		Log.i(LOGTAG,
				"setFontSize: X-dpi=" + activity.getResources().getDisplayMetrics().xdpi + " X-width="
						+ activity.getResources().getDisplayMetrics().widthPixels + "  fontSize=" + fontSize
						+ "  normalTextSize=" + normalTextSize + "  smallTextSize=" + smallTextSize);
	}

	public float getFFTRatio() {
		if (StateHandler.isScanning() && !scannerWaterfall)
			return 1;
		else
			return fftWaterfallRatio;
	}

	public void setFFTRatio(float d) {
		fftWaterfallRatio = d;

	}

	@Override
	public String getName() {
		return "gui";
	}

	@Override
	public int getResID() {
		return R.xml.gui_preferences;
	}

	public int getFramerate() {
		return frameRate;
	}

	public boolean isDynamicFrameRate() {
		return dynamicFramerate;
	}

	public boolean isPeakHold() {
		return peakHold;
	}

	public boolean isWaterfallPaused() {
		return pauseWaterfall;
	}

	public WaterfallColorMap getWaterfallColorMap() {
		return colormap;
	}

	public boolean isScannerWaterfall() {
		return scannerWaterfall;
	}

	public void setBandwidth(int inputRate) {
		this.bandwidth = inputRate;

	}

	public boolean isAutoscale() {
		return autoscale;
	}

	public void setSquelchSatisfied(boolean b) {
		squelchSatisfied = b;

	}

	public boolean isSquelchSatisfied() {
		return squelchSatisfied;
	}

	public boolean isTabsHide() {
		return hideTabs;
	}

	// /**
	// * Sets the fft to waterfall ratio
	// *
	// * @param fftRatio
	// * percentage of the fft on the screen (0 -> 0%; 1 -> 100%)
	// */
	// public void setFftRatio(float fftRatio) {
	// if (fftRatio != this.fftRatio) {
	// this.fftRatio = fftRatio;
	//
	// waterfallDrwb.createWaterfallLineBitmaps(); // recreate the
	// // waterfall
	// // Recreate the shaders:
	// ColorPreference.FFT_PAINT.setShader(
	// new LinearGradient(0, 0, 0, getFftHeight(), Color.WHITE, Color.BLUE,
	// Shader.TileMode.MIRROR));
	// }
	// }
}
