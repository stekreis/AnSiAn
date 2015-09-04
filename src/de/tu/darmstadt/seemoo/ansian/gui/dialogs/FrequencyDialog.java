package de.tu.darmstadt.seemoo.ansian.gui.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import de.greenrobot.event.EventBus;
import de.tu.darmstadt.seemoo.ansian.R;
import de.tu.darmstadt.seemoo.ansian.control.SourceControl;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.control.events.RequestFrequencyEvent;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * Will pop up a dialog to let the user input a new frequency
 */
public class FrequencyDialog extends MyDialogFragment {

	// calculate max frequency of the source in MHz:
	private static final String LOGTAG = "FrequencyDialog";
	final double maxFreqMHz = SourceControl.getSource().getMaxFrequency() / 1000000f;
	private NumberPicker frequencyPicker;
	private CheckBox cb_bandwidth;
	private EditText et_bandwidth;
	private Spinner sp_bandwidthUnit;
	private TextView tv_warning;
	private Spinner frequencyUnitSpinner;

	@SuppressLint("InflateParams")
	@Override
	protected View createView() {
		view = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.tune_to_frequency, null);

		final long frequency = Preferences.GUI_PREFERENCE.getFrequency();
		// int selection = 0;
		// if (frequency % 1000 == 0)
		// selection++;
		// if (frequency % 1000000 == 0)
		// selection++;
		// frequencyUnitSpinner.setSelection(selection);
		frequencyPicker = (NumberPicker) view.findViewById(R.id.frequencyPicker);
		frequencyUnitSpinner = (Spinner) view.findViewById(R.id.frequencyUnitSpinner);

		// ArrayAdapter<CharSequence> adapter;
		// if (SourceControl.getSource().getMinFrequency()<1000)
		// adapter = ArrayAdapter.createFromResource(
		// getActivity(), R.array.frequency_units,
		// android.R.layout.simple_spinner_item);
		// else
		// adapter = ArrayAdapter.createFromResource(
		// getActivity(), R.array.frequency_units_no_hz,
		// android.R.layout.simple_spinner_item);
		// frequencyUnitSpinner.setAdapter(adapter);
		frequencyUnitSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				int divider = 1;
				switch (position) {
				case 0:
					divider = 1000000;
					break;
				case 1:
					divider = 1000;
					break;
				}

				frequencyPicker.setMinValue((int) (SourceControl.getSource().getMinFrequency() / divider));
				frequencyPicker.setMaxValue((int) (SourceControl.getSource().getMaxFrequency() / divider));
				frequencyPicker.setValue((int) (frequency / divider));

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		cb_bandwidth = (CheckBox) view.findViewById(R.id.cb_tune_to_frequency_bandwidth);
		et_bandwidth = (EditText) view.findViewById(R.id.et_tune_to_frequency_bandwidth);
		sp_bandwidthUnit = (Spinner) view.findViewById(R.id.sp_tune_to_frequency_bandwidth_unit);
		tv_warning = (TextView) view.findViewById(R.id.tv_tune_to_frequency_warning);

		if (SourceControl.getSource() == null)
			return null;

		// Show warning if we are currently recording to file:
		if (StateHandler.isRecording())
			tv_warning.setVisibility(View.VISIBLE);

		cb_bandwidth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				et_bandwidth.setEnabled(isChecked);
				sp_bandwidthUnit.setEnabled(isChecked);
			}
		});
		cb_bandwidth.toggle(); // to trigger the onCheckedChangeListener at
								// least once to set inital state
		sp_bandwidthUnit.setSelection(2);
		// TODOcb_bandwidth.setChecked(preferences.isSetBandwidth());
		et_bandwidth.setText("" + Preferences.GUI_PREFERENCE.getBandwidth());
		// TODO sp_bandwidthUnit.setSelection(preferences.getBandwidthUnit());
		return view;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
		builder.setTitle("Tune to Frequency").setPositiveButton("Set", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					int newFreq = (int) (frequencyPicker.getValue()
							* Math.pow(1000, 2-frequencyUnitSpinner.getSelectedItemPosition()));
					EventBus.getDefault().post(new RequestFrequencyEvent(newFreq));
					// AnalyzerSurface.getInstance()
					// .setVirtualFrequency(newFreq);

					// Set bandwidth (virtual sample rate):
					if (cb_bandwidth.isChecked() && et_bandwidth.getText().length() != 0) {
						float bandwidth = Float.valueOf(et_bandwidth.getText().toString());
						if (sp_bandwidthUnit.getSelectedItemPosition() == 0) // MHz
							bandwidth *= 1000000;
						else if (sp_bandwidthUnit.getSelectedItemPosition() == 1) // KHz
							bandwidth *= 1000;
						if (bandwidth > SourceControl.getSource().getMaxSampleRate())
							bandwidth = SourceControl.getSource().getMaxFrequency();
						SourceControl.getSource().setSampleRate(
								SourceControl.getSource().getNextHigherOptimalSampleRate((int) bandwidth));

						// safe preferences:
						// TODOpreferences.setBandwidthSet(cb_bandwidth.isChecked());

						// TODO
						// preferences.setBandwidth(et_bandwidth.getText().toString());
						// TODOpreferences.setBandwidthUnit(sp_bandwidthUnit.getSelectedItemPosition());

					}
				} catch (NumberFormatException e) {
					Log.e(LOGTAG, "tuneToFrequency: Error while setting frequency: " + e.getMessage());
				}
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// do nothing
			}
		});
		return builder.create();
	}

}
