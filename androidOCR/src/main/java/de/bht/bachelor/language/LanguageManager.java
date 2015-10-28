package de.bht.bachelor.language;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import de.beutch.bachelorwork.util.file.Path;
import de.bht.bachelor.R;
import de.bht.bachelor.beans.TtsEngine;
import de.bht.bachelor.file.FileManager;
import de.bht.bachelor.helper.NetworkHelper;
import de.bht.bachelor.manager.Preferences;
import de.bht.bachelor.message.ServiceMessenger;
import de.bht.bachelor.tasks.DownloadOcrTraineddata;
import de.bht.bachelor.tts.TTS;

/**
 * This class is giving access on :
 * 
 * <ul>
 * <li>The current Device-Language (Locale)
 * <li>The current TTS-Language (Locale)
 * <li>The current OCR-Language (ISO-3 String)
 * </ul>
 * 
 * The Default Language for TTS and OCR is the Device-language.
 * There is an UI to set up other Language for TTS and/or OCR, every change will be saved in the instance of this class.
 * <p>
 * To get an Instance of this class call <code>AppSetting.getInstance().getLanguageManager()</code>
 * 
 * @author andrzej
 * 
 */
public class LanguageManager {

	/* the OCR-language position in the spine component */
	private int ocrSpinnerPosition = -1;
	/* the TTS-language position in the spine component */
	private int ttsSpinnerposition = -1;
	/* current TTS language */
	private Locale currentTtsLanguage;
	/* current OCR language */
	private String currentOcrLanguage;
	/* current device language */
	private Locale currentDeviceLanguage;
	/* tts languages from xml, initialised in AppSetting */
	private List<Locale> ttsLocaleListISO2 = new ArrayList<Locale>();

	private List<Locale> ttsLanguagesListISO2 = new ArrayList<Locale>();

	private List<String> ttsLanguagesListISO2ToDisplay = new ArrayList<String>();
	/* ocr languages from xml, initialised in AppSetting */
	private List<String> ocrLanguagesListISO2 = new ArrayList<String>();



	private static final String TAG = LanguageManager.class.getSimpleName();

	public static int DEFAULT_LANGUAGE_POSITION = 0;
	public static final String CHOOSE_TTS_LANGUAGE_NAME = "CHOOSE_TTS_LANGUAGE";
	public static final String CHOOSE_OCR_LANGUAGE_NAME = "CHOOSE_OCR_LANGUAGE";
	public static final int CHOOSE_TTS_LANGUAGE_MODE = 10;
	public static final int CHOOSE_OCR_LANGUAGE_MODE = 20;


	private List<TtsEngine> ttsEngineList = new ArrayList<TtsEngine>();

	private Map<String, Locale> localeCountryMap;
	private Map<String, Locale> localeLanguageMap;



	private TtsEngine ttsEngine;


	public List<String> getOcrLanguagesListISO2() {
		return ocrLanguagesListISO2;
	}

	/**
	 * The main language List displayed on user view. The Lists should be loaded from values/array.xml.
	 * By initialising TTS, if the device language exist in the OCR / TTS language list , will be set up as default.
	 * If not, the first position in OCR / TTS Language list is the Default language.
	 * 
	 * @param ttsLanguagesListISO2
	 *            list with supported languages for TTS
	 * @param ocrLanguagesListISO2
	 *            list with supported languages for OCR
	 * @param deviceLocale
	 *            current device language as Locale
	 */
	public LanguageManager(ArrayList<String> ttsLanguagesListISO2, ArrayList<String> ocrLanguagesListISO2, Locale deviceLocale) {
		Log.d(TAG, "LanguagesManager()...");
		this.ocrLanguagesListISO2 = ocrLanguagesListISO2;
//		this.ttsLanguagesListISO2 = ttsLanguagesListISO2;
		this.currentDeviceLanguage = Locale.getDefault();
		initCountryCodeMapping();
		initLanguageCodeMapping();
		initOcrLanguage();
//		initTtsLanguage();
	}

