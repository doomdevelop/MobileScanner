package de.bht.bachelor.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Locale;

import de.bht.bachelor.R;
import de.bht.bachelor.helper.ChangeActivityHelper;
import de.bht.bachelor.language.LanguageManager;
import de.bht.bachelor.manager.OrientationManger;
import de.bht.bachelor.manager.Preferences;
import de.bht.bachelor.setting.AppSetting;
import de.bht.bachelor.tts.TTS;

/**
 * Main Menu Activity class.
 * Extend this Activity to provide access to the App-Setting.
 * This class check access to the TTS on the device.
 * If there is not TTS, will start Install-Intent.
 *
 * @author kozlowski76@yahoo.de
 */
public class MenuCreatorActivity extends FragmentActivity implements OnInitListener {

    // *****************************************************************************************************************
    private Vibrator vibrator;
    private LanguageManager languageManager;
    private static final String TAG = MenuCreatorActivity.class.getSimpleName();
    private boolean hasDifferentDeviceLocale = false;

    /**
     * Doration of vibration for long click.
     */
    public static final int VIBRATE_DURATION = 35;
    protected final int MY_DATA_CHECK_CODE = 0;
    private Handler resultHandler;
    private static int SCREEN_OFF_RECEIVER_DELAY = 500;
    protected boolean isScreenOn = true;
    protected boolean isReceiveRegistered = false;
    protected boolean isBackPressed = false;
    private OcrResultActivity.OnCheckedTTS onCheckedTTS;

    public void setOnTtsInitCallback(OnTtsInitCallback onTtsInitCallback) {
        this.onTtsInitCallback = onTtsInitCallback;
    }

    private OnTtsInitCallback onTtsInitCallback;

    public interface OnTtsInitCallback {
        void onSuccessfully();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()..............");
        super.onCreate(savedInstanceState);

        vibrator = (Vibrator) getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
        // tts
        testTTSFlags();
        boolean isChecked = Preferences.getInstance().getBoolean(Preferences.TTS_CHECKED_KEY, false);
        TTS.getInstance().setApiChecked(isChecked);

        startCheckTTS();
        if (TTS.getInstance().isInitialized()) {
            Log.d(TAG, "---------------TTS is initialized yet---------");
        } else {
            Log.d(TAG, "---------------TTS is NOT initialized yet---------");
        }
    }

    private void testTTSFlags() {
        Log.d(TAG, "---------------isInitialized(): " + TTS.getInstance().isInitialized());
        Log.d(TAG, "---------------isOnChacking(): " + TTS.getInstance().isOnChacking());
        Log.d(TAG, "---------------isApiChecked(): " + TTS.getInstance().isApiChecked());
    }


    private void initTTS() {
        Log.d(TAG, "initTTS().....");
        if (resultHandler != null)
            TTS.getInstance().setHandler(resultHandler);
        if (!TTS.getInstance().isInitialized()) {
            TTS.getInstance().init(new TextToSpeech(this, this), AppSetting.getInstance().getLanguageManager().getCurrentTtsLanguage());
        }
    }

    public void setLanguageToTTS() {
        Log.d(TAG, "setLanguageToTTS().......");
        if (languageManager == null) {
            languageManager = AppSetting.getInstance().getLanguageManager();
        }
        if (languageManager != null) {
            Locale loc = languageManager.getCurrentTtsLanguage();
            TTS.getInstance().setLanguage(loc);
        }
    }

    private void startCheckTTS() {
        Log.d(TAG, "starting new activity to Check TTS");

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    }

    private void changeActivity() {
        Log.d(TAG, "starting new custom check Activity : CheckLanguageActivity");
        Intent checkLanguage = new Intent(getApplicationContext(), CheckLanguageActivity.class);
        checkLanguage.putExtra(ChangeActivityHelper.RUN_CHECK_SETUP, ChangeActivityHelper.RUN_CHECK_SETUP);
        // starting new activity
        startActivity(checkLanguage);
    }

