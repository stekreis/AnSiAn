package de.tu.darmstadt.seemoo.ansian.gui.dialogs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import de.tu.darmstadt.seemoo.ansian.MainActivity;
import de.tu.darmstadt.seemoo.ansian.R;
import de.tu.darmstadt.seemoo.ansian.control.SourceControl;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.control.events.RequestRecordingEvent;
import de.tu.darmstadt.seemoo.ansian.gui.misc.MyToast;
import de.tu.darmstadt.seemoo.ansian.model.Recording;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.Demodulation.DemoType;
import de.tu.darmstadt.seemoo.ansian.model.preferences.MiscPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;
import de.tu.darmstadt.seemoo.ansian.model.sources.IQSourceInterface.SourceType;

public class RecordingDialog extends MyDialogFragment {

	String externalDir;
	int[] supportedSampleRates;
	double maxFreqMHz; // max frequency of the source in MHz
	SourceType sourceType;
	MiscPreferences preferences;
	SimpleDateFormat simpleDateFormat;

	public static final String RECORDING_DIR = "AnSiAn";
	public static final String LOGTAG = "RecordingDialog";

	EditText et_filename;
	EditText et_frequency;
	Spinner sp_sampleRate;
	TextView tv_fixedSampleRateHint;
	CheckBox cb_stopAfter;
	EditText et_stopAfter;
	Spinner sp_stopAfter;
	private boolean startRecording = true;

	public RecordingDialog(boolean b) {
		startRecording = b;
	}

	@Override
	protected View createView() {
		preferences = Preferences.MISC_PREFERENCE;
		// Get references to the GUI components:
		view = getActivity().getLayoutInflater().inflate(R.layout.start_recording, null);
		et_filename = (EditText) view.findViewById(R.id.et_recording_filename);
		et_frequency = (EditText) view.findViewById(R.id.et_recording_frequency);
		et_frequency.setText("" + Preferences.GUI_PREFERENCE.getFrequency());
		sp_sampleRate = (Spinner) view.findViewById(R.id.sp_recording_sampleRate);

		tv_fixedSampleRateHint = (TextView) view.findViewById(R.id.tv_recording_fixedSampleRateHint);
		cb_stopAfter = (CheckBox) view.findViewById(R.id.cb_recording_stopAfter);
		et_stopAfter = (EditText) view.findViewById(R.id.et_recording_stopAfter);
		sp_stopAfter = (Spinner) view.findViewById(R.id.sp_recording_stopAfter);

		externalDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		supportedSampleRates = SourceControl.getSource().getSupportedSampleRates();
		maxFreqMHz = SourceControl.getSource().getMaxFrequency() / 1000000f; // max
																				// frequency
																				// of
																				// the
																				// source
																				// in
																				// MHz
		sourceType = preferences.getSourceType();
		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);

		// Setup the sample rate spinner:
		final ArrayAdapter<Integer> sampleRateAdapter = new ArrayAdapter<Integer>(MainActivity.instance,
				android.R.layout.simple_list_item_1);
		for (int sampR : supportedSampleRates)
			sampleRateAdapter.add(sampR);
		sampleRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_sampleRate.setAdapter(sampleRateAdapter);

