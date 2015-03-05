package de.bht.bachelor.activities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import de.bht.bachelor.R;
import de.bht.bachelor.helper.CameraFlashState;
import de.bht.bachelor.helper.MenuSettingHelper;
import de.bht.bachelor.tts.TTS;

public class MenuActivity extends Activity implements OnSeekBarChangeListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
		res = getResources();
		vibrator = (Vibrator) getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
		ttsrateTextView = (TextView) findViewById(R.id.ttsRateTextView);
		ttsrateTextView.setTextColor(R.color.black);
		// ttsrateTextView.setTextSize(22);
		ttsrateTextView.setBackgroundColor(getResources().getColor(R.color.white));
		ttsrateSeekBar = (SeekBar) findViewById(R.id.ttsRateSeekBar);
		ttsrateSeekBar.setOnSeekBarChangeListener(this);
		ttsrateSeekBar.setProgress(TTS.getInstance().getSeekBarRateValue());
		// --------------------camera flash -------------------------------
		flashIconButton = (Button) findViewById(R.id.flashButton);
		flashSeekbar = (SeekBar) findViewById(R.id.flashSeekBar);
		flashSeekbar.setOnSeekBarChangeListener(this);

		// set long click for TTS tool tip
		onLongClick();
		// set default for camera flash
		setNewFlashSetting(MenuSettingHelper.getInstance().getCurrentFlashState());
		// set correctly component position
		flashSeekbar.setProgress(MenuSettingHelper.getInstance().getSeekbarValue());
	}

	// ********************************** TTS Listeners *********************************************
	public void onLongClick() {
		this.ttsrateTextView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				CameraFlashState state;
				if (v == ttsrateTextView) {
					state = MenuSettingHelper.getInstance().getCurrentFlashState();
					vibrator.vibrate(VIBRATE_DURATION);
					try {
						switch (state) {
						case FLASH_AUTO:
							TTS.getInstance().speak(getString(R.string.rate), false);
							break;
						case FLASH_ON:
							TTS.getInstance().speak(getString(R.string.rate), false);
							break;
						case FLASH_OFF:
							TTS.getInstance().speak(getString(R.string.rate), false);
							break;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.d(TAG, "Could not start tts");
					}
				}
				return true;
			}
		});

		flashIconButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				CameraFlashState state;
				if (v == flashIconButton) {
					state = MenuSettingHelper.getInstance().getCurrentFlashState();
					vibrator.vibrate(VIBRATE_DURATION);
					try {
						switch (state) {
						case FLASH_AUTO:
							TTS.getInstance().speak(getString(R.string.flash), false);
							break;
						case FLASH_ON:
							TTS.getInstance().speak(getString(R.string.flash), false);
							break;
						case FLASH_OFF:
							TTS.getInstance().speak(getString(R.string.flash), false);
							break;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.d(TAG, "Could not start tts");
					}
				}
				return true;
			}
		});
	}

	@Override
	public void onProgressChanged(SeekBar sb, final int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		if (sb == this.ttsrateSeekBar) {
			onTtsRateChange(arg1);
		} else if (sb == flashSeekbar) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onCameraFlashChange(arg1);
				}
			});
			onCameraFlashChange(arg1);
		}

	}

	private void onTtsRateChange(int value) {
		TTS.getInstance().setSeekBarRateValue(value);
		float result = (float) (value + 1) / 10;// seek bar has 0 as first value, we wont measure from with 1
		Log.d(TAG, "result : " + result);
		TTS.getInstance().setRate(result);
		result = round(result, 2);
		Log.d(TAG, "result : " + result);
		SpannableString text = new SpannableString(Float.toString(result));
		text.setSpan(new RelativeSizeSpan(2f), 0, text.length(), 0);
		text.setSpan(new ForegroundColorSpan(Color.BLACK), 0, text.length(), 0);
		ttsrateTextView.setText(text, BufferType.SPANNABLE);
	}

	// ************************************************** Setting for TTS rate **************************************************
	/**
	 * http://forum.4programmers.net/Java/76132-Zaokraglenie_liczby_floatdouble_do_n_miejsc_po_przecinku?p=236292
	 * 
	 * @param val
	 * @param places
	 * @return
	 */
	public float round(float val, int places) {
		long factor = (long) Math.pow(10, places);
		val = val * factor;
		long tmp = Math.round(val);
		return (float) tmp / factor;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	// ******************************************** Setting for Camera Flesh ********************************************

	private void onCameraFlashChange(int value) {
		MenuSettingHelper.getInstance().setSeekbarValue(value);

		switch (value) {
		case 0:
			setNewFlashSetting(CameraFlashState.FLASH_AUTO);
			break;
		case 1:
			setNewFlashSetting(CameraFlashState.FLASH_ON);
			break;
		case 2:
			setNewFlashSetting(CameraFlashState.FLASH_OFF);
			break;
		}
	}

	private void setNewFlashSetting(CameraFlashState state) {
		MenuSettingHelper.getInstance().setCurrentFlashState(state);

		switch (state) {
		case FLASH_AUTO:
			setIconToFlashSetting(res.getDrawable(R.drawable.flash_auto));

			break;
		case FLASH_ON:
			setIconToFlashSetting(res.getDrawable(R.drawable.flash_on));
			break;
		case FLASH_OFF:
			setIconToFlashSetting(res.getDrawable(R.drawable.flash_off));
			break;
		}
	}

	private void setIconToFlashSetting(Drawable drawable) {
		// Drawable dr = drawable.setBounds(flashIconButton.getB)
		flashIconButton.setBackgroundDrawable(drawable);
		// flashIconButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
	}

	// **********************************************************************************

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy()................");
		super.onDestroy();
	}

	private static final String TAG = MenuActivity.class.getSimpleName();
	private TextView ttsrateTextView;
	private SeekBar ttsrateSeekBar;
	private Button flashIconButton;
	private SeekBar flashSeekbar;
	private Resources res;
	private Vibrator vibrator;
	private static final int VIBRATE_DURATION = 35;

}
