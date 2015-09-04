package de.tu.darmstadt.seemoo.ansian.model.preferences;

import android.content.Context;
import android.util.AttributeSet;

public class FloatEditTextPreference extends MyEditTextPreference {

	public FloatEditTextPreference(Context context) {
		super(context);
	}

	public FloatEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FloatEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getPersistedString(String defaultReturnValue) {

		return String.valueOf(getPersistedFloat(-1));

	}

	@Override
	protected boolean persistString(String value) {

		return persistFloat(Float.valueOf(value));

	}

}
