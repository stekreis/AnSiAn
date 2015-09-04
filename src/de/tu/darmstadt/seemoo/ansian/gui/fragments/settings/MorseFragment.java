package de.tu.darmstadt.seemoo.ansian.gui.fragments.settings;

import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * Fragment for Morse preferences
 *
 */
public class MorseFragment extends MyPreferenceFragment {

	@SuppressWarnings("unused")private static final String LOGTAG = "MorseFragment";
	
	public MorseFragment() {
		super(Preferences.MORSE_PREFERENCE);
		}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		super.onSharedPreferenceChanged(sharedPreferences, key);	
		 Preference pref = findPreference(key);
		    if (key=="receive_mode") {
		    	ListPreference listPref = (ListPreference) pref;
		    	SwitchPreference switchPref= (SwitchPreference) findPreference("automatic_init");
		    	if(listPref.getValue()=="2")	{
		    		switchPref.setEnabled(false);
		    	}	    	
		    	 else switchPref.setEnabled(true);}
		   
		    }
		    		
	


}
