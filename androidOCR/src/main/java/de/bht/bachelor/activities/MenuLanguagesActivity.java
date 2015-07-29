package de.bht.bachelor.activities;

import java.io.File;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import de.beutch.bachelorwork.util.file.Path;
import de.bht.bachelor.R;
import de.bht.bachelor.beans.OnLanguageChangeBean;
import de.bht.bachelor.beans.TtsEngine;
import de.bht.bachelor.exception.LanguageNotSupportedException;
import de.bht.bachelor.file.FileManager;
import de.bht.bachelor.helper.ChangeActivityHelper;
import de.bht.bachelor.helper.NetworkHelper;
import de.bht.bachelor.language.LanguageManager;
import de.bht.bachelor.manager.Preferences;
import de.bht.bachelor.setting.AppSetting;
import de.bht.bachelor.tasks.DownloadOcrTraineddata;
import de.bht.bachelor.tts.TTS;

public class MenuLanguagesActivity extends Activity implements OnItemSelectedListener {


    private static final String TAG = MenuLanguagesActivity.class.getSimpleName();
    private Resources res;
    private Vibrator vibrator;
    private static final int VIBRATE_DURATION = 35;
    private Spinner ttsSpinner;
    private ArrayAdapter<String> ttsAdapter;
    private TextView ttsLanguageTextView;
    private TextView ocrLanguageTextView;
    private Spinner ocrSpinner;
    private ArrayAdapter<CharSequence> ocrAdapter;
    private LanguageManager languageManager;
    private ProgressDialog mProgressDialog;
    private DownloadOcrTraineddata downloadOcrTessdata;
    private Handler resultHandler;                                                                                                                                                                                                                                                          
    private int backUpIso3LanguagePosition = -1;
    private OnLanguageChangeBean onLanguageChangeBean;
    private TextView infoTextView;
    private TextView deviceLanguageTextView;
    private int chooseTtsLanguageMode = -1;
    private int chooseOcrLanguageMode = -1;
	private static final int DIALOG_TTS_LANGUAGE_NOT_SUPPORTED = 1;
	private static  int CHECK_TTS_DATA_REQUEST_CODE = 10;

	private  TextToSpeech tts;

    private ColorStateList ttsTextViewBakgroundColor = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// --------------------- language setting --------------------------
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate().....");
		setContentView(R.layout.languages);
		languageManager = AppSetting.getInstance().getLanguageManager();
		// createHandler();
		readIntentExtra();
		initTtsSpinerAdapter();
		initTtsSpinner();

		ocrSpinner = (Spinner) findViewById(R.id.ocrSpinner);
		ocrAdapter = ArrayAdapter.createFromResource(this, R.array.ocrLanguagesArray, android.R.layout.simple_spinner_item);
		ocrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ocrSpinner.setAdapter(ocrAdapter);

		ttsSpinner.setOnItemSelectedListener(this);
		ocrSpinner.setOnItemSelectedListener(this);

		infoTextView = (TextView) findViewById(R.id.info);
		deviceLanguageTextView = (TextView) findViewById(R.id.deviceLanguageTextView);
		ocrLanguageTextView = (TextView) findViewById(R.id.ocrLanguageTextView);
		ttsLanguageTextView = (TextView) findViewById(R.id.ttsLanguageTextView);
		ttsTextViewBakgroundColor = ttsLanguageTextView.getTextColors();

//		try {

//		} catch (NullPointerException ex) {
//			AppSetting.getInstance().initLanguageMenager(this);
//			languageManager = AppSetting.getInstance().getLanguageManager();
//		}
		int ttsSpinnerPosition = Preferences.getInstance().getInt(Preferences.TTS_SPINER_POSITION);
		if(ttsSpinnerPosition ==-1){
			ttsSpinnerPosition = languageManager.getTtsSpinnerposition();
		}else {
			languageManager.setTtsSpinnerPosition(ttsSpinnerPosition);
		}
		ocrSpinner.setSelection(languageManager.getOcrSpinnerPosition());
		ttsSpinner.setSelection(ttsSpinnerPosition);

