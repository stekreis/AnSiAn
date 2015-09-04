package de.tu.darmstadt.seemoo.ansian.control.threads;

import android.app.Notification;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.control.SourceControl;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.control.events.ChangeChannelWidthEvent;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.Demodulation.DemoType;
import de.tu.darmstadt.seemoo.ansian.model.preferences.MiscPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;
import de.tu.darmstadt.seemoo.ansian.model.sources.IQSourceInterface;
import de.tu.darmstadt.seemoo.ansian.model.sources.IQSourceInterface.SourceType;

/**
 * This Service enables demodulating, recording and general data collection
 * while running in the background.
 * 
 * @author Markus Grau and Steffen Kreis
 * 
 */

public class AnsianService extends Service {

	private static MainActivity activity;
	private MiscPreferences preferences;
	private SourceControl sourceControl;
	private static final String LOGTAG = "ThreadHandler";
	private Alarm alarm;

	public AnsianService() {
		activity = MainActivity.instance;
		preferences = Preferences.MISC_PREFERENCE;
		sourceControl = SourceControl.getInstance();
		alarm = new Alarm(activity);
		EventBus.getDefault().register(this);		
	}

	public Scheduler scheduler;
	public Demodulator demodulator;
	private FFTCalcThread fftCalc;

	public Scheduler getScheduler() {
		return scheduler;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}



	/**
	 * Will start AnSiAn. This includes creating a source (if null), open a
	 * source (if not open), starting the scheduler (which starts the source)
	 * and starting the processing loop.
	 * 
	 * @param recording
	 * @param demodulation
	 * @return
	 */
	public boolean startService(DemoType demodulation) {
		alarm.start();

		if (SourceControl.getSource() == null) {
			if (!SourceControl.getInstance().createSource())
				return false;
		}

		SourceControl.getInstance().changeSource();
		// check if the source is open. if not, open it!
		if (!sourceControl.isOpen()) {
			if (!sourceControl.openSource()) {
				Toast.makeText(activity, "Source not available (" + SourceControl.getSource().getName() + ")",
						Toast.LENGTH_LONG).show();
				// StateHandler.stop();
				return false;
			}
			return false; // we have to wait for the source to become ready...
			// onIQSourceReady() will call startAnalyzer() again...
		} else {
			if (!StateHandler.isStopped())
				Log.d(LOGTAG, "" + getSource());
			// Create a new instance of Scheduler and Processing Loop:
			scheduler = new Scheduler(getSource());

			Log.d(LOGTAG, "" + scheduler);
			scheduler.start();

			fftCalc = new FFTCalcThread();
			fftCalc.start();

			// Start the demodulator thread:
			demodulator = new Demodulator(scheduler.getDemodQueue(), getSource().getPacketSize(), demodulation);
			demodulator.start();

			// Prevent the screen from turning off:
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
			});
					}		
		return true;
	}

	private IQSourceInterface getSource() {
		return SourceControl.getSource();
	}

	public void setDemodulationMode(DemoType demod) {
		if (demodulator != null)
			demodulator.setDemodulationMode(demod);
	}

	/**
	 * Will stop AnSiAn. This includes shutting down the scheduler (which turns
	 * off the source), the processing loop and the demodulator if running.
	 */
	public void stopService() {

		alarm.stop();
		// Stop the Scheduler if running:
		if (scheduler != null) {
			scheduler.stopScheduler();
		}

		// Stop the Demodulator if running:
		if (demodulator != null)
			demodulator.stopDemodulator();

		if (fftCalc != null)
			fftCalc.stopFFTCalcThread();
		// Stop the scanner if running:

		// allow screen to turn off again:
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			}
		});
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		// stop Service
		Toast.makeText(this, "service should be stopped", Toast.LENGTH_SHORT).show();
		Log.d("test", "serviceOnDestroy was called");
		stopService();
		// close source
		if (getSource() != null && getSource().isOpen())
			getSource().close();

		// shut down RTL2832U driver if running
		IQSourceInterface source = getSource();
		if (!StateHandler.isStopped() && source != null && source.getType() == SourceType.RTLSDR_SOURCE
				&& preferences.isExternalSource()) {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("iqsrc://-x")); // -x is invalid. will
															// cause the driver
															// to shut down (if
															// running)
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Log.e(LOGTAG, "onDestroy: RTL2832U is not installed");
			}
		}

		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		stopService();
		stopSelf();
		return super.onUnbind(intent);
	}


	@Subscribe
	public void onEvent(ChangeChannelWidthEvent event) {
		if (demodulator != null) {
			demodulator.setChannelWidth(event.getChannelWidth());
		}

	}

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