		// Add listener to the frequency textfield, the sample rate spinner and
		// the checkbox:
		et_frequency.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (et_frequency.getText().length() == 0)
					return;
				double freq = Double.valueOf(et_frequency.getText().toString());
				if (freq < maxFreqMHz)
					freq = freq * 1000000;
				et_filename.setText(simpleDateFormat.format(new Date()) + "_" + sourceType.toString() + "_"
						+ (long) freq + "Hz_" + sp_sampleRate.getSelectedItem() + "Sps.iq");
			}
		});
		sp_sampleRate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (et_frequency.getText().length() == 0)
					return;
				double freq = Double.valueOf(et_frequency.getText().toString());
				if (freq < maxFreqMHz)
					freq = freq * 1000000;
				et_filename.setText(simpleDateFormat.format(new Date()) + "_" + sourceType.toString() + "_"
						+ (long) freq + "Hz_" + sp_sampleRate.getSelectedItem() + "Sps.iq");
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		cb_stopAfter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				et_stopAfter.setEnabled(isChecked);
				sp_stopAfter.setEnabled(isChecked);
			}
		});

		// // Set default frequency, sample rate and stop after values:
		// et_frequency.setText(""
		// + AnalyzerSurface.getInstance().getVirtualFrequency());
		int sampleRateIndex = 0;
		int lastSampleRate = preferences.getSampleRate();
		for (; sampleRateIndex < supportedSampleRates.length; sampleRateIndex++) {
			if (supportedSampleRates[sampleRateIndex] >= lastSampleRate)
				break;
		}
		if (sampleRateIndex >= supportedSampleRates.length)
			sampleRateIndex = supportedSampleRates.length - 1;
		sp_sampleRate.setSelection(sampleRateIndex);
		cb_stopAfter.toggle(); // just to trigger the listener at least once!
		cb_stopAfter.setChecked(preferences.isRecordingStoppedAfterEnabled());
		et_stopAfter.setText("" + preferences.getRecordingStoppedAfterValue());
		sp_stopAfter.setSelection(preferences.getRecordingStoppedAfterUnit());

		// disable sample rate selection if demodulation is running:
		if (preferences.getDemodulation() != DemoType.OFF) {
			sampleRateAdapter.add(SourceControl.getSource().getSampleRate()); // add
																				// the
																				// current
																				// sample
																				// rate
																				// in
																				// case
																				// it's
																				// not
																				// already
																				// in
																				// the
																				// list
			sp_sampleRate.setSelection(sampleRateAdapter.getPosition(SourceControl.getSource().getSampleRate())); // select
																													// it
			sp_sampleRate.setEnabled(false); // disable the spinner
			tv_fixedSampleRateHint.setVisibility(View.VISIBLE);
		}
		return view;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		builder.setTitle("Start recording").setView(view).setPositiveButton("Record",
				new DialogInterface.OnClickListener() {
					private File recordingFile;

					public void onClick(DialogInterface dialog, int whichButton) {
						String filename = et_filename.getText().toString();
						final int stopAfterUnit = sp_stopAfter.getSelectedItemPosition();
						final int stopAfterValue = Integer.valueOf(et_stopAfter.getText().toString());
						// todo check filename

						// Set the frequency in the source:
						if (et_frequency.getText().length() == 0)
							return;
						double freq = Double.valueOf(et_frequency.getText().toString());
						if (freq < maxFreqMHz)
							freq = freq * 1000000;
						if (freq <= SourceControl.getSource().getMaxFrequency()
								&& freq >= SourceControl.getSource().getMinFrequency())
							SourceControl.getSource().setFrequency((long) freq);
						else {
							Toast.makeText(MainActivity.instance, "Frequency is invalid!", Toast.LENGTH_LONG).show();
							return;
						}

						// Set the sample rate (only if demodulator is
						// off):
						if (preferences.getDemodulation() == DemoType.OFF)
							SourceControl.getSource().setSampleRate((Integer) sp_sampleRate.getSelectedItem());

						// Open file and start recording:
						recordingFile = new File(externalDir + "/" + RECORDING_DIR + "/" + filename);
						recordingFile.getParentFile().mkdir(); // Create
																// directory
																// if
																// it
																// does
																// not
																// yet
																// exist

						// safe preferences:

						preferences.setRecordingSampleRate((Integer) sp_sampleRate.getSelectedItem());
						preferences.setRecordingStoppedAfterEnabled(cb_stopAfter.isChecked());
						preferences.setRecordingStoppedAfterValue(stopAfterValue);
						preferences.setRecordingStoppedAfterUnit(stopAfterUnit);
						if (startRecording)
							EventBus.getDefault().post(new RequestRecordingEvent(new Recording(recordingFile)));
						else
							Preferences.ALARM_PREFERENCE.setRecording(new Recording(recordingFile));
					}
				});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Preferences.ALARM_PREFERENCE.setRecording(false);
			}
		});

		MainActivity.instance.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		return builder.create();

	}

	@Override
	public void show() {
		if (StateHandler.isStopped() || SourceControl.getSource() == null) {
			MyToast.makeText("Analyzer must be running to start recording", Toast.LENGTH_LONG);
		} else
			super.show();
	}

}
