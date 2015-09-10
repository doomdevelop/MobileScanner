package de.bht.bachelor.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.LinearLayout;

import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.bht.bachelor.R;
import de.bht.bachelor.beans.TtsEngine;
import de.bht.bachelor.exception.LanguageNotSupportedException;
import de.bht.bachelor.language.LanguageManager;
import de.bht.bachelor.manager.Preferences;
import de.bht.bachelor.setting.AppSetting;
import de.bht.bachelor.tts.TTS;
import de.bht.bachelor.ui.ScanView;

/**
 * Created by and on 11.04.15.
 */
public class InitActivity extends Activity {

    private static final String TAG = InitActivity.class.getSimpleName();
    protected final int MY_DATA_CHECK_CODE = 0;
    protected final int MY_DATA_INSTALL_CODE = -1;
    private LanguageManager languageManager;
    public static final String availableVoices = "availableVoices";
    private      TextToSpeech tts = null;
   private boolean ttsDDataInstallWasRunning = true;
    private ScanView scanView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.init_activity);
//        startCheckTTS();
        languageManager = AppSetting.getInstance().getLanguageManager();
        LinearLayout scanPaent = (LinearLayout) findViewById(R.id.init_parent_scaner);
        scanView = new ScanView(getApplicationContext());
        scanPaent.addView(scanView);
        startCheckTTS();
    }

    private void startCheckTTS() {
        getEngines();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()...");
        TTS.getInstance().shutdown();
    }


    private void startInstallTTSDataIntent() {
        Log.d(TAG, "startInstallTTSDataIntent");
        Intent installIntent = new Intent();
        installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        startActivityForResult(installIntent, MY_DATA_INSTALL_CODE);
    }

    private void startCameraActivity() {
        Log.d(TAG, "startCameraActivity");
        Intent camera = new Intent(this, CameraActivity.class);
        startActivity(camera);
        finish();
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d(TAG, "onActivityResult()...............");
//        super.onActivityResult(requestCode, resultCode, data);
//        boolean ttsDDataInstallWasRunning = true;
//        if (requestCode == MY_DATA_CHECK_CODE) {
//            ttsDDataInstallWasRunning = Preferences.getInstance().getBoolean(Preferences.TTS_CHECKED_KEY, false);
//            Preferences.getInstance().putBoolean(Preferences.TTS_CHECKED_KEY, true);
//            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
//                Log.d(TAG, "Success, create the TTS instanc");
//                if (!TTS.getInstance().isApiChecked() || !TTS.getInstance().isInitialized()) {
//                    languageManager = AppSetting.getInstance().getLanguageManager();
//                    /*
//                     * the language manger is ready, we can start check
//					 * and if there is a need to download tessdata files
//					 * start check Activity CheckLanguageActivity checkFiles()
//					 */
//
//                    TTS.getInstance().setApiChecked(true);
//                    TTS.getInstance().setOnChacking(false);
//
//                    ArrayList<String> availableLanguages = data.getStringArrayListExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES);
//                    ArrayList<String> unavailableLanguages = data.getStringArrayListExtra(TextToSpeech.Engine.EXTRA_UNAVAILABLE_VOICES);
//                    for(String s : unavailableLanguages){
//                        Log.d(TAG,"unavailableLanguage: "+s);
//                    }
//                    Log.d(TAG," ************************ ");
//                    for(String s : availableLanguages){
//                        Log.d(TAG,"availableLanguages: "+s);
//                    }
//                    Log.d(TAG, "availableLanguages: " + availableLanguages.size() + " ttsDDataInstallWasRunning : " + ttsDDataInstallWasRunning);
//                    if (availableLanguages.isEmpty() && !ttsDDataInstallWasRunning) {
//                        startInstallTTSDataIntent();
//                    } else {
//                        startCameraActivity();
//                    }
//                }
//            } else {
//
//                Log.d(TAG, "missing tessdata, install it");
//                // TODO: info to user , need internet connecton
//                Intent installIntent = new Intent();
//                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
//                startActivityForResult(installIntent, MY_DATA_INSTALL_CODE);
//            }
//        } else if (requestCode == MY_DATA_INSTALL_CODE) {
//            Log.d(TAG, "MY_DATA_INSTALL_CODE : result : " + resultCode);
//            startCheckTTS();
//        }
//    }



// Usage within an Activity - Debugging only!

    private ArrayList<TtsEngine> ttsEngineList;
    private int requestCount;

    private void getEngines() {

        requestCount = 0;

        final Intent ttsIntent = new Intent();
        ttsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);

        final PackageManager pm = getPackageManager();

        final List<ResolveInfo> list = pm.queryIntentActivities(ttsIntent, PackageManager.GET_META_DATA);

        ttsEngineList = new ArrayList<TtsEngine>(list.size());

        for (int i = 0; i < list.size(); i++) {

            final TtsEngine cve = new TtsEngine();

            cve.setLabel(list.get(i).loadLabel(pm).toString());

            cve.setPackageName(list.get(i).activityInfo.applicationInfo.packageName);

            final Intent getIntent = new Intent();
            getIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);

            getIntent.setPackage(cve.getPackageName());
            getIntent.getStringArrayListExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES);
            getIntent.getStringArrayListExtra(TextToSpeech.Engine.EXTRA_UNAVAILABLE_VOICES);

            cve.setIntent(getIntent);

            ttsEngineList.add(cve);
        }

        Log.d(TAG, "ttsEngineList: " + ttsEngineList.size());

        for (int i = 0; i < ttsEngineList.size(); i++) {
            startActivityForResult(ttsEngineList.get(i).getIntent(), i);
        }

    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.i(TAG, "onActivityResult: requestCount: " + " - requestCode: " + requestCode);

         if (requestCode == MY_DATA_INSTALL_CODE) {
            Log.d(TAG, "MY_DATA_INSTALL_CODE : result : " + resultCode);
            startCheckTTS();
             return;
        }else  if (requestCode != requestCount) {
             return;
         }

        requestCount++;

        try {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                ttsDDataInstallWasRunning = Preferences.getInstance().getBoolean(Preferences.TTS_CHECKED_KEY, false);
                Preferences.getInstance().putBoolean(Preferences.TTS_CHECKED_KEY, true);
            }
            if (data != null) {

                final Bundle bundle = data.getExtras();

                if (bundle != null) {

                    Log.d(TAG, ttsEngineList.get(requestCode).getLabel() + " - Bundle Data");
                    final Set<String> keys = bundle.keySet();
                    final Iterator<String> it = keys.iterator();

                    while (it.hasNext()) {
                        final String key = it.next();

                        Log.d(TAG, "Key: " + key + " = " + bundle.get(key));
                    }

                }

                if (data.hasExtra("availableVoices")) {
                    ttsEngineList.get(requestCode).setAvailableLanguages(data.getStringArrayListExtra("availableVoices"));
                } else {
                    ttsEngineList.get(requestCode).setAvailableLanguages(new ArrayList<String>());
                }
                if (data.hasExtra("unavailableVoices")) {
                    ttsEngineList.get(requestCode).setUnavailableLanguages(data.getStringArrayListExtra("unavailableVoices"));
                } else {
                    ttsEngineList.get(requestCode).setUnavailableLanguages(new ArrayList<String>());
                }
            }
            Log.d(TAG, "requestCount: " + requestCount + " ttsEngineList size: " + ttsEngineList.size());
            if (requestCount == ttsEngineList.size()) {
                if(ttsEngineList.size()==0){
                    startInstallTTSDataIntent();
                    return;
                }
                if (Preferences.getInstance().getBoolean(Preferences.TTS_CHECKED_KEY, false)) {
                    TTS.getInstance().setApiChecked(true);
                    TTS.getInstance().setOnChacking(false);
                }

                languageManager.setTtsEngineList(ttsEngineList);

                for (int i = 0; i < ttsEngineList.size(); i++) {

                    Log.v(TAG, "cve: " + ttsEngineList.get(i).getLabel() + " - "
                            + ttsEngineList.get(i).getAvailableLanguages().size() + " - " + ttsEngineList.get(i).getAvailableLanguages().toString()+" - "+ttsEngineList.get(i).getPackageName());

                }
                this.tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if (i == TextToSpeech.SUCCESS) {
                            String currentEngine = tts.getDefaultEngine();
                            Log.d(TAG,"  currentEngine:" +currentEngine+ " is on device: "+  languageManager.getTtsEngineByPackageName(currentEngine));
                            TtsEngine currenTtsEngine = languageManager.getTtsEngineByPackageName(currentEngine);
                            if(currenTtsEngine != null){

                                languageManager.initLanguageManagerForEngine(currenTtsEngine);
                                String language= "eng_GBR";
                                try {
//                                    TTS.getInstance().init(tts,languageManager.getCurrentTtsLanguage());
                                    TTS.getInstance().init(tts,new Locale(language));
                                } catch (LanguageNotSupportedException e) {
                                    Log.e(TAG, "Could not inti TTS with language: " + language);
                                }
                                if(currenTtsEngine.getAvailableLanguages().size()>0) {
                                    startCameraActivity();
                                }else{
                                    if(ttsDDataInstallWasRunning){
                                        startCameraActivity();
                                    }else {
                                        startInstallTTSDataIntent();
                                    }
                                }
                            }

                        }else if(i == TextToSpeech.ERROR){
                            confirmDialog();
                        }
                    }
                });

            }

        } catch (final IndexOutOfBoundsException e) {
            Log.e(TAG, "IndexOutOfBoundsException", e);
            e.printStackTrace();
        } catch (final NullPointerException e) {
            Log.e(TAG, "NullPointerException", e);
            e.printStackTrace();
        } catch (final Exception e) {
            Log.e(TAG, "Exception", e);
            e.printStackTrace();
        }
    }
    private void confirmDialog(){
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("Install recommeded speech engine?");
        d.setMessage("Your device isn't using the recommended speech engine. Do you wish to install it?");

        d.setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                startInstallTTSDataIntent();
                dialog.dismiss();
            }
        });
        d.setNegativeButton("No, later", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {


                dialog.dismiss();
            }
        });
        d.show();
    }

}

//0 21:23:55.075  28354-28354/? D/InitActivity﹕ onActivityResult()...............
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ Success, create the TTS instanc
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ unavailableLanguage: eng-USA
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ unavailableLanguage: fra-FRA
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ unavailableLanguage: ita-ITA
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ unavailableLanguage: deu-DEU
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ unavailableLanguage: eng-GBR
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ unavailableLanguage: spa-ESP
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ unavailableLanguage: rus-RUS
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ unavailableLanguage: por-BRA
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ unavailableLanguage: kor-KOR
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ unavailableLanguage: spa-MEX
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ ************************
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ availableLanguages: eng-USA
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ availableLanguages: eng-GBR
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ availableLanguages: spa-ESP
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ availableLanguages: fra-FRA
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ availableLanguages: deu-DEU
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ availableLanguages: ita-ITA
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ availableLanguages: kor-KOR
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ availableLanguages: zho-CHN
//        06-10 21:23:55.330  28354-28354/? D/InitActivity﹕ availableLanguages: 8 ttsDDataIn
