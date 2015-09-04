package de.tu.darmstadt.seemoo.ansian.gui.views;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.R;
import de.tu.darmstadt.seemoo.ansian.SettingsActivity;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler.State;
import de.tu.darmstadt.seemoo.ansian.control.events.DemodulationEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.RecordingEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.RequestStateEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.StateEvent;
import de.tu.darmstadt.seemoo.ansian.gui.dialogs.AdjustGainDialog;
import de.tu.darmstadt.seemoo.ansian.gui.dialogs.DemodulatorDialog;
import de.tu.darmstadt.seemoo.ansian.gui.dialogs.FrequencyDialog;
import de.tu.darmstadt.seemoo.ansian.gui.dialogs.RecordingDialog;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.Demodulation.DemoType;
import de.tu.darmstadt.seemoo.ansian.model.preferences.MiscPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * 
 * Toolbar on the bottom of the screen with often used buttons
 *
 */

public class MyToolbarView extends Toolbar {
	private static final String LOGTAG = "MyActionBarView";
	private MainActivity activity;
	private MiscPreferences preferences;

	public MyToolbarView(Context context) {
		super(context);
		init(context);
	}

	public MyToolbarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MyToolbarView(Context context, AttributeSet attrs, int defaultStyle) {
		super(context, attrs, defaultStyle);
		init(context);
	}

	private void init(Context context) {
		activity = MainActivity.instance;
		preferences = Preferences.MISC_PREFERENCE;
		EventBus.getDefault().register(this);
		inflateMenu(R.menu.toolbar);
		setPaddingRelative(0, 0, 20, 0);

		// Experimental opening for Settings
		// setOnTouchListener(new OnTouchListener() {
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// if (event.getDownTime()>2000)
		// { openSettings();
		// return true;}
		// else return false;
		// }
		// });

		setDemodulationMode(preferences.getDemodulation());
		setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {

				switch (menuItem.getItemId()) {
				case R.id.action_stop:
					EventBus.getDefault().post(new RequestStateEvent(State.STOPPED));
					return true;

				case R.id.action_pause:
					EventBus.getDefault().post(new RequestStateEvent(State.PAUSED));
					return true;

				case R.id.action_start:
					EventBus.getDefault().post(new RequestStateEvent(State.MONITORING));
					return true;

				case R.id.action_setDemodulation:
					new DemodulatorDialog().show();
					return true;
				case R.id.action_setFrequency:
					new FrequencyDialog().show();
					return true;
				case R.id.action_setGain:
					new AdjustGainDialog().show();
					return true;
				case R.id.action_settings:
					openSettings();
					return true;
				case R.id.action_record:
					if (!StateHandler.isRecording())
						new RecordingDialog(true).show();
					else
						EventBus.getDefault().post(new RecordingEvent(false));
					return true;
				case R.id.action_help:
					Intent intentShowHelp = new Intent(Intent.ACTION_VIEW);
					intentShowHelp.setData(Uri.parse(activity.getString(R.string.help_url)));
					activity.startActivity(intentShowHelp);
					// openManualPDF();
					return true;
				default:
					return false;

				}

			}

			@SuppressWarnings("deprecation")
			private void openManualPDF() {
				AssetManager assetManager = activity.getAssets();

				InputStream in = null;
				OutputStream out = null;
				File file = new File(activity.getFilesDir(), "manual.pdf");
				try {
					in = assetManager.open("manual.pdf");
					out = activity.openFileOutput(file.getName(), Context.MODE_WORLD_READABLE);

					copyFile(in, out);
					in.close();
					in = null;
					out.flush();
					out.close();
					out = null;
				} catch (Exception e) {
					Log.e("tag", e.getMessage());
				}

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse("file://" + activity.getFilesDir() + "/manual.pdf"), "application/pdf");

				try {
					activity.startActivity(intent);
				} catch (ActivityNotFoundException e) {
					// TODO
				}

			}
		});
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	@Subscribe
	public void onEventMainThread(final StateEvent event) {

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				MenuItem pause = getMenu().findItem(R.id.action_pause);
				MenuItem start = getMenu().findItem(R.id.action_start);
				MenuItem stop = getMenu().findItem(R.id.action_stop);
				switch (event.getState()) {
				case SCANNING:
				case MONITORING:
					stop.setVisible(true);
					pause.setVisible(true);
					start.setVisible(false);
					break;
				case STOPPED:
					stop.setVisible(false);
					pause.setVisible(false);
					start.setVisible(true);
					break;
				case PAUSED:
					stop.setVisible(true);
					pause.setVisible(false);
					start.setVisible(true);
					break;
				default:
					break;
				}

			}
		});

	}

	@Subscribe
	public void onEventMainThread(final DemodulationEvent event) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setDemodulationMode(event.getDemodulation());

			}

		});

	}

	public void openSettings() {
		if (SettingsActivity.instance == null) {
			Intent intentShowSettings = new Intent(MainActivity.instance.getApplicationContext(),
					SettingsActivity.class);
			MainActivity.instance.startActivity(intentShowSettings);
		}

	}

	private void setDemodulationMode(DemoType demodulation) {
		int iconRes = -1;
		int titleRes = -1;
		MenuItem item = getMenu().findItem(R.id.action_setDemodulation);
		switch (demodulation) {
		case OFF:
			iconRes = R.drawable.ic_action_demod_off;
			titleRes = R.string.action_demodulation_off;
			break;
		case AM:
			iconRes = R.drawable.ic_action_demod_am;
			titleRes = R.string.action_demodulation_am;
			break;
		case NFM:
			iconRes = R.drawable.ic_action_demod_nfm;
			titleRes = R.string.action_demodulation_nfm;
			break;
		case WFM:
			iconRes = R.drawable.ic_action_demod_wfm;
			titleRes = R.string.action_demodulation_wfm;
			break;
		case LSB:
			iconRes = R.drawable.ic_action_demod_lsb;
			titleRes = R.string.action_demodulation_lsb;
			break;
		case USB:
			iconRes = R.drawable.ic_action_demod_usb;
			titleRes = R.string.action_demodulation_usb;
			break;
		case MORSE:
			iconRes = R.drawable.ic_action_demod_morse;
			titleRes = R.string.action_demodulation_morse;
			break;
		default:
			Log.e(LOGTAG, "updateActionBar: invalid mode: " + preferences.getDemodulation());
		}

		item.setTitle(titleRes);
		item.setIcon(iconRes);
	}

	@Subscribe
	public void onEventMainThread(final RecordingEvent event) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				MenuItem item = getMenu().findItem(R.id.action_record);
				if (event.isRecording()) {
					item.setTitle(R.string.action_recordOn);
					item.setIcon(R.drawable.ic_action_record_on);
				} else {
					item.setTitle(R.string.action_recordOff);
					item.setIcon(R.drawable.ic_action_record_off);
				}
			}
		});
	}

}