		updateLanguagetextView();
	}

	private void readIntentExtra() {
		Intent i = getIntent();
		chooseTtsLanguageMode = i.getIntExtra(LanguageManager.CHOOSE_TTS_LANGUAGE_NAME, -1);
		chooseOcrLanguageMode = i.getIntExtra(LanguageManager.CHOOSE_OCR_LANGUAGE_NAME, -1);
		Log.d(TAG, "the extra value from intent is : " + chooseTtsLanguageMode);
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume..................");
		super.onResume();
		if (this.onLanguageChangeBean != null) {
			int result = this.onLanguageChangeBean.getStatusAfterDownload();
			switch (result) {
			case NetworkHelper.CANCELL_DOWNLOAD:
				onDownloadCancell(null);
				break;
			case NetworkHelper.DOWNLOAD_NOT_SUCCESSFULLY:
				onDownloadCancell(null);
				onNoInternetConnection();
				break;
			case NetworkHelper.DOWNLOAD_SUCCESSFULLY:
				onDownloadSuccessfully();
				break;
			default:
				break;
			}
		}
		this.onLanguageChangeBean = null;
	}

	// ******************************************** GUI ************************************************************

	private void initTtsSpinerAdapter() {

//		if (chooseTtsLanguageMode == LanguageManager.CHOOSE_TTS_LANGUAGE_MODE)
//			ttsAdapter = ArrayAdapter.createFromResource(this, R.array.ttsLanguagesArrayChooseMode, android.R.layout.simple_spinner_item);
//		else
//			ttsAdapter = ArrayAdapter.createFromResource(this, R.array.ttsLanguagesArray, android.R.layout.simple_spinner_item);

		ttsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,languageManager.getTtsLanguagesListISO2ToDisplay());
	}

	private void initTtsSpinner() {
		ttsSpinner = (Spinner) findViewById(R.id.ttsSpinner);
		ttsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ttsSpinner.setAdapter(ttsAdapter);
	}

	// ***************************************** NETWORK ***********************************************************
	private void changeActivity() {
		Intent checkLanguage = new Intent(getApplicationContext(), CheckLanguageActivity.class);
		checkLanguage.putExtra(ChangeActivityHelper.RUN_LANGUAGE_SETUP, ChangeActivityHelper.RUN_LANGUAGE_SETUP);
		// starting new activity

		startActivity(checkLanguage);
	}

	// ******************************************** Language *******************************************************

	private void onNewTtsLanguage(final int position, final boolean callAfrerCheckTtsData){
		final Locale l = languageManager.getTtsLanguagesListISO2().get(position);//adapter.getItemAtPosition(position).toString();
		if (!l.getLanguage().equals(languageManager.getCurrentTtsLanguage().getLanguage())) {
			Log.d(TAG, "Old TTS language: " + languageManager.getCurrentTtsLanguage().getLanguage() + ". New one: " + l);
			if (chooseTtsLanguageMode == LanguageManager.CHOOSE_TTS_LANGUAGE_MODE) {
				ttsLanguageTextView.setTextColor(getResources().getColor((R.color.errorColor)));
			}
			this.tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
				@Override
				public void onInit(int i) {
					if (i == TextToSpeech.SUCCESS) {
						Locale locale =l;
						try {
							TTS.getInstance().init(tts, locale);
							languageManager.setTtsSpinnerPosition(position);
							languageManager.setCurrentTtsLanguage(locale);
							ttsLanguageTextView.setText(locale.getDisplayLanguage());
							Preferences.getInstance().putInt(Preferences.TTS_SPINER_POSITION, position);
						} catch (LanguageNotSupportedException e) {
							if(!callAfrerCheckTtsData) {
								Log.e(TAG, "Could not inti TTS with language: " + AppSetting.getInstance().getLanguageManager().getCurrentTtsLanguage());
								CHECK_TTS_DATA_REQUEST_CODE = position;
								confirmDialog();
							}
						}
					} else if (i == TextToSpeech.ERROR) {
						confirmDialog();
					}
				}
			});
		}
	}
	@Override
	public void onItemSelected(AdapterView<?> adapter, View view, final int position, long id) {
		Log.d(TAG, "onItemSelected().....");
		String iso3;
		Locale locale;

		if (adapter == ttsSpinner) {
			onNewTtsLanguage(position,false);
		} else if (adapter == ocrSpinner) {
			final String l = languageManager.getOcrLanguagesListISO2().get(position);//adapter.getItemAtPosition(position).toString();
			iso3 = languageManager.iso2LanguageCodeToIso3LanguageCode(l);
			if (!iso3.equals(languageManager.getCurrentOcrLanguage())) {
				locale = new Locale(l);
				if (checkForTraineddata(locale, iso3, position)) {
					setNewlanguage(locale, iso3, position);
				}
				Log.d(TAG, "ocr language " + l + ", " + locale.getDisplayLanguage());
			}
		}
		if (chooseTtsLanguageMode == LanguageManager.CHOOSE_TTS_LANGUAGE_MODE && languageManager.getTtsSpinnerposition() != 0) {
			// the user choose TTS language, go out from "language choose mode"
			onChooseTtsLanguageMode();
			// ttsLanguageTextView.setTextColor(new TextView(this).getCurrentTextColor());
			ttsLanguageTextView.setTextColor(ttsTextViewBakgroundColor);
			infoTextView.setText("");
		}
		updateLanguagetextView();
	}
	private void startInstallTTSDataIntent() {
		Log.d(TAG, "startInstallTTSDataIntent");
		Intent installIntent = new Intent();
		installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
		startActivityForResult(installIntent, CHECK_TTS_DATA_REQUEST_CODE);
	}
	private void confirmDialog(){
		AlertDialog.Builder d = new AlertDialog.Builder(this);
		d.setTitle("Install recommeded speech engine?");
		d.setMessage("Your device isn't using the recommended speech engine. Do you wish to install it?");

		d.setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				startInstallTTSDataIntent();
			}
		});
		d.setNegativeButton("No, later", new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int arg1) {


				dialog.dismiss();
			}
		});
		d.show();
	}

	private void startTTSinstallDataIntent(){
		Intent installIntent = new Intent();
		installIntent.setAction(
				TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
		startActivity(installIntent);
	}

	private Dialog createDialog(int id, String message) {
		switch (id) {
			case DIALOG_TTS_LANGUAGE_NOT_SUPPORTED:
			final	AlertDialog.Builder aleBuilder = new AlertDialog.Builder(
						this);
				aleBuilder.setTitle("TTS language error");

				aleBuilder.setMessage(message)
						.setCancelable(false)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, close
								// current activity
								dialog.dismiss();
							}
						});

				Dialog dialog = aleBuilder.show();
				Window window = dialog.getWindow();
				window.setLayout(500, 500);

				window.setGravity(Gravity.BOTTOM);

				return dialog;
		}
		return super.onCreateDialog(id);

	}


	private void onChooseTtsLanguageMode() {
		chooseTtsLanguageMode = -1;
		initTtsSpinerAdapter();
		initTtsSpinner();
	}

	/*
	 * 
	 */
	private boolean checkForTraineddata(Locale l, String iso3Language, int newPosition) {
		Log.d(TAG, "checkForTraineddata().. ");
		try {
			backUpIso3LanguagePosition = languageManager.getOcrSpinnerPosition();
			String traineddataNameUnCompress = languageManager.getTraineddataNameByLanguage(this, iso3Language, false);
			if (!new FileManager().check(new File(Path.LANGUAGE_TRAINEDDATA_DIR + traineddataNameUnCompress), false)) {
				if (NetworkHelper.isConnected(this)) {
					// languageManager.startDownloadTraineddata(this, resultHandler, iso3Language);
					this.onLanguageChangeBean = new OnLanguageChangeBean(backUpIso3LanguagePosition, languageManager.getCurrentOcrLanguage(), iso3Language, l, newPosition);
					ChangeActivityHelper.getInstance().setOnLanguageChangeBean(onLanguageChangeBean);
					changeActivity();
					/*
					 * if the download did't work, the handler will send massage back call (see case 6 with Exception)
					 */
				} else {
					onDownloadCancell("Das treineddata für " + l.getDisplayLanguage() + " könnte nicht unter geladen weren. Keine internetverbindung");
					onNoInternetConnection();
					return false;
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, "Could not check for Traineddata", ex);
		}
		return true;
	}

	/*
	 * Starting user dialog to set up Internet connection (WLAN or ROAMING)
	 */
	private void onNoInternetConnection() {
		NetworkHelper.onNoConnectionDialog(this, "Keine Internet Verbindung", resultHandler);
	}

	private void setNewlanguage(Locale locale, String iso3, int position) {
		languageManager.setCurrentOcrLanguage(iso3);
		ocrLanguageTextView.setText(locale.getDisplayLanguage());
		languageManager.setOcrSpinnerPosition(position);
	}

	/*
	 * Something went wrong , set back language and inform user
	 */
	private void onDownloadCancell(String message) {
		if (this.onLanguageChangeBean.getBackUpIso3LanguagePosition() > -1) {
			ocrSpinner.setSelection(this.onLanguageChangeBean.getBackUpIso3LanguagePosition());
			backUpIso3LanguagePosition = -1;
		}
		if (message != null)
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	private void onDownloadSuccessfully() {
		if (this.onLanguageChangeBean != null) {
			setNewlanguage(this.onLanguageChangeBean.getNewLocale(), this.onLanguageChangeBean.getNewIso3Language(), this.onLanguageChangeBean.getNewPosition());
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DownloadOcrTraineddata.DIALOG_DOWNLOAD_PROGRESS:
			if (downloadOcrTessdata != null) {
				mProgressDialog = new ProgressDialog(this);
				mProgressDialog.setMessage("Downloading file..");
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgressDialog.setCancelable(false);
				// set dialog to the tack
				downloadOcrTessdata.setmProgressDialog(mProgressDialog, getString(R.string.downloadDialogText));
				mProgressDialog.show();
				return mProgressDialog;
			}
		default:
			return null;
		}
	}

	private void updateLanguagetextView() {
		Log.d(TAG, "updateLanguagetextView().");
		if (languageManager == null) {
//			try {
				languageManager = AppSetting.getInstance().getLanguageManager();
//			} catch (NullPointerException ex) {
//				languageManager = AppSetting.getInstance().initLanguageMenager(this);
//			}
		}

		this.deviceLanguageTextView.setText(getString(R.string.deviceLanguageLabel) + languageManager.getCurrentDeviceDisplayLanguage());
		this.ocrLanguageTextView.setText(getString(R.string.ocrLanguageLabel) + languageManager.getCurrentOcrDisplayLanguage());
		if (chooseTtsLanguageMode == LanguageManager.CHOOSE_TTS_LANGUAGE_MODE && languageManager.getTtsSpinnerposition() == 0) {
			// there is no language , nut information to choose the language, can not use it
			this.ttsLanguageTextView.setText(getString(R.string.ttsLanguageLabel));
		} else {
			this.ttsLanguageTextView.setText(getString(R.string.ttsLanguageLabel) + languageManager.getCurrentTtsDisplayLanguage());
		}

	}

	// ******************************************** END of Language *********************************************+

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy()................");
		super.onDestroy();

	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause()................");
		TTS.getInstance().shutdown();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		StringBuilder stringBuilder = null;
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Log.d(TAG, "Back button has been Pressed");

			if (LanguageManager.CHOOSE_OCR_LANGUAGE_MODE == chooseOcrLanguageMode) {
				stringBuilder = new StringBuilder().append(getResources().getString(R.string.languageOCR_info_no_language));
			}

			if (LanguageManager.CHOOSE_TTS_LANGUAGE_MODE == chooseTtsLanguageMode) {

				if (stringBuilder != null)
					stringBuilder.append("\n" + getResources().getString(R.string.languageTTS_info_no_language));
				else
					stringBuilder = new StringBuilder().append(getResources().getString(R.string.languageTTS_info_no_language));
			}
			if (stringBuilder != null) {
				infoTextView.setTextColor(getResources().getColor(R.color.errorColor));
				infoTextView.setText(stringBuilder);
				return false;
			}
			return super.onKeyDown(keyCode, event);
		}

		return super.onKeyDown(keyCode, event);
	}

	public void setDownloadOcrTessdata(DownloadOcrTraineddata downloadOcrTessdata) {
		this.downloadOcrTessdata = downloadOcrTessdata;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == CHECK_TTS_DATA_REQUEST_CODE){
			onNewTtsLanguage(CHECK_TTS_DATA_REQUEST_CODE,true);
			Log.d(TAG, "MY_DATA_INSTALL_CODE : result : " + resultCode);

		}
	}
}
