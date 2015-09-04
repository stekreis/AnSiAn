package de.tu.darmstadt.seemoo.ansian.gui.fragments.settings;

import java.io.File;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.widget.Toast;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;
import de.tu.darmstadt.seemoo.ansian.tools.FileUtils;

/**
 * Fragment for miscellaneous preferences as source, frequency and FFT settings
 *
 */
public class MiscFragment extends MyPreferenceFragment {

	public MiscFragment() {
		super(Preferences.MISC_PREFERENCE);
	}

	private static final int FILESOURCE_RESULT_CODE = 1;
	@SuppressWarnings("unused")
	private static final String LOGTAG = "MiscFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Add click listener to preferences which use external apps:
		Preference pref = findPreference("filesource_file_name");
		pref.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		super.onPreferenceClick(preference);
		// FileSource file:
		if (preference.getKey().equals("filesource_file_name")) {
			try {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				startActivityForResult(Intent.createChooser(intent, "Select a file (8-bit complex IQ samples)"),
						FILESOURCE_RESULT_CODE);

				// No error so far... let's dismiss the text input dialog:
				Dialog dialog = ((EditTextPreference) preference).getDialog();
				if (dialog != null)
					dialog.dismiss();
				return true;
			} catch (ActivityNotFoundException e) {
				Toast.makeText(MiscFragment.this.getActivity(), "No file browser is installed!", Toast.LENGTH_LONG)
						.show();
				// Note that there is still the text dialog visible for the user
				// to input a file path... so no more error handling necessary
			}
			return false;
		}
		// Show Log:
		else if (preference.getKey().equals("show_log")) {
			try {
				String logfile = ((EditTextPreference) findPreference("logfile")).getText();
				Uri uri = Uri.fromFile(new File(logfile));
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(uri, "text/plain");
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(intent);
				return true;
			} catch (ActivityNotFoundException e) {
				Toast.makeText(MiscFragment.this.getActivity(), "No text viewer is installed!", Toast.LENGTH_LONG)
						.show();
			}
			return false;
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			switch (requestCode) {
			case FILESOURCE_RESULT_CODE:
				Uri uri = data.getData();
				if (uri != null) {
					String filepath = FileUtils.getPath(getActivity(), uri);
					if (filepath != null) {
						((EditTextPreference) findPreference("filesource_file_name")).setText(filepath);
						updateFileSourcePrefs(filepath);
					} else {
						Toast.makeText(MiscFragment.this.getActivity(),
								"Can't resolve file path from: " + uri.toString(), Toast.LENGTH_LONG).show();
					}
				}
				break;
			default:
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void updateFileSourcePrefs(String filename) {
		// Format. Search for strings like hackrf, rtl-sdr, ...
		if (filename.matches(".*hackrf.*") || filename.matches(".*HackRF.*") || filename.matches(".*HACKRF.*")
				|| filename.matches(".*hackrfone.*"))
			Preferences.MISC_PREFERENCE.setSourceFileFormat(0);

		if (filename.matches(".*rtlsdr.*") || filename.matches(".*rtl-sdr.*") || filename.matches(".*RTLSDR.*")
				|| filename.matches(".*RTL-SDR.*"))
			Preferences.MISC_PREFERENCE.setSourceFileFormat(1);

		// Sampe Rate. Search for pattern XXXXXXXSps
		if (filename.matches(".*(_|-|\\s)([0-9]+)(sps|Sps|SPS).*"))
			Preferences.MISC_PREFERENCE.setFileSourceSampleRate(
					Integer.valueOf(filename.replaceFirst(".*(_|-|\\s)([0-9]+)(sps|Sps|SPS).*", "$2")));
		if (filename.matches(".*(_|-|\\s)([0-9]+)(msps|Msps|MSps|MSPS).*"))
			Preferences.MISC_PREFERENCE.setSampleRate(
					Integer.valueOf(filename.replaceFirst(".*(_|-|\\s)([0-9]+)(msps|Msps|MSps|MSPS).*", "$2"))
							* 1000000);

		// Frequency. Search for pattern XXXXXXXHz
		if (filename.matches(".*(_|-|\\s)([0-9]+)(hz|Hz|HZ).*"))
			Preferences.MISC_PREFERENCE.setFileSourceFrequency(
					Integer.valueOf(filename.replaceFirst(".*(_|-|\\s)([0-9]+)(hz|Hz|HZ).*", "$2")));

		if (filename.matches(".*(_|-|\\s)([0-9]+)(mhz|Mhz|MHz|MHZ).*"))
			Preferences.MISC_PREFERENCE.setFileSourceFrequency(
					Integer.valueOf(filename.replaceFirst(".*(_|-|\\s)([0-9]+)(mhz|Mhz|MHz|MHZ).*", "$2")) * 1000000);

		Preferences.MISC_PREFERENCE.savePreference();

	}
	

}
