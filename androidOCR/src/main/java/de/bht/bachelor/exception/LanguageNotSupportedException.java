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

}