	/*
	 * Default Language for OCR and TTS will be current Language on the device.
	 * If there is no support of this language , English will be set up.
	 */

	private void initOcrLanguage(){
		Log.d(TAG, "setDefaultLanguages()..");

		Log.d(TAG, "currentDeviceLanguage.getLanguage()..." + currentDeviceLanguage.getLanguage());
		if (ocrLanguagesListISO2.contains(currentDeviceLanguage.getLanguage())) {
			Log.d(TAG, "set Default Language for ocr: " + currentDeviceLanguage.getLanguage());
			currentOcrLanguage = iso2LanguageCodeToIso3LanguageCode(currentDeviceLanguage.getLanguage());
			setOcrSpinnerPosition(ocrLanguagesListISO2.indexOf(currentDeviceLanguage.getLanguage()));
		} else {
			// the first position in Language list is the Default language
			currentOcrLanguage = iso2LanguageCodeToIso3LanguageCode(ocrLanguagesListISO2.get(0));
			setOcrSpinnerPosition(1);
		}
	}

	private void initTtsLanguage(){
		int lastPosition = Preferences.getInstance().getInt(Preferences.TTS_SPINER_POSITION);
		if(lastPosition == -1) {
			if (isInTTSLanguageList(currentDeviceLanguage)) {
				Log.d(TAG, "currentDeviceLanguage is available in TTS engine, set it up");

				currentTtsLanguage = ttsLanguagesListISO2.get(ttsSpinnerposition);
//				currentTtsLanguage = new Locale(language, language.toUpperCase());
			} else {
				setDefaultTtsLanguage();
			}
		}else{
			ttsSpinnerposition = lastPosition;
			currentTtsLanguage = ttsLanguagesListISO2.get(ttsSpinnerposition);
//			currentTtsLanguage = new Locale(language, language.toUpperCase());
		}
	}



	private void initLanguageCodeMapping() {
		String[] languages = Locale.getISOLanguages();
		localeLanguageMap = new HashMap<String, Locale>(languages.length);
		for (String language : languages) {
			Locale locale = new Locale(language, "");
			localeLanguageMap.put(locale.getISO3Language().toLowerCase(), locale);
		}
	}
	private void initCountryCodeMapping() {
		String[] countries = Locale.getISOCountries();
		localeCountryMap = new HashMap<String, Locale>(countries.length);
		for (String country : countries) {
			Locale locale = new Locale("", country);
			localeCountryMap.put(locale.getISO3Country().toUpperCase(), locale);
		}
	}
	private String iso3CountryCodeToIso2CountryCode(String iso3CountryCode) {
		return localeCountryMap.get(iso3CountryCode).getLanguage();
	}

	private String iso3LanguageCodeToIso2LanguageCode(String iso3LanguageCode) {
		return localeLanguageMap.get(iso3LanguageCode).getLanguage();
	}

	/**
	 *
	 * @param engine engine package created from TextToSpeech.getDefaultEngine()
	 * @return
	 */
	public TtsEngine getTtsEngineByPackageName(String engine){

		for (int i = 0; i < ttsEngineList.size(); i++) {
			if(ttsEngineList.get(i).getPackageName().equals(engine)){

				return ttsEngineList.get(i);
			}
		}
		return null;
	}

	public List<String> getTtsLanguagesListISO2ToDisplay() {
		return ttsLanguagesListISO2ToDisplay;
	}

	public void initLanguageManagerForEngine(TtsEngine engine){

		//Key: availableVoices = [eng-USA, eng-GBR, spa-ESP, fra-FRA, deu-DEU, ita-ITA, kor-KOR, zho-CHN]
		//Key: unavailableVoices = [eng-USA, fra-FRA, ita-ITA, deu-DEU, eng-GBR, spa-ESP, rus-RUS, por-BRA, kor-KOR, spa-MEX]

		this.ttsEngine = engine;
		List<String> availableList = ttsEngine.getAvailableLanguages();
		List<String> unavailableList = ttsEngine.getUnavailableLanguages();
		ttsLanguagesListISO2.clear();
		ttsLanguagesListISO2ToDisplay.clear();
		String iso2 = null;
		for(String iso3: availableList){
			ttsLanguagesListISO2ToDisplay.add(iso3);
			String[] split = iso3.split("-");

//			iso2 = iso3LanguageCodeToIso2LanguageCode(split[0]);

			if(split[0].equals("eng")){
//				iso2 =Locale.US.getLanguage();
				iso2 =Locale.US.getISO3Language();
			}
			Log.d(TAG, "converted " + split[0] + " to iso2: " + iso2);
			if(split[0] != null) {
				String locStr = split[0].toLowerCase()+"_"+split[1].toUpperCase();
						ttsLanguagesListISO2.add(new Locale(locStr));

			}


		}
		initTtsLanguage();
	}



	private static final String por_bra = "por-bra";
	private static final String nld_nld = "nld-nld";
	private static final String eng_gbr=  "eng-gbr";
	private static final String eng_usa= "eng-usa";
 	private static final String spa_usa= "spa-usa";
	private static final String ita_ita= "ita-ita";
	private static final String hin_ind= "hin-ind";
	private static final String deu_deu= "deu-deu";
	private static final String ind_idn= "ind-idn";
	private static final String fra_fra= "fra-fra";
	private static final String kor_kor= "kor-kor";
	private static final String pol_pol ="pol-pol";
	private static final String eng_ind= "eng-ind";
	private static final String jpn_jpn= "jpn-jpn";
	private static final String rus_rus= "rus-rus";
	private static final String spa_esp = "spa-esp";


	private Locale getLocaleByIso3Language(String iso3){
		return null;
	}

	/**
	 * During init this LanguageMenager , TTS was not initialised.
	 * Check available of the current (device) tts language, if is not available set default language.
	 */
	public void checkDefaultLanguage() {
		if (!TTS.getInstance().isLanguageAvailable(currentTtsLanguage)) {
			setDefaultTtsLanguage();
		}
	}

	/*
	 * the first position in Language list is the Default language
	 */
	private void setDefaultTtsLanguage() {
		Log.d(TAG, "setDefaultTtsLanguage()................");
		int pos = DEFAULT_LANGUAGE_POSITION;
		setCurrentTtsLanguage(Locale.US);

		setTtsSpinnerPosition(pos);
	}

	/*
	 * Return true if the TTS-languages list contain language from given locale 'l'.
	 * If so , the position will be set for spinner.
	 */
	private boolean isInTTSLanguageList(Locale l) {
		Log.d(TAG,"isInTTSLanguageList().." +l.getLanguage());
		int pos = 0;
		Locale locale;
		for (Locale loc : ttsLanguagesListISO2) {
			Log.d(TAG,"compare with :" +loc.getCountry());
			if (loc.getLanguage().equals(l.getLanguage())) {
				setTtsSpinnerPosition(pos);
				return true;
			}
			pos++;
		}
		return false;
	}

	/**
	 * Convert 2 character language code in to 3 character
	 * 
	 * @param iso2LanguageCode
	 *            2 character language code
	 * @return iso3CountryCode
	 */
	public String iso2LanguageCodeToIso3LanguageCode(String iso2LanguageCode) {
		Locale locale = new Locale(iso2LanguageCode, "");
		return locale.getISO3Language();
	}

	/**
	 * 
	 * @param context
	 *            App context
	 * @param iso3
	 *            language in iso3 code
	 * @param compress
	 *            if true ending name of Traineddata contain '.gz'
	 * @return fully Traineddata name for given language
	 */
	public String getTraineddataNameByLanguage(Context context, String iso3, boolean compress) throws NullPointerException, IllegalArgumentException {				

		if (iso3 == null)
			throw new NullPointerException("iso3 can not be null");
		if (iso3.equals(""))
			throw new IllegalArgumentException("wrong language code iso3: " + iso3);

		String traineddataName = iso3;
		if (compress) {
//			traineddataName += context.getString(R.string.traineddata_ending_name_compress);
			String newName = "tesseract-ocr-3.01."+iso3+".tar.gz ";
			return newName;
		} else {
			traineddataName += context.getString(R.string.traineddata_ending_name_uncompress);
		}
		return traineddataName;
	}

