package de.tu.darmstadt.seemoo.ansian.model.demodulation.morse;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.control.events.DemodulationEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.FFTDataEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.StateEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseStateEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.RequestMorseStateEvent;
import de.tu.darmstadt.seemoo.ansian.model.FFTSample;
import de.tu.darmstadt.seemoo.ansian.model.SamplePacket;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.AM;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.Demodulation;
import de.tu.darmstadt.seemoo.ansian.model.preferences.MorsePreference;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

public class Morse extends Demodulation {
	private String LOGTAG = "Morse";
	private List<FFTSample> ffts;
	private MorseStats stats;
	private int initMs;
	private long timestamp;
	// private MorseDecodingThread thread;
	private FFTSample last;

	public static enum State {
		CONFIG, INIT, INIT_TIMING, DECODE, STOPPED, INIT_STATS
	}

	public static enum Mode {
		AUTOMATIC, SEMI, MANUAL
	}

	private static State state = State.STOPPED;

	private static Mode mode;
	private MorsePreference preference;
	private AM am;

	public Morse() {
		am = new AM();
		setState(State.CONFIG);
		EventBus.getDefault().register(this);
		MIN_USER_FILTER_WIDTH = 10000;
		MAX_USER_FILTER_WIDTH = 200000;
		preference = Preferences.MORSE_PREFERENCE;
		init();
	}

	void init() {
		mode = Mode.values()[preference.getMode()];
		setState(State.INIT);
		Log.d(LOGTAG, mode.toString());
		if (mode == Mode.AUTOMATIC) {
			estimateDitTime();
		} else {
			stats = new MorseStats(preference.getDitDuration());
			setState(State.DECODE);
		}

	}

	void estimateDitTime() {
		setState(State.INIT_TIMING);
		ffts = new ArrayList<FFTSample>();
		initMs = preference.getInitTime();
		timestamp = -1;
	};

	private void initializeStats() {
		setState(State.INIT_STATS);
		FFTSample[] samples = new FFTSample[ffts.size()];
		samples = ffts.toArray(samples);
		stats = new MorseStats(samples, initMs);
		setState(State.DECODE);
	}

	private void setState(State pState) {
		Log.d(LOGTAG, "" + pState);
		state = pState;
		EventBus.getDefault().postSticky(new MorseStateEvent(pState));
	}

	@Override
	public DemoType getType() {
		return DemoType.MORSE;
	}

	@Override
	public void demodulate(SamplePacket input, SamplePacket output) {
		if (preference.isAmDemod())
			am.demodulate(input, output);
	}

	@Subscribe
	public void onEvent(FFTDataEvent event) {
		FFTSample sample = event.getSample();
		if (sample != null)
			switch (state) {
			case CONFIG:
				break;
			case INIT:
				break;
			case INIT_TIMING:
				ffts.add(sample);
				// set timestamp for first fft sample to use
				if (timestamp == -1)
					timestamp = sample.getTimestamp();
				else {
					long time = (sample.getTimestamp() - timestamp);
					if (time > initMs) {
						initializeStats();
					}

				}
				break;
			case INIT_STATS:
				break;
			case DECODE:
				if (mode != Mode.MANUAL && stats != null) {
					stats.calcThreshold(sample);
					if (preference.isAutomaticReinit())
						if (!stats.checkStats())
							init();
				}
				decode(sample);
				break;
			case STOPPED:
				break;
			default:
				break;
			}
	}

	@Subscribe
	public void onEvent(DemodulationEvent event) {
		if (event.getDemodulation() == DemoType.MORSE)
			init();
		else
			setState(State.STOPPED);
	}

	@Subscribe
	public void onStateEvent(RequestMorseStateEvent event) {
		switch (event.getState()) {
		case INIT:
			init();
			break;

		default:
			break;
		}

	}

	@Subscribe
	public void onStateEvent(StateEvent event) {
		if (event.getState() == de.tu.darmstadt.seemoo.ansian.control.StateHandler.State.STOPPED)
			setState(State.STOPPED);

	}

	private void decode(FFTSample sample) {
		sample.setMorseBit(stats.estimateValue(sample));
		if (last == null) {
			last = sample;
		} else {
			boolean lastBit = last.getMorseBit();
			boolean currentBit = sample.getMorseBit();
			// signal changed
			if (lastBit != currentBit) {
				long time = sample.getTimestamp() - last.getTimestamp();
				// ascending edge -> was high
				boolean high = lastBit == true;
				if (stats.decode(high, time))
					last = sample;
			}
		}

	}

	public static State getState() {
		return state;
	}

	public static Mode getMode() {
		return mode;
	}

}
