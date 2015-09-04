package de.tu.darmstadt.seemoo.ansian.model;

import java.util.Arrays;

import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.control.threads.SourceControlThread;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

public class GuiFFTDataAdapter {
	@SuppressWarnings("unused")
	private static final String LOGTAG = "GuiFFTDataAdapter";
	private float[] tempMagnitudes;
	private int tempBandwith;
	private int targetValues;
	private long guiCenterFrequency;
	private long guiBandwidth;
	private FFTDrawData fftDrawData;
	private FFTSample sample;
	private float ratio;
	private long lowerSampleFrequency;
	private long upperGuiFrequency;
	private long lowerGuiFrequency;
	private long upperSampleFrequency;

	public GuiFFTDataAdapter(FFTSample sample, int pixel) {
		this.sample = sample;
		ratio = 0;
		init(pixel);
	}

	public void init(int values) {
		targetValues = values;
		guiBandwidth = Preferences.GUI_PREFERENCE.getBandwidth();
		guiCenterFrequency = Preferences.GUI_PREFERENCE.getFrequency();
		tempBandwith = getFFTBandwidth();
		tempMagnitudes = sample.getMagnitudes(tempBandwith);
		lowerGuiFrequency = guiCenterFrequency - guiBandwidth / 2;
		upperGuiFrequency = guiCenterFrequency + guiBandwidth / 2;
		lowerSampleFrequency = sample.getCenterFrequency() - tempBandwith / 2;
		upperSampleFrequency = sample.getCenterFrequency() + tempBandwith / 2;
	}

	private void scaleValues() {
		if (tempMagnitudes != null) {
			long pixels = targetValues;
			// if (StateHandler.isScanning()) {
			pixels = (long) (pixels * ((float) tempBandwith / guiBandwidth) * (1 - ratio));
			// }
			float[] result = new float[(int) pixels];
			// Log.d(LOGTAG, "1 scaleResult" + Arrays.toString(result));
			float scale = (float) tempMagnitudes.length / pixels;

			if (scale != 1) {
				float max = 0;
				for (int i = 0; i < pixels; i++) {
					result[i] = calcAverage(tempMagnitudes, Math.round(max), Math.round(max += scale));
				}
				tempMagnitudes = result;
			}
		}
	}

	private float calcAverage(float[] values, int from, int till) {
		float avg = 0;
		if (from >= values.length || till >= values.length) {
			return Float.NaN;
		}
		if (from == till) {
			return values[from];
		}
		for (int pos = from; pos < till; pos++) {
			avg += values[pos];
		}
		return avg / (till - from);
	}

	private void cutValues() {

		float lowerRatio = 0;
		float upperRatio = 0;
		if (lowerSampleFrequency < lowerGuiFrequency) {
			lowerRatio = (lowerGuiFrequency - lowerSampleFrequency) / (float) tempBandwith;

		}
		if (upperSampleFrequency > upperGuiFrequency) {
			upperRatio = (upperSampleFrequency - upperGuiFrequency) / (float) tempBandwith;
		}
		if (lowerRatio != 0 || upperRatio != 0) {
			ratio = lowerRatio + upperRatio;
			if (ratio < 1 && tempMagnitudes != null) {
				int length = tempMagnitudes.length;
				tempMagnitudes = Arrays.copyOfRange(tempMagnitudes, (int) (lowerRatio * length),
						(int) ((1 - upperRatio) * length));
			} else {
				tempMagnitudes = null;
			}
		}
	}

	public FFTDrawData getFftDrawData(int pixel) {
		if (fftDrawData != null && pixel == targetValues && guiBandwidth == Preferences.GUI_PREFERENCE.getBandwidth()
				&& guiCenterFrequency == Preferences.GUI_PREFERENCE.getFrequency()) {
			return fftDrawData;
		} else {
			init(pixel);
			// Log.d(LOGTAG, "1 precut" + Arrays.toString(tempMagnitudes));
			cutValues();
			// Log.d(LOGTAG, "2 postcut" + Arrays.toString(tempMagnitudes));
			// Scaling
			scaleValues();
			// Log.d(LOGTAG, "3 postScale" + Arrays.toString(tempMagnitudes));
			fftDrawData = new FFTDrawData(tempMagnitudes, calcStartIndex());
			return fftDrawData;
		}

	}

	private int getFFTBandwidth() {
		if (StateHandler.isScanning()) {
			return (int) (sample.getBandwidth() * SourceControlThread.getScanDataFactor());
		} else
			return sample.getBandwidth();
	};

	private int calcStartIndex() {
		float firstTemp = (float) lowerSampleFrequency - lowerGuiFrequency;
		float temp = (firstTemp / guiBandwidth);
		if (temp != 0) {
			// temp=(int) (temp+(float)tempBandwith/guiBandwidth);
			temp = Math.max(0, temp * targetValues);
		}
		return Math.round(temp);
	}

}
