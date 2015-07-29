package de.bht.bachelor.tts;

import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

import de.bht.bachelor.activities.MenuActivity;
import de.bht.bachelor.exception.LanguageNotSupportedException;
import de.bht.bachelor.exception.NotInitializedTtsException;
import de.bht.bachelor.message.ServiceMessenger;
import de.bht.bachelor.setting.AppSetting;

/**
 * This class is a Singleton and is an interface for the TTS engine installed on this device
 *
 * @author andrzej kozlowski
 */
public class TTS {

    private Handler handler;
    private boolean apiChecked = false;
    private TextToSpeech tts;
    private volatile boolean isInitialized = false;
    public final String endOfOcrResultSpeech = "EndOfOcrResultSpeech";
    public final String endOfToolTipsToSpeech = "endOfToolTipsToSpeech";
    private final HashMap<String, String> myHashSpeech = new HashMap<String, String>();
    private static final String TAG = TTS.class.getSimpleName();
    private boolean restoreSpeach = false;
    private float currentRate = 0;
    private int seekBarRateValue = 6;
    private boolean isOnChacking = false;

    /*
     * private constructor
     */
    private TTS() {

    }

    /*
     * private class instancing this class
     */
    private static class InstanceHolder {
        public static final TTS INSTANCE = new TTS();
    }

    /**
     * @return instance of the tts
     */
    public static final TTS getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * @param tts
     * @param language
     */
    public void init(TextToSpeech tts, Locale language) throws LanguageNotSupportedException {
        Log.d(TAG, "init TTS engine ");
        if (isInitialized && this.tts != null) {
            if (this.tts.isSpeaking()) {
                this.tts.stop();
            }
            this.tts.shutdown();
        }

        this.tts = tts;
        if (language != null) {
            Log.d(TAG, "init().." + language.getLanguage());
            setLanguage(language);
        }
        isInitialized = true;

    }

    public boolean setLanguage(Locale loc) throws LanguageNotSupportedException {
        Log.d(TAG, "setLanguage(): " + loc.getDisplayLanguage());
        int result;
        AppSetting.getInstance().getLanguageManager().printLocale(loc);
        Locale backupLocale = AppSetting.getInstance().getLanguageManager().getCurrentTtsLanguage();
        if (isInitialized) {
            if (tts.isSpeaking())
                tts.stop();
        }
//        int res = tts.isLanguageAvailable(loc);
//        switch (res) {
//
//            case TextToSpeech.LANG_AVAILABLE:
//                Log.e(TAG, "*********LANG_AVAILABLE*************");
//                break;
//            case TextToSpeech.LANG_COUNTRY_AVAILABLE:
//                Log.e(TAG, "*********LANG_COUNTRY_AVAILABLE*************");
//                break;
//            case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
//                Log.e(TAG, "*********LANG_COUNTRY_VAR_AVAILABLE*************");
//                break;
//            case TextToSpeech.LANG_MISSING_DATA:
//                Log.e(TAG, "*********LANG_MISSING_DATA*************");
//                break;
//            case TextToSpeech.LANG_NOT_SUPPORTED:
//                Log.e(TAG, "*********LANG_NOT_SUPPORTED*************");
//                break;
//
//
//        }

        result = this.tts.setLanguage(loc);

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {

            StringBuilder sb = new StringBuilder("could not set language " + loc.getCountry() + " in tts. Reason:\n ");
            switch (result) {
                case TextToSpeech.LANG_MISSING_DATA:
                    Log.e(TAG, "*********LANG_MISSING_DATA*************");
                    sb.append("Language missing data ");
                    break;
                case TextToSpeech.LANG_NOT_SUPPORTED:
                    Log.e(TAG, "*********LANG_NOT_SUPPORTED*************");
                    sb.append("language not supported");
                    break;
            }

            if (backupLocale != null) {
                Log.e(TAG, "Seting last Language back");
                // tts.setLanguage(backupLocale);
            }
            throw new LanguageNotSupportedException(sb.toString());
//            return false;
        } else {
            Log.d("TTS", "********************** This Language is supported ************************");
            this.isInitialized = true;
            return true;
        }
    }

    /**
     * @return true if the tts engine is speaking now
     */
    public boolean isSpeaking() {
        return tts.isSpeaking();
    }

