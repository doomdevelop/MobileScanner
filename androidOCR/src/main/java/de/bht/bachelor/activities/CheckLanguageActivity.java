package de.bht.bachelor.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import de.bht.bachelor.R;
import de.bht.bachelor.helper.ChangeActivityHelper;
import de.bht.bachelor.helper.NetworkHelper;
import de.bht.bachelor.setting.AppSetting;
import de.bht.bachelor.tasks.DownloadOcrTrainedDataTask;

public class CheckLanguageActivity extends Activity {


    private boolean checkSetupAgain = false;
    private DownloadOcrTrainedDataTask downloadOcrTessdata;
    private ProgressDialog mProgressDialog;
    private String runningBy = null;
    private static final String TAG = CheckLanguageActivity.class.getSimpleName();
    private Handler resultHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()....");
        super.onCreate(savedInstanceState);
        createHandler();
        String extraDownload = getIntent().getStringExtra(CameraActivity.EXTRA_MESSAGE_ISO3_TO_DOWNLOAD);
        if (extraDownload != null) {
            Log.d(TAG, "will start download for iso : " + extraDownload);
            AppSetting.getInstance().getLanguageManager().startDownloadTraineddata(this, resultHandler, extraDownload);
        } else {
            if (runningByAppSetup()) {
                checkSetup();
            } else {
                AppSetting.getInstance().getLanguageManager().startDownloadTraineddata(this, resultHandler, ChangeActivityHelper.getInstance().getOnLanguageChangeBean().getNewIso3Language());
            }
        }
    }

    public String getRunningBy() {
        return runningBy;
    }

    public void setRunningBy(String runningBy) {
        this.runningBy = runningBy;
    }

    private void createHandler() {
        resultHandler = new MyHandler(this);
    }

    private static class MyHandler extends Handler {
        CheckLanguageActivity checkLanguageActivity;

        MyHandler(CheckLanguageActivity ac) {
            checkLanguageActivity = ac;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg == null) {
                Log.e(TAG, "Could not read message !");
                return;
            }
            switch (msg.what) {
                case 5:
                    // calling from MenuCreatorActivity, LanguageManger has been initialised
                    checkLanguageActivity.checkSetup();
                    break;
                case 6:
                    if (msg.arg1 == DownloadOcrTrainedDataTask.DIALOG_DOWNLOAD_PROGRESS) {
                        /*
						 * the dialog will be created, see : onCreateDialog(int id)
						 * called by DownloadOcrTessdata.onPreExecute()
						 */
                        checkLanguageActivity.setDownloadOcrTessdata((DownloadOcrTrainedDataTask) msg.obj);
                        checkLanguageActivity.showDialog(DownloadOcrTrainedDataTask.DIALOG_DOWNLOAD_PROGRESS);
                    } else {
						/*
						 * Called wenn download finish by DownloadOcrTessdata.onPostExecute
						 */
                        if (msg.obj != null && msg.obj instanceof Exception) {
                            Toast.makeText(checkLanguageActivity.getApplicationContext(), ((Exception) msg.obj).getMessage(), Toast.LENGTH_LONG).show();
                            checkLanguageActivity.dismissDialog(DownloadOcrTrainedDataTask.DIALOG_DOWNLOAD_PROGRESS);
                            if (checkLanguageActivity.getRunningBy() != null && checkLanguageActivity.getRunningBy().equals(ChangeActivityHelper.RUN_LANGUAGE_SETUP)) {
                                checkLanguageActivity.onNoInternetConnection();
                                ChangeActivityHelper.getInstance().getOnLanguageChangeBean().setStatusAfterDownload(NetworkHelper.DOWNLOAD_NOT_SUCCESSFULLY);
                            }
                        }
                        if (msg.arg1 == NetworkHelper.NO_INTERNET_CONECTION)
                            if (msg.obj == null)
                                checkLanguageActivity.onNoInternetConnection(null);
                            else if (msg.obj instanceof String) {
                                // by UnknownHostException
                                checkLanguageActivity.onNoInternetConnection((String) msg.obj);
                            }

                        if (msg.arg1 == NetworkHelper.DOWNLOAD_SUCCESSFULLY) {
                            checkLanguageActivity.dismissDialog(DownloadOcrTrainedDataTask.DIALOG_DOWNLOAD_PROGRESS);
                            if (checkLanguageActivity.getResources() != null && checkLanguageActivity.getRunningBy().equals(ChangeActivityHelper.RUN_LANGUAGE_SETUP))
                                ChangeActivityHelper.getInstance().getOnLanguageChangeBean().setStatusAfterDownload(NetworkHelper.DOWNLOAD_SUCCESSFULLY);
                            checkLanguageActivity.finish();
                        }
                        if (msg.arg1 == NetworkHelper.NOTHING_TO_DO) {
                            // ChangeActivityHelper.getInstance().getOnLanguageChangeBean().setStatusAfterDownload(NetworkHelper.DOWNLOAD_SUCCESSFULLY);
                            checkLanguageActivity.finish();
                        }
                    }
                    break;
                case 7:
                    if (msg.arg1 == NetworkHelper.RUN_WLAN_Setting) {
                        checkLanguageActivity.runWLANSetting();
                    }
                    if (msg.arg1 == NetworkHelper.RUN_ROAMING_Setting) {
                        checkLanguageActivity.runROAMINGSetting();
                    }
                    if (msg.arg1 == NetworkHelper.CANCELL_DOWNLOAD) {
                        if (checkLanguageActivity.getRunningBy() != null && checkLanguageActivity.getRunningBy().equals(ChangeActivityHelper.RUN_LANGUAGE_SETUP)) {
                            ChangeActivityHelper.getInstance().getOnLanguageChangeBean().setStatusAfterDownload(NetworkHelper.CANCELL_DOWNLOAD);
                            checkLanguageActivity.finish();
                        }
                        // Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }


    private void checkSetup() {
        AppSetting.getInstance().getLanguageManager().checkSetup(this, resultHandler);
    }

    public void setDownloadOcrTessdata(DownloadOcrTrainedDataTask downloadOcrTessdata) {
        this.downloadOcrTessdata = downloadOcrTessdata;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DownloadOcrTrainedDataTask.DIALOG_DOWNLOAD_PROGRESS:
                if (downloadOcrTessdata != null) {
                    mProgressDialog = new ProgressDialog(this);
                    mProgressDialog.setMessage("Downloading file..");
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setCancelable(false);
                    // set dialog to the tack
                    downloadOcrTessdata.setmProgressDialog(mProgressDialog, getString(R.string.downloadDialogText));
                    mProgressDialog.show();
                    return mProgressDialog;
                }
            default:
                return null;
        }
    }

    // ***************************************** NETWORK ***********************************************************

    /*
     * Starting user dialog to set up Internet connection (WLAN or ROAMING)
     */
    private void onNoInternetConnection() {
        NetworkHelper.onNoConnectionDialog(this, "Keine Internet Verbindung", resultHandler);
    }

    /*
     * Start intent with WLAN setting
     */
    private void runWLANSetting() {
        startActivity(NetworkHelper.getWlanSettingIntent());
        checkSetupAgain = true;
    }

    /*
     * Start intent with ROAMING setting
     */
    private void runROAMINGSetting() {
        startActivity(NetworkHelper.getRoamingSettingIntent());
    }

    /*
     * Starting user dialog to set up Internet connection (WLAN or ROAMING)
     */
    private void onNoInternetConnection(String message) {
        if (message == null)
            NetworkHelper.onNoConnectionDialog(this, "Keine Internet Verbindung", resultHandler);
        else
            NetworkHelper.onNoConnectionDialog(this, message, resultHandler);
    }

    // ****************************************************************************************************************
	/*
	 * return true if this Activity started by App setup
	 */
    private boolean runningByAppSetup() {
        Intent i = getIntent();
        runningBy = i.getStringExtra(ChangeActivityHelper.RUN_CHECK_SETUP);
        if (runningBy == null)
            return false;
        return runningBy.equals(ChangeActivityHelper.RUN_CHECK_SETUP);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkSetupAgain) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, "Sleep is interrupted", e);
            }
            checkSetup();
        }
    }

}
