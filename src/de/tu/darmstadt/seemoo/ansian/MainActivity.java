package de.tu.darmstadt.seemoo.ansian;

import java.io.File;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.control.SourceControl;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.control.events.DemodulationEvent;
import de.tu.darmstadt.seemoo.ansian.gui.misc.AnsianNotification;
import de.tu.darmstadt.seemoo.ansian.gui.misc.MyToast;
import de.tu.darmstadt.seemoo.ansian.gui.tabs.MainActivityPagerAdapter;
import de.tu.darmstadt.seemoo.ansian.gui.tabs.MyViewPager;
import de.tu.darmstadt.seemoo.ansian.gui.tabs.SlidingTabLayout;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.Demodulation.DemoType;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;
import de.tu.darmstadt.seemoo.ansian.model.sources.RtlsdrSource;

/**
 * <h1>AnSiAn - Main Activity</h1>
 *
 * Module: MainActivity.java Description: Main Activity of AnSiAn
 *
 * @author Dennis Mantz
 * @author Markus Grau
 * @author Steffen Kreis
 *
 *         Copyright (C) 2014 Dennis Mantz License:
 *         http://www.gnu.org/licenses/gpl.html GPL version 2 or higher
 *
 *         This library is free software; you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation; either version 2 of the License, or (at
 *         your option) any later version.
 *
 *         This library is distributed in the hope that it will be useful, but
 *         WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *         General Public License for more details.
 *
 *         You should have received a copy of the GNU General Public License
 *         along with this library; if not, write to the Free Software
 *         Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *         02110-1301 USA
 */
public class MainActivity extends AppCompatActivity {

	public static MainActivity instance;
	public Process logcat = null;

	private StateHandler stateHandler;
	public static AnsianNotification notification;
	private MyViewPager viewPager;
	private SlidingTabLayout slidingTabLayout;
	public static final String LOGTAG = "MainActivity";
	public static final int RTL2832U_RESULT_CODE = 1234; // arbitrary value,
															// used when sending
															// intent to
															// RTL2832U

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		EventBus.getDefault().register(this);
		super.onCreate(savedInstanceState);
		instance = this;

		// init all prefs
		new Preferences(this);

		// Set view for this activity
		setContentView(R.layout.activity_main);

		// ViewPager and its adapters use support library
		// fragments, so use getSupportFragmentManager.
		// Get the ViewPager and set it's PagerAdapter so that it can display
		// items
		viewPager = (MyViewPager) findViewById(R.id.viewpager);
		MainActivityPagerAdapter pagerAdapter = new MainActivityPagerAdapter(getSupportFragmentManager(), viewPager,
				this);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(1);

		// Give the SlidingTabLayout the ViewPager
		slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
		// Center the tabs in the layout
		slidingTabLayout.setDistributeEvenly(true);
		slidingTabLayout.setViewPager(viewPager);
		stateHandler = StateHandler.getInstance();
		stateHandler.start(Preferences.MISC_PREFERENCE.isAutostart());

		// Set the hardware volume keys to work on the music audio stream:
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Start logging if enabled:
		if (Preferences.MISC_PREFERENCE.isLogging()) {
			try {
				File logfile = Preferences.MISC_PREFERENCE.getLogFile();
				logfile.getParentFile().mkdir(); // Create folder
				logcat = Runtime.getRuntime().exec("logcat -f " + logfile);
				Log.i("MainActivity", "onCreate: started logcat (" + logcat.toString() + ") to " + logfile);
			} catch (Exception e) {
				Log.e("MainActivity", "onCreate: Failed to start logging!");
			}
		}

