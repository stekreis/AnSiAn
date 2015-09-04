package de.tu.darmstadt.seemoo.ansian.tools.morse;

import android.os.AsyncTask;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseCharPlayedEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseDitDurationEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseSendEvent;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

public class MorsePlayer extends AsyncTask<String, Integer, Boolean> {
	@SuppressWarnings("unused")
	private static final String LOGTAG = "MorsePlayer";
	private char[] morseString;
	private int dit, dah, word;
	private MorseAudioTrack ditTrack, dahTrack;
	private boolean running = true;

	private void playDah() {
		dahTrack.playSound();
	}

	private void playDit() {
		ditTrack.playSound();
	}

	protected void onPostExecute(Boolean result) {
		ditTrack.release();
		dahTrack.release();
		EventBus.getDefault().unregister(this);
	}

	@Override
	protected void onPreExecute() {
		EventBus.getDefault().register(this);
		initTiming();
		super.onPreExecute();
	}

	private void initTiming() {
		dit = Preferences.MORSE_PREFERENCE.getDitDuration();
		dah = 3 * dit;
		word = 7 * dit;
		ditTrack = generateTone(Preferences.MORSE_PREFERENCE.getMorseFrequency(), dit);
		dahTrack = generateTone(Preferences.MORSE_PREFERENCE.getMorseFrequency(), dah);

	}

	@Override
	protected Boolean doInBackground(String... params) {
		morseString = params[0].toCharArray();
		boolean result = true;
		boolean lastSymbol = false;
		int counter = 1;

		for (char c : morseString) {
			if (!running)
				continue;
			switch (c) {
			case '.':
				if (lastSymbol)
					wait(dit);
				playDit();
				lastSymbol = true;
				break;
			case '-':
				if (lastSymbol)
					wait(dit);
				playDah();
				lastSymbol = true;
				break;
			case ' ':
				wait(dah);
				lastSymbol = false;
				break;
			case '/':
				wait(word);
				lastSymbol = false;
				break;
			default:
				result = false;
			}
			EventBus.getDefault().post(new MorseCharPlayedEvent(counter));
			counter++;
		}

		EventBus.getDefault().post(new MorseSendEvent(false));
		return result;
	}

	private MorseAudioTrack generateTone(double freqHz, int durationMs) {
		int samplingRate = 44100; /* in Hz */
		int count = (int) (samplingRate * 2.0 * (durationMs / 1000.0)) & ~1;

		short[] sound = new short[count];
		for (int i = 0; i < count; i += 2) {
			short sample = (short) (Math.sin(2 * Math.PI * i / (44100.0 / freqHz)) * 0x7FFF);
			sound[i + 0] = sample;
			sound[i + 1] = sample;
		}
		MorseAudioTrack track = new MorseAudioTrack(samplingRate, sound);

		return track;
	}

	private void wait(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop() {
		running = false;

	}

	@Subscribe
	public void onEvent(MorseDitDurationEvent event) {
		initTiming();
	}

}
