package de.bht.bachelor.activities;

import android.app.Application;

import de.bht.bachelor.manager.Preferences;

/**
 * Created by and on 13.01.15.
 */
public class OcrApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Preferences.createInstance(this);
    }
}
