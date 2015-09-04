package de.tu.darmstadt.seemoo.ansian.model.preferences;

import android.media.ToneGenerator;
import android.util.Log;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.R;
import de.tu.darmstadt.seemoo.ansian.model.Recording;

public class AlarmPreferences extends MySharedPreferences {

	public AlarmPreferences(MainActivity activity) {
		super(activity);
	}

	public static final String ALARM_PREFERENCES = "alarm_preferences";

	private static final String LOGTAG = "AlarmPreferences";

	private boolean active;
	private boolean vibrate;
	private boolean sound;
	private float threshold;
	private int toneType;
	private int alarm_interval;
	private boolean recording;
	private Recording plannedRecording;

	/**
	 * Will check if any preference conflicts with the current state of the app
	 * and fix it
	 */
	public void loadPreference() {
		// Alarm
		active = getBoolean("active", false);
		vibrate = getBoolean("vibrate", true);
		sound = getBoolean("sound", true);
		threshold = getFloat("threshold", -20);
		toneType = getInt("tone_type", ToneGenerator.TONE_PROP_BEEP);
		alarm_interval = getInt("alarm_interval", 1000);
		recording = getBoolean("recording", false);
	}

	public void savePreference() {
		// create editor
		MyEditor editor = edit();
		editor.putBoolean("active", active);
		editor.putBoolean("vibrate", vibrate);
		editor.putBoolean("sound", sound);
		editor.putFloat("threshold", threshold);
		editor.putBoolean("recording", recording);
		Log.d(LOGTAG, LOGTAG + " saved: " + editor.commit());
	}

	public boolean isAlarmSound() {
		return sound;
	}

	public boolean isAlarmVibration() {

		return vibrate;
	}

	public boolean isAlarmActive() {

		return active;
	}

	public float getAlarmThreshold() {
		return threshold;
	}

	@Override
	public String getName() {
		return "alarm";
	}

	@Override
	public int getResID() {
		return R.xml.alarm_preferences;
	}

	public int getToneType() {
		return toneType;
	}

	public long getAlarmInterval() {
		return alarm_interval;
	}

	public void setRecording(Recording recording) {
		plannedRecording = recording;

	}

	public void setRecording(boolean b) {
		recording = b;
		if (b)
			plannedRecording = null;
	}

	public boolean isRecording() {
		return recording;
	}

	public Recording getPlannedRecording() {
		return plannedRecording;
	}

}
