package de.tu.darmstadt.seemoo.ansian.gui.views.morse;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.R;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseDitDurationEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseSendEvent;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * This view bunches all the sensor data in a compact view with a picture for
 * understanding in the layout.
 * 
 * @author Markus
 *
 */
public class MorseButtonView extends MyMorseView {

	private SeekBar morseWPMSeekBar;
	private TextView morseWPMLabel;
	private Button playButton;
	private static boolean sending = false;

	public MorseButtonView(Context context) {
		super(context);
	}

	public MorseButtonView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MorseButtonView(Context context, AttributeSet attrs, int defaultStyle) {
		super(context, attrs, defaultStyle);
	}

	protected void init() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.morse_button, this);

		morseWPMLabel = (TextView) findViewById(R.id.morseWPMLabel);

		morseWPMSeekBar = (SeekBar) findViewById(R.id.morseWPMSeekBar);

		morseWPMSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					int wpm = progress + 1;
					Preferences.MORSE_PREFERENCE.setWPM(wpm);
					EventBus.getDefault()
							.postSticky(new MorseDitDurationEvent(Preferences.MORSE_PREFERENCE.getDitDuration()));
				}
				updateWPMLabel();
			}
		});

		playButton = (Button) findViewById(R.id.morseButton);
		playButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventBus.getDefault().post(new MorseSendEvent(!sending));
			}
		});
		setButtonText(sending);
		update();

	}

	public void update() {
		updateWPMLabel();
		morseWPMSeekBar.setProgress(Preferences.MORSE_PREFERENCE.getWPM());

	}

	private void updateWPMLabel() {
		morseWPMLabel.setText(String.format(getContext().getString(R.string.morse_wpm_label),
				getWPMStringRepresentation(), Preferences.MORSE_PREFERENCE.getDitDuration()));
	}

	public String getWPMStringRepresentation() {
		float wpm = Preferences.MORSE_PREFERENCE.getWPM();
		if (wpm < 1)
			return "<1";
		if (wpm > 20)
			return ">20";

		return "" + (int) wpm;
	}

	private void setButtonText(boolean b) {
		if (b)
			playButton.setText(R.string.morse_button_stop);
		else
			playButton.setText(R.string.morse_button_send);
	}

	@Subscribe
	public void onEvent(final MorseSendEvent event) {
		MainActivity.instance.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setButtonText(sending = event.isSending());
			}
		});

	}

}
