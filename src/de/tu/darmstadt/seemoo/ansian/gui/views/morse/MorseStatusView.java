package de.tu.darmstadt.seemoo.ansian.gui.views.morse;

import android.content.Context;
import android.content.Context;
import android.util.AttributeSet;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.R;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseCodeEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseDitEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseStateEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseSymbolEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.RequestMorseStateEvent;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.morse.Morse.State;

/**
 * This view bunches all the sensor data in a compact view with a picture for
 * understanding in the layout.
 * 
 * @author Markus Grau
 *
 */
public class MorseStatusView extends MyMorseView {
	private TextView morseSymbolSuccessRate;
	private TextView morseCodeSuccessRate;
	private TextView morseEstimatedDitDuration;
	private Button morseInitButton;
	private TextView morseThreshold;
	private static TextView morseStateText;

	public MorseStatusView(Context context) {
		super(context);
	}

	public MorseStatusView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public MorseStatusView(Context context, AttributeSet attrs, int defaultStyle) {
		super(context, attrs, defaultStyle);

	}

	protected void init() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.morse_status_view, this);
		morseInitButton = (Button) findViewById(R.id.morseInitButton);
		morseStateText = (TextView) findViewById(R.id.morseStateText);
		morseCodeSuccessRate = (TextView) findViewById(R.id.morseCodeSuccessRate);
		morseSymbolSuccessRate = (TextView) findViewById(R.id.morseSymbolSuccessRate);
		morseEstimatedDitDuration = (TextView) findViewById(R.id.morseEstimatedDitDuration);
		morseThreshold = (TextView) findViewById(R.id.morseThreshold);
		morseInitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EventBus.getDefault().post(new RequestMorseStateEvent(State.INIT));
			}
		});

	}

	public void update() {
		// init();
		onEventMainThread(EventBus.getDefault().getStickyEvent(MorseCodeEvent.class));
		onEventMainThread(EventBus.getDefault().getStickyEvent(MorseSymbolEvent.class));
		onEventMainThread(EventBus.getDefault().getStickyEvent(MorseStateEvent.class));
		onEventMainThread(EventBus.getDefault().getStickyEvent(MorseDitEvent.class));
		// morseStateText.setText(Morse.getState().toString());
		// morseSymbolSuccessRate.setText(Preferences.MORSE_PREFERENCE.getSymbolSuccessRate());
		// morseCodeSuccessRate.setText(Preferences.MORSE_PREFERENCE.getCodeSuccessRate());

	}

	@Subscribe
	public void onEventMainThread(final MorseStateEvent event) {
		if (event != null)
			MainActivity.instance.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					morseStateText.setText(event.getState().toString());
				}
			});

	}

	@Subscribe
	public void onEventMainThread(final MorseCodeEvent event) {
		if (event != null)
			MainActivity.instance.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					morseCodeSuccessRate.setText(event.getSuccessRateString());
					morseThreshold.setText(event.getThresholdString());
				}
			});

		// if (event.getInRange())
		// morseCodeField.append(event.getCode());

	}

	@Subscribe
	public void onEventMainThread(final MorseSymbolEvent event) {
		if (event != null)
			MainActivity.instance.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					morseSymbolSuccessRate.setText(event.getSuccessRateString());
				}
			});

		// if (event.getInRange())
		// morseCodeField.append(event.getCode());
	}

	@Subscribe
	public void onEventMainThread(final MorseDitEvent event) {
		if (event != null)
			MainActivity.instance.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					morseEstimatedDitDuration.setText(event.getDit() + " ms");
				}
			});

		// if (event.getInRange())
		// morseCodeField.append(event.getCode());
	}

}
