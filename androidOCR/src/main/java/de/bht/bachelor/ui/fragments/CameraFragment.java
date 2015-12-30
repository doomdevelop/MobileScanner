package de.bht.bachelor.ui.fragments;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import de.bht.bachelor.R;
import de.bht.bachelor.activities.CameraActivity;
import de.bht.bachelor.camera.CameraMode;
import de.bht.bachelor.camera.OrientationMode;
import de.bht.bachelor.camera.Preview;
import de.bht.bachelor.ui.CharacterBoxView;

/**
 * Created by and on 24.12.14.
 */
public class CameraFragment  extends Fragment implements View.OnClickListener{
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private LayoutInflater controlInflater = null;
    private Preview mPreview;
    private Button cancel;
    private static final String TAG = CameraFragment.class.getSimpleName();
    private LinearLayout layoutBackground;
    private CharacterBoxView characterBoxView;
    private FrameLayout runOCR;
    private boolean componentsVisibe = true;
    private SensorManager mySensorManager;
    private volatile boolean sensorrunning = false;
    private boolean checkSetupAgain = false;


    public void setResultHandler(Handler resultHandler) {
        this.resultHandler = resultHandler;
    }

    private Handler resultHandler;
    // private boolean isScreenOn = true;
    private String texttString;
    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.camera_fragment, container, false);
        Log.d(TAG," onCreateView()..");
        initLayout();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        this.runOCR.setEnabled(true);
        this.cancel.setEnabled(true);


        if (mPreview != null) {
            mPreview.cleanOcrResultList();
            mPreview.setControllVariablesToDefault();
        }
    }
    @Override
    public View getView(){
        if(super.getView() == null) {
            return view;
        }
        return super.getView();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();


        if (mPreview.getOcr() != null) {
            mPreview.getOcr().clearAndCloseApi();
        }
        mPreview.cancelAllOcrTasks();
        if (this.mPreview != null)
            mPreview.setControllVariablesToDefault();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initLayout(){

        initSurfaceHolder();
        initComponents();
        initCameraPreview();
    }
    private void initSurfaceHolder() {
        surfaceView  = (SurfaceView) getView().findViewById(R.id.camera_surface_view);
        surfaceHolder = surfaceView.getHolder();

        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // SURFACE_TYPE_NORMA
        surfaceView.setOnClickListener(this);
    }

    public void initCameraPreview() {
        if(mPreview != null){
            return;
        }
        Log.d(TAG, "initCameraPreview().....");
        mPreview = new Preview(getActivity(), surfaceHolder);
        mPreview.setResultHandler(resultHandler);
        surfaceHolder.addCallback(mPreview);
        this.cancel.setEnabled(false);
    }
    public void takePicture() {
        mPreview.takePicture();
    }

    /*
     * Init UI components
     */
    private void initComponents() {
//        this.layoutBackground = (LinearLayout) getView().findViewById(R.id.background);
//        layoutBackground.setOnClickListener(this);
        this.cancel = (Button) getView().findViewById(R.id.cancel);
        this.cancel.setOnClickListener(this);
        this.runOCR = (FrameLayout) getView().findViewById(R.id.run_ocr);
        this.runOCR.setOnClickListener(this);
        // this.tessTextFrame = (TessTextFrame) findViewById(R.id.tessTextFrame);
    }
    @Override
    public void onClick(View view) {

        if (view == surfaceView) {
            if (componentsVisibe) {
                componentsVisibe = false;
                cancel.setVisibility(Button.INVISIBLE);
                runOCR.setVisibility(Button.INVISIBLE);
            } else {
                componentsVisibe = true;
                cancel.setVisibility(Button.VISIBLE);
                runOCR.setVisibility(Button.VISIBLE);
            }
        } else {
            if (view == cancel) {
                // take photo
                setEnabledToAllButtons(false);
                mPreview.startAutoFocus(CameraMode.Photo);
            } else if (view.getId() == R.id.run_ocr) {
                // run ocr
                setEnabledToAllButtons(false);
                setOwnerContainerSizeToPreview();
                mPreview.startAutoFocus(CameraMode.Video);
            }
        }
    }


    private CameraActivity getCameraActivity(){
        return  (CameraActivity) getActivity();
    }
    private void setEnabledToAllButtons(boolean value) {
        this.runOCR.setEnabled(value);
        this.cancel.setEnabled(value);
    }

    private void setOwnerContainerSizeToPreview() {
        // TODO: set it with listener when surfaceView has been created
        mPreview.setDisplayHeight(surfaceView.getHeight());
        mPreview.setDisplayWidth(surfaceView.getWidth());
    }

}
