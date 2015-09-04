package de.tu.darmstadt.seemoo.ansian.gui.fragments.settings;

import android.content.SharedPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * Fragment for GUI preferences
 *
 */
public class GuiFragment extends MyPreferenceFragment {

	@SuppressWarnings("unused")private static final String LOGTAG = "GuiFragment";
	
	public GuiFragment() {
		super(Preferences.GUI_PREFERENCE);
		}




	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
				super.onSharedPreferenceChanged(sharedPreferences, key);
		if(key=="fft_waterfall_ratio"){		
			Preferences.GUI_PREFERENCE.updateFFTWaterfallRatio();
			}		
	}



}
