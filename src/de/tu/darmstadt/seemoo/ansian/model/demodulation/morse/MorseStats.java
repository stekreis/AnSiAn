package de.tu.darmstadt.seemoo.ansian.model.demodulation.morse;

import java.util.Arrays;

import android.util.Log;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseCodeEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseDitEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseSymbolEvent;
import de.tu.darmstadt.seemoo.ansian.model.ErrorBitSet;
import de.tu.darmstadt.seemoo.ansian.model.FFTSample;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.morse.Morse.Mode;
import de.tu.darmstadt.seemoo.ansian.model.preferences.DemodFrequencyEvent;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;
import de.tu.darmstadt.seemoo.ansian.tools.morse.Decoder;
import de.tu.darmstadt.seemoo.ansian.tools.morse.MorseCodeCharacterGetter;

public class MorseStats {
	private static final String LOGTAG = "MorseStats";
	private int dit;
	private int dah;
	private int word;
	private ErrorBitSet codeSuccess = new ErrorBitSet(100);
	private ErrorBitSet symbolSuccess = new ErrorBitSet(30);
	private int offset = 0;
	private float threshold;
	private int[] stats;
	private MorseBitSet bits;
	private int demodBandwidth;
	private int sampleDuration;
	private StringBuilder currentSymbolCode = new StringBuilder();
	private Decoder decoder = new Decoder();
	private long demodFrequency;
	private float low;
	private float high;

	public MorseStats(FFTSample[] fftSamples, int timeMs) {
		this();
		sampleDuration = timeMs / fftSamples.length;
		calcThreshold(fftSamples);
		bits = new MorseBitSet(fftSamples, this);
		calcTimings();
	}

	private MorseStats() {
		EventBus.getDefault().register(this);
		high = -Float.MAX_VALUE;
		low = Float.MAX_VALUE;
		demodBandwidth = Preferences.GUI_PREFERENCE.getDemodBandwidth();
		demodFrequency = Preferences.GUI_PREFERENCE.getDemodFrequency();
	}

	public MorseStats(int ditDuration) {
		this();
		setTimings(ditDuration);
		threshold = Preferences.GUI_PREFERENCE.getSquelch();
	}

	private void setTimings(int dit) {
		this.dit = dit;
		dah = 3 * dit;
		word = 7 * dit;
		offset = (int) Math.round(dit * 0.5);

	}

	public boolean decode(boolean high, long l) {

		if (l < dit - offset)
			return false;
		String code = null;
		// high
		if (high) {
			// dit
			if (dit - offset < l && l < dit + offset) {
				code = ".";
			}
			// dah
			if (dah - dit < l && l < dah + dit) {
				code = "-";

			}
		}
		// low
		else {
			// dit
			if (dit - offset < l && l < dit + offset) {
				code = "";
			}
			// dah
			if (dah - dit < l && l < dah + dit) {
				code = " ";
			}
			// word
			if (word - 2 * dit < l) {

				code = "/";
			}
			// pause/no signal
			if (word + offset < l) {
				code = "";
			}

		}

		// check if code recognized
		if (code == null) {
			// not in range
			codeSuccess.setBit(false);
			code = "[not in range: " + l + "]";
		} else {
			codeSuccess.setBit(true);
			currentSymbolCode.append(code);
		}
		Log.d(LOGTAG, high + " time in ms: " + l + " code: " + code);
		EventBus.getDefault()
				.postSticky(new MorseCodeEvent(code, high, l, codeSuccess.getSuccessRate(), getThreshold()));

		// check if new symbol
		if (code == " ") {
			createSymbol(code);
		} else
			currentSymbolCode.append(code);

		return true;
	}

	private void createSymbol(String code) {
		String currentSymbolCodeString = currentSymbolCode.toString();
		String symbol = decoder.decode(currentSymbolCodeString);
		boolean recognized = !(symbol.contains(MorseCodeCharacterGetter.ESCAPE_START)
				|| symbol.contains(MorseCodeCharacterGetter.ESCAPE_END));
		symbolSuccess.setBit(recognized);
		float successRate = getSymbolSuccessRate();
		EventBus.getDefault()
				.postSticky(new MorseSymbolEvent(currentSymbolCodeString, symbol, recognized, successRate));
		currentSymbolCode = new StringBuilder();
	}

	private void calcTimings() {
		stats = new int[bits.getLength()];
		int num = 0;

		for (int i = 0; i < bits.getLength();) {
			num = bits.getNextBits(i);
			stats[Math.abs(num)]++;
			i += Math.abs(num);
		}
		Log.d(LOGTAG, " Stats: " + Arrays.toString(stats));

		int samples = findPosOfMaxOccurance(sumNeighbours(stats, 1)) + 1;

		setTimings(samples * sampleDuration);
		EventBus.getDefault().postSticky(new MorseDitEvent(dit));
	}

	private int findPosOfMaxOccurance(int[] stats) {
		int pos = 0;
		int value = 0;
		for (int i = 0; i < stats.length; i++) {
			if (stats[i] >= value) {
				pos = i;
				value = stats[i];
			}
		}
		return pos;
	}

	private int[] sumNeighbours(int[] stats, int x) {
		int[] result = Arrays.copyOf(stats, stats.length);

		for (int i = 0; i < result.length; i++) {
			for (int j = 1; j <= x; j++) {
				if (i - j > 0)
					result[i] += stats[i - j];
				if (i + j < stats.length)
					result[i] += stats[i + j];

			}
		}
		Log.d(LOGTAG, " Neighborstats: " + Arrays.toString(result));
		return result;
	}

	private void calcThreshold(FFTSample[] fftSamples) {
		for (int i = 0; i < fftSamples.length; i++) {
			calcThreshold(fftSamples[i]);
		}
	}

	private float getThreshold() {
		if (Morse.getMode() == Mode.MANUAL)
			return Preferences.GUI_PREFERENCE.getSquelch();
		else
			return threshold;
	}

	public boolean estimateValue(FFTSample sample) {
		return sample.getAverage(demodFrequency, demodBandwidth) > getThreshold();
	}

	public float getSymbolSuccessRate() {
		return codeSuccess.getSuccessRate();
	}

	@Subscribe
	public void onEvent(DemodFrequencyEvent event) {
		demodFrequency = event.getDemodFrequency();
		if (Morse.getMode() != Mode.MANUAL) {
			high = -Float.MAX_VALUE;
			low = Float.MAX_VALUE;
		}

	}

	public void calcThreshold(FFTSample sample) {
		float average = sample.getAverage(demodFrequency, demodBandwidth);
		high = Math.max(high, average);
		low = Math.min(low, average);
		threshold = low + (high - low) / 2;
	}

	public boolean checkStats() {
		return (symbolSuccess.checkStats() && symbolSuccess.checkStats());

	}

}
