package de.tu.darmstadt.seemoo.ansian.gui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.gui.tabs.MyTabFragment;
import de.tu.darmstadt.seemoo.ansian.gui.views.WaveformView;

public class WaveformFragment extends MyTabFragment {

	public WaveformFragment(MainActivity activity) {
		super("Waveform", activity);
		// TODO Auto-generated constructor stub
	}

	private WaveformView waveformView;

	// Store instance variables based on arguments passed
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	// Inflate the view for the fragment based on layout XML
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		waveformView = new WaveformView(container.getContext());

		return waveformView;
	}

	public WaveformView getWaveformView() {
		return waveformView;
	}

}
