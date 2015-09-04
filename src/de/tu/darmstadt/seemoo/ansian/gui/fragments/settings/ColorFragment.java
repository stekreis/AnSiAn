package de.tu.darmstadt.seemoo.ansian.gui.fragments.settings;

import android.content.SharedPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * Fragment for color preferences
 *
 */
public class ColorFragment extends MyPreferenceFragment {

	@SuppressWarnings("unused")private static final String LOGTAG = "ColorFragment";
	
	public ColorFragment() {
		super(Preferences.COLOR_PREFERENCE);
		}




	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		super.onSharedPreferenceChanged(sharedPreferences, key);
		
	}



}
