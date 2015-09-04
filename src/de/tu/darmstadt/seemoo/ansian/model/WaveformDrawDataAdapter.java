package de.tu.darmstadt.seemoo.ansian.model;

import de.tu.darmstadt.seemoo.ansian.control.DataHandler;
import de.tu.darmstadt.seemoo.ansian.tools.ArrayHelper;

/**
 * 
 * @author Steffen Kreis
 * 
 *         This adapter is used by the Waveform to get the desired data. In
 *         later releases (with scrolling WaveformView working) it is supposed
 *         to hold the drawing data, so it is not necessary to recalculate that
 *         data as long as no change in scaling/scrolling/'orientation change'
 *         happened
 *
 */
public class WaveformDrawDataAdapter {

	public float[] getDrawArrayRe(int pixel, float xScale, float yScale) {
		WaveformDrawData[] drawData;
		float[] result = null;
		drawData = DataHandler.getInstance().getWaveformDrawData((int) Math.ceil(xScale));
		if (drawData != null) {
			result = new float[0];
			for (int pos = 0; pos < drawData.length; pos++) {
				if (drawData[pos] != null) {
					float[] re = drawData[pos].getDrawData((int) (pixel / Math.max(xScale, 1)), yScale);
					result = ArrayHelper.concatenate(result, re);
				}
			}
		}
		return result;
	};

}
