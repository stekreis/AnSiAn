package de.tu.darmstadt.seemoo.ansian.gui.views.morse;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView.BufferType;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.R;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseCharPlayedEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseEditTextEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseSendEvent;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;
import de.tu.darmstadt.seemoo.ansian.tools.morse.Decoder;
import de.tu.darmstadt.seemoo.ansian.tools.morse.Encoder;
import de.tu.darmstadt.seemoo.ansian.tools.morse.MorsePlayer;

/**
 * This view bunches all the sensor data in a compact view with a picture for
 * understanding in the layout.
 * 
 * @author Markus
 *
 */
public class MorseSendingView extends MyMorseView {

	private EditText morseTextField;
	private EditText morseCodeField;
	private Decoder morseDecoder;
	private Encoder morseEncoder;
	private String morseCodeTemp;

	protected boolean updating = false;
	private static MorsePlayer player;

	public MorseSendingView(Context context) {
		super(context);
	}

	public MorseSendingView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MorseSendingView(Context context, AttributeSet attrs, int defaultStyle) {
		super(context, attrs, defaultStyle);
	}

	protected void init() {
		morseDecoder = new Decoder();
		morseEncoder = new Encoder();
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.morse_sending_view, this);

		morseCodeField = (EditText) findViewById(R.id.morseCode);
		morseCodeField.setTextColor(Color.WHITE);

		morseTextField = (EditText) findViewById(R.id.morseText);
		morseTextField.setTextColor(Color.WHITE);

		morseCodeField.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// if(morseDecoder.decode(s.toString()).contains(MorseCodeCharacterGetter.ERROR_STRING))
				// morseCodeField.setText(morseCodeField.getText().subSequence(0,
				// start));
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				updateMorseText();

			}
		});

		morseTextField.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				updateMorseCode();

			}
		});

		MorseEditTextEvent event = EventBus.getDefault().getStickyEvent(MorseEditTextEvent.class);
		if (event != null) {
			morseTextField.setText(event.getText());
			morseCodeTemp = event.getMorseCodeTemp();
			updateMorseCode();
		}
	}

	private void updateMorseText() {
		if (updating)
			return;
		updating = true;
		morseTextField.setText(morseDecoder.decode(morseCodeField.getText().toString()));
		updating = false;
	}

	private void updateMorseCode() {
		if (updating)
			return;
		updating = true;
		morseCodeField.setText(morseEncoder.encode(morseTextField.getText().toString()));
		updating = false;
	}

	public String getWPMStringRepresentation() {
		float wpm = Preferences.MORSE_PREFERENCE.getWPM();
		if (wpm < 1)
			return "<1";
		if (wpm > 20)
			return ">20";

		return "" + (int) wpm;
	}

	@Subscribe
	public void onEvent(final MorseCharPlayedEvent event) {
		MainActivity.instance.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setMorseCodeHighlight(event.getCounter());
			}
		});

	}

	@Subscribe
	public void onEvent(final MorseSendEvent event) {
		MainActivity.instance.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (event.isSending())
					playMorse();
				else
					stopMorse();
			}
		});

	}

	private void setMorseCodeHighlight(int index) {
		if (index < morseCodeTemp.length()) {
			SpannableString text = new SpannableString(morseCodeTemp);
			text.setSpan(new BackgroundColorSpan(Color.RED), index, index + 1, 0);
			// text.setSpan(new ForegroundColorSpan(Color.MAGENTA), index,
			// index+1, 0);
			morseCodeField.setText(text, BufferType.SPANNABLE);
		} else {
			morseCodeField.setText(morseCodeTemp);
			enable(Preferences.MORSE_PREFERENCE.isClearAfter());
		}
		;
	}

	private void disable() {
		// morseStateText.setText(R.string.morse_state_playing_sound);
		findViewById(R.id.morseLayout).requestFocus();
		morseCodeField.setFocusableInTouchMode(false);
		morseTextField.setFocusableInTouchMode(false);
	}

	private void enable(boolean clear) {
		// morseStateText.setText(R.string.morse_state_user_input);
		morseCodeField.setFocusableInTouchMode(true);
		morseTextField.setFocusableInTouchMode(true);
		if (clear)
			morseTextField.setText("");

	}

	protected void playMorse() {
		disable();
		player = new MorsePlayer();
		morseCodeTemp = morseCodeField.getText().toString();
		player.execute(morseCodeTemp);
		setMorseCodeHighlight(0);
	}

	protected void stopMorse() {
		enable(false);
		morseCodeField.setText(morseCodeTemp);
		player.stop();
	}

	@Override
	protected void onDetachedFromWindow() {
		EventBus.getDefault().postSticky(new MorseEditTextEvent(morseTextField.getText(), morseCodeTemp));
		super.onDetachedFromWindow();
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}
}
