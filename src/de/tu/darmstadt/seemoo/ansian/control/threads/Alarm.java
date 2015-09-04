package de.tu.darmstadt.seemoo.ansian.control.threads;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Vibrator;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.control.events.FFTDataEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.RecordingEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.RequestRecordingEvent;
import de.tu.darmstadt.seemoo.ansian.model.FFTSample;
import de.tu.darmstadt.seemoo.ansian.model.preferences.AlarmPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * Alarm thread gets started with the AnsianService.
 * 
 * @author Markus Grau
 *
 */
public class Alarm {

	private int volume = 100;
	private int vibrationDuration = 100;
	private ToneGenerator toneGenerator;
	private Vibrator vibrator;
	private AlarmPreferences preferences;
	private long last;

	public Alarm(MainActivity activity) {
		last = System.currentTimeMillis();
		preferences = Preferences.ALARM_PREFERENCE;
		setActive(true);
		toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, volume);
		vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
	}

	private void setActive(boolean b) {
		if (b)
			if (!EventBus.getDefault().isRegistered(this))
				EventBus.getDefault().register(this);
			else if (EventBus.getDefault().isRegistered(this))
				EventBus.getDefault().unregister(this);
	}

	private void check(FFTSample fftSample) {
		float[] values = fftSample.getMagnitudes();
		int length = values.length;
		if (values[length / 2] > preferences.getAlarmThreshold()) {
			alarm();
		}
	}

	private void alarm() {
		if (preferences.isAlarmVibration())
			vibrator.vibrate(vibrationDuration);
		if (preferences.isAlarmSound())
			toneGenerator.startTone(preferences.getToneType());
		if (preferences.isRecording())
			new RequestRecordingEvent(preferences.getPlannedRecording());

	}

	public void start() {
		setActive(preferences.isAlarmActive());
	}

	public void stop() {
		setActive(false);
	}

	@Subscribe
	public void onEvent(FFTDataEvent event) {
		if (System.currentTimeMillis() - last > preferences.getAlarmInterval()) {
			last = System.currentTimeMillis();
			if (!preferences.isAlarmActive())
				return;
			check(event.getSample());
		}
	}

	@Subscribe
	public void onEvent(RecordingEvent event) {
		if (event.getRecording() == preferences.getPlannedRecording())
			preferences.setRecording(false);
	}
}
