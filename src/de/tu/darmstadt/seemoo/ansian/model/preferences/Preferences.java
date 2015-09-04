package de.tu.darmstadt.seemoo.ansian.model.preferences;

import de.tu.darmstadt.seemoo.ansian.MainActivity;

public class Preferences {

	public static MorsePreference MORSE_PREFERENCE;
	public static GuiPreferences GUI_PREFERENCE;
	public static AlarmPreferences ALARM_PREFERENCE;
	public static ColorPreference COLOR_PREFERENCE;
	public static MiscPreferences MISC_PREFERENCE;

	public Preferences(MainActivity mainActivity) {
		MORSE_PREFERENCE = new MorsePreference(mainActivity);
		MISC_PREFERENCE = new MiscPreferences(mainActivity);
		COLOR_PREFERENCE = new ColorPreference(mainActivity);
		GUI_PREFERENCE = new GuiPreferences(mainActivity);
		ALARM_PREFERENCE = new AlarmPreferences(mainActivity);
		loadAll();
	}

	public static void saveAll() {
		MySharedPreferences.saveAll();
	}

	public static void loadAll() {
		MySharedPreferences.loadAll();
	}

}
