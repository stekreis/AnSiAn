package de.tu.darmstadt.seemoo.ansian.gui.fragments.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import de.tu.darmstadt.seemoo.ansian.model.preferences.MyEditTextPreference;
import de.tu.darmstadt.seemoo.ansian.model.preferences.MySharedPreferences;

/**
 * General fragment for preferences
 *
 */
public abstract class MyPreferenceFragment extends PreferenceFragment
		implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

	@SuppressWarnings("unused")
	private static final String LOGTAG = "MyPreferenceFragment";
	protected String name;
	protected int resID;
	protected MySharedPreferences preference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Define the settings file to use by this settings fragment
		getPreferenceManager().setSharedPreferencesName(name);
		// Load the preferences from an XML resource
		addPreferencesFromResource(resID);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference pref = findPreference(key);
		if (pref instanceof MyEditTextPreference) {
			MyEditTextPreference editPref = (MyEditTextPreference) pref;
			editPref.updateSummary();
		}
		preference.loadPreference();

	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		return false;
	}

	public MyPreferenceFragment(MySharedPreferences preference) {
		this.preference = preference;
		this.name = preference.getName();
		this.resID = preference.getResID();
	}

	public String getTitle() {
		return name;
	}

	@Override
	public void onResume() {
		super.onResume();
		PreferenceScreen bla = getPreferenceScreen();
		SharedPreferences blubb = bla.getSharedPreferences();
		blubb.registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
}
