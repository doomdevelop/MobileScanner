package de.bht.bachelor.exception;

import android.speech.tts.TextToSpeech;
import android.util.Log;

/**
 * Created by and on 11.04.15.
 */
public class LanguageNotSupportedException extends Exception {
    private static final long serialVersionUID = 1L;

    public LanguageNotSupportedException() {
        super();
    }

    public LanguageNotSupportedException(String message) {
        super(message);
    }
    public LanguageNotSupportedException(String language, int ttsResult) {
        super("");
        StringBuilder sb = new StringBuilder("could not set language "+language+" in tts. Reason:\n ");
        switch (ttsResult){
            case TextToSpeech.LANG_MISSING_DATA:
                sb.append("Language missing data ");
                break;
            case TextToSpeech.LANG_NOT_SUPPORTED:
                sb.append("language not supported");
                break;
        }

    }
}
