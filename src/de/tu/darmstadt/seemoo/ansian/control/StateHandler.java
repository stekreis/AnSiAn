package de.tu.darmstadt.seemoo.ansian.control;

import android.app.Notification;
import android.util.Log;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.control.events.DemodulationEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.RecordingEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.RequestRecordingEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.RequestStateEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.StateEvent;
import de.tu.darmstadt.seemoo.ansian.control.threads.AnsianService;
import de.tu.darmstadt.seemoo.ansian.control.threads.SurfaceUpdateThread;
import de.tu.darmstadt.seemoo.ansian.gui.misc.MyToast;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.Demodulation.DemoType;
import de.tu.darmstadt.seemoo.ansian.model.preferences.MiscPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

public class StateHandler {

	public static enum State {
		MONITORING, PAUSED, STOPPED, SCANNING
	}

	public static SurfaceUpdateThread surfaceUpdateThread;
	// public static ScannerThread scannerThread;

	public static final String LOGTAG = "StateHandler";

	private static StateHandler instance;
	private static boolean recording = false;
	private static boolean regularMonitoring = true;

	private static DemoType demodulation;
	private static MiscPreferences preferences;
	private static AnsianService service;

	private static State state;
	private static State last;

	public static StateHandler getInstance() {
		if (instance == null)
			instance = new StateHandler();
		return instance;
	}

	private StateHandler() {
		EventBus.getDefault().register(this);
		preferences = Preferences.MISC_PREFERENCE;
		state = State.STOPPED;
		demodulation = preferences.getDemodulation();
		service = new AnsianService();
		}

	private static void start() {
		if (startService()) {
			setState(State.MONITORING);
		}
	};

	private static void setState(State newState) {
		state = newState;
		EventBus.getDefault().post(new StateEvent(state));
	}

	private static boolean startService() {
		boolean started = service.startService(demodulation);
		if (started) {
			setDemodulationMode(demodulation);
			startGUI();
		}
		
		return started;

	}

	private static void startGUI() {
		// TODO organize, use only one fragment for either one of both views
		// TODO deactivate demodulation when using scanner mode
		if (regularMonitoring) {
			surfaceUpdateThread = new SurfaceUpdateThread();
			surfaceUpdateThread.start();
		} else {
			// scannerThread = new ScannerThread();
			// scannerThread.start();
		}
	}

	private static void stopGUI() {
		// Stop the Processing Loop if running:
		if (surfaceUpdateThread != null) {
			surfaceUpdateThread.stopLoop();
		}
	}

	private static void stop() {
		setState(State.STOPPED);
		stopGUI();
		service.stopService();

	};

	private static void pause() {
		last = state;
		setState(State.PAUSED);
		// pauseGUI();

	};

	private void unpause() {
		setState(last);
		// unpauseGUI();
	};
	//
	// public static boolean startRecording(BufferedOutputStream
	// bufferedOutputStream, File recordingFile) {
	// Log.d(LOGTAG, "Rec started");
	// if (!recording) {
	// recording = true;
	// service.startRecording(bufferedOutputStream, recordingFile);
	// EventBus.getDefault().post(new RecordingEvent(true));
	// return true;
	// } else
	// return false;
	// }

	public void start(boolean b) {
		if (b)
			start();
		else
			stop();

	}

	public static boolean isRecording() {
		return recording;
	}

	public static State getState() {
		return state;
	}

	/**
	 * Will set the modulation mode to the given value. Takes care of adjusting
	 * the scheduler and the demodulator respectively and updates the action bar
	 * menu item.
	 *
	 * @param mode
	 *            Demodulator.DEMODULATION_OFF, *_AM, *_NFM, *_WFM
	 */
	public static void setDemodulationMode(DemoType mode) {
		if (mode != DemoType.OFF && isScanning()) {
			MyToast.makeText("Demodulation not allowed in wide scanning mode", Toast.LENGTH_LONG);
		} else {
			demodulation = mode;
			preferences.setDemodulation(mode);
			service.setDemodulationMode(mode);
			EventBus.getDefault().post(new DemodulationEvent(mode));
		}
	}

	// private static void pauseGUI() {
	// if (surfaceUpdateThread != null) {
	// surfaceUpdateThread.pause();
	// }
	// }
	//
	// private void unpauseGUI() {
	// if (surfaceUpdateThread != null) {
	// surfaceUpdateThread.unpause();
	// }
	// }

	@Subscribe
	public void onEvent(RequestStateEvent event) {
		Log.d(LOGTAG, "ismonitoring" + isMonitoring() + " event: " + event.getRequestState());
		State requestedState = event.getRequestState();
		if (requestedState != state)
			switch (requestedState) {
			case STOPPED:
				stop();
				break;

			case PAUSED:
				pause();
				break;

			case MONITORING:
				if (isScanning()) {
					setState(requestedState);
					break;
				}
				if (isPaused())
					unpause();
				else
					start();
				break;

			case SCANNING:
				if (!isDemodulating()) {
					if (isMonitoring()) {
						setState(requestedState);
						break;
					}

					if (isPaused())
						unpause();
					else
						start();
				}
				break;
			}

	}

	// public static void stopRecording() {
	// service.stopRecording();
	// }

	public static boolean isStopped() {
		return state == State.STOPPED;
	}

	public static boolean isScanning() {
		return state == State.SCANNING;
	}

	public static boolean isMonitoring() {
		return state == State.MONITORING;
	}

	public static boolean isPaused() {
		return state == State.PAUSED;
	}

	public static boolean isDemodulating() {
		return (DemoType.OFF != demodulation);
	}

	public static DemoType getActiveDemodulationMode() {
		return demodulation;

	}

	public AnsianService getService() {
		return service;
	}

	@Subscribe
	public void onEvent(RequestRecordingEvent event) {
		switch (state) {
		case PAUSED:
		case MONITORING:
			if (recording = false) {
				recording = true;
				EventBus.getDefault().post(new RecordingEvent(event));
			}
			break;
		case SCANNING:
			recording = false;
			EventBus.getDefault().post(new RecordingEvent("Recording while scanning is not supported."));
			break;
		case STOPPED:
			recording = false;
			EventBus.getDefault().post(new RecordingEvent("Service stopped. Please it start first."));
			break;
		default:
			break;
		}

	}

	@Subscribe
	public void onEvent(RecordingEvent event) {
		recording = event.isRecording();
	}

	public static void startOrStop() {
		if (isStopped()) {
			EventBus.getDefault().post(new RequestStateEvent(State.MONITORING));
		} else {
			EventBus.getDefault().post(new RequestStateEvent(State.STOPPED));
		}
	}

}
