package de.tu.darmstadt.seemoo.ansian.model;

import android.graphics.Color;
import android.util.Log;

public class WaterfallColorMap {

	private static int colormapType;
	private static int[] waterfallColorMap; // Colors used to draw the waterfall

	// definition of the different color map types
	private enum ColorMapType {
		JET, HOT, OLD, GQRX;
	}

	public WaterfallColorMap(int i) {
		if (waterfallColorMap == null || colormapType != i) {
			colormapType = i;
			createWaterfallColorMap(ColorMapType.values()[i]);
		}
	}

	/**
	 * Will populate the waterfallColorMap array with color instances
	 * 
	 * @param type
	 */
	private static void createWaterfallColorMap(ColorMapType type) {
		switch (type) {
		case JET: // BLUE(0,0,1) - LIGHT_BLUE(0,1,1) - GREEN(0,1,0)
					// - YELLOW(1,1,0) - RED(1,0,0)
			waterfallColorMap = new int[256 * 4];
			for (int i = 0; i < 256; i++)
				waterfallColorMap[i] = Color.argb(0xff, 0, i, 255);
			for (int i = 0; i < 256; i++)
				waterfallColorMap[256 + i] = Color.argb(0xff, 0, 255, 255 - i);
			for (int i = 0; i < 256; i++)
				waterfallColorMap[512 + i] = Color.argb(0xff, i, 255, 0);
			for (int i = 0; i < 256; i++)
				waterfallColorMap[768 + i] = Color.argb(0xff, 255, 255 - i, 0);
			break;
		case HOT: // BLACK (0,0,0) - RED (1,0,0) - YELLOW (1,1,0) -
					// WHITE (1,1,1)
			waterfallColorMap = new int[256 * 3];
			for (int i = 0; i < 256; i++)
				waterfallColorMap[i] = Color.argb(0xff, i, 0, 0);
			for (int i = 0; i < 256; i++)
				waterfallColorMap[256 + i] = Color.argb(0xff, 255, i, 0);
			for (int i = 0; i < 256; i++)
				waterfallColorMap[512 + i] = Color.argb(0xff, 255, 255, i);
			break;
		case OLD:
			waterfallColorMap = new int[512];
			for (int i = 0; i < 512; i++) {
				int blue = i <= 255 ? i : 511 - i;
				int red = i <= 255 ? 0 : i - 256;
				waterfallColorMap[i] = Color.argb(0xff, red, 0, blue);
			}
			break;
		case GQRX:
			waterfallColorMap = new int[256];
			for (int i = 0; i < 256; i++) {
				if (i < 20)
					waterfallColorMap[i] = Color.argb(0xff, 0, 0, 0); // level
																		// 0:
																		// black
																		// background
				else if ((i >= 20) && (i < 70))
					waterfallColorMap[i] = Color.argb(0xff, 0, 0, 140 * (i - 20) / 50); // level
																						// 1:
																						// black
																						// ->
																						// blue
				else if ((i >= 70) && (i < 100))
					waterfallColorMap[i] = Color.argb(0xff, 60 * (i - 70) / 30, 125 * (i - 70) / 30,
							115 * (i - 70) / 30 + 140); // level
														// 2:
														// blue
														// ->
														// light-blue
														// /
														// greenish
				else if ((i >= 100) && (i < 150))
					waterfallColorMap[i] = Color.argb(0xff, 195 * (i - 100) / 50 + 60, 130 * (i - 100) / 50 + 125,
							255 - (255 * (i - 100) / 50)); // level 3: light
															// blue ->
															// yellow
				else if ((i >= 150) && (i < 250))
					waterfallColorMap[i] = Color.argb(0xff, 255, 255 - 255 * (i - 150) / 100, 0); // level
																									// 4:
																									// yellow
																									// ->
																									// red
				else if (i >= 250)
					waterfallColorMap[i] = Color.argb(0xff, 255, 255 * (i - 250) / 5, 255 * (i - 250) / 5); // level
																											// 5:
																											// red
																											// ->
																											// white
			}
			break;
		default:
			Log.e("WaterfallColorMap", "createWaterfallColorMap: Unknown color map type: " + type);

		}
	}

	public int getLength() {
		return waterfallColorMap.length;
	}

	public int getColor(int i) {
		return waterfallColorMap[i];
	}

}
