package de.tu.darmstadt.seemoo.ansian.gui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.gui.tabs.MyTabFragment;
import de.tu.darmstadt.seemoo.ansian.gui.views.AnalyzerSurface;

public class AnalyzerFragment extends MyTabFragment {

	private AnalyzerSurface analyzerView;

	public AnalyzerFragment(MainActivity activity) {
		super("Analyzer", activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		analyzerView = new AnalyzerSurface(activity);
		super.onCreate(savedInstanceState);
	}

	// Inflate the view for the fragment based on layout XML
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return analyzerView;
	}

	@Override
	public void onStart() {

		super.onStart();
	}

}
