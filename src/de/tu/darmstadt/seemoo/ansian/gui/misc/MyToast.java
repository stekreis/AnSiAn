package de.tu.darmstadt.seemoo.ansian.gui.misc;

import android.widget.Toast;
import de.tu.darmstadt.seemoo.ansian.MainActivity;

/**
 * This is the central toast class, which is overwriting older toasts
 * automatically and has an easier interface.
 * 
 * @author Markus
 *
 */
public class MyToast {

	private static Toast toast;

	public static void makeText(final String text, final int duration) {
		if (toast != null)
			toast.cancel();

		MainActivity.instance.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				toast = Toast.makeText(MainActivity.instance, text, duration);
				toast.show();

			}
		});

	}

}
