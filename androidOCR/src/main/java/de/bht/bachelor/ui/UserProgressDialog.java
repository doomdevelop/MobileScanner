package de.bht.bachelor.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

public class UserProgressDialog extends ProgressDialog {
	public UserProgressDialog(Context context) {
		super(context);
		this.context = context;
	}

	public UserProgressDialog(Context context, String message) {
		super(context);
		this.context = context;
		this.message = message;
		showDialog();
	}

	public void showDialog() {
		setMessage(message);
		setCancelable(false);
		setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				dismiss();
			}
		});

		show();
	}

	private final Context context;
	private String message;
}
