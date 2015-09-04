package de.tu.darmstadt.seemoo.ansian.control;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler.State;
import de.tu.darmstadt.seemoo.ansian.control.events.BandwidthEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.DemodulationEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.FrequencyEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.RequestFrequencyEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.RequestStateEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.SourceEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.StateEvent;
import de.tu.darmstadt.seemoo.ansian.control.threads.Demodulator;
import de.tu.darmstadt.seemoo.ansian.control.threads.SourceControlThread;
import de.tu.darmstadt.seemoo.ansian.model.preferences.MiscPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;
import de.tu.darmstadt.seemoo.ansian.model.sources.FileIQSource;
import de.tu.darmstadt.seemoo.ansian.model.sources.HackrfSource;
import de.tu.darmstadt.seemoo.ansian.model.sources.IQSourceInterface;
import de.tu.darmstadt.seemoo.ansian.model.sources.IQSourceInterface.SourceType;
import de.tu.darmstadt.seemoo.ansian.model.sources.RtlsdrSource;

/**
 * SourceControl handles the communication with the different sources
 * 
 * @author Steffen Kreis
 *
 */
public class SourceControl implements IQSourceInterface.Callback {
	private MiscPreferences preferences;
	public static final String LOGTAG = "SourceControl";
	public static final String RECORDING_DIR = "AnSiAn";
	public static final int RTL2832U_RESULT_CODE = 1234; // arbitrary value,
															// used when sending
															// intent to
															// RTL2832U

	private static MainActivity activity;
	private static IQSourceInterface source;
	private static SourceControl instance;
	public SourceControlThread scannerThread;

	public static SourceControl getInstance() {
		if (instance == null) {
			instance = new SourceControl();
		}
		return instance;
	}

	private SourceControl() {
		activity = MainActivity.instance;
		preferences = Preferences.MISC_PREFERENCE;
		EventBus.getDefault().register(this);
		createSource();
	}

	/**
	 * Will create a IQ Source instance according to the user settings.
	 *
	 * @return true on success; false on error
	 */
	public boolean createSource() {
		long frequency = Preferences.GUI_PREFERENCE.getFrequency();

		int sampleRate = Preferences.MISC_PREFERENCE.getSampleRate();

		SourceType sourceType = preferences.getSourceType();

		switch (sourceType) {
		case FILE_SOURCE:
			// Create IQ Source (filesource)
			String filename = preferences.getSourceFileName();
			int fileFormat = preferences.getSourceFileFormat();
			boolean repeat = preferences.isRepeating();
			source = new FileIQSource(filename, Preferences.MISC_PREFERENCE.getFileSourceSampleRate(),
					Preferences.MISC_PREFERENCE.getFileSourceFrequency(), 16384, repeat, fileFormat);
			break;
		case HACKRF_SOURCE:
			// Create HackrfSource
			source = new HackrfSource();
			source.setFrequency(frequency);
			source.setSampleRate(sampleRate);
			((HackrfSource) source).setVgaRxGain(preferences.getVgaRxGain());
			((HackrfSource) source).setLnaGain(preferences.getVgaRxGain());
			((HackrfSource) source).setAmplifier(preferences.getAmplifier());
			((HackrfSource) source).setAntennaPower(preferences.getAntennaPower());
			((HackrfSource) source).setFrequencyShift(preferences.getHackRfFrequenyShift());
			break;
		case RTLSDR_SOURCE:
			// Create RtlsdrSource
			if (preferences.isExternalServer())
				source = new RtlsdrSource(preferences.getRtlSdrIp(), preferences.getRtlSdrPort());
			else {
				source = new RtlsdrSource("127.0.0.1", 1234);
			}

			if (sampleRate > 2000000) // might be the case after switching over
										// from HackRF
				sampleRate = 2000000;
			source.setFrequency(frequency);
			source.setSampleRate(sampleRate);

			((RtlsdrSource) source).setFrequencyCorrection(preferences.getFrequencyCorrection());
			((RtlsdrSource) source).setFrequencyShift(preferences.getRtlsdrFrequencyShift());
			((RtlsdrSource) source).setManualGain(preferences.isManualGain());

			if (((RtlsdrSource) source).isManualGain()) {
				((RtlsdrSource) source).setGain(preferences.getGain());
				((RtlsdrSource) source).setIFGain(preferences.getIFGain());
			}
			break;
		default:
			Log.e(LOGTAG, "createSource: Invalid source type: " + sourceType);
			return false;
		}

		// inform the analyzer surface about the new source
		EventBus.getDefault().post(new SourceEvent(source));
		return true;
	}

