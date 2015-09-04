package de.tu.darmstadt.seemoo.ansian.control.threads;

import android.util.Log;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.control.SourceControl;
import de.tu.darmstadt.seemoo.ansian.control.events.ScanAreaUpdateEvent;
import de.tu.darmstadt.seemoo.ansian.model.ScannerBuffer;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;
import de.tu.darmstadt.seemoo.ansian.model.sources.IQSourceInterface;
import de.tu.darmstadt.seemoo.ansian.model.sources.RtlsdrSource;

/**
 * This thread takes care of switching the frequency for wider band scanning
 * mode. Wider band scanning mode extends the maximum visible bandwidth of the
 * input source by switching the frequency in a given area and appending those
 * smaller spectrums to a whole one. See {@link ScannerBuffer} on how this data
 * is collected
 * 
 * @author Steffen Kreis
 *
 */
public class SourceControlThread extends Thread {

	private boolean stopRequested = false;

	private long lowerCenterFrequency;
	private long upperCenterFrequency;
	// samplerate will be changed only when Thread is started new
	private long shownSamplerate;
	private long sourceSamplerate;
	private static int cropDataFactor = 1;
	private long desiredFrequency = -1;

	private IQSourceInterface source;

	private boolean running = true;

	private long idleTime = 300;

	private static final String LOGTAG = "ScannerThread";

	public SourceControlThread(int shownSamplerate) {
		this.shownSamplerate = shownSamplerate;
		this.sourceSamplerate = shownSamplerate;
		EventBus.getDefault().register(this);
		long guiCenterFrequency = Preferences.GUI_PREFERENCE.getFrequency();
		long guiBandwidth = Preferences.GUI_PREFERENCE.getBandwidth();
		setLowerFrequency((guiCenterFrequency - guiBandwidth / 2) + shownSamplerate / 2);
		setUpperFrequency((guiCenterFrequency + guiBandwidth / 2) + shownSamplerate / 2);
		// TODO assure the source is usable for scanning
		source = SourceControl.getSource();

	}

	public static float getScanDataFactor() {
		return cropDataFactor;
	}

	@Override
	public void run() {
		stopRequested = false;
		shownSamplerate = sourceSamplerate / cropDataFactor;
		source.setSampleRate((int) sourceSamplerate);
		((RtlsdrSource) SourceControl.getSource()).setManualGain(true);

		while (!stopRequested) {
			if (running && SourceControl.getSource().isTunerSettled()) {
				Log.d(LOGTAG, "changing freq");
				if (desiredFrequency < lowerCenterFrequency || desiredFrequency >= upperCenterFrequency) {
					// in case scanner was scrolled further right than current
					// displayed frequency
					desiredFrequency = lowerCenterFrequency;
				} else if (desiredFrequency < upperCenterFrequency && desiredFrequency > 0) {
					// regular scan frequency tuning
					desiredFrequency += shownSamplerate;
				}
				source.setFrequency(desiredFrequency);
				try {
					Thread.sleep(idleTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		((RtlsdrSource) SourceControl.getSource()).setManualGain(Preferences.MISC_PREFERENCE.isManualGain());
	}

	public void setLowerFrequency(long lowerFrequency) {
		this.lowerCenterFrequency = ScannerBuffer.calcSuitableFrequency(lowerFrequency, true);
	}

	public void setUpperFrequency(long upperFrequency) {
		this.upperCenterFrequency = ScannerBuffer.calcSuitableFrequency(upperFrequency, false);
	}

	public void setSampleRate(long samplerate) {
		this.shownSamplerate = samplerate;
		this.sourceSamplerate = samplerate;
	}

	public void setScanCropDataFactor(int pCropDataFactor) {
		cropDataFactor = pCropDataFactor;
	}

	public void stopScanner() {
		stopRequested = true;
	}

	@Subscribe
	public void onEvent(ScanAreaUpdateEvent event) {
		setLowerFrequency(event.getLowerFrequency());
		setUpperFrequency(event.getUpperFrequency());
		setSampleRate(event.getSamplerate());
		setScanCropDataFactor(event.getScanCropDataFactor());
	}

}
