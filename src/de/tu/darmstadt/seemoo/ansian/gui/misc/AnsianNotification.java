package de.tu.darmstadt.seemoo.ansian.gui.misc;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.R;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler.State;
import de.tu.darmstadt.seemoo.ansian.control.events.StateEvent;

/**
 * Notification to provide a control for the AnSiAn Service from outside the
 * activity
 *
 */
public class AnsianNotification {

	private NotificationManager notificationManager;
	private MainActivity activity;
	private String buttonAction = "ANSIAN_BUTTON_CLICKED";
	private ButtonListener receiver;

	public AnsianNotification(MainActivity activity) {
		this.activity = activity;
		EventBus.getDefault().register(this);
		notificationManager = (NotificationManager) activity.getSystemService(activity.NOTIFICATION_SERVICE);
		updateNotification(StateHandler.getState());
		IntentFilter filter = new IntentFilter();
		filter.addAction(buttonAction);
		receiver = new ButtonListener();
		activity.registerReceiver(receiver, filter);
	}

	
	/**
	 * Updates the notification state
	 * 
	 * @param state
	 */
	private void updateNotification(State state) {

		Intent intent = new Intent(activity, MainActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		PendingIntent pIntent = PendingIntent.getActivity(activity, 0, intent, 0);

		Intent buttonIntent = new Intent();
		buttonIntent.setAction(buttonAction);
		PendingIntent buttonPressed = PendingIntent.getBroadcast(activity, 0, buttonIntent, 0);

		Builder builder = new Builder(activity);
		builder.setContentTitle("AnSiAn - Android Signal Analyzer");

		switch (state) {
		case MONITORING:
			builder.setContentText("Service monitoring").addAction(R.drawable.ic_action_stop, "Stop", buttonPressed);
			break;
		case PAUSED:
			builder.setContentText("Service monitoring - GUI paused").addAction(R.drawable.ic_action_stop, "Stop",
					buttonPressed);
			break;
		case STOPPED:
			builder.setContentText("Service stopped").addAction(R.drawable.ic_action_play, "Start", buttonPressed);
			break;
		default:
			break;
		}

		builder.setLargeIcon(BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_launcher))
				.setSmallIcon(R.drawable.ic_notification).setContentIntent(pIntent).setAutoCancel(false)
				.setOngoing(true).setOnlyAlertOnce(true);

		notificationManager.notify(0, builder.build());
	}

	public static class ButtonListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// EventBus.getDefault().post(new StateEvent(State.));
			StateHandler.startOrStop();
		}
	}

	@Subscribe
	public void onEvent(StateEvent event) {
		updateNotification(event.getState());

	}

	public void destroy() {
		notificationManager.cancelAll();
		activity.unregisterReceiver(receiver);
	}

}