    @Override
    public void onInit(int status) {
        // calling after onActivityResult

        Log.d(TAG, "onInit().....");
        if (status == TextToSpeech.SUCCESS) {
            // Text-To-Speech engine has been checked successfully
            if (!TTS.getInstance().isInitialized()) {
                TTS.getInstance().setInitialized(true);
                TTS.getInstance().setOnChacking(false);
                if (languageManager == null) {
                    languageManager = AppSetting.getInstance().getLanguageManager();
                }
                Locale loc = languageManager.getCurrentTtsLanguage();

                TTS.getInstance().setLanguage(loc);

                Locale locTTS = TTS.getInstance().getCurrentEngineLanguage();

                if (locTTS != null && loc != null && !locTTS.getDisplayLanguage().equalsIgnoreCase(loc.getDisplayLanguage())) {
                    startMenuLanguageActivityInChooseMode();

                } else if (locTTS == null || loc == null) {
                    startMenuLanguageActivityInChooseMode();
                } else {
                    changeActivity();
                }
                // Toast.makeText(this, "Text-To-Speech engine has been checked successfully ", Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "Nothing to do , TTS is initialized.");
                if (onTtsInitCallback != null) {
                    this.onTtsInitCallback.onSuccessfully();
                }
            }
        } else if (status == TextToSpeech.ERROR) {
            Toast.makeText(this, "Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
            TTS.getInstance().setApiChecked(false);
        }

    }

    private void startMenuLanguageActivityInChooseMode() {
        Log.d(TAG, "Could not set language to the engine, user must do it manually");
        languageManager.setTtsSpinnerPosition(0);
        Intent menu = new Intent(getApplicationContext(), MenuLanguagesActivity.class);
        menu.putExtra(LanguageManager.CHOOSE_TTS_LANGUAGE_NAME, LanguageManager.CHOOSE_TTS_LANGUAGE_MODE);
        startActivity(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()...............");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_DATA_CHECK_CODE) {

            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                Log.d(TAG, "Success, create the TTS instanc");
                if (!TTS.getInstance().isApiChecked() || !TTS.getInstance().isInitialized()) {
//					languageManager = AppSetting.getInstance().initLanguageMenager(this);
                    languageManager = AppSetting.getInstance().getLanguageManager();
                    /*
                     * the language manger is ready, we can start check
					 * and if there is a need to download tessdata files
					 * start check Activity CheckLanguageActivity checkFiles()
					 */
                    if (!TTS.getInstance().isInitialized()) {
                        initTTS();
                    }
                    TTS.getInstance().setApiChecked(true);
                    TTS.getInstance().setOnChacking(false);
                    Preferences.getInstance().putBoolean(Preferences.TTS_CHECKED_KEY, true);
                    // languageManager.checkDefaultLanguage();
                    if (onCheckedTTS != null) {
                        onCheckedTTS.onCheckedTTS(true);
                    }
                }
            } else {
                if (onCheckedTTS != null) {
                    onCheckedTTS.onCheckedTTS(false);
                }
                Log.d(TAG, "missing tessdata, install it");
                // TODO: info to user , need internet connecton
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    /**
     * @return true if the Device language is not equals TTS language .
     */
    public boolean onDifferentDeviceLanguage() {
        Log.d(TAG, "onDifferentDeviceLanguage().......");
        if (languageManager == null)
//			try {
            languageManager = AppSetting.getInstance().getLanguageManager();
//			} catch (NullPointerException ex) {
//				languageManager = AppSetting.getInstance().initLanguageMenager(this);
//			}
        try {
            if (!languageManager.getCurrentTtsLanguage().getDisplayLanguage().equals(languageManager.getCurrentDeviceLanguage().getDisplayLanguage())) {
                Log.d(TAG, "The device language is not equals with TTS language");
                Locale locale = languageManager.getCurrentDeviceLanguage();
                Log.d(TAG, "going to set device language: " + locale.getDisplayLanguage());
                Locale l = new Locale(locale.getLanguage(), locale.getLanguage().toUpperCase());
                if (!TTS.getInstance().isLanguageAvailable(l)) {
                    Log.d(TAG, l.getDisplayLanguage() + " is not suported , we will use default language");
                    setLanguage(Locale.ENGLISH);

                    languageManager.setTtsSpinnerPosition(1);
                } else {
                    setLanguage(l);
                }
                hasDifferentDeviceLocale = true;
                onInitTtsCompletedListener();

                return true;
            } else {
                Log.d(TAG, "The device language is equals with TTS language");
                if (TTS.getInstance().getCurrentEngineLanguage() == null) {
                    Log.d(TAG, "but engine has no language, set to TTS language from language manager");
                    Locale locale = languageManager.getCurrentTtsLanguage();

                    Locale l = new Locale(locale.getLanguage(), locale.getLanguage().toUpperCase());
                    setLanguage(l);
                }
            }
        } catch (NullPointerException ex) {
            Log.d(TAG, "Could not run check onDifferentDeviceLanguage", ex);
            return false;

        }
        return false;
    }

    private void setLanguage(Locale locale) {
//		try {
        TTS.getInstance().setLanguage(locale);
//		} catch (NullPointerException ex) {
//			AppSetting.getInstance().initLanguageMenager(this);
//			TTS.getInstance().setLanguage(locale);
//		}
    }

    private void onInitTtsCompletedListener() {
        Log.d(TAG, "onInit()....");
        TTS.getInstance().getTts().setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
            /*
             * Will be called when tts finish speak.
             * (non-Javadoc)
             *
             * @see android.speech.tts.TextToSpeech.OnUtteranceCompletedListener#onUtteranceCompleted(java.lang.String)
             */
            @Override
            public void onUtteranceCompleted(String utteranceId) {
                Log.d(TAG, "onUtteranceCompleted()....." + utteranceId);
                if (utteranceId.equals(TTS.getInstance().endOfToolTipsToSpeech)) {
                    // call it just when tts spoke the tool-tips text
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // UI changes

                            Log.d(TAG, "stop speech tool-tips, setLanguageBack");
                            setLanguageBack();
                        }
                    });
                }
            }
        });
    }

    /*
     * The TTS-language has been changed temporary just to use tool-tips, set back the language
     */
    private void setLanguageBack() {
        Log.d(TAG, "setLanguageBack().......");
        if (hasDifferentDeviceLocale) {
            TTS.getInstance().setLanguage(languageManager.getCurrentTtsLanguage());
            hasDifferentDeviceLocale = false;
        }
    }

    public void checkSetup() {
        changeActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.languageManager == null) {
//            try {
            languageManager = AppSetting.getInstance().getLanguageManager();
//            } catch (NullPointerException ex) {
//                languageManager = AppSetting.getInstance().initLanguageMenager(this);
//            }
        }
