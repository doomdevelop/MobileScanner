package de.bht.bachelor.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import de.bht.bachelor.R;

/**
 * Created by and on 11.01.15.
 */
public class Preferences {

    private final SharedPreferences sharedPreferences;
    private static Preferences instance ;
    private static final String PREFERENCES_NAME = "OcrPreferences";
    public static final String TTS_CHECKED_KEY = "TTS_CHECKED_KEY";
    public static final String IMAGE_DATA = "IMAGE_DATA";
    public static Preferences createInstance(Context context){
        if(instance != null) throw (new IllegalStateException("Preferences already initialized!"));
        instance = new Preferences(context);
        return instance;
    }
    public static Preferences getInstance(){
        return instance;
    }
    private Preferences(Context context){
      sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME,Context.MODE_PRIVATE);
    }

    public void putBoolean(String key, boolean value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void putArrayData(String key, byte[] array){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String converted = Base64.encodeToString(array, Base64.DEFAULT);
        editor.putString(key,converted);
        editor.commit();
    }

    public byte[] getArrayData(String key){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String dataStr = sharedPreferences.getString(key,null);
        if(dataStr == null){
            return null;
        }
        byte[] array = Base64.decode(dataStr, Base64.DEFAULT);
        return array;
    }

    public boolean getBoolean(String key,boolean defoultValue){
        return sharedPreferences.getBoolean(key,defoultValue);
    }


}
