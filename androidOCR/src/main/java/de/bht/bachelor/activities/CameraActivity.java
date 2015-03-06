package de.bht.bachelor.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

import de.bht.bachelor.R;
import de.bht.bachelor.beans.OcrResult;
import de.bht.bachelor.camera.CameraMode;
import de.bht.bachelor.camera.OrientationMode;
import de.bht.bachelor.camera.Preview;
import de.bht.bachelor.exception.NoLanguageOnTtsException;
import de.bht.bachelor.exception.NotInitializedTtsException;
import de.bht.bachelor.helper.ActivityType;
import de.bht.bachelor.language.LanguageManager;
import de.bht.bachelor.manager.AnimationManager;
import de.bht.bachelor.manager.OrientationManger;
import de.bht.bachelor.manager.Preferences;
import de.bht.bachelor.message.ServiceMessenger;
import de.bht.bachelor.setting.AppSetting;
import de.bht.bachelor.tts.TTS;
import de.bht.bachelor.ui.CharacterBoxView;

/**
 * This is the start activity, initialising camera and showing camera preview.
 *
 * @author Andrzej Kozlowski
 */
public class CameraActivity extends MenuCreatorActivity implements OnClickListener, SensorEventListener {

    private Handler resultHandler;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private LayoutInflater controlInflater = null;
    private Preview mPreview;
    private Button cancelBtn;
    private static final String TAG = CameraActivity.class.getSimpleName();
    private LinearLayout layoutBackground;
    private CharacterBoxView characterBoxView;
    private FrameLayout runOCR;
    private Button cameraLens;
    private boolean componentsVisibe = true;
    private SensorManager mySensorManager;
    private volatile boolean sensorrunning = false;
    private boolean checkSetupAgain = false;
    // private boolean isScreenOn = true;
    private String texttString;
    private static final int DIALOG_CAMERA_FOCUS = 10;
    private Dialog focusDialog;
    public static final String OCR_RESULT_EXTRA_KEY = "ocr_result_extra_key";
    private Animation animation;

    private ProgressDialog progressDialog;
    private OrientationEventListener myOrientationEventListener;
    public final static String EXTRA_MESSAGE_ISO3_TO_DOWNLOAD = "de.bht.bachelor.activities.DOWNLOAD_TESS_FOR_CURENT_ISO";

    //SESNORS
    private Sensor sensorGrav = null;
    private Sensor sensorMag = null;
    static final float ALPHA = 0.15f;
    private volatile boolean isPreviewInitialized = false;

    private AnimationManager animationManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageManager languageManager = AppSetting.getInstance().getLanguageManager();//AppSetting.getInstance().initLanguageMenager(this);

