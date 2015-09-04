package de.tu.darmstadt.seemoo.ansian.gui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import de.tu.darmstadt.seemoo.ansian.R;
import de.tu.darmstadt.seemoo.ansian.control.SourceControl;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.gui.misc.MyToast;
import de.tu.darmstadt.seemoo.ansian.model.preferences.MiscPreferences;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;
import de.tu.darmstadt.seemoo.ansian.model.sources.HackrfSource;
import de.tu.darmstadt.seemoo.ansian.model.sources.IQSourceInterface.SourceType;
import de.tu.darmstadt.seemoo.ansian.model.sources.RtlsdrSource;

/**
 * Will pop up a dialog to let the user adjust gain settings
 */
public class AdjustGainDialog extends MyDialogFragment {

	protected View createView() {
		final MiscPreferences preferences = Preferences.MISC_PREFERENCE;
		SourceType sourceType = preferences.getSourceType();

		switch (sourceType) {
		case FILE_SOURCE:
			MyToast.makeText(getActivity().getString(R.string.filesource_doesnt_support_gain), Toast.LENGTH_LONG);
			break;
		case HACKRF_SOURCE:
			// Prepare layout:
			view = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.hackrf_gain, null);
			final SeekBar sb_hackrf_vga = (SeekBar) view.findViewById(R.id.sb_hackrf_vga_gain);
			final SeekBar sb_hackrf_lna = (SeekBar) view.findViewById(R.id.sb_hackrf_lna_gain);
			final TextView tv_hackrf_vga = (TextView) view.findViewById(R.id.tv_hackrf_vga_gain);
			final TextView tv_hackrf_lna = (TextView) view.findViewById(R.id.tv_hackrf_lna_gain);
			sb_hackrf_vga.setMax(HackrfSource.MAX_VGA_RX_GAIN / HackrfSource.VGA_RX_GAIN_STEP_SIZE);
			sb_hackrf_lna.setMax(HackrfSource.MAX_LNA_GAIN / HackrfSource.LNA_GAIN_STEP_SIZE);
			sb_hackrf_vga.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					tv_hackrf_vga.setText("" + progress * HackrfSource.VGA_RX_GAIN_STEP_SIZE);
					((HackrfSource) SourceControl.getSource())
							.setVgaRxGain(progress * HackrfSource.VGA_RX_GAIN_STEP_SIZE);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});
			sb_hackrf_lna.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					tv_hackrf_lna.setText("" + progress * HackrfSource.LNA_GAIN_STEP_SIZE);
					((HackrfSource) SourceControl.getSource()).setLnaGain(progress * HackrfSource.LNA_GAIN_STEP_SIZE);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});
			sb_hackrf_vga.setProgress(
					((HackrfSource) SourceControl.getSource()).getVgaRxGain() / HackrfSource.VGA_RX_GAIN_STEP_SIZE);
			sb_hackrf_lna.setProgress(
					((HackrfSource) SourceControl.getSource()).getLnaGain() / HackrfSource.LNA_GAIN_STEP_SIZE);

			// dialog:
			builder.setTitle("Adjust Gain Settings").setView(view)
					.setPositiveButton("Set", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// safe preferences:
							preferences.setVgaRxGain(sb_hackrf_vga.getProgress() * HackrfSource.VGA_RX_GAIN_STEP_SIZE);
							preferences.setLnaGain(sb_hackrf_lna.getProgress() * HackrfSource.LNA_GAIN_STEP_SIZE);

						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// do nothing
						}
					}).create();
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					// sync source with (new/old) settings
					int vgaRxGain = preferences.getVgaRxGain();
					int lnaGain = preferences.getLnaGain();
					if (((HackrfSource) SourceControl.getSource()).getVgaRxGain() != vgaRxGain)
						((HackrfSource) SourceControl.getSource()).setVgaRxGain(vgaRxGain);
					if (((HackrfSource) SourceControl.getSource()).getLnaGain() != lnaGain)
						((HackrfSource) SourceControl.getSource()).setLnaGain(lnaGain);
				}
			});

			return view;

		case RTLSDR_SOURCE:
			final int[] possibleGainValues = ((RtlsdrSource) SourceControl.getSource()).getPossibleGainValues();
			final int[] possibleIFGainValues = ((RtlsdrSource) SourceControl.getSource()).getPossibleIFGainValues();
			if (possibleGainValues.length <= 1 && possibleIFGainValues.length <= 1) {
				MyToast.makeText(SourceControl.getSource().getName() + " does not support gain adjustment!",
						Toast.LENGTH_LONG);
			}
			// Prepare layout:
			view = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.rtlsdr_gain, null);
			final LinearLayout ll_rtlsdr_gain = (LinearLayout) view.findViewById(R.id.ll_rtlsdr_gain);
			final LinearLayout ll_rtlsdr_ifgain = (LinearLayout) view.findViewById(R.id.ll_rtlsdr_ifgain);
			final Switch sw_rtlsdr_manual_gain = (Switch) view.findViewById(R.id.sw_rtlsdr_manual_gain);
			final SeekBar sb_rtlsdr_gain = (SeekBar) view.findViewById(R.id.sb_rtlsdr_gain);
			final SeekBar sb_rtlsdr_ifGain = (SeekBar) view.findViewById(R.id.sb_rtlsdr_ifgain);
			final TextView tv_rtlsdr_gain = (TextView) view.findViewById(R.id.tv_rtlsdr_gain);
			final TextView tv_rtlsdr_ifGain = (TextView) view.findViewById(R.id.tv_rtlsdr_ifgain);

			// Assign current gain:
			int gainIndex = 0;
			int ifGainIndex = 0;
			for (int i = 0; i < possibleGainValues.length; i++) {
				if (((RtlsdrSource) SourceControl.getSource()).getGain() == possibleGainValues[i]) {
					gainIndex = i;
					break;
				}
			}
			for (int i = 0; i < possibleIFGainValues.length; i++) {
				if (((RtlsdrSource) SourceControl.getSource()).getIFGain() == possibleIFGainValues[i]) {
					ifGainIndex = i;
					break;
				}
			}
			sb_rtlsdr_gain.setMax(possibleGainValues.length - 1);
			sb_rtlsdr_ifGain.setMax(possibleIFGainValues.length - 1);
			sb_rtlsdr_gain.setProgress(gainIndex);
			sb_rtlsdr_ifGain.setProgress(ifGainIndex);
			tv_rtlsdr_gain.setText("" + possibleGainValues[gainIndex]);
			tv_rtlsdr_ifGain.setText("" + possibleIFGainValues[ifGainIndex]);

			// Assign current manual gain and agc setting
			sw_rtlsdr_manual_gain.setEnabled(!StateHandler.isScanning());
			sw_rtlsdr_manual_gain.setChecked(((RtlsdrSource) SourceControl.getSource()).isManualGain());

			// Add listener to gui elements:
			sw_rtlsdr_manual_gain.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					sb_rtlsdr_gain.setEnabled(isChecked);
					tv_rtlsdr_gain.setEnabled(isChecked);
					sb_rtlsdr_ifGain.setEnabled(isChecked);
					tv_rtlsdr_ifGain.setEnabled(isChecked);
					((RtlsdrSource) SourceControl.getSource()).setManualGain(isChecked);
					if (isChecked) {
						((RtlsdrSource) SourceControl.getSource())
								.setGain(possibleGainValues[sb_rtlsdr_gain.getProgress()]);
						((RtlsdrSource) SourceControl.getSource())
								.setIFGain(possibleIFGainValues[sb_rtlsdr_ifGain.getProgress()]);
					}
				}
			});
			sb_rtlsdr_gain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					tv_rtlsdr_gain.setText("" + possibleGainValues[progress]);
					((RtlsdrSource) SourceControl.getSource()).setGain(possibleGainValues[progress]);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});
			sb_rtlsdr_ifGain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					tv_rtlsdr_ifGain.setText("" + possibleIFGainValues[progress]);
					((RtlsdrSource) SourceControl.getSource()).setIFGain(possibleIFGainValues[progress]);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});

			// Disable gui elements if gain cannot be adjusted:
			if (possibleGainValues.length <= 1)
				ll_rtlsdr_gain.setVisibility(View.GONE);
			if (possibleIFGainValues.length <= 1)
				ll_rtlsdr_ifgain.setVisibility(View.GONE);

			if (!sw_rtlsdr_manual_gain.isChecked()) {
				sb_rtlsdr_gain.setEnabled(false);
				tv_rtlsdr_gain.setEnabled(false);
				sb_rtlsdr_ifGain.setEnabled(false);
				tv_rtlsdr_ifGain.setEnabled(false);
			}

			// dialog:
			builder.setTitle("Adjust Gain Settings").setView(view)
					.setPositiveButton("Set", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// safe preferences:
							preferences.setManualGain(sw_rtlsdr_manual_gain.isChecked());
							preferences.setGain(possibleGainValues[sb_rtlsdr_gain.getProgress()]);
							preferences.setIfGain(possibleGainValues[sb_rtlsdr_gain.getProgress()]);

						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							resetToPreferences();
						}
					}).create();
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					resetToPreferences();
				}

			});
			break;
		default:
			Log.e("AdjustGainDialog", "adjustGain: Invalid source type: " + sourceType);
			break;
		}
		return view;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
		return builder.create();

	}

	private void resetToPreferences() {
		((RtlsdrSource) SourceControl.getSource()).setManualGain(Preferences.MISC_PREFERENCE.isManualGain());
		((RtlsdrSource) SourceControl.getSource()).setGain(Preferences.MISC_PREFERENCE.getIFGain());
		((RtlsdrSource) SourceControl.getSource()).setIFGain(Preferences.MISC_PREFERENCE.getGain());
		if (Preferences.MISC_PREFERENCE.isManualGain()) {
			// Note: This is a workaround. After setting manual gain
			// to true we must
			// rewrite the manual gain values:
			((RtlsdrSource) SourceControl.getSource()).setGain(Preferences.MISC_PREFERENCE.getIFGain());
			((RtlsdrSource) SourceControl.getSource()).setIFGain(Preferences.MISC_PREFERENCE.getGain());
		}

	}
}
