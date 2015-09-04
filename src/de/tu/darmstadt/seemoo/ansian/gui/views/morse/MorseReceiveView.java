package de.tu.darmstadt.seemoo.ansian.gui.views.morse;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.TextView;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.R;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseCodeEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseStateEvent;
import de.tu.darmstadt.seemoo.ansian.control.events.morse.MorseSymbolEvent;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.morse.Morse.State;
import de.tu.darmstadt.seemoo.ansian.tools.morse.Decoder;

public class MorseReceiveView extends MyMorseView {

	private TextView codeText, symbolText;

	private String LOGTAG = "MorseReceiveView";
	StringBuffer codeBuffer;
	StringBuffer symbolLine;
	Decoder decoder;

	public MorseReceiveView(Context context) {
		super(context);
	}

	public MorseReceiveView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MorseReceiveView(Context context, AttributeSet attrs, int defaultStyle) {
		super(context, attrs, defaultStyle);
	}

	@Override
	protected void init() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.morse_receive_view, this);
		codeText = (TextView) findViewById(R.id.code_text);
		symbolText = (TextView) findViewById(R.id.symbol_text);
		decoder = new Decoder();
		codeText.setGravity(Gravity.RIGHT);
		codeText.setHorizontallyScrolling(true);
		symbolText.setGravity(Gravity.RIGHT);
		symbolText.setHorizontallyScrolling(true);
		MorseCodeEvent event = EventBus.getDefault().getStickyEvent(MorseCodeEvent.class);
		if (event != null) {
			codeBuffer = new StringBuffer(event.getCompleteCodeString());
			codeText.setText(codeBuffer);
			symbolText.setText(new StringBuilder().append(decoder.decode(cutString(codeBuffer))));
		} else {
			codeBuffer = new StringBuffer();
		}

	}

	@Subscribe
	public void onEvent(final MorseCodeEvent event) {
		if (event.isInRange()) {
			MainActivity.instance.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					codeBuffer = new StringBuffer(event.getCompleteCodeString());
					// if(codeLine.length()>150)
					// codeLine= new
					// StringBuffer(codeLine.subSequence(codeLine.length()-150,
					// codeLine.length()));
					// setText(new
					// StringBuilder().append(codeLine).append("\n").append(decoder.decode(codeLine.toString())));
					// show/hide morse live ticker
					// showHideViews();
					symbolText.setText(new StringBuilder().append(decoder.decode(cutString(codeBuffer))));
					codeText.setText(codeBuffer);
				}

			});

		}
	}

	@Subscribe
	public void onEvent(final MorseSymbolEvent event) {
		if (event.isRecognized()) {
			MainActivity.instance.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// String symbol = event.getSymbol();
					// int length = event.getCurrentSymbolCode().length();
					// for(int i =0;i<length/2;i++)
					// symbolLine.append(" ");
					// symbolLine.append(symbol);
					// for(int i =0;i<length/2;i++)
					// symbolLine.append(" ");
					// if(symbolLine.length()>50)
					//
					// symbolLine= new
					// StringBuffer(symbolLine.subSequence(symbolLine.length()-50,
					// symbolLine.length()));
					// setText(new
					// StringBuilder().append(codeLine).append("\n").append(symbolLine));
					// symbolLine= ;
					// Log.d(LOGTAG , "MorseSymbolEvent");

				}
			});
		}
	}

	@Subscribe
	public void onEvent(final MorseStateEvent event) {
		if (event.getState() == State.STOPPED) {
			MainActivity.instance.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					codeBuffer = new StringBuffer();
					symbolLine = new StringBuffer();
				}
			});
		}
	}

	private String cutString(StringBuffer buffer) {
		int start = buffer.indexOf(" ");
		int end = buffer.lastIndexOf(" ");
		if (start == end || start == -1 || end == -1)
			return "";
		else
			return buffer.substring(start, end);
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}
}
