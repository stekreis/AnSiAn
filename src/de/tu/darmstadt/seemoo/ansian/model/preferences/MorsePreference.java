package de.tu.darmstadt.seemoo.ansian.model.preferences;

import android.util.Log;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.R;

public class MorsePreference extends MySharedPreferences {

	private static final String LOGTAG = "MorsePreference";
	private int ditDuration;
	private boolean fixedDit;
	private int morseFrequency;
	private boolean clearTextAfter;
	private boolean automaticReinit;
	private boolean ubiquitousTicker;
	private boolean amDemod;
	private int initTime;

	public MorsePreference(MainActivity activity) {
		super(activity);
	}

	@Override
	public void loadPreference() {
		ditDuration = getInt("dit_duration", 300);
		morseFrequency = getInt("morse_frequency", 1000);
		fixedDit = getBoolean("dah_frequency", false);
		clearTextAfter = getBoolean("clear_text_after", false);
		automaticReinit = getBoolean("automatic_init", false);
		initTime = getInt("init_time", 5);
		amDemod = getBoolean("am_demod", true);
		ubiquitousTicker = getBoolean("ubiquitous_ticker", true);
	}

	@Override
	public void savePreference() {
		// create editor
		MyEditor editor = edit();
		editor.putInt("dit_duration", ditDuration);
		editor.putInt("init_time", initTime);
		editor.putInt("morse_frequency", morseFrequency);
		editor.putBoolean("fixed_dit", fixedDit);
		editor.putBoolean("clear_text_after", clearTextAfter);
		Log.d(LOGTAG, LOGTAG + " saved: " + editor.commit());
	}

	@Override
	public String getName() {
		return "morse";
	}

	@Override
	public int getResID() {
		return R.xml.morse_preferences;
	}

	public int getDitDuration() {
		return ditDuration;
	}

	public int getMorseFrequency() {
		return morseFrequency;
	}

	public boolean isClearAfter() {
		return clearTextAfter;
	}

	public void setWPM(int i) {
		ditDuration = 1200 / i;

	}

	public int getWPM() {
		return 1200 / ditDuration;
	}

	public int getInitTime() {
		return initTime * 1000;
	}

	public boolean isDitFixed() {

		return fixedDit;
	}

	public int getMode() {
		return getInt("receive_mode", 0);
	}

	public boolean isAmDemod() {
		return amDemod;
	}

	public boolean isUbiquitousTicker() {
		return ubiquitousTicker;
	}

	public boolean isAutomaticReinit() {
		return automaticReinit;
	}

}
