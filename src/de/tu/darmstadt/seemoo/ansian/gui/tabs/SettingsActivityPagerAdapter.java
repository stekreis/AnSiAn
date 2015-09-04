package de.tu.darmstadt.seemoo.ansian.gui.tabs;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import de.tu.darmstadt.seemoo.ansian.gui.fragments.settings.AlarmFragment;
import de.tu.darmstadt.seemoo.ansian.gui.fragments.settings.GuiFragment;
import de.tu.darmstadt.seemoo.ansian.gui.fragments.settings.MiscFragment;
import de.tu.darmstadt.seemoo.ansian.gui.fragments.settings.MorseFragment;
import de.tu.darmstadt.seemoo.ansian.gui.fragments.settings.MyPreferenceFragment;

public class SettingsActivityPagerAdapter extends FragmentStatePagerAdapter {

	private MyPreferenceFragment[] fragments;

	private static String LOGTAG = "MyFragmentPagerAdapter";

	public SettingsActivityPagerAdapter(FragmentManager fragmentManager, MyViewPager viewPager) {
		super(fragmentManager);
		fragments = new MyPreferenceFragment[] { new MiscFragment(), new GuiFragment(), new MorseFragment(),
				new AlarmFragment() };
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

	// Returns the page title for the top indicator
	@Override
	public CharSequence getPageTitle(int position) {
		// Generate title based on item position
		return fragments[position].getTitle();
	}

}
