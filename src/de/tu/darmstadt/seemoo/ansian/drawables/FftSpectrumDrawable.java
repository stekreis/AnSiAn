package de.tu.darmstadt.seemoo.ansian.drawables;

import android.graphics.Canvas;
import android.util.Log;
import de.tu.darmstadt.seemoo.ansian.control.DataHandler;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.model.FFTDrawData;
import de.tu.darmstadt.seemoo.ansian.model.preferences.ColorPreference;
import de.tu.darmstadt.seemoo.ansian.model.preferences.MiscPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * 
 * Drawable for the frequency spectrum in AnalyzerSurface
 * 
 * @author Steffen Kreis
 *
 */
public class FftSpectrumDrawable extends MyDrawable {

	MiscPreferences preferences;

	private int fftHeight;
	private int fftWidth;

	public FftSpectrumDrawable() {
		preferences = Preferences.MISC_PREFERENCE;
	}

	private float[] peaks; // peak hold points

	private static final String LOGTAG = "FftSpectrumDrawable";

	public void init(int fftHeight, int fftWidth) {
		this.fftHeight = fftHeight;
		this.fftWidth = fftWidth;
	}

	@Override
	public void draw(Canvas canvas) {

		FFTDrawData fftDrawData;
		if (StateHandler.isScanning()) {
			fftDrawData = DataHandler.getInstance().getScannerDrawData(fftWidth);

		} else {
			fftDrawData = DataHandler.getInstance().getFrequencyDrawData(fftWidth);
		}
		float[] mag = null;
		int startX = 0;
		if (fftWidth != 0 && fftDrawData != null) {
			mag = fftDrawData.getValues();
			startX = fftDrawData.getStart();
		}
		if (mag != null) {
			final float minDB = Preferences.GUI_PREFERENCE.getCurMinDB();
			final float maxDB = Preferences.GUI_PREFERENCE.getCurMaxDB();

			// Log.d(LOGTAG, "checkScaletotalXscale: " +
			// analyzerSurface.getTotalXScale());
			// float[] peaks = analyzerSurface.getCurrentPeaks();
			float dbDiff = maxDB - minDB;
			// Size (in pixel) per 1dB in the fft
			float dbWidth = fftHeight / dbDiff;
			// y coordinate of the previously processed pixel (only used with
			// drawing type line)
			float previousY = fftHeight;
			// y coordinate of the currently processed pixel
			float currentY;

			// Update Peak Hold
			if (Preferences.GUI_PREFERENCE.isPeakHold()) {
				// First verify that the array is initialized correctly:
				if (peaks == null || peaks.length != mag.length) {
					peaks = new float[mag.length];
					for (int i = 0; i < peaks.length; i++)
						peaks[i] = -999999F; // == no peak ;)
				}

				// Update the peaks:
				for (int i = 0; i < mag.length; i++)
					peaks[i] = Math.max(peaks[i], mag[i]);
			} else {
				peaks = null;
			}

			for (int pos = 0; pos < mag.length; pos++) {
				// FFT:
				if (pos >= 0) {

					if (mag[pos] > minDB) {
						int currentX = pos + startX;
						currentY = fftHeight - (mag[pos] - minDB) * dbWidth;
						if (currentY < 0)
							currentY = 0;

						switch (Preferences.GUI_PREFERENCE.getFftDrawingType()) {

						case 1:
							canvas.drawLine(currentX, fftHeight, currentX, currentY, ColorPreference.FFT_PAINT);
							break;
						case 2:
							if (currentX == startX) {
								canvas.drawPoint(currentX, currentY, ColorPreference.FFT_PAINT);
							} else {
								canvas.drawLine(currentX - 1, previousY, currentX, currentY, ColorPreference.FFT_PAINT);
							}
							previousY = currentY;
							if (currentX + 1 == startX + mag.length)
								canvas.drawPoint(currentX, currentY, ColorPreference.FFT_PAINT);
							break;
						default:
							Log.e(LOGTAG, "drawFFT: Invalid fft drawing type");
						}
					}
				}
			}
		}
	}

	public void resetPeaks() {
		// Check if the frequency or sample rate of the incoming signals
		// is
		// different from the ones before:
		if (peaks != null) {
			for (int i = 0; i < peaks.length; i++)
				peaks[i] = -999999F; // reset peaks. We could also shift
										// and
										// scale. But for now they are
										// simply reset.
		}
	}

}
