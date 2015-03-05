package de.bht.bachelor.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import de.bht.bachelor.R;
import de.bht.bachelor.beans.Dimension;
import de.bht.bachelor.camera.OrientationMode;
import de.bht.bachelor.graphic.transform.ImageProcessing;
import de.bht.bachelor.helper.ActivityType;
import de.bht.bachelor.helper.ChangeActivityHelper;
import de.bht.bachelor.language.LanguageManager;
import de.bht.bachelor.ocr.OCR;
import de.bht.bachelor.services.PreprocessingService;
import de.bht.bachelor.setting.AppSetting;
import de.bht.bachelor.tts.TTS;
import de.bht.bachelor.ui.Corner;
import de.bht.bachelor.ui.CustomImageView;
import de.bht.bachelor.ui.UserProgressDialog;

/**
 * This is the User Interface witch is showing the image from camera.
 * With his Components is giving possibility to interacting much with the user.
 * This is also a client for the service witch is calling OCR. See {@link PreprocessingService}
 * 
 * 
 * @author Andrzej Kozlowski
 * 
 */
public class PhotoActivity extends MenuCreatorActivity implements OnClickListener {

    // private IBinder
    private LanguageManager languageManager;

    private Handler resultHandler;
    private CustomImageView imageView;
    private static final String TAG = PhotoActivity.class.getSimpleName();
    private FrameLayout viewGroup;
    private Button buttonCrop;
    private Button buttonFrameToWholeImage;
    private Button buttonHistory;
    private Button buttonRunOcr;
    private CheckBox checkBoxImageFrame;
    private long startTime;
    private long stopTime;
    private ProgressDialog pDialog;
    private final int CHECK_TTS = 0;
    /* ocr service */
    private Intent ocrIntent;
    private final boolean checkSetupAgain = false;
    private OCR ocr;
    private SendBitmapToOCRTasc sendBitmapToOCRTasc;
    private boolean restoreImageView = false;
    private static final String IMAGE_VIEW_KEY = "IMAGE_VIEW_KEY";
    private Dimension[] dimensions;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()....");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo);

		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			// imageView = (CustomImageView) savedInstanceState.getParcelable(IMAGE_VIEW_KEY);
			// imageView.onRestoreInstanceState(savedInstanceState);
			restoreInstanceState(savedInstanceState);

		}
		if (imageView == null)
			imageView = (CustomImageView) findViewById(R.id.picview);
		imageView.setRestoreImageView(restoreImageView);

		viewGroup = (FrameLayout) findViewById(R.id.frameLayout);
		buttonFrameToWholeImage = (Button) findViewById(R.id.frameToWholeImage);
		buttonFrameToWholeImage.setEnabled(false);
		buttonFrameToWholeImage.setOnClickListener(this);
		imageView.setViewGroup(viewGroup);
		imageView.setWindowManager(getWindowManager());

		buttonCrop = (Button) findViewById(R.id.crop);
		buttonCrop.setOnClickListener(this);

		buttonHistory = (Button) findViewById(R.id.backHistory);
		buttonHistory.setEnabled(false);
		buttonHistory.setOnClickListener(this);
		checkBoxImageFrame = (CheckBox) findViewById(R.id.addFrame);
		setMarginLeftToCheckBox();
		checkBoxImageFrame.setChecked(true);
		checkBoxImageFrame.setOnClickListener(this);
		buttonRunOcr = (Button) findViewById(R.id.runOrgOcr);
		buttonRunOcr.setOnClickListener(this);
		setVisibleToAllGuiElements(false);
		createHandler();
		imageView.setHandler(resultHandler);
		setPixToPictureview();
		imageView.createImageFrame(dimensions);
		toolTipsToSpeech();
		if (languageManager == null)
			languageManager = AppSetting.getInstance().getLanguageManager();

	}

	private void createHandler() {

		resultHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				boolean v;
				if (msg == null) {
					throw new NullPointerException("The message has value null !");
				}
				switch (msg.what) {
				case 0:
					changeActivity(msg.obj, ActivityType.OCR_VIEW);// calling by OCR
					break;
				case 1:
					buttonHistory.setEnabled(msg.arg1 == 0 ? false : true);
					break;
				case 2:
					v = msg.arg1 == 0 ? false : true;
					Log.d(TAG, "get new velue to set in check button : " + v);
					addFrames(v);// check button for frames
					break;
				case 3:
					buttonFrameToWholeImage.setEnabled(msg.arg1 == 0 ? false : true);
					break;
				case 4:
					v = msg.arg1 == 0 ? false : true;// picture get touched, change visible of buttons
					setVisibleToAllGuiElements(v);
					break;

				}
			}
		};
	}

	// **************************************** init TTS ***********************************************

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult()");
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CHECK_TTS) {

			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				Log.d(TAG, "Success, create the TTS instanc");
				TTS.getInstance().init(new TextToSpeech(getBaseContext(), this), AppSetting.getInstance().getLanguageManager().getCurrentTtsLanguage());
				TTS.getInstance().setApiChecked(true);
				// tts.setLanguage(Locale.GERMANY);
			} else {
				Log.d(TAG, "missing data, install it");
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}

	// *************************************** End of Init TTS *****************************************
	private void setMarginLeftToCheckBox() {
		int margin = getWindowManager().getDefaultDisplay().getWidth() / 2 - 50;
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) checkBoxImageFrame.getLayoutParams();
		params.setMargins(margin, 0, 0, 0);
		checkBoxImageFrame.setLayoutParams(params);
	}

	private void addFrames(boolean v) {
		checkBoxImageFrame.setChecked(v);
		checkBoxImageFrame.invalidate();
	}

	/**
	 * This Method will set the image to the ImageView
	 */
	public void setPixToPictureview() {

		byte[] data = ChangeActivityHelper.getInstance().getRawPicture();
		try {
			final BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			Bitmap currentOrgBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
			if (currentOrgBitmap != null) {
				Log.d(TAG, "seing bitmap,w: " + currentOrgBitmap.getWidth() + ",h: " + currentOrgBitmap.getHeight());
				imageView.initImageBitmap(currentOrgBitmap);
				buttonFrameToWholeImage.setEnabled(true);
			}
		} catch (NullPointerException ex) {
			Log.d(TAG, "Could not view bitmap!", ex);
		}
	}

	/**
	 * this method is calling ocr or changing activity after ocr
	 * 
	 * @param obj
	 *            Could be Pix from preprocessing service or String as Result from OCR
	 * @param activityTyp
	 */
	public void changeActivity(Object obj, String activityTyp) {
		// Starting a new Intent

		if (obj instanceof String) {
			// getting text as result from OCR process
			stopTime = System.currentTimeMillis();
			float time = (stopTime - startTime) / 1000;
			if (time / 60 <= 60)
				Log.d(TAG, "file preparing and OCR run time : " + time + " sek");
			else
				Log.d(TAG, "file preparing and OCR run time : " + time / 60 + " min");
			buttonRunOcr.setEnabled(true);
			Intent resultScreen = new Intent(getApplicationContext(), OcrResultActivity.class);
			resultScreen.putExtra("result", (String) obj);
			// starting new activity

			pDialog.dismiss();
			startActivity(resultScreen);
		}
		// }
	}

	@Override
	public void onClick(View v) {
		if (v == buttonCrop) {
			try {
				imageView.cropToFrame();
			} catch (Exception e) {
				Log.d(TAG, "exception by croping image", e);
			}
		} else if (v == checkBoxImageFrame) {
			if (checkBoxImageFrame.isChecked()) {
				if (imageView.hasNoFrame()) {
					imageView.addFrame();
					buttonFrameToWholeImage.setEnabled(true);
					buttonCrop.setEnabled(true);
					// imageView.onPreDraw();
				}
			} else {
				if (!imageView.hasNoFrame()) {
					imageView.removeFrame();
					buttonFrameToWholeImage.setEnabled(false);
					buttonCrop.setEnabled(false);
				}
			}
		} else if (v == buttonHistory) {
			imageView.setImageFromHistory();
		} else if (v == buttonRunOcr) {
			if (this.languageManager.checkTraineddataForCurrentLanguage(this)) {
				try {
					onDifferentDeviceLanguage();
					TTS.getInstance().speak(getString(R.string.ocrOnWork), false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "Could not start tts", e);
				}
				pDialog = new ProgressDialog(PhotoActivity.this);
				pDialog.setMessage(getString(R.string.ocrOnWork));
				pDialog.setCancelable(false);
				pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						if (ocr != null)
							ocr.setCancelResult(true);

						if (sendBitmapToOCRTasc != null && !sendBitmapToOCRTasc.isCancelled()) {
							sendBitmapToOCRTasc.cancel(false);
						}
						pDialog.dismiss();
						TTS.getInstance().stop();
					}
				});

				pDialog.show();

				setVisibleToAllGuiElements(false);

				callOcr(imageView.getOrgBitmapForCurrentRect(), imageView.getCurrentOrgCropBitmap(), imageView.getCurrentRect());
			} else {
				languageManager.checkSetup(this, resultHandler);
			}
		} else if (v == buttonFrameToWholeImage) {
			imageView.setFrameToWholeImage();
			setVisibleToAllGuiElements(false);
		}
	}

	/*
	 * The Long click Listener for the User Interface Tool Tips
	 */
	private void toolTipsToSpeech() {

		this.buttonCrop.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				try {
					getVibrator().vibrate(VIBRATE_DURATION);
					onDifferentDeviceLanguage();
					TTS.getInstance().speak(getString(R.string.crop), false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "Could not start tts", e);
				}
				return true;
			}
		});
		this.buttonFrameToWholeImage.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				try {
					getVibrator().vibrate(VIBRATE_DURATION);
					onDifferentDeviceLanguage();
					TTS.getInstance().speak(getString(R.string.frameToImage), false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "Could not start tts", e);
				}
				return true;
			}
		});

		this.buttonHistory.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				try {
					getVibrator().vibrate(VIBRATE_DURATION);
					onDifferentDeviceLanguage();
					TTS.getInstance().speak(getString(R.string.back), false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "Could not start tts", e);
				}
				return true;
			}
		});

		this.buttonRunOcr.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				try {// there was an error , go back
						// serviceMessenger = new ServiceMessenger(resultHandler);
						// serviceMessenger.sendMessage(0, 0, new String(""));
					getVibrator().vibrate(VIBRATE_DURATION);
					onDifferentDeviceLanguage();
					TTS.getInstance().speak(getString(R.string.runOCR), false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "Could not start tts", e);
				}
				return true;
			}
		});

		this.checkBoxImageFrame.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				try {
					getVibrator().vibrate(VIBRATE_DURATION);
					onDifferentDeviceLanguage();
					if (checkBoxImageFrame.isChecked()) {
						TTS.getInstance().speak(getString(R.string.removeFrame), false);
					} else {
						TTS.getInstance().speak(getString(R.string.addFrame), false);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "Could not start tts");
				}
				return true;
			}
		});
	}

	private void setVisibleToAllGuiElements(boolean value) {
		if (value) {
			buttonCrop.setVisibility(Button.VISIBLE);
			buttonCrop.setVisibility(Button.VISIBLE);
			buttonFrameToWholeImage.setVisibility(Button.VISIBLE);
			buttonHistory.setVisibility(Button.VISIBLE);
			buttonRunOcr.setVisibility(Button.VISIBLE);
			checkBoxImageFrame.setVisibility(Button.VISIBLE);
		} else {
			buttonCrop.setVisibility(Button.INVISIBLE);
			buttonCrop.setVisibility(Button.INVISIBLE);
			buttonFrameToWholeImage.setVisibility(Button.INVISIBLE);
			buttonHistory.setVisibility(Button.INVISIBLE);
			buttonRunOcr.setVisibility(Button.INVISIBLE);
			checkBoxImageFrame.setVisibility(Button.INVISIBLE);
		}
	}

	private class SendBitmapToOCRTasc extends AsyncTask<Bitmap, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.d(TAG, "onPreExecute()...");
		}

		@Override
		protected String doInBackground(Bitmap... params) {

			if (params[0] == null)
				throw new NullPointerException("bitmap can not be a null");

			Bitmap bm = params[0];
			OrientationMode orientationMode;
			String result = null;
			try {
				// ****************** test************
				// runTestsOCR(orgBitmap, bm, rect);
				// **********************************
				// if (ocr == null)
				ocr = new OCR(resultHandler);
				orientationMode = ChangeActivityHelper.getInstance().getOrientationMode();
				if (orientationMode == OrientationMode.PORTRAIT || orientationMode == OrientationMode.PORTRAIT_UPSIDE_DOWN) {
					result = ocr.runWithCropped8888(new ImageProcessing().rotate(bm, 90));
				} else {
					result = ocr.runWithCropped8888(bm);
				}
			} catch (NullPointerException ex) {
				Log.d(TAG, "Could not run OCR!", ex);

			} catch (RuntimeException e) {
				Log.d(TAG, "Could not convert : pix = ReadFile.readBytes8(data, width, height)", e);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null)
				changeActivity(result, ActivityType.OCR_VIEW);// calling by OCR
			else {
				pDialog = new UserProgressDialog(PhotoActivity.this, "Unbekannte Fehler , bitte Versuchen Sie nochmal");
				Log.d(TAG, "Could not start new Intent, result OCR null");
			}
		}
	}

	public void callOcr(Bitmap orgBitmap, Bitmap bm, Rect rect) {

		if (bm == null) {
			Log.d(TAG, "No cropped bitmap, run with original image ");
			bm = orgBitmap;
		}
		if (rect != null)
			Log.d(TAG, "Rect dimesions width = " + ((rect.right) - (rect.left)) + "hight = " + (rect.bottom - rect.top));
		Log.d(TAG, "org bitmap width = " + orgBitmap.getWidth() + ", height = " + orgBitmap.getHeight());
		Log.d(TAG, "org bitmap conf :" + orgBitmap.getConfig().name());
		Log.d(TAG, "croped bitmap width = " + bm.getWidth() + ", height = " + bm.getHeight());
		Log.d(TAG, "croped bitmap conf :" + bm.getConfig().name());
		sendBitmapToOCRTasc = new SendBitmapToOCRTasc();
		sendBitmapToOCRTasc.execute(bm);
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume..................");
        super.onResume();
	}

	@Override
	public void onDestroy() {

		Log.d(TAG, "onDestroy()...");
		if (isFinishing()) {
			if (this.imageView != null) {
				// the app is not
				imageView.onDestroy();

			}
			ChangeActivityHelper.getInstance().setRawPicture(null);
		}
		if (isFinishing() && !isBackPressed) {
			isBackPressed = false;
		}

		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Log.d(TAG, "Back button has bee Pressed");
			isBackPressed = true;
			return super.onKeyDown(keyCode, event);
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// ignore orientation/keyboard change
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "onSaveInstanceState().......");
		try {
			Corner[] corners = imageView.getCorners();
			for (Corner corner : corners) {
				savedInstanceState.putSerializable(CustomImageView.CORNER_DIMENSION_ + corner.getCornerID(), corner.getDimension());
			}
			// savedInstanceState.putParcelable(IMAGE_VIEW_KEY, imageView.onSaveInstanceState());
		} catch (Exception ex) {
			Log.e(TAG, "could not put in to bundle", ex);
		}
	}

	private void restoreInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "onRestoreInstanceState().......");
		// imageView.onRestoreInstanceState(savedInstanceState);
		dimensions = new Dimension[4];
		int i = 0;
		while (i < 4) {
			dimensions[i] = (Dimension) savedInstanceState.getSerializable(CustomImageView.CORNER_DIMENSION_ + i);
			i++;
		}
		restoreImageView = true;
	}




}
