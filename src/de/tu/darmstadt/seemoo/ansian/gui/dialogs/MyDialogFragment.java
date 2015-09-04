package de.tu.darmstadt.seemoo.ansian.gui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.view.View;
import de.tu.darmstadt.seemoo.ansian.MainActivity;

public abstract class MyDialogFragment extends DialogFragment {
	protected AlertDialog.Builder builder;
	protected View view;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		builder = new Builder(getActivity());
		view = createView();
		if (view != null)
			builder.setView(view);
		return super.onCreateDialog(savedInstanceState);
	}

	protected View createView() {
		return null;
	}

	public void show() {
		show(MainActivity.instance.getSupportFragmentManager(), this.getClass().toString());
	};
}