		// Notification
		notification = new AnsianNotification(this);
	}

	@Override
	protected void onDestroy() {
		Preferences.saveAll();
		// stop logging:
		if (logcat != null) {
			try {
				logcat.destroy();
				logcat.waitFor();
				Log.i(LOGTAG, "onDestroy: logcat exit value: " + logcat.exitValue());
			} catch (Exception e) {
				Log.e(LOGTAG, "onDestroy: couldn't stop logcat: " + e.getMessage());
			}
		}
		notification.destroy();
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		// show/hide tabs if desired
		if (Preferences.GUI_PREFERENCE.isTabsHide())
			slidingTabLayout.setVisibility(View.GONE);
		else
			slidingTabLayout.setVisibility(View.VISIBLE);
		// show/hide morse ticker if desired
		if (Preferences.MORSE_PREFERENCE.isUbiquitousTicker()
				&& StateHandler.getActiveDemodulationMode() == DemoType.MORSE) {
			findViewById(R.id.ubiquitousMorseTicker).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.ubiquitousMorseTicker).setVisibility(View.GONE);
		}

		super.onStart();
	}

	@Override
	public void onBackPressed() {
		if (StateHandler.isStopped())
			super.onBackPressed();
		else
			moveTaskToBack(true);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Intent intentShowSettings = new Intent(MainActivity.instance.getApplicationContext(), SettingsActivity.class);
		MainActivity.instance.startActivity(intentShowSettings);
		return false;
	}

	@Override
	protected void onPause() {
		if (!StateHandler.isStopped())
			MyToast.makeText("AnSiAn service will continue running in background!", Toast.LENGTH_SHORT);
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// err_info from RTL2832U:
		String[] rtlsdrErrInfo = { "permission_denied", "root_required", "no_devices_found", "unknown_error", "replug",
				"already_running" };

		switch (requestCode) {
		case RTL2832U_RESULT_CODE:
			// This happens if the RTL2832U driver was started.
			// We check for errors and print them:
			if (resultCode == RESULT_OK)
				Log.i(LOGTAG, "onActivityResult: RTL2832U driver was successfully started.");
			else {
				int errorId = -1;
				int exceptionCode = 0;
				String detailedDescription = null;
				if (data != null) {
					errorId = data.getIntExtra("marto.rtl_tcp_andro.RtlTcpExceptionId", -1);
					exceptionCode = data.getIntExtra("detailed_exception_code", 0);
					detailedDescription = data.getStringExtra("detailed_exception_message");
				}
				String errorMsg = "ERROR NOT SPECIFIED";
				if (errorId >= 0 && errorId < rtlsdrErrInfo.length)
					errorMsg = rtlsdrErrInfo[errorId];

				Log.e(LOGTAG, "onActivityResult: RTL2832U driver returned with error: " + errorMsg + " (" + errorId
						+ ")"
						+ (detailedDescription != null ? ": " + detailedDescription + " (" + exceptionCode + ")" : ""));

				SourceControl.getInstance();
				SourceControl.getInstance();
				if (SourceControl.getSource() != null && SourceControl.getSource() instanceof RtlsdrSource) {
					SourceControl.getInstance();
					Toast.makeText(MainActivity.this,
							"Error with Source [" + SourceControl.getSource().getName() + "]: " + errorMsg + " ("
									+ errorId + ")"
									+ (detailedDescription != null
											? ": " + detailedDescription + " (" + exceptionCode + ")" : ""),
							Toast.LENGTH_LONG).show();
					SourceControl.getInstance();
					SourceControl.getSource().close();
				}
			}
			break;
		}
	}

	/**
	 * Set visibility for the global morse receive view
	 * 
	 * @param event
	 */
	@Subscribe
	public void onEvent(final DemodulationEvent event) {
		if (Preferences.MORSE_PREFERENCE.isUbiquitousTicker()) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (event.getDemodulation() == DemoType.MORSE) {
						findViewById(R.id.ubiquitousMorseTicker).setVisibility(View.VISIBLE);
						findViewById(R.id.morseReceiveView).setVisibility(View.GONE);
					} else {
						findViewById(R.id.ubiquitousMorseTicker).setVisibility(View.GONE);
						findViewById(R.id.morseReceiveView).setVisibility(View.VISIBLE);
					}
				}
			});
		}

	}

	public MyViewPager getViewPager() {
		return viewPager;
	}

}
