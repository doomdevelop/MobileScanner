package de.bht.bachelor.beans;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by and on 11.04.15.
 */
public class TtsEngine {
    private String label;
    private String packageName;
    private List<String> availableLanguages;
    private List<String> unavailableLanguages;

    private Intent intent;

    public TtsEngine() {

    }

    public TtsEngine(final String label, final String packageName, final ArrayList<String> availableLanguages, final Intent intent) {
        this.label = label;
        this.packageName = packageName;
        this.availableLanguages = availableLanguages;
        this.intent = intent;

    }

    public void setUnavailableLanguages(List<String> unavailableLanguages) {
        this.unavailableLanguages = unavailableLanguages;
    }

    public List<String> getUnavailableLanguages() {
        return unavailableLanguages;
    }


    public Intent getIntent() {
        return intent;
    }

    public void setIntent(final Intent intent) {
        this.intent = intent;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public List<String> getAvailableLanguages() {
        return availableLanguages;
    }

    public void setAvailableLanguages(final ArrayList<String> availableLanguages) {
        this.availableLanguages = availableLanguages;
    }
}
