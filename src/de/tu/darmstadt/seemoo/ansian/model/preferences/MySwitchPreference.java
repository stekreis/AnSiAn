package de.tu.darmstadt.seemoo.ansian.model.preferences;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class MySwitchPreference extends SwitchPreference {

	public MySwitchPreference(Context context) {
		super(context);
	}

	public MySwitchPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MySwitchPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {

		return super.onCreateView(parent);
	}

	@Override
	protected String getPersistedString(String defaultReturnValue) {
		return String.valueOf(getPersistedInt(-1));
	}

	@Override
	protected boolean persistString(String value) {
		return persistInt(Integer.valueOf(value));
	}

}
