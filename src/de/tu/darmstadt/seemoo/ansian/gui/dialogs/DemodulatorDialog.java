package de.tu.darmstadt.seemoo.ansian.gui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;
import de.tu.darmstadt.seemoo.ansian.R;
import de.tu.darmstadt.seemoo.ansian.control.SourceControl;
import de.tu.darmstadt.seemoo.ansian.control.StateHandler;
import de.tu.darmstadt.seemoo.ansian.gui.misc.MyToast;
import de.tu.darmstadt.seemoo.ansian.model.demodulation.Demodulation.DemoType;
import de.tu.darmstadt.seemoo.ansian.model.preferences.Preferences;

/**
 * Dialog to choose the desired Demodulation (or turning it off)
 *
 */

public class DemodulatorDialog extends MyDialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
		return builder.setTitle("Select a demodulation mode:").setSingleChoiceItems(R.array.demodulation_modes,
				Preferences.MISC_PREFERENCE.getDemodulation().ordinal(), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						StateHandler.setDemodulationMode(DemoType.values()[which]);
						dialog.dismiss();
					}
				}).create();
	}

	@Override
	public void show() {
		if (SourceControl.getSource() == null) {
			MyToast.makeText("Analyzer must be running to change modulation mode", Toast.LENGTH_LONG);
			return;

		} else
			super.show();
	}

}