package de.bht.bachelor.setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import de.bht.bachelor.R;
import de.bht.bachelor.language.LanguageManager;

public class AppSetting {

    private int contrastEnhanceStatus = AppSetting.CONTRAST_ENHANCE_INACTIVE;
    private static final String TAG = AppSetting.class.getSimpleName();
    private volatile LanguageManager languageManager = null;
    public static final int CONTRAST_ENHANCE_ACTIVE = 1;// ContrastEnhance
    public static final int CONTRAST_ENHANCE_INACTIVE = 0;// ContrastEnhance

	private AppSetting() {

	}

	/*
	 * private class instancing this class
	 */
	private static class InstanceHolder {
		public static final AppSetting INSTANCE = new AppSetting();
	}

	/**
	 * @return instance of the AppSetting
	 */
	public static final AppSetting getInstance() {

		return InstanceHolder.INSTANCE;
	}


	public void initLanguageMenager(Context context) {
		if (languageManager == null) {
			Log.d(TAG, "initLanguageMenager()....");
			//TODO: this init take > 200 ms, move to background task
			Resources res = context.getResources();
			Locale deviceLocale = context.getResources().getConfiguration().locale;
			if (deviceLocale == null)
				deviceLocale = Locale.ENGLISH;
			Locale devLoc = new Locale(deviceLocale.getDisplayLanguage());

			Log.d(TAG, "Device Language iso3: " + deviceLocale.getISO3Language());
			Log.d(TAG, "Device Language: " + deviceLocale.getDisplayLanguage() + ", country: " + deviceLocale.getDisplayCountry());
			List<String> ttsList = Arrays.asList(res.getStringArray(R.array.ttsLanguagesArray));
			List<String> ocrList = Arrays.asList(res.getStringArray(R.array.ocrLanguagesArray));
			languageManager = new LanguageManager(new ArrayList<String>(ttsList), new ArrayList<String>(ocrList), devLoc);

		}

	}

	public LanguageManager getLanguageManager() {
		if (languageManager == null)
			throw new NullPointerException("languageManager has value Null, you need to init it first");
		return languageManager;
	}

	public int getContrastEnhanceStatus() {
		return contrastEnhanceStatus;
	}

	public void setContrastEnhanceStatus(int contrastEnhanceStatus) {
		this.contrastEnhanceStatus = contrastEnhanceStatus;
	}

	public boolean isContrastEnhanceActive() {
		return contrastEnhanceStatus == AppSetting.CONTRAST_ENHANCE_ACTIVE;
	}
}
