package de.tu.darmstadt.seemoo.ansian.gui.fragments.settings;

import android.content.SharedPreferences;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import de.tu.darmstadt.seemoo.ansian.gui.dialogs.RecordingDialog;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

public class AlarmFragment extends MyPreferenceFragment {

	@SuppressWarnings("unused")
	private static final String LOGTAG = "AlarmFragment";

	public AlarmFragment() {
		super(Preferences.ALARM_PREFERENCE);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferenceScreen screen = getPreferenceScreen();
		CharSequence[] toneValues = new String[] { "" + ToneGenerator.TONE_SUP_ERROR, "" + ToneGenerator.TONE_PROP_BEEP,
				"" + ToneGenerator.TONE_PROP_BEEP2, "" + ToneGenerator.TONE_SUP_RADIO_ACK,
				"" + ToneGenerator.TONE_SUP_RINGTONE };
		CharSequence[] toneEntries = new String[] { "TONE_SUP_ERROR", "TONE_PROP_BEEP", "TONE_PROP_BEEP2",
				"TONE_SUP_RADIO_ACK", "TONE_SUP_RINGTONE" };

		ListPreference tonePreference = new ListPreference(screen.getContext());
		tonePreference.setEntries(toneEntries);
		tonePreference.setTitle("Tone");
		tonePreference.setSummary("Tone type: %s");
		tonePreference.setEntryValues(toneValues);
		tonePreference.setKey("tone_type");
		tonePreference.setDefaultValue("" + ToneGenerator.TONE_PROP_BEEP);

		screen.addPreference(tonePreference);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		super.onSharedPreferenceChanged(sharedPreferences, key);

		Preference pref = findPreference(key);
		if (key == "recording") {
			new RecordingDialog(false).show();
		}

	}

}
