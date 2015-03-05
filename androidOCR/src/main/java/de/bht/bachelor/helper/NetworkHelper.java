package de.bht.bachelor.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import de.bht.bachelor.message.ServiceMessenger;

public class NetworkHelper {

	/**
	 * Checks if the phone has network connection.
	 * 
	 * @param context
	 *            the context
	 * @return <code>true</code> if the phone is connected
	 */
	public static boolean isConnected(Context context) {

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm == null)
			return false;

		NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetwork != null) {
			if (wifiNetwork.isConnectedOrConnecting())
				return true;
		}

		NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobileNetwork != null) {
			if (mobileNetwork.isConnectedOrConnecting())
				return true;
		}

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null) {
			return activeNetwork.isConnectedOrConnecting();
		}

		return false;
	}

	/**
	 * Creating and starting Dialog witch has 3 Buttons.
	 * The user can open setting of WLAN or ROAMING or to cancel dialog.
	 * 
	 * The setting is running from Activity , called by handler.
	 * 
	 * @param context
	 *            context of current calling Activity
	 * @param message
	 *            message to show in the dialog
	 * @param handler
	 *            handler to call back Activity
	 */
	public static void onNoConnectionDialog(Context context, String message, final Handler handler) {
		final ProgressDialog pDialog = new ProgressDialog(context);
		pDialog.setMessage(message);
		pDialog.setCancelable(false);
		pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ServiceMessenger serviceMessenger = new ServiceMessenger(handler);
				serviceMessenger.sendMessage(CANCELL_DOWNLOAD, 7, null);
				pDialog.dismiss();
			}
		});
		pDialog.setButton(DialogInterface.BUTTON_POSITIVE, "WLAN-Einstellungen", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ServiceMessenger serviceMessenger = new ServiceMessenger(handler);
				serviceMessenger.sendMessage(RUN_WLAN_Setting, 7, null);
				pDialog.dismiss();
			}
		});

		pDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "ROAMING-Einstellungen", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ServiceMessenger serviceMessenger = new ServiceMessenger(handler);
				serviceMessenger.sendMessage(RUN_ROAMING_Setting, 7, null);
				pDialog.dismiss();
			}
		});

		pDialog.show();
	}

	/**
	 * 
	 * @return Intent with WLAN Setting
	 */
	public static Intent getWlanSettingIntent() {
		/*
		 * http://www.androidadb.com/source/quick-settings-read-only/quick-settings/src/com/bwx/bequick/handlers/WiFiSettingHandler.java.html
		 */
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setClassName("com.android.settings", "com.android.settings.wifi.WifiSettings");
		return intent;
	}

	/**
	 * 
	 * @return Intent with ROAMING Setting
	 */
	public static Intent getRoamingSettingIntent() {
		/*
		 * Bug workaround , see for more :
		 * http://stackoverflow.com/questions/4407818/error-opening-mobile-network-settings-menu/5537293#5537293
		 */
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setClassName("com.android.phone", "com.android.phone.Settings");
		return intent;
	}

	public static final int NO_INTERNET_CONECTION = -1;
	public static final int CANCELL_DOWNLOAD = 0;
	public static final int RUN_WLAN_Setting = 1;
	public static final int RUN_ROAMING_Setting = 2;
	/* if download was successfully */
	public static final int DOWNLOAD_SUCCESSFULLY = 3;
	public static final int DOWNLOAD_NOT_SUCCESSFULLY = 4;
	public static final int NOTHING_TO_DO = 5;
}
