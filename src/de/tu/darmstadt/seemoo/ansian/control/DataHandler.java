package de.tu.darmstadt.seemoo.ansian.control;

import java.util.Arrays;
import java.util.Hashtable;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler.State;
import de.tu.darmstadt.seemoo.ansian.control.events.DataEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.DemodDataEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.FFTDataEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.ScanAreaUpdateEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.StateEvent;
import de.tu.darmstadt.seemoo.ansian.model.FFTDrawData;
import de.tu.darmstadt.seemoo.ansian.model.FFTSample;
import de.tu.darmstadt.seemoo.ansian.model.ObjectRingBuffer;
import de.tu.darmstadt.seemoo.ansian.model.ScannerBuffer;
import de.tu.darmstadt.seemoo.ansian.model.WaveformBuffer;
import de.tu.darmstadt.seemoo.ansian.model.WaveformDrawData;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * 
 * DataHandler intermediately stores buffers and other data for further operations
 * 
 * @author Markus Grau
 * @author Steffen Kreis
 *
 */
public class DataHandler {

	private static DataHandler instance;
	private static ObjectRingBuffer<FFTSample> fftBuffer;
	private ScannerBuffer scannerBuffer;
	private Hashtable<Long, FFTSample> lastHash;
	private FFTSample last;
	private FFTSample[] ffts;
	private WaveformBuffer waveformBuffer;

	public static DataHandler getInstance() {
		if (instance == null)
			instance = new DataHandler();
		return instance;
	}

	public DataHandler() {
		fftBuffer = new ObjectRingBuffer<FFTSample>(FFTSample.class);
		scannerBuffer = new ScannerBuffer();
		waveformBuffer = new WaveformBuffer();
		EventBus.getDefault().register(this);
	}

	public FFTDrawData getScannerDrawData(int pixelWidth) {
		if (lastHash != null)
			return scannerBuffer.getDrawData(lastHash, pixelWidth);
		else
			return scannerBuffer.getDrawData(pixelWidth);
	}

	public FFTSample[] getSamples(int i) {
		if (ffts != null)
			return Arrays.copyOf(ffts, i);
		return fftBuffer.getLast(i);
	}

	public FFTSample getLastFFTSample() {
		return fftBuffer.getLast();
	}

	public FFTSample[] getSamples() {
		return fftBuffer.getSamples();
	}

	@Subscribe
	public void onEvent(ScanAreaUpdateEvent event) {
		if (scannerBuffer != null) {
			scannerBuffer.setSamplerate(event.getSamplerate());
		}
	}

	@Subscribe
	public void onEvent(FFTDataEvent event) {
		FFTSample sample = event.getSample();
		fftBuffer.add(sample);
		scannerBuffer.addSample(sample);
	}

	@Subscribe
	public void onEvent(DataEvent event) {
		if (!StateHandler.isDemodulating()) {
			waveformBuffer.addPacket(event.getSample(), false);
		}
	}

	@Subscribe
	public void onEvent(DemodDataEvent event) {
		if (StateHandler.isDemodulating()) {
			waveformBuffer.addPacket(event.getSample(), true);
		}
	}

	@SuppressWarnings("unchecked")
	@Subscribe
	public void onEvent(StateEvent event) {
		if (event.getState() == State.PAUSED) {
			lastHash = (Hashtable<Long, FFTSample>) scannerBuffer.getScannerSamples().clone();
			last = getLastFFTSample();
			ffts = getSamples();
		} else {
			lastHash = null;
			last = null;
			ffts = null;
		}
	}

	public FFTDrawData getFrequencyDrawData(int width) {
		if (last != null)
			return last.getDrawData(width);
		else
			return getDrawData(width);
	}

	public FFTDrawData getWaterfallDrawData(int width) {
		if (last != null && Preferences.GUI_PREFERENCE.isWaterfallPaused())
			return last.getDrawData(width);
		else
			return getDrawData(width);
	}

	private FFTDrawData getDrawData(int width) {
		FFTSample sample = getLastFFTSample();
		if (sample != null)
			return getLastFFTSample().getDrawData(width);
		else
			return null;
	}

	public WaveformDrawData[] getWaveformDrawData(int shownDataAmount) {
		return waveformBuffer.getDrawData(shownDataAmount);
	}

}
