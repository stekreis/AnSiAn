package de.tu.darmstadt.seemoo.ansian.model.preferences;

import java.util.Set;

import android.content.SharedPreferences.Editor;

public class MyEditor implements Editor {

	private Editor editor;
	private String prefix;

	public MyEditor(String string, Editor edit) {
		prefix = string;
		editor = edit;
	}

	@Override
	public Editor putString(String key, String value) {
		return editor.putString(prefix + key, value);
	}

	@Override
	public Editor putStringSet(String key, Set<String> values) {
		return editor.putStringSet(prefix + key, values);
	}

	@Override
	public Editor putInt(String key, int value) {
		return editor.putInt(prefix + key, value);
	}

	@Override
	public Editor putLong(String key, long value) {
		return editor.putLong(prefix + key, value);
	}

	@Override
	public Editor putFloat(String key, float value) {
		return editor.putFloat(prefix + key, value);
	}

	@Override
	public Editor putBoolean(String key, boolean value) {
		return editor.putBoolean(prefix + key, value);
	}

	@Override
	public Editor remove(String key) {
		return editor.remove(prefix + key);
	}

	@Override
	public Editor clear() {
		return editor.clear();
	}

	@Override
	public boolean commit() {
		return editor.commit();
	}

	@Override
	public void apply() {
		editor.apply();
	}

}
