package de.tu.darmstadt.seemoo.ansian.model.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.tu.darmstadt.seemoo.ansian.MainActivity;

public abstract class MySharedPreferences implements SharedPreferences {

	@SuppressWarnings("unused")
	private static final String LOGTAG = "MySharedPreferences";
	private SharedPreferences preferences;
	protected MainActivity activity;
	private String prefix;
	private static List<MySharedPreferences> prefs = new ArrayList<MySharedPreferences>();

	public MySharedPreferences(MainActivity activity) {
		this.activity = activity;
		// Set default Settings on first run:
		PreferenceManager.setDefaultValues(activity, getName(), activity.MODE_PRIVATE, getResID(), false);
		preferences = activity.getSharedPreferences(getName(), activity.MODE_PRIVATE);
		loadPreference();
		prefs.add(this);
		prefix = "";// getName()+"_";
	}

	public static void saveAll() {
		for (MySharedPreferences pref : prefs)
			pref.savePreference();
	}

	public static void loadAll() {
		for (MySharedPreferences pref : prefs)
			pref.loadPreference();
	}

	public abstract void loadPreference();

	public abstract void savePreference();

	public abstract String getName();

	public abstract int getResID();

	@Override
	public Map<String, ?> getAll() {
		return preferences.getAll();
	}

	@Override
	public String getString(String key, String defValue) {
		return String.valueOf(preferences.getString(prefix + key, defValue));
	}

	@Override
	public Set<String> getStringSet(String key, Set<String> defValues) {
		return preferences.getStringSet(prefix + key, defValues);
	}

	@Override
	public int getInt(String key, int defValue) {
		try {
			return preferences.getInt(prefix + key, defValue);
		} catch (ClassCastException e) {
			return Integer.valueOf(preferences.getString(prefix + key, "" + defValue));
		}

	}

	// private Object catchClassCastException(ClassCastException e) {
	// String message = e.getMessage();
	// message.lastIndexOf("java.lang."))
	//
	// return Integer.valueOf(preferences.getString(prefix+key, "" + defValue));
	// }

	@Override
	public long getLong(String key, long defValue) {
		try {
			return preferences.getLong(prefix + key, defValue);
		} catch (ClassCastException e) {
			return Long.valueOf(preferences.getString(prefix + key, "" + defValue));
		}
	}

	@Override
	public float getFloat(String key, float defValue) {
		try {
			return preferences.getFloat(prefix + key, defValue);
		} catch (ClassCastException e) {
			return Float.valueOf(preferences.getString(prefix + key, "" + defValue));
		}
	}

	@Override
	public boolean getBoolean(String key, boolean defValue) {
		try {
			return preferences.getBoolean(prefix + key, defValue);
		} catch (ClassCastException e) {
			return Boolean.valueOf(preferences.getString(prefix + key, "" + defValue));
		}
	}

	protected String getString(int resId) {

		return activity.getString(resId);
	}

	@Override
	public boolean contains(String key) {
		return preferences.contains(prefix + key);
	}

	@Override
	public MyEditor edit() {
		return new MyEditor(prefix, preferences.edit());
	}

	@Override
	public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		preferences.registerOnSharedPreferenceChangeListener(listener);

	}

	@Override
	public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		preferences.unregisterOnSharedPreferenceChangeListener(listener);

	}

	public boolean getBoolean(String key, String defValue) {
		return Boolean.valueOf(preferences.getString(prefix + key, defValue));
	}

}
