package de.bht.bachelor;

import android.app.Application;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import de.bht.bachelor.file.FileManager;
import de.bht.bachelor.manager.OrientationManger;
import de.bht.bachelor.manager.Preferences;
import de.bht.bachelor.setting.AppSetting;
import de.bht.bachelor.tts.TTS;

/**
 * Created by and on 11.01.15.
 */
public class OcrApplication extends Application implements PreferenceManager.OnActivityDestroyListener {
    private static final String TAG = OcrApplication.class.getSimpleName();
    @Override
    public void onActivityDestroy() {
        Log.d(TAG,"onActivityDestroy() destroy manager instances...");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() manager instances...");
        Preferences.createInstance(this);
        AppSetting.getInstance().initLanguageMenager(this);
        OrientationManger.createInstance();
        new FileManager().prepareFileSystem();
    }
}
