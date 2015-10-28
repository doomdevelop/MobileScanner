package de.bht.bachelor.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.ClipData;
import android.text.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import de.bht.bachelor.R;
import de.bht.bachelor.beans.OcrResult;
import de.bht.bachelor.graphic.transform.ImageProcessing;
import de.bht.bachelor.manager.Preferences;
import de.bht.bachelor.setting.AppSetting;
import de.bht.bachelor.tts.TTS;
import de.bht.bachelor.ui.fragments.OcrResultImagePageFragment;

public class OcrResultActivity extends MenuCreatorActivity implements OnClickListener {


    private Handler resultHandler;
    private TextView textView;
    private static final String TAG = OcrResultActivity.class.getSimpleName();
    private Button buttonStop;
    private Button buttonSpeak;
    private OcrResult ocrResult;
    private String ocrResultText;
    private final int SAVE_FILE_RESULT_CODE = 10;
    private TextToSpeech tts;
    private boolean firstCall = true;
    private final int CHECK_TTS = 0;
    private ViewPager viewPager;
    private List<Fragment> slides;
    private CustomScreenSlideAdapter pagerAdapter;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate..................");
		super.onCreate(savedInstanceState);
        super.setOnTtsInitCallback(this.ttsInitCallback);
		setContentView(R.layout.result);
		textView = (TextView) findViewById(R.id.textView);
		textView.setBackgroundColor(getResources().getColor(R.color.white));
		textView.setTextColor(Color.BLACK);

