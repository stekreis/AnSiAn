package de.tu.darmstadt.seemoo.ansian.gui.tabs;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.gui.fragments.MorseFragment;
import de.tu.darmstadt.seemoo.ansian.gui.fragments.AnalyzerFragment;
import de.tu.darmstadt.seemoo.ansian.gui.fragments.WaveformFragment;

/**
 * Holds the fragments in the main view and organizes
 *
 */
public class MainActivityPagerAdapter extends FragmentStatePagerAdapter {

	private MyTabFragment fragments[];

	public MainActivityPagerAdapter(FragmentManager fragmentManager, MyViewPager viewPager, MainActivity activity) {
		super(fragmentManager);
		fragments = new MyTabFragment[] { new MorseFragment(activity), new AnalyzerFragment(activity),
				new WaveformFragment(activity) };
	}

	// Returns total number of pages
	@Override
	public int getCount() {
		return fragments.length;
	}

	// Returns the fragment to display for that page
	@Override
	public Fragment getItem(int position) {
		return fragments[position];
	}

	public int getItemPosition(Object item) {

		int position = getFragmentPosition((MyTabFragment) item);

		if (position >= 0) {
			return position;
		} else {
			return POSITION_NONE;
		}
	}

	// Returns the page title for the top indicator
	@Override
	public CharSequence getPageTitle(int position) {
		// Generate title based on item position
		return fragments[position].getTitle();
	}

	private int getFragmentPosition(MyTabFragment fragment) {
		for (int i = 0; i < fragments.length; i++) {
			if (fragment.getClass().equals(fragments[i].getClass()))
				return i;
		}
		return -1;
	}

	// @Subscribe
	// public void onEvent(StateEvent event) {
	// Log.d(LOGTAG, "swap fragment event: " + event.getState());
	//
	// switch (event.getState()) {
	// case PAUSED:
	// break;
	// case SCANNING:
	//// fragments[0] = new ScannerFragment(activity);
	// notifyDataSetChanged();
	// break;
	// default:
	// fragments[0] = new WaterfallFragment(activity);
	// notifyDataSetChanged();
	// }
	//
	// }

}
