package de.tu.darmstadt.seemoo.ansian.gui.fragments;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.R;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.gui.tabs.MyTabFragment;
import de.tu.darmstadt.seemoo.ansian.gui.views.morse.MorseSendingView;
import de.tu.darmstadt.seemoo.ansian.gui.views.morse.MorseStatusView;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.Demodulation.DemoType;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * Fragment for the Morse view
 *
 */

public class MorseFragment extends MyTabFragment {

	private MorseStatusView morseStatusView;
	private MorseSendingView morseSendingView;

	public MorseFragment(MainActivity activity) {
		super("Morse", activity);

	}

	// Inflate the view for the fragment based on layout XML
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.morse_fragment, container, false);
		return v;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		populateViewForOrientation(inflater, (ViewGroup) getView());
		init();
		update();
	}

	private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
		viewGroup.removeAllViewsInLayout();
		inflater.inflate(R.layout.morse_fragment, viewGroup);
	}

	private void update() {
		morseStatusView.update();
		morseSendingView.update();
	}

	@Override
	public void onResume() {
		super.onResume();
		init();
		update();
	}

	@Override
	public void onStart() {
		super.onStart();
		// show/hide morse ticker if desired
		if (!Preferences.MORSE_PREFERENCE.isUbiquitousTicker()
				|| StateHandler.getActiveDemodulationMode() != DemoType.MORSE) {
			getView().findViewById(R.id.morseReceiveView).setVisibility(View.VISIBLE);
		} else {
			getView().findViewById(R.id.morseReceiveView).setVisibility(View.GONE);
		}

	}

	private void init() {
		morseStatusView = (MorseStatusView) getView().findViewById(R.id.morseStatusView);
		morseSendingView = (MorseSendingView) getView().findViewById(R.id.morseSendingView);
		getView().setBackgroundColor(Color.BLACK);
	}

}
