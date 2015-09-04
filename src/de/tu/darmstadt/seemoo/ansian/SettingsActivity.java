package de.tu.darmstadt.seemoo.ansian;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import de.tu.darmstadt.seemoo.ansian.gui.tabs.MyViewPager;
import de.tu.darmstadt.seemoo.ansian.gui.tabs.SettingsActivityPagerAdapter;
import de.tu.darmstadt.seemoo.ansian.gui.tabs.SlidingTabLayout;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * Activity used for all application settings.
 * 
 * @author Markus Grau
 *
 */
public class SettingsActivity extends AppCompatActivity {

	public static SettingsActivity instance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;

		setContentView(R.layout.activity_settings);

		MyViewPager viewPager = (MyViewPager) findViewById(R.id.settings_pager);
		SettingsActivityPagerAdapter pagerAdapter = new SettingsActivityPagerAdapter(getFragmentManager(), viewPager);
		viewPager.setAdapter(pagerAdapter);
		int item = MainActivity.instance.getViewPager().getCurrentItem();
		switch (item) {
		// Morse
		case 0:
			item = 2;
			break;
		// Waterfall
		case 1:
			item = 0;
			break;
		// Waveform
		case 2:
			item = 0;
			break;

		default:
			break;
		}
		viewPager.setCurrentItem(item);

		// Give the SlidingTabLayout the ViewPager
		SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.settings_tabs);
		// Center the tabs in the layout
		slidingTabLayout.setDistributeEvenly(true);
		slidingTabLayout.setViewPager(viewPager);

	}

	@Override
	protected void onStart() {
		Preferences.saveAll();
		super.onStart();
	}

	@Override
	protected void onPause() {
		Preferences.loadAll();
		super.onPause();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		onBackPressed();
		return false;
	}

	@Override
	protected void onDestroy() {
		instance = null;
		super.onDestroy();
	}

}