    /**
     * Will call tts engine to speak given text.
     * If the given text is not ocr result, and tts is currently speaking ,
     * the call will be ignore.
     *
     * @param text            text to speak
     * @param speechOcrResult true if this text is ocr result
     * @throws Exception throw when there is no tts library or is not initialised yet
     */
    public void speak(String text, boolean speechOcrResult) throws Exception {
        Log.d(TAG, "speak()........");

        if (tts == null) {
            throw new Exception("Can not use TTS api, tts is null");
        }

        Log.d(TAG, "tts engine language: " + tts.getLanguage());
        if (tts.getLanguage() == null) {
            setLanguage(AppSetting.getInstance().getLanguageManager().getCurrentTtsLanguage());
        }

        if (!isInitialized) {
            throw new NotInitializedTtsException("Can not use TTS api, is not initialized", text);
        }
        if (!apiChecked) {
            throw new Exception("TTS api is not chacked ");
        }

        if (text == null) {
            throw new NullPointerException("text has value null !");
        }
        if (speechOcrResult && tts.isSpeaking()) {
            restoreSpeach = true;
            tts.stop();
        }
        if (speechOcrResult) {
            myHashSpeech.clear();
            myHashSpeech.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, this.endOfOcrResultSpeech);
            if (restoreSpeach) {
                // some speech was just interrupted, give short break before next speak
                makeBreak(500);
            }
            tts.speak(text, TextToSpeech.QUEUE_ADD, myHashSpeech);
            if (restoreSpeach) {
                Log.d(TAG, "preparing message : ");
                ServiceMessenger serviceMessenger = new ServiceMessenger(handler);
                serviceMessenger.sendMessage(0, 0, null);
                // removeValueFromMap("");
                restoreSpeach = false;
            }
        } else {
            if (tts.isSpeaking()) {
                int stop = tts.stop();
            }
            myHashSpeech.clear();
            myHashSpeech.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, this.endOfToolTipsToSpeech);

            tts.speak(text, TextToSpeech.QUEUE_ADD, myHashSpeech);
//            } else {
//                Log.d(TAG, "could not call api to speek, tts is speaking now ");
//            }
        }

    }

    private void makeBreak(long sleeTime) {
        Log.d(TAG, "makeBreak() " + sleeTime + " ms");
        try {
            Thread.sleep(sleeTime);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "Sleep was interrupted");
        }
    }

    /**
     * Stopping tts Speech
     */
    public void stop() {
        Log.d(TAG, "stop()...........");
        tts.stop();
        if (myHashSpeech.containsValue(this.endOfOcrResultSpeech)) {
            Log.d(TAG, "removing  KEY_PARAM_UTTERANCE_ID ");
            myHashSpeech.remove(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        }
    }

    /**
     * Set speech rate to the tts engine
     *
     * @param rate
     */
    public void setRate(float rate) {
        if (rate != this.currentRate) {
            this.currentRate = rate;
            tts.setSpeechRate(rate);
        }
    }

    /**
     * @return current speech rate of the tts engine
     */
    public float getRate() {

        if (currentRate > 0)
            return currentRate;
        return 1.0f;
    }

    /**
     * Shutdown tts engine
     */
    public void shutdown() {
        Log.d(TAG, "shutdown()...");
        isInitialized = false;
        apiChecked = true;
        isOnChacking = false;
        if (tts != null)
            tts.shutdown();
    }

    /**
     * @return true if the device has the tts library
     */
    public boolean isApiChecked() {
        return apiChecked;
    }

    /**
     * Set true if the device has the tts library
     *
     * @param apiChecked
     */
    public void setApiChecked(boolean apiChecked) {
        this.apiChecked = apiChecked;
    }

    /**
     * @return true if the tts engine has been correctly initialised
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Set given instance of {@link Handler}
     *
     * @param handler
     */
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * @param isInitialized
     */
    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    /**
     * @return value of speech rate current showing in Menu
     * @see MenuActivity
     */
    public int getSeekBarRateValue() {
        return seekBarRateValue;
    }

    /**
     * Set current value of speech rate showing in Menu
     *
     * @param seekBarRateValue
     * @see MenuActivity
     */
    public void setSeekBarRateValue(int seekBarRateValue) {
        this.seekBarRateValue = seekBarRateValue;
    }

    public TextToSpeech getTts() {
        return tts;
    }

    public boolean isLanguageAvailable(Locale locale) {
        if (tts == null)
            return false;
        int result = tts.isLanguageAvailable(locale);
        if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
            return false;
        }
        return true;
    }

    public Locale getCurrentEngineLanguage() {
        return tts.getLanguage();
    }

    public boolean isOnChacking() {
        return isOnChacking;
    }

    public void setOnChacking(boolean isOnChacking) {
        this.isOnChacking = isOnChacking;
    }

}