        Log.d(TAG, "onCreate()....");
        Log.d(TAG, "THREAD ID: " + Thread.currentThread().getId());
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            restoreInstanceState(savedInstanceState);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        createHandler();
        super.setResultHandler(resultHandler);
        if (!languageManager.checkTraineddataForCurrentLanguage(this)) {
            String currentIso3 = AppSetting.getInstance().getLanguageManager().getCurrentOcrLanguage();
            Intent intent = new Intent(this, CheckLanguageActivity.class);
            intent.putExtra(EXTRA_MESSAGE_ISO3_TO_DOWNLOAD, currentIso3);
            startActivity(intent);
        }
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.camera);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        // Create our Preview view and set it as the content of our activity.
        animationManager = new AnimationManager(this);

        initSurfaceHolder();
        initComponents();
        initCameraPreview();
    }

    private void onDismissDialog(int id) {
        switch (id) {
            case DIALOG_CAMERA_FOCUS:
                if (this.focusDialog != null) {
                    this.focusDialog.dismiss();
                }
                if (this.progressDialog != null) {
                    this.progressDialog.dismiss();
                }
                break;
        }
    }

    private void createHandler() {
        resultHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg == null) {
                    Log.e(TAG, "Could not read message !");
                    return;
                }
                switch (msg.what) {
                    case ServiceMessenger.MSG_TAKE_PICURE:
//                        onDismissDialog(DIALOG_CAMERA_FOCUS);
                        //animation.cancel();
//                        animationManager.stopZoomAnimation();

                        takePicture();
                        Log.d(TAG, "Get message to set enable in to the button");
                        break;
                    case 1:
                        changeActivity(msg.obj, ActivityType.PICTURE_VIEW);// start PhotoActivity
////                        controlInflater = LayoutInflater.from(getBaseContext());
////                        getLayoutInflater().inflate(R.layout.fragment,null,false);
//                        View viewControl = getLayoutInflater().inflate(R.layout.fragment,null,false);
////                        View viewControl = controlInflater.inflate(R.layout.fragment, null);
//                        LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
//                        CameraActivity.this.addContentView(viewControl, layoutParamsControl);
//                         FragmentManager fragmentManager = getSupportFragmentManager();
//                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                        PhotoFragment fragment = new PhotoFragment();
//                        fragmentTransaction.add(R.id.contant, fragment);
//                        fragmentTransaction.commit();

                        break;
                    case ServiceMessenger.MSG_CAMERA_ON_FOUCUS:
//                        onDismissDialog(DIALOG_CAMERA_FOCUS);
                        animationManager.stopZoomAnimation();
                        setEnabledToAllButtons((msg.arg1 == 0) ? false : true);
                        break;
                    case 3:
                        addCharacterOutlinesView((CharacterBoxView) msg.obj);
                        break;
                    case 4:
//                        mPreview.cancelAllOcrTasks();
//                        mPreview.resetPreview();
                        changeActivity(msg.obj, ActivityType.OCR_VIEW);// start OCR Result
                        break;
                    case 8:
                        initCameraPreview();
                        break;
                    case 9:
                        getMessageForNoOcrOnPreview();
                        break;

                    case 10:
                        if (texttString != null) {
                            try {
                                Log.d(TAG, "calling tts again from handler");
                                onDifferentDeviceLanguage();
                                TTS.getInstance().speak(texttString, false);
                                texttString = null;
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
        };
    }

    /*
     * Add boxes to each recognised character on the top of the preview
     */
    private void addCharacterOutlinesView(CharacterBoxView characterBoxView) {
        Log.d(TAG, "THREAD ID: " + Thread.currentThread().getId());
        if (this.characterBoxView == null) {
            addContentView(characterBoxView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

            this.characterBoxView = characterBoxView;
        }
        Log.d(TAG, "surfaceView size : width: " + surfaceView.getWidth() + ", height: " + surfaceView.getHeight());
    }

    private void changeActivity(Object object, String activityTyp) {
        Log.d(TAG, "changeActivity()");
        OcrResult ocrResult;
        if (activityTyp.equals(ActivityType.PICTURE_VIEW)) {
            Intent pixScreen = new Intent(this, PhotoActivity.class);
            startActivity(pixScreen);

            // this.cancelBtn.setEnabled(true);
        } else {
            if (activityTyp.equals(ActivityType.OCR_VIEW)) {
                stopAndClosePreview();
                Log.d(TAG, "starting result activity ");
                ocrResult = (OcrResult) object;
                Preferences.getInstance().putArrayData(Preferences.IMAGE_DATA, ocrResult.getImageData());
                ocrResult.setImageData(null);
                ocrResult.setHasImageData(true);
                Intent resultScreen = new Intent(getApplicationContext(), OcrResultActivity.class);

                resultScreen.putExtra(OCR_RESULT_EXTRA_KEY, ocrResult);
                startActivity(resultScreen);
            }
        }
    }

    private void stopAndClosePreview() {
        Log.e(TAG,"stopAndClosePreview()....");
        if (mPreview != null) {
            mPreview.cancelAllOcrTasks();

            if (mPreview.getOcr() != null) {
                mPreview.getOcr().clearAndCloseApi();

            }
            if(mPreview.getCamera() != null) {
                mPreview.getCamera().setPreviewCallback(null);
            }
            surfaceHolder.removeCallback(mPreview);
            isPreviewInitialized = false;
            mPreview.stopCamera();
//            mPreview.resetPreview();

        }
    }

    private void initSurfaceHolder() {
        surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
        surfaceHolder = surfaceView.getHolder();

        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // SURFACE_TYPE_NORMA
        surfaceView.setOnClickListener(this);
        controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.control, null);
        LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        this.addContentView(viewControl, layoutParamsControl);
    }

    private void initCameraPreview() {
        Log.d(TAG, "initCameraPreview().....");

        mPreview = new Preview(this, surfaceHolder);
        mPreview.setResultHandler(resultHandler);
        surfaceHolder.addCallback(mPreview);
        ImageView img = (ImageView) findViewById(R.id.camera_preview_img);
        mPreview.setImageView(img);
        isPreviewInitialized = true;
    }

    /*
     * Init UI components
     */
    private void initComponents() {
        this.layoutBackground = (LinearLayout) findViewById(R.id.background);
        this.layoutBackground.setOnClickListener(this);
        this.cancelBtn = (Button) findViewById(R.id.cancel);
        this.cancelBtn.setOnClickListener(this);
        this.runOCR = (FrameLayout) findViewById(R.id.run_ocr);
        this.runOCR.setOnClickListener(this);
        this.cameraLens = (Button) findViewById(R.id.camera_lens);
        this.cameraLens.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view == surfaceView) {
            if (componentsVisibe) {
                componentsVisibe = false;
                cancelBtn.setVisibility(Button.INVISIBLE);
                runOCR.setVisibility(Button.INVISIBLE);
            } else {
                componentsVisibe = true;
                cancelBtn.setVisibility(Button.VISIBLE);
                runOCR.setVisibility(Button.VISIBLE);
            }
        } else {
            if (view.getId() == R.id.cancel) {

                mPreview.resetPreview();
                mPreview.cancelAllOcrTasks();
//                mPreview.clearAndCloseApi();
                animationManager.stopZoomAnimation();
                cameraLens.setEnabled(true);
                cancelBtn.setEnabled(false);
            } else if (view.getId() == R.id.camera_lens) {
                if (characterBoxView != null) {
                    this.characterBoxView.resetView();
                }
                cancelBtn.setEnabled(true);
                runOCR.setEnabled(false);
                cameraLens.setEnabled(false);
                setOwnerContainerSizeToPreview();

                mPreview.startAutoFocus(CameraMode.Video);

                animationManager.addAndStartAnimation(cameraLens);
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CAMERA_FOCUS:

                this.progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("camera focus");
//                progressDialog.set
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setIndeterminate(false);
//                progressDialog.setCancelable(true);
                progressDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPreview.resetPreview();
                        setEnabledToAllButtons(true);
                        progressDialog.dismiss();
                    }
                });
                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        mPreview.resetPreview();
                        setEnabledToAllButtons(true);
                    }
                });
                progressDialog.show();

                Window window = progressDialog.getWindow();
                window.setLayout(500, 500);

                window.setGravity(Gravity.BOTTOM);

                return progressDialog;
        }
        return super.onCreateDialog(id);

    }

    private void setEnabledToAllButtons(boolean value) {
        this.runOCR.setEnabled(value);
        this.cancelBtn.setEnabled(value);
    }

    private void setOwnerContainerSizeToPreview() {
        // TODO: set it with listener when surfaceView has been created
        mPreview.setDisplayHeight(surfaceView.getHeight());
        mPreview.setDisplayWidth(surfaceView.getWidth());

    }

    /*
     * take camera picturenew
     */
    private void takePicture() {
        mPreview.takePicture();
    }

    /*
     * The Long click Listener for the User Interface Acoustic Tool Tips
     */
    private void toolTipsToSpeech() {

        this.cancelBtn.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {

                    getVibrator().vibrate(VIBRATE_DURATION);
                    onDifferentDeviceLanguage();
                    TTS.getInstance().speak(getString(R.string.camera), false);
                } catch (NotInitializedTtsException ex) {
                    Log.d(TAG, "geting text from exception : " + ex.getTextToSpeech());
                    texttString = ex.getTextToSpeech();
//                    initTTS();
                } catch (NoLanguageOnTtsException ex) {
                    Log.e(TAG, "TTS has no language");
                } catch (Exception e) {
                    Log.d(TAG, "Could not start tts", e);
                }
                return true;
            }
        });

        this.runOCR.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    getVibrator().vibrate(VIBRATE_DURATION);
                    onDifferentDeviceLanguage();
                    TTS.getInstance().speak(getString(R.string.runOCRLive), false);
                    // setLanguageBack();
                } catch (NotInitializedTtsException ex) {
                    texttString = ex.getTextToSpeech();
//                    initTTS();

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Log.d(TAG, "Could not start tts", e);
                }
                return true;
            }
        });

    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float a,p,r;
        float[] gravSensor;
        switch(event.sensor.getType()){
            case Sensor.TYPE_ORIENTATION:
                boolean workWithOrientationSensor = false;

                float[] gravSensorVals = new float[event.values.length];

                System.arraycopy(event.values, 0, gravSensorVals, 0, 3);
                lowPass(gravSensorVals, gravSensorVals);
                float azimuth = gravSensorVals[0];
                float pitch = gravSensorVals[1];
                float roll = gravSensorVals[2];
                OrientationMode om = OrientationManger.getInstance().calculateOrientationWithSensor(gravSensorVals);
                if (om != null) {
                    this.mPreview.changeOrientation(om);
                } else {
                    Log.e(TAG, "------ onSensorChanged() EVENT  OrientationMode is NULL ");
                }
                Log.d(TAG, "onSensorChanged() TYPE_ORIENTATION azimuth: " + azimuth + ", pitch: " + pitch + ", roll: " + roll);

                break;
            case Sensor.TYPE_MAGNETIC_FIELD :
              gravSensor = new float[event.values.length];
                System.arraycopy(event.values, 0, gravSensor, 0, 3);
                 a = gravSensor[0];
                 p = gravSensor[1];
                 r = gravSensor[2];
                Log.d(TAG, "onSensorChanged() TYPE_MAGNETIC_FIELD azimuth: " + a + ", pitch: " + p + ", roll: " + r);
            case Sensor.TYPE_ACCELEROMETER :
                 gravSensor = new float[event.values.length];
                System.arraycopy(event.values, 0, gravSensor, 0, 3);
                 a = gravSensor[0];
                 p = gravSensor[1];
                 r = gravSensor[2];
                Log.d(TAG, "onSensorChanged() TYPE_ACCELEROMETER azimuth: " + a + ", pitch: " + p + ", roll: " + r);


        }

    }


    protected float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged()  ");
        super.onConfigurationChanged(newConfig);