		buttonStop = (Button) findViewById(R.id.stop);
		buttonStop.setOnClickListener(this);
		buttonStop.setEnabled(false);
		buttonStop.setFocusable(true);
		buttonStop.setLongClickable(true);
		// ******************** long click ***********************************
		buttonStop.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				try {
					getVibrator().vibrate(VIBRATE_DURATION);
					onDifferentDeviceLanguage();
					TTS.getInstance().speak(getString(R.string.stopTTS), false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "Could not start tts");
				}
				return true;
			}
		});
		buttonSpeak = (Button) findViewById(R.id.speak);
		buttonSpeak.setOnClickListener(this);
		buttonSpeak.setEnabled(false);
		// ******************** long click ***********************************
		buttonSpeak.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				try {

					getVibrator().vibrate(VIBRATE_DURATION);
//					onDifferentDeviceLanguage();
					TTS.getInstance().speak(getString(R.string.speak), false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "Could not start tts");
				}
				return true;
			}
		});
		if (savedInstanceState != null)
			restoreInstanceState(savedInstanceState);

        slides = new ArrayList<Fragment>();
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        pagerAdapter = new CustomScreenSlideAdapter (getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        setTextOcrResult();
        if(ocrResult != null) {
            if (ocrResult.isHasImageData()) {
                Bitmap bmp = null;
                byte[] data = Preferences.getInstance().getArrayData(Preferences.IMAGE_DATA);
                if(ocrResult.isLandscape()){
                    int[] rgb = new int[data.length];
                    ImageProcessing.decodeYUV420RGB(rgb, data, ocrResult.getW(), ocrResult.getH());
                     bmp = Bitmap.createBitmap(rgb, ocrResult.getW(), ocrResult.getH(), Bitmap.Config.ARGB_8888);
                }else {
//                    bmp = OcrResult.convertDataToBitmap(data);

                    int[] rgb = new int[data.length];
                    ImageProcessing.decodeYUV420RGB(rgb, data, ocrResult.getW(), ocrResult.getH());
                    bmp = Bitmap.createBitmap(rgb, ocrResult.getW(), ocrResult.getH(), Bitmap.Config.ARGB_8888);
                    int rotateValue = ocrResult.getRotateValue();
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotateValue, ocrResult.getW() / 2, ocrResult.getH() / 2);
                    bmp = Bitmap.createBitmap(bmp, 0, 0, ocrResult.getW(), ocrResult.getH(), matrix, false);
                }
                slides.add(OcrResultImagePageFragment.newInstance(bmp));
                pagerAdapter.notifyDataSetChanged();
            }                                                                                                                                           
        }
		Button copy = (Button) findViewById(R.id.copy_to_clipboard);
		copy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(textView.getText());
				Toast.makeText(OcrResultActivity.this,getString(R.string.copy_text_to_clipboard_msg),Toast.LENGTH_LONG).show();
			}
		});
		createHandler();

	}
    public void showFullScreenDialog(Bitmap img, int resId) {
        final Dialog dialog = new Dialog(this, R.style.DialogDimTheme);
        dialog.setContentView(R.layout.custom_full_screen_dialog);
        final WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.8f; // Dim level. 0.0 - no dim, 1.0 - completely opaque
        dialog.getWindow().setAttributes(lp);
        ImageView imageView = (ImageView) dialog.findViewById(R.id.custom_assessment_dialog_img);
        if (img != null) {
            imageView.setImageBitmap(img);
        } else {
            imageView.setImageResource(resId);
        }
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface di) {
                Log.d(TAG,"DIALOG DISMISSED");
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//                lp.dimAmount = 0.0f; // Dim level. 0.0 - no dim, 1.0 - completely opaque
//                dialog.getWindow().setAttributes(lp);
            }
        });
        dialog.show();
    }
    private OnTtsInitCallback ttsInitCallback = new OnTtsInitCallback() {
        @Override
        public void onSuccessfully() {
            if (firstCall) {
                initLocalTTS();
            }
        }
    };

	private void createHandler() {
		Log.d(TAG, "createHandler()....");
		resultHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg == null) {
					throw new NullPointerException("The message has value null !");
				}
				switch (msg.what) {
				case 0:
					onSpeak();
					break;
				}
			}
		};
		TTS.getInstance().setHandler(resultHandler);
	}

	private void onEndOfSpeech() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				buttonSpeak.setBackgroundResource(R.drawable.play);
				buttonStop.setEnabled(false);
			}
		});
	}

	private void initLocalTTS() {

		if (tts == null) {
			Log.d(TAG, "init Local TTS reference....");
			tts = TTS.getInstance().getTts();
		}
        if(TTS.getInstance().isOnChacking()){
                    setCallbackOnCheckedTts(new OnCheckedTTS() {
                        @Override
                        public void onCheckedTTS(boolean successfully) {
                            if(successfully) {
                                onInit();
                            }
                        }
                    });
        }else{
            onInit();
        }

	}

    public interface OnCheckedTTS{
      void  onCheckedTTS(boolean successfully);
    }

	private void onInit() {
		Log.d(TAG, "onInit()....");
		tts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
			/*
			 * Will be called when tts finish speak.
			 * (non-Javadoc)
			 * 
			 * @see android.speech.tts.TextToSpeech.OnUtteranceCompletedListener#onUtteranceCompleted(java.lang.String)
			 */
			@Override
			public void onUtteranceCompleted(String utteranceId) {
				Log.d(TAG, "onUtteranceCompleted()....." + utteranceId);

				if (utteranceId.equals(TTS.getInstance().endOfOcrResultSpeech)) {
					// call it just when tts spoke the ocr-result-text
					// if was this case, change icon from restore-play to play
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// UI changes

							Log.d(TAG, "stop speech result text, prepare massege");
							onEndOfSpeech();
						}
					});
				}
			}
		});
		// will call if the tts has been initialised and is a first call
		if (firstCall) {
			firstCall = false;
			// TTS.getInstance().setInitialized(true);

			try {
				onSpeak();
				if (ocrResultText.length() > 0) {
					// onDifferentDeviceLanguage();
					buttonSpeak.setEnabled(true);
					TTS.getInstance().speak(ocrResultText, true);
				}
			} catch (NullPointerException ex) {
				Log.d(TAG, "Error", ex);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.d(TAG, "Could not use tts", e);
			}
		}
	}

	private void onSpeak() {
		// change button icon when start tts speak
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				buttonSpeak.setBackgroundResource(R.drawable.restore_play);
				buttonStop.setEnabled(true);
			}
		});
	}

	@Override
	public void onClick(View v) {
		if (v == buttonSpeak) {
			try {
				TTS.getInstance().speak(ocrResultText, true);
				onSpeak();
			} catch (Exception ex) {
				Log.d(TAG, ex.getMessage(), ex);
			}
		} else if (v == this.buttonStop) {
			TTS.getInstance().stop();
			onEndOfSpeech();
		}
	}

	private void setTextOcrResult() {

		Intent i = getIntent();
		ocrResult = i.getParcelableExtra(CameraActivity.OCR_RESULT_EXTRA_KEY);
        ocrResultText = ocrResult.getResult();
		Log.d(TAG, "receive result text from OCR :");
		Log.d(TAG, ocrResultText);
		textView.setText(ocrResultText);

	}

	// ************************************************* file dialog **************************************************
	private void saveToFile(File aFile) {
		Uri theUri = Uri.fromFile(aFile).buildUpon().scheme("file.new").build();
		Intent theIntent = new Intent(Intent.ACTION_PICK);
		theIntent.setData(theUri);
		theIntent.putExtra(Intent.EXTRA_TITLE, "A Custom Title"); // optional
		theIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); // optional
		try {
			startActivityForResult(theIntent, SAVE_FILE_RESULT_CODE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case SAVE_FILE_RESULT_CODE: {
			if (resultCode == RESULT_OK && data != null && data.getData() != null) {
				// String theFilePath = data.getData().getPath();
				// new FileUtil().saveFile(new File(theFilePath + ".txt"));
			}
			break;
		}
		}
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume..................");
        super.onResume();

		textView.setText(ocrResultText);
		buttonSpeak.setEnabled(true);
//		createTTS();
        if (firstCall && TTS.getInstance().isInitialized()){
            initLocalTTS();
        }

	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart..................");
		super.onStart();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy().....");

		if (isFinishing() && !isBackPressed) {
//			TTS.getInstance().shutdown();
//			AppSetting.getInstance().onDestroy();
			isBackPressed = false;
		}
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "onSaveInstanceState().....");
		super.onSaveInstanceState(savedInstanceState);
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		savedInstanceState.putBoolean("firstCall", firstCall);
		savedInstanceState.putString("ocrResult", ocrResultText);
	}

	private void restoreInstanceState(Bundle savedInstanceState) {
		firstCall = savedInstanceState.getBoolean("firstCall");
		ocrResultText = savedInstanceState.getString("ocrResult");
		textView.setText(ocrResultText);
	}

	// @Override
	// public void onRestoreInstanceState(Bundle savedInstanceState) {
	// Log.d(TAG, "onRestoreInstanceState().....");
	// super.onRestoreInstanceState(savedInstanceState);
	// // Restore UI state from the savedInstanceState.
	// // This bundle has also been passed to onCreate.
	// firstCall = savedInstanceState.getBoolean("firstCall");
	// }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Log.d(TAG, "Back button has bee Pressed");
			isBackPressed = true;
			return super.onKeyDown(keyCode, event);
		}

		return super.onKeyDown(keyCode, event);
	}
    class CustomScreenSlideAdapter extends FragmentStatePagerAdapter {

        public CustomScreenSlideAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return slides.get(position);
        }

        @Override
        public int getCount() {
            return slides.size();
        }
    }
}
