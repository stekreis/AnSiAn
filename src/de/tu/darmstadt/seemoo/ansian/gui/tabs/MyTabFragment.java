package de.tu.darmstadt.seemoo.ansian.gui.tabs;

import android.support.v4.app.Fragment;
import de.tu.darmstadt.seemoo.ansian.MainActivity;

/**
 * General fragment which is inherited by the main fragments
 *
 */
public abstract class MyTabFragment extends Fragment {
	protected MainActivity activity;
	protected String title;

	public MyTabFragment(String title, MainActivity activity) {
		this.activity = activity;
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

}