//        if (!TTS.getInstance().isInitialized()) {
//            try {
//                TTS.getInstance().init(new TextToSpeech(getBaseContext(), this), AppSetting.getInstance().getLanguageManager().getCurrentTtsLanguage());
//            } catch (NullPointerException ex) {
//                AppSetting.getInstance().initLanguageMenager(this);
//                TTS.getInstance().init(new TextToSpeech(getBaseContext(), this), AppSetting.getInstance().getLanguageManager().getCurrentTtsLanguage());
//            }
//        }


//        if (checkSetupAgain) {
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                Log.d(TAG, "Sleep is interrupted", e);
//            }
//            checkSetup();
//        }
    }

    // *************************************************** Menu ******************************************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent menu;
        if (item == null)
            return false;

        switch (item.getItemId()) {
            case R.id.menuIcon:
                menu = new Intent(getApplicationContext(), MenuActivity.class);
                startActivity(menu);
                return true;
            case R.id.languageIcon:
                menu = new Intent(getApplicationContext(), MenuLanguagesActivity.class);
                startActivity(menu);
                return true;
            case R.id.imagePreprocesingIcon:
                menu = new Intent(getApplicationContext(), ImagePreprocesingSettingActivity.class);
                startActivity(menu);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * @return vibrator created in this Activity
     */
    public Vibrator getVibrator() {
        return vibrator;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()........");
        super.onDestroy();
//		if (ChangeActivityHelper.getInstance().isOnMainScreen()) {
			/*
			 * During first init by main Activity the destroy() and create() is calling more than ones,
			 * if the tts after initialisation will be not shutdown before activity destroy,
			 * the app will get a problem of ServiceConnectionLeaked :
			 * ServiceConnectionLeaked Activity has leaked ServiceConnection android.speech.tts.TextToSpeech
			 * After that TTS is not working any more , even after new initialising
			 */
//        if (isFinishing()) {
//            TTS.getInstance().shutdown();
//        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()........");
        super.onPause();
//        TTS.getInstance().shutdown();

    }


    public void setCallbackOnCheckedTts(OcrResultActivity.OnCheckedTTS callback) {
        this.onCheckedTTS = callback;
    }

    public void setResultHandler(Handler resultHandler) {
        this.resultHandler = resultHandler;
    }


}
