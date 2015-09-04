package de.tu.darmstadt.seemoo.ansian.model.preferences;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class MyEditTextPreference extends EditTextPreference {

	@SuppressWarnings("unused")
	private static final String LOGTAG = "MyEditTextPreference";
	private String defaultSummary;

	public MyEditTextPreference(Context context) {
		super(context);
	}

	public MyEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		updateSummary();
		return super.onCreateView(parent);
	}

	public String getDefaultSummary() {
		if (defaultSummary == null && getSummary() != null)
			defaultSummary = getSummary().toString();
		return defaultSummary;

	}

	public void updateSummary() {
		if (getDefaultSummary() != null)
			setSummary(String.format(getDefaultSummary(), getText()));

	}

}