//        changeOrientation(newConfig);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume..................");
        super.onResume();
        if(!isPreviewInitialized) {
            surfaceHolder.addCallback(mPreview);
            mPreview.cleanOcrResultList();
            mPreview.resetPreview();
        }

        myOrientationEventListener
                = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int arg0) {
//                Log.e(TAG, "------ onSensorChanged() onOrientationChanged() ------ " + arg0);
                final float x = arg0;
//			Log.d(TAG, "onSensorChanged: " + ", x: " + x);
                OrientationMode om = OrientationManger.getInstance().calculateOrientation(x);
                if (om != null) {
                    CameraActivity.this.mPreview.changeOrientation(om);
                } else {
                    Log.e(TAG, "------ onSensorChanged()  OrientationMode is NULL ");
                }

            }
        };
        if (myOrientationEventListener.canDetectOrientation()) {

            myOrientationEventListener.enable();
        }

        this.runOCR.setEnabled(true);
        this.cancelBtn.setEnabled(false);
        this.cameraLens.setEnabled(true);

        toolTipsToSpeech();
        initSensorOrientation();
        initAccelerometerMagneticField();
        ImageView img = (ImageView) findViewById(R.id.camera_preview_img);
        img.setVisibility(View.GONE);
    }

    private void initAccelerometerMagneticField() {
        this.mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = this.mySensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

        if (sensors.size() > 0) {
            sensorGrav = sensors.get(0);
        }
        sensors = mySensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        if (sensors.size() > 0) {
            sensorMag = sensors.get(0);
        }
        mySensorManager.registerListener(this, sensorGrav, SensorManager.SENSOR_DELAY_NORMAL);
        mySensorManager.registerListener(this, sensorMag, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Log.d(TAG, "Back button has bee Pressed");
            isBackPressed = true;
            return super.onKeyDown(keyCode, event);
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy().....isFinishing: " + isFinishing());
        super.onDestroy();
        if (isFinishing() && isBackPressed) {
            if (mPreview.getOcr() != null) {
                mPreview.getOcr().clearAndCloseApi();
                if (!TTS.getInstance().isOnChacking()) {
                    TTS.getInstance().shutdown();

                }
            }
            stopAndClosePreview();
            isBackPressed = false;

        }

        if (this.mPreview != null)
            mPreview.setControllVariablesToDefault();

    }

    /*
     * Orientation Sensor needed for OCR so the tesseract can work on Landscape and Portrait orientation
     * The UI-application is ignoring screen changing, working just in Landscape.
     */
    private void initSensorOrientation() {
        if (sensorrunning) {
            Log.d(TAG, "sensor is running , unregister first");
            unregisterListener();
            // return;
        }

        Log.d(TAG, "init sensor manager");
        if (mySensorManager == null) {
            this.mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        }
        List<Sensor> mySensors = this.mySensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        if (mySensors.size() > 0) {
            mySensorManager.registerListener(this, mySensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
            sensorrunning = true;
        }

    }

    private void unregisterListener() {
        Log.d(TAG, "unregister Listener by mySensorManager");
        if (mySensorManager == null) {
            this.mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }
        if (sensorrunning) {
            mySensorManager.unregisterListener(this);
            sensorrunning = false;
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause().....");
        super.onPause();
        if (sensorGrav != null && sensorMag != null) {
            mySensorManager.unregisterListener(this, sensorGrav);
            mySensorManager.unregisterListener(this, sensorMag);

        }
        if (myOrientationEventListener != null) {
            myOrientationEventListener.disable();
        }
        if (sensorrunning) {
            unregisterListener();
            OrientationManger.getInstance().reset();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onSaveInstanceState().....");
        super.onSaveInstanceState(savedInstanceState);
        // unregisterListener();

        if (!((PowerManager) getSystemService(Context.POWER_SERVICE)).isScreenOn()) {
            Log.d(TAG, "----------------- the Screen is not On------------------");
            TTS.getInstance().shutdown();
            isScreenOn = false;
        }
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putBoolean("sensorrunning", sensorrunning);
        savedInstanceState.putBoolean("checkSetupAgain", checkSetupAgain);
        savedInstanceState.putBoolean("componentsVisibe", componentsVisibe);
        savedInstanceState.putBoolean("isScreenOn", isScreenOn);
        savedInstanceState.putBoolean("isReceiveRegistered", isReceiveRegistered);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "restoreInstanceState().....");

        sensorrunning = savedInstanceState.getBoolean("sensorrunning");
        checkSetupAgain = savedInstanceState.getBoolean("checkSetupAgain");
        componentsVisibe = savedInstanceState.getBoolean("componentsVisibe");
        isScreenOn = savedInstanceState.getBoolean("isScreenOn");
        isReceiveRegistered = savedInstanceState.getBoolean("isReceiveRegistered");
        if (!isScreenOn) {
            Log.d(TAG, "----------------- the Screen is not On------------------");
            TTS.getInstance().shutdown();
            isScreenOn = false;
            setLanguageToTTS();
        }
    }


    private void getMessageForNoOcrOnPreview() {
        try {
            LanguageManager languageManager = AppSetting.getInstance().getLanguageManager();
            String dataName = languageManager.getTraineddataNameByLanguage(this, languageManager.getCurrentOcrLanguage(), false);
            String message = "Kann ocr nicht starten , data: " + dataName + " nicht verhanden";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Log.e(TAG, "Could not get message", ex);
        }
    }
}