	/**
	 * 
	 * @return true if for current language an OCR Traineddata is existing.
	 */
	public boolean checkTraineddataForCurrentLanguage(Context context) throws NullPointerException, IllegalArgumentException {
		return new FileManager().check(new File(Path.LANGUAGE_TRAINEDDATA_DIR + getTraineddataNameByLanguage(context, getCurrentOcrLanguage(), false)), false);
	}

	/**
	 * Call it by App start.
	 * Check file directory's and TRAINEDDATA for tesseract on the device.
	 * If there is no TTRAINEDDATA for default language (device language),
	 * the download will be initialised
	 * 
	 * @param context
	 *            context from current Activity
	 * @param resultHandler
	 *            Handler from current Activity.
	 *            By this handler the download task will call Activity to start and finish the ProgressDialog.
	 */
	public void checkSetup(Context context, Handler resultHandler) {
		Log.d(TAG, "checkSetup()...");

		new FileManager().prepareFileSystem();

		startDownloadTraineddata(context, resultHandler, getCurrentOcrLanguage());
	}

	/**
	 * Check if device has Traineddata for given language.
	 * If not the Traineddata will be downloaded to the right directory by download task {@link DownloadOcrTraineddata}.
	 * 
	 * @param context
	 *            context from current Activity
	 * @param resultHandler
	 *            Handler from current Activity.
	 *            By this handler the download task will call Activity to start and finish the ProgressDialog.
	 *            Also download task is using this handler to start intent of network setting if the Internet connection is off.
	 * @see DownloadOcrTraineddata
	 * @param iso3Language
	 *            Language for Traineddata
	 */
	public void startDownloadTraineddata(Context context, Handler resultHandler, String iso3Language) {
		String traineddataNameCompress;
		String traineddataNameUncompress;
		DownloadOcrTraineddata downloadOcrTessdata;
		try {
			String urlStr = context.getString(R.string.traineddata_url_new);
			traineddataNameUncompress = getTraineddataNameByLanguage(context, iso3Language, false);
			if (!new FileManager().check(new File(Path.LANGUAGE_TRAINEDDATA_DIR + traineddataNameUncompress), false)) {
				if (NetworkHelper.isConnected(context)) {
					traineddataNameCompress = getTraineddataNameByLanguage(context, iso3Language, false);
					// downloadOcrTessdata = new DownloadOcrTessdata(mProgressDialog, "pol.traineddata.gz", "pol.traineddata", this.resultHandler);
					downloadOcrTessdata = new DownloadOcrTraineddata(traineddataNameCompress, traineddataNameUncompress, resultHandler);
					try {
						downloadOcrTessdata.execute(new URL(urlStr + traineddataNameCompress));
					} catch (MalformedURLException e) {
						Log.d(TAG, "Could not download tessdata !", e);
					}
				} else {
					/*
					 * Start User-Dialog, user can jump to internet-connection setting,after that download will start automatically
					 */
					ServiceMessenger serviceMessenger = new ServiceMessenger(resultHandler);
					serviceMessenger.sendMessage(NetworkHelper.NO_INTERNET_CONECTION, 6, null);
				}
			} else {
				ServiceMessenger serviceMessenger = new ServiceMessenger(resultHandler);
				serviceMessenger.sendMessage(NetworkHelper.NOTHING_TO_DO, 6, null);
			}
		} catch (Exception ex) {
			Log.e(TAG, "Could not download Traineddata!", ex);
		}
	}

	/**
	 * 
	 * @return language current used by tts
	 */
	public Locale getCurrentTtsLanguage() {
		return currentTtsLanguage;
	}

	/**
	 * 
	 * @param currentTtsLanguage
	 *            language current used by tts
	 */
	public void setCurrentTtsLanguage(Locale currentTtsLanguage) {
//		Log.d(TAG, "Setting new TTS locale: " + currentTtsLanguage.getLanguage() + ",iso3 Language: " + currentTtsLanguage.getISO3Language() + "display language: " + currentTtsLanguage.getDisplayLanguage());
		this.currentTtsLanguage = currentTtsLanguage;
	}

	/**
	 * 
	 * @return current used language by ocr as 3 character code (iso 3).
	 */
	public String getCurrentOcrLanguage() {
		return currentOcrLanguage;
	}

	/**
	 * 
	 * @return display OCR language , same as Locale.getDisplayLanguage() is returning
	 */
	public String getCurrentOcrDisplayLanguage() {
		return new Locale(currentOcrLanguage, currentOcrLanguage.toLowerCase()).getDisplayLanguage();
	}

	/**
	 * 
	 * @return display TTS language , same as Locale.getDisplayLanguage() is returning
	 */
	public String getCurrentTtsDisplayLanguage() {
		return currentTtsLanguage.getDisplayLanguage();
	}

	/**
	 * 
	 * @return display Device language , same as Locale.getDisplayLanguage() is returning
	 */
	public String getCurrentDeviceDisplayLanguage() {
		return currentDeviceLanguage.getDisplayLanguage();
	}

	/**
	 * Set language to use in ocr.
	 * 
	 * @param currentOcrLanguage
	 *            3 character language code (iso3)
	 */
	public void setCurrentOcrLanguage(String currentOcrLanguage) {
		Log.d(TAG, "Setting new OCR Language : " + currentOcrLanguage);
		this.currentOcrLanguage = currentOcrLanguage;
	}

	/**
	 * Language current used on the device.
	 * 
	 * @return locale current used on the device
	 */
	public Locale getCurrentDeviceLanguage() {
		return currentDeviceLanguage;
	}

	/**
	 * 
	 * @param currentDeviceLanguage
	 *            current used language as locale
	 */
	public void setCurrentDeviceLanguage(Locale currentDeviceLanguage) {
		this.currentDeviceLanguage = currentDeviceLanguage;
	}

	/**
	 * 
	 * @return
	 */
	public int getOcrSpinnerPosition() {
		return ocrSpinnerPosition;
	}

	/**
	 * 
	 * @param ocrSpinnerPosition
	 */
	public void setOcrSpinnerPosition(int ocrSpinnerPosition) {
		Log.d(TAG, "Set OCR Spinner position: " + ocrSpinnerPosition);
		this.ocrSpinnerPosition = ocrSpinnerPosition;
	}

	/**
	 * 
	 * @return
	 */
	public int getTtsSpinnerposition() {
		return ttsSpinnerposition;
	}

	/**
	 * 
	 * @param ttsSpinnerposition
	 */
	public void setTtsSpinnerPosition(int ttsSpinnerposition) {
		Log.d(TAG, "Set TTS Spinner position: " + ttsSpinnerposition);
		this.ttsSpinnerposition = ttsSpinnerposition;
	}

	public void printLocale(Locale locale) {
		Log.d(TAG, "getDisplayLanguage : " + locale.getDisplayLanguage());
//		Log.d(TAG, "getISO3Language : " + locale.getISO3Language());
		Log.d(TAG, "getLanguage : " + locale.getLanguage());
		Log.d(TAG, "getCountry : " + locale.getCountry());
//		Log.d(TAG, "getVariant : " + locale.getVariant());
	}

//	public String getCurrentTtsLanguageFromList() {
//		if (ttsSpinnerposition >= 0 && ttsSpinnerposition <= ttsLanguagesListISO2.size()) {
//			return ttsLanguagesListISO2.get(ttsSpinnerposition);
//		}
//		return "";
//	}

	public List<Locale> getTtsLanguagesListISO2() {
		return ttsLanguagesListISO2;
	}

	/**
	 *
	 * @return list with supported Languages by TTS
	 */


	public void setTtsEngineList(List<TtsEngine> ttsEngineList) {
		this.ttsEngineList = ttsEngineList;
	}

}