	/**
	 * Will open the IQ Source instance. Note: some sources need special
	 * treatment on opening, like the rtl-sdr source.
	 *
	 * @return true on success; false on error
	 */
	public boolean openSource() {
		SourceType sourceType = preferences.getSourceType();

		switch (sourceType) {
		case FILE_SOURCE:
			if (source != null && source instanceof FileIQSource)
				return source.open(activity, this);
			else {
				Log.e(LOGTAG, "openSource: sourceType is FILE_SOURCE, but source is null or of other type.");
				return false;
			}
		case HACKRF_SOURCE:
			if (source != null && source instanceof HackrfSource)
				return source.open(activity, this);
			else {
				Log.e(LOGTAG, "openSource: sourceType is HACKRF_SOURCE, but source is null or of other type.");
				return false;
			}
		case RTLSDR_SOURCE:
			if (source != null && source instanceof RtlsdrSource) {
				// We might need to start the driver:
				if (!preferences.isExternalSource()) {
					// start local rtl_tcp instance:
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("iqsrc://-a 127.0.0.1 -p 1234 -n 1"));
						activity.startActivityForResult(intent, RTL2832U_RESULT_CODE);
					} catch (ActivityNotFoundException e) {
						Log.e(LOGTAG, "createSource: RTL2832U is not installed");

						// Show a dialog that links to the play market:
						new AlertDialog.Builder(activity).setTitle("RTL2832U driver not installed!")
								.setMessage("You need to install the (free) RTL2832U driver to use RTL-SDR dongles.")
								.setPositiveButton("Install from Google Play", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										Intent marketIntent = new Intent(Intent.ACTION_VIEW,
												Uri.parse("market://details?id=marto.rtl_tcp_andro"));
										activity.startActivity(marketIntent);
									}
								}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										// do nothing
									}
								}).show();
						return false;
					}
				}

				return source.open(activity, this);
			} else {
				Log.e(LOGTAG, "openSource: sourceType is RTLSDR_SOURCE, but source is null or of other type.");
				return false;
			}
		default:
			Log.e(LOGTAG, "openSource: Invalid source type: " + sourceType);
			return false;
		}
	}

	public static IQSourceInterface getSource() {
		if (instance == null)
			getInstance();
		return source;
	}

	@Override
	public void onIQSourceReady(IQSourceInterface source) { // is called after
															// source.open()
		Log.d(LOGTAG, "onIQSourceReady called");
		EventBus.getDefault().post(new RequestStateEvent(State.MONITORING));
		// will start the
		// processing loop,
		// scheduler and
		// source
	}

	@Override
	public void onIQSourceError(final IQSourceInterface source, final String message) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, "Error with Source: [" + source.getName() + "]: " + message, Toast.LENGTH_LONG)
						.show();
			}
		});
		EventBus.getDefault().post(new RequestStateEvent(State.STOPPED));

		if (source != null && source.isOpen())
			source.close();
	}

	public boolean isOpen() {
		return source.isOpen();
	}

	public void changeSource() {

		if (source != null) {

			boolean change = false;

			if (source.getType() != preferences.getSourceType())
				change = true;
			if (source instanceof FileIQSource) {
				if (!((FileIQSource) source).getFilename().equals(preferences.getSourceFileName())) {
					change = true;
				}
			}

			if (change) {
				if (source.isOpen())
					source.close();
				createSource();
			}
		}
	}

	/**
	 * starts wider band scanning mode
	 */
	private void startScanner() {
		if (scannerThread == null) {
			scannerThread = new SourceControlThread(source.getMaxSampleRate());
		}
		scannerThread.start();
	}

	/**
	 * starts wider band scanning mode
	 */
	private void stopScanner() {
		if (scannerThread != null)
			scannerThread.stopScanner();
		scannerThread = null;

	}

	@Subscribe
	public void onEvent(StateEvent event) {
		if (event.getState() == State.SCANNING)
			startScanner();
		else
			stopScanner();
	}

	@Subscribe
	public void onEvent(BandwidthEvent event) {
		int bandwidth = event.getBandwidth();

		if (!StateHandler.isPaused()) {
			if (bandwidth > source.getMaxSampleRate() && source.allowsScanning())
				EventBus.getDefault().post(new RequestStateEvent(State.SCANNING));
			else
				EventBus.getDefault().post(new RequestStateEvent(State.MONITORING));
		}

		if (preferences.isAdaptiveSamplerate() && scannerThread == null && !StateHandler.isDemodulating())
			source.setSampleRate(source.getNextHigherOptimalSampleRate(bandwidth));
	}

	@Subscribe
	public void onEvent(DemodulationEvent event) {
		if (event.isOff())
			source.setSampleRate(Preferences.GUI_PREFERENCE.getBandwidth());
		else { // Preferences.GUI_PREFERENCE.setBandwidth(Demodulator.INPUT_RATE);
			source.setSampleRate(Demodulator.INPUT_RATE);
		}
	}

	/**
	 * Handle frequency change request. Checks if desired frequency can be tuned
	 * to and throws a FrequencyEvent which notifies others that the frequency
	 * actually has been changed
	 * 
	 * @param event
	 */
	@Subscribe
	public void onEvent(RequestFrequencyEvent event) {
		long frequency = event.getRequestedFrequency();
		if (event.isCheckPrevious()) {
			long freqIndicator = frequency - event.getPreviousFrequency();
			if ((source.getMinFrequency() > frequency && freqIndicator > 0)
					|| (frequency > source.getMaxFrequency() && freqIndicator < 0)) {
				source.setFrequency(event.getRequestedFrequency());
				EventBus.getDefault().post(new FrequencyEvent(frequency));
				return;
			}
		}
		if (source.getMinFrequency() < frequency && frequency < source.getMaxFrequency()) {
			source.setFrequency(event.getRequestedFrequency());
			EventBus.getDefault().post(new FrequencyEvent(frequency));

		}
	}

}
