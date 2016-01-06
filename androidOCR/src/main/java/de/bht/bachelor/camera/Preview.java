package de.bht.bachelor.camera;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.WriteFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import de.beutch.bachelorwork.util.file.FileUtil;
import de.bht.bachelor.beans.OcrResult;
import de.bht.bachelor.graphic.transform.ImageProcessing;
import de.bht.bachelor.helper.ActivityType;
import de.bht.bachelor.helper.CameraFlashState;
import de.bht.bachelor.helper.ChangeActivityHelper;
import de.bht.bachelor.helper.MenuSettingHelper;
import de.bht.bachelor.message.ServiceMessenger;
import de.bht.bachelor.ocr.OCR;
import de.bht.bachelor.setting.AppSetting;
import de.bht.bachelor.tasks.OcrTaskResultCallback;
import de.bht.bachelor.tasks.SendFrameToOcrTasc;
import de.bht.bachelor.ui.CharacterBoxView;

/**
 * Custom camera preview class.
 * Supporting video and photo mode to use with ocr.
 *
 * @author and
 */
public class Preview implements SurfaceHolder.Callback {

    private ServiceMessenger serviceMessenger;
    private Handler resultHandler;
    private final Activity context;
    // ************************************** sizes ******************************************
    // the frame size , could be different than size of view (surfaceView) witch is displaying every each frame
    // the values are NEVER FLIP !
    private int frameWidth, frameHeight;

    // the size of frame, if the orientation is changing the values are FLIP w = h, h = w, see : onOrientationChanged()
    private volatile int w = 0, h = 0;
    // ***************************************************************************************
    private static final String TAG = Preview.class.getSimpleName();
    private boolean previewing = false;
    private int previewbpp;// BytesPerPixel
    private final SurfaceHolder surfaceHolder;
    private Camera camera = null;
    private List<SendFrameToOcrTasc> ocrTacks = new ArrayList<SendFrameToOcrTasc>();

    private int buffer;
    /* For video mode, if true the next frame can be set to OCR by starting new SendFrameToOCRTasc */
    private volatile boolean freeFrame = true;
    /*
     * if false the video mode is running OCR in loop till 3 results are saved in the list.
     * if one of the result is good enough,no more calls are needed, the activity is switching
     * Set to t to switch it safely off (no more running ocr in background)
     */
//    private volatile boolean isLocked = false;
    private int bpp, bpl;
    private OCR ocr;
    private int rotate = 0;

    private volatile CameraMode currentCameraMode;
    private volatile boolean camerReleased = true;
    /**
     * ************ draw ***************
     */

    private CharacterBoxView characterBoxView;
    private final Vector<OcrResult> ocrResultList = new Vector<OcrResult>();
    private OrientationMode orientationMode = OrientationMode.LANDSCAPE;
    /* is true till first set up for video mode will be done (like setting buffer for camera) */
    private boolean firstInitVideoCall = true;
    /*
     * if there is a call for new camera focus and ocr task is running,
     * to not call any more ocr task after current running task,
     * set it to true ,
     * call focus in loop
     * wait till task is ready
     */
    private volatile boolean waitForCameraFocus = false;
    private ImageProcessing imageProcessing;
    private ImageView imageView;
    private final static int NUMBER_OF_OCR_RUN = 1;
    private SurfaceView surfaceView;

    public Preview(Activity context, SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
        this.surfaceHolder = surfaceView.getHolder();
        this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.context = context;
        this.surfaceHolder.addCallback(this);
    }

    public void removePrewievCallback() {
        Log.d(TAG, " removePreviewCallback()..");
        if (camera != null && !this.camerReleased) {
            this.firstInitVideoCall = true;

            camera.setPreviewCallback(null);
            camera.setPreviewCallbackWithBuffer(null);
        }
    }

    public void closeCamera(Camera camera) {
        if (camera == null || camerReleased) {
            return;
        }
//        synchronized (camera) {
            Log.d(TAG, "closeCamera()..");
            camerReleased = true;
            surfaceView.destroyDrawingCache();
            camera.stopPreview();
            camera.release();
            this.camera = null;
//        }
    }

    private int getCameraId(Camera camera) {
        int numberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the default camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        Log.d(TAG, "surfaceCreated()...");

        try {
            if (camera != null) {
                closeCamera(this.camera);
            }
            openCamera();
            Log.d(TAG, "seting preview display");
            camera.setPreviewDisplay(holder);

        } catch (IOException exception) {
            closeCamera(camera);
            Log.e(TAG, "Could not det preview", exception);
            // TODO: add more exception handling logic here
        } catch (RuntimeException ex) {

            if (camera != null) {
                closeCamera(camera);
            }
            Log.e(TAG, "Could not det preview", ex);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed()..........");
        /*
         * Surface will be destroyed when we return, so stop the preview.
		 * Because the CameraDevice object is not a shared resource, it's very
		 * important to release it when the activity is paused.
		 */

        try {
            if (camera != null) {
                camera.stopPreview();
                removePrewievCallback();
                closeCamera(camera);
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "Camera is null.", e);
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged()..width: " + width + " height " + height);
        if (camera == null) {
            return;
        }
        onSurfaceChanged(camera);
    }

    public void onSurfaceChanged(Camera camera) {
        Log.d(TAG, "onSurfaceChanged()");
        Camera.Parameters params = camera.getParameters();

        if (previewing) {
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
                setPreviewSize(params);
                getPreviewValues(params);

                setFlashSetiing(params);
                params.setPreviewFrameRate(15);
                params.setPictureFormat(PixelFormat.JPEG);// PixelFormat.JPEG
                buffer = this.frameWidth * this.frameHeight * this.previewbpp / 8;
                Log.d(TAG, "Computed new Buffer: " + buffer);
                setCameraDisplayOrientation(context, getCameraId(camera), camera);

                camera.setParameters(params);
                camera.startPreview();

                previewing = true;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "Could not start preview", e);
            }
        }
    }

    public OrientationMode getOrientationMode() {
        return orientationMode;
    }

    public void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        int devOrient = context.getResources().getConfiguration().orientation;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                Log.d(TAG, "ROTATION_0");
                if (devOrient == Configuration.ORIENTATION_LANDSCAPE) {

                    Log.d(TAG, "devOrient ORIENTATION_LANDSCAPE");
                    this.orientationMode = OrientationMode.LANDSCAPE;
                } else {
                    this.orientationMode = OrientationMode.PORTRAIT;
                    Log.d(TAG, "devOrient PORTRAIT");
                    if (w > h) {
                        int temp = h;
                        h = w;
                        w = temp;
                    }
                }
                break;
            case Surface.ROTATION_90:
                Log.d(TAG, "ROTATION_90");
                degrees = 90;
                if (devOrient == Configuration.ORIENTATION_LANDSCAPE) {
                    this.orientationMode = OrientationMode.LANDSCAPE;
                } else {
                    this.orientationMode = OrientationMode.PORTRAIT;
                }
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                this.orientationMode = OrientationMode.PORTRAIT_UPSIDE_DOWN;
                Log.d(TAG, "ROTATION_180");
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                this.orientationMode = OrientationMode.LANDSCAPE_UPSIDE_DOWN;
                Log.d(TAG, "ROTATION_270");
                break;
        }
        Log.d(TAG, " orientationMode : " + Preview.this.orientationMode.name() + " display rotation: " + degrees);
        getRotateByNewOrientationMode(null);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        camera.setDisplayOrientation(result);
    }


    private void setPreviewSize(Parameters cameraParameters) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Log.d(TAG, "Display size : w " + display.getWidth() + ", h= " + display.getHeight());

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        Log.d(TAG, "DisplayMetrics size : widthPixels " + dm.widthPixels + ", heightPixels= " + dm.heightPixels);
        final List<Camera.Size> sizes = cameraParameters.getSupportedPreviewSizes();
        Collections.sort(sizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                return new Integer(o2.width).compareTo(o1.width);
            }
        });

        for (Camera.Size size : sizes) {
            cameraParameters.setPreviewSize(size.width, size.height);
            camera.setParameters(cameraParameters);
            Log.d(TAG, "attempt preview size:" + size.width + "x" + size.height);
            try {
                camera.startPreview();
                Log.d(TAG, "...accepted - go along");
                this.frameHeight = size.height;
                this.frameWidth = size.width;
                w = frameWidth;
                h = frameHeight;
                Log.d(TAG, "Seting new sizes for frame w: " + frameWidth + ", h: " + frameHeight);
                // ok, camera accepted out settings. since we know,
                // that some implementations may choose different preview format,
                // we retrieve parameters again. just to be sure
                cameraParameters = camera.getParameters();
                camera.stopPreview();
                return;
            } catch (RuntimeException rx) {
                // ups, camera did not like this size
                Log.d(TAG, "...barfed, try next");
            }

        }
    }


    private void getPreviewValues(Parameters params) {
        int previewFormat = params.getPreviewFormat(); // retrieve the Previewformat according to your camera

        PixelFormat pf = new PixelFormat(); // create a PixelFormat object
        PixelFormat.getPixelFormatInfo(previewFormat, pf); // get through the previewFormat-int the PixelFormat

        this.bpp = pf.bytesPerPixel; // save the BytesPerPixel for this Pixelformat
        this.bpl = bpp * this.frameWidth; // BytesPerLines is just the "BPP * width"
        setPreviewbppBpp(pf.bitsPerPixel);
        Log.d(TAG, "bitsPerPixel has value : " + pf.bitsPerPixel);
        Log.d(TAG, "bytesPerPixel has value : " + pf.bytesPerPixel);
    }

    private void setFlashSetiing(Parameters params) {
        CameraFlashState state = MenuSettingHelper.getInstance().getCurrentFlashState();
        String flashValue;

        switch (state) {
            case FLASH_AUTO:
                flashValue = Camera.Parameters.FLASH_MODE_AUTO;
                break;
            case FLASH_ON:
                flashValue = Camera.Parameters.FLASH_MODE_ON;
                break;
            case FLASH_OFF:
                flashValue = Camera.Parameters.FLASH_MODE_OFF;
                break;

            default:
                flashValue = Camera.Parameters.FLASH_MODE_AUTO;
                break;
        }
        params.setFlashMode(flashValue);
    }

    // ---------------------------work with camera -----------------------------
    public void takePicture() {
        Log.d(TAG, "takePicture().....");
        if (camera == null) {
            Log.d(TAG, "opening new camera >>>>>>>>>>");
            openCamera();

            onSurfaceChanged(camera);
        }
        camera.takePicture(null, null, jpegCallback);
    }

    /**
     * Starting auto focus.
     *
     * @param cameraMode 0 = photo mode, 1 = vidoe mode
     * @see CameraMode
     */
    public void startAutoFocus(CameraMode cameraMode) {
        freeFrame = false;
        Log.d(TAG, "startAutoFocus()....cameraMode: " + cameraMode.toString());
        if (camera == null) {
            Log.d(TAG, "opening new camera >>>>>>>>>>");
            openCamera();
            onSurfaceChanged(camera);
            ocrResultList.clear();
            firstInitVideoCall = true;
        }
//        synchronized (camera) {
            if (cameraMode == CameraMode.Video) {

                if (currentCameraMode == CameraMode.Video) {
                /*
                 * if this is not the first time call
				 * wait till task is ready and lock next calls till the new focus
				 */
                    waitForCameraFocus = true;
                }

                Log.d(TAG, "----------------- task is locked -------------");
            }
            if (this.currentCameraMode == CameraMode.Video && cameraMode == CameraMode.Photo) {
                // changing mode from video in to the photo
                cancelAllOcrTasks();
                Log.d(TAG, "changing from video in to photo mode ,set camera to null, call auto focus");
                closeCamera(camera);
                firstInitVideoCall = true;// set it back to correctly initialise next call in video mode
                if (this.characterBoxView != null)
                    this.characterBoxView.cleanView();
            }

            this.currentCameraMode = cameraMode;

            camera.autoFocus(myAutoFocusCallback);
//        }
    }

    public void resetPreview() {

        Log.d(TAG, "resetPreview()...");
//        synchronized (this) {
            if (this.characterBoxView != null) {
                this.characterBoxView.cleanView();
            }
            setControllVariablesToDefault();
//        }
    }

    public boolean isCamerReleased() {
        return camerReleased;
    }

    public void openCamera() {
        Log.d(TAG, "camer is null : " + (camera == null) + ", camerReleased= " + camerReleased);
        if (camerReleased) {
            Log.e(TAG, "open camera()...");
            camera = Camera.open();
            camerReleased = false;
            firstInitVideoCall = true;
        }
    }

    public void clearAndCloseApi() {
        if (getOcr() != null) {
            getOcr().clearAndCloseApi();
        }
    }

    public void cancelAllOcrTasks() {
        if (ocrTacks == null) {
            return;
        }
//        synchronized (ocrTacks) {
            for (SendFrameToOcrTasc ocrTasc : ocrTacks) {
                if (ocrTasc != null && !ocrTasc.isCancelled()) {
                    ocrTasc.cancel(true);
                    Log.d(TAG, "Canceled task " + ocrTasc.getStatus().name());
                }
            }
            ocrTacks.clear();
//        }
    }

    private AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean isOnFocus, Camera arg1) {
            try {
                if (currentCameraMode == CameraMode.Photo) {
                    Log.d(TAG, "onAutoFocus()....picture mode, focus: " + isOnFocus);

                    if (isOnFocus) {
                        getServiceMessenger().sendMessage(1, ServiceMessenger.MSG_TAKE_PICURE, null);// set enable=true to the button and call take photo
                    } else {
                        startAutoFocus(currentCameraMode);
                    }
                } else {
                    if (currentCameraMode == CameraMode.Video) {
                        Log.d(TAG, "onAutoFocus()....video mode, isOnFocus: " + isOnFocus);
                        Log.d(TAG, "firstInitVideoCall. " + firstInitVideoCall + ", freeframe: " + freeFrame + ", waitForCameraFocus: " + waitForCameraFocus);
                        if (!isOnFocus) {
                            Log.d(TAG, "Didn't get Focus, run startAutoFocus() again !");
                            startAutoFocus(currentCameraMode);
                        } else if (!freeFrame && waitForCameraFocus || firstInitVideoCall) {
                            // if during running task (firstInitVideoCall == false) new focus is creating
                            // freeFramwe == false and waitForCameraFocus == true

                            waitForCameraFocus = false;
                            freeFrame = true;
                            // we start new focus, it could be new picture contend,clean the list and collect new  results
                            cleanOcrResultList();

                            if (firstInitVideoCall) {
                                camera.addCallbackBuffer(new byte[buffer]);
                                camera.setPreviewCallbackWithBuffer(previewCallback);
                                firstInitVideoCall = false;
                            }

                            getServiceMessenger().sendMessage(1, ServiceMessenger.MSG_CAMERA_ON_FOUCUS, null);// set enable=true to the button and call take photo
                        } else {
                            startAutoFocus(currentCameraMode);
                        }
                    }
                }
            } catch (NullPointerException ex) {
                Log.d(TAG, "Could not get camera focus", ex);
            }
        }
    };

    /**
     * Using during photo modus : currentCameraMode == CameraMode.Photo
     */
    PictureCallback jpegCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera cam) {
            Log.d(TAG, "onPictureTaken()...");
            camera.startPreview();
            ChangeActivityHelper.getInstance().setRawPicture(data);
            ChangeActivityHelper.getInstance().setOrientationMode(orientationMode);
            getServiceMessenger().sendMessage(0, 1, ActivityType.PICTURE_VIEW);
        }
    };
    /**
     * Using during video modus currentCameraMode == CameraMode.Video
     */
    private PreviewCallback previewCallback = new PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
//            synchronized (this) {

                if (data == null) {
                    Log.d(TAG, "Data has value null, return");
                    camera.addCallbackBuffer(data);
                    return;
                }
                if (!freeFrame) {
                    Log.e(TAG, "new task-call is locked, freeFrame : " + freeFrame);
                    camera.addCallbackBuffer(data);
                    return;
                }
                byte[] rotatedData = null;
                byte[] contrastData = null;

                if (AppSetting.getInstance().isContrastEnhanceActive()) {
                    Pix pix = ImageProcessing.binarizeData(data, contrastData, Preview.this.w, Preview.this.h);
                    imageView.setImageBitmap(WriteFile.writeBitmap(pix));
                    imageView.setVisibility(View.VISIBLE);
                } else {
//                imageView.setVisibility(View.GONE);
                }

                Log.d(TAG, "--------- Callback Preview with Buffer -------------");
                Log.d(TAG, "*******************************************");
                Log.d(TAG, "freeFrame, prepare for new async tasc");
                Log.d(TAG, "*******************************************");
                if (camera == null) {
                    return;
                }
                Camera.Parameters parameters = camera.getParameters();
                int imageFormat = parameters.getPreviewFormat();
                if (imageFormat == ImageFormat.NV21) {
                    Log.d(TAG, "image format is NV21");
                } else {
                    throw new IllegalArgumentException("Wrong data format from camera. Must be NV21, the current format value is " + imageFormat);
                }
                getPreviewValues(parameters);

                Log.d(TAG, "frame width : " + w + ", hight : " + h + ", ocrResultList.size(): " + ocrResultList.size() + " rotate: " + rotate + " orient mode: " + Preview.this.orientationMode.name());

                if (freeFrame && ocrResultList.size() < NUMBER_OF_OCR_RUN) {

                    if (rotate != 0) {
                        Bitmap b = ImageProcessing.convertAndRotate(data, rotate, h, w);
                        imageView.setImageBitmap(b);
                    } else {
                        int[] rgb = new int[data.length];
                        ImageProcessing.decodeYUV420RGB(rgb, data, w, h);
                        imageView.setImageBitmap(Bitmap.createBitmap(rgb, w, h, Bitmap.Config.ARGB_8888));
                    }
                    imageView.setVisibility(View.VISIBLE);
                    SendFrameToOcrTasc sendFrameToOcrTasc = new SendFrameToOcrTasc(w, h, bpp, Preview.this.rotate, context, orientationMode, ocrTaskResultCallback);
                    sendFrameToOcrTasc.execute(contrastData != null ? contrastData : data);
                    ocrTacks.add(sendFrameToOcrTasc);

                } else {
                    Log.d(TAG, "Can not start OCR task ! freeFrame: " + freeFrame + " ocrResultList size: " + ocrResultList.size());
                }

                camera.addCallbackBuffer(data);
            }
//        }
    };

    /*
     * Convert YUV data int to RGB and save as Bitmap
     */
    private void saveOrginalData(byte[] data) {
        FileUtil.saveOrginalData(data, w, h);
    }

    private OcrTaskResultCallback ocrTaskResultCallback = new OcrTaskResultCallback() {
        @Override
        public void onOcrTaskPreExecute() {
//            imageView.setVisibility(View.INVISIBLE);
            freeFrame = false;
        }

        @Override
        public void onOcrTaskResultCallback(OcrResult ocrResult) {
            int w = ocrResult.getW();
            int h = ocrResult.getH();
            if (ocrResult != null) {
                ocrResultList.add(ocrResult);
                if (ocrResult.getRotateValue() != 0) {
                    ocrResult.setLandscape(false);
                } else {
                    ocrResult.setLandscape(true);
                }
            }
            ocrResult.setRotateValue(ocrResult.getRotateValue());
            Vector<Rect> boxes = new Vector<Rect>(ocrResult.getBoxes());
            if (boxes != null) {
                Log.d(TAG, "returning " + boxes.size() + " boxes");
            }

            if (boxes == null || (boxes != null && boxes.isEmpty())) {
                Log.d(TAG, "Could not get the boxes, run again. THREAD ID: " + Thread.currentThread().getId());

                if (!waitForCameraFocus) {
                    freeFrame = true;
                }
                return;
            }
            Log.d(TAG, "THREAD ID: " + Thread.currentThread().getId());
            if (orientationMode == OrientationMode.PORTRAIT || orientationMode == OrientationMode.PORTRAIT_UPSIDE_DOWN) {
                int temp = ocrResult.getW();
                w = ocrResult.getH();
                h = temp;
            }
            if (characterBoxView == null) {
                throw new NullPointerException("CharacterBoxView has value null!");
            }

            characterBoxView.setRotate(ocrResult.getRotateValue());

            characterBoxView.setHeight(h);
            characterBoxView.setWidth(w);
            characterBoxView.setFrameHeight(h);
            characterBoxView.setFrameWidth(w);
            characterBoxView.setRects(new Vector<Rect>(boxes));
            characterBoxView.restoreView();

            if (ocrResultList.size() >= NUMBER_OF_OCR_RUN) {
                OcrResult bestOcrResult = findBestResult();
                if (bestOcrResult != null) {
                    Log.d(TAG, "GET BEST RESULT,WILL FINISHING VIDEO CALLBACK AND START RESULT ACTIVITY !");
                    freeFrame = false;
                    getServiceMessenger().sendMessage(1, ServiceMessenger.MSG_OCR_RESULT, bestOcrResult);
                } else {
                    freeFrame = false;
                    cleanOcrResultList();
                    cancelAllOcrTasks();
//                    imageView.setVisibility(View.GONE);
                }
            }

            getServiceMessenger().sendMessage(0, ServiceMessenger.MSG_OCR_BOX_VIEW, characterBoxView);// set enable=true to the button

            if (!waitForCameraFocus) {
                // is not waiting for camera focus , so can run next task
                freeFrame = false;
            } else {
                Log.d(TAG, "is waiting , do not run a task, give a space to make new focus");
                freeFrame = false;
            }
            Log.d(TAG, "----- return onPostExecute()------");
        }

        @Override
        public void onTrainDataerror() {
            getServiceMessenger().sendMessage(0, 9, null);
        }
    };

    private ServiceMessenger getServiceMessenger(){
        if(serviceMessenger == null){
            serviceMessenger = new ServiceMessenger(resultHandler);
        }
        return serviceMessenger;
    }
    private void getRotateByNewOrientationMode(OrientationMode orientationMode) {
        Log.d(TAG, "-------- getRotateByNewOrientationMode -------- ");
        if (orientationMode == null) {
            switch (this.orientationMode) {
                case LANDSCAPE_UPSIDE_DOWN:
                    rotate = 180;
                    break;
                case PORTRAIT:
                    rotate = 90;
                    break;
                case PORTRAIT_UPSIDE_DOWN:
                    rotate = -90;
                    break;
                case LANDSCAPE:
                    rotate = 0;
            }
            return;
        }
        Log.d(TAG, "getRotateByNewOrientationMode new: " + orientationMode.name + ", old: " + this.orientationMode.name + " rotate: " + rotate);
        if (this.orientationMode == orientationMode) {
            return;
        }


        switch (orientationMode) {
            case LANDSCAPE:
                switch (this.orientationMode) {
                    case LANDSCAPE_UPSIDE_DOWN:
                        rotate = 180;
                        break;
                    case PORTRAIT:
                        rotate = -90;
                        break;
                    case PORTRAIT_UPSIDE_DOWN:
                        rotate = 90;
                        break;
                }
                break;
            case LANDSCAPE_UPSIDE_DOWN:
                switch (this.orientationMode) {
                    case LANDSCAPE:
                        rotate = 180;
                        break;
                    case PORTRAIT:
                        rotate = 90;
                        break;
                    case PORTRAIT_UPSIDE_DOWN:
                        rotate = -90;
                        break;
                }
                break;
            case PORTRAIT:
                switch (this.orientationMode) {
                    case LANDSCAPE:
                        rotate = 90;
                        break;
                    case LANDSCAPE_UPSIDE_DOWN:
                        rotate = -90;
                        break;
                    case PORTRAIT_UPSIDE_DOWN:
                        rotate = 180;
                        break;
                }
                break;
            case PORTRAIT_UPSIDE_DOWN:
                switch (this.orientationMode) {
                    case LANDSCAPE:
                        rotate = -90;
                        break;
                    case LANDSCAPE_UPSIDE_DOWN:
                        rotate = 90;
                        break;
                    case PORTRAIT:
                        rotate = 180;
                        break;
                }
        }
        Log.d(TAG, "---- getRotateByNewOrientationMode ----- END " + orientationMode.name + ", old: " + this.orientationMode.name + " rotate: " + rotate);
    }

    private OcrResult findBestResult() {
        OcrResult ocrResultBest = null;
        for (OcrResult ocrResult : ocrResultList) {
            if (ocrResultBest == null) {
                ocrResultBest = ocrResult;
            } else {
                if (ocrResultBest.getMeanConfidences() < ocrResult.getMeanConfidences() && ocrResult.getResult().length() > 0) {
                    ocrResultBest = ocrResult;
                }
            }
        }
        if (ocrResultBest.getMeanConfidences() < 60 || ocrResultBest.getResult().length() <= 0)
            return null;
        return ocrResultBest;
    }


    /**
     * Control variables are for controlling new task with are calling OCR
     * or for new calls camera focus.
     */
    public void setControllVariablesToDefault() {
        Log.d(TAG, "setControllVariablesToDefault()...");
        freeFrame = false;
        waitForCameraFocus = false;
        firstInitVideoCall = true;
    }

    /*
     * Return true if device is in orientation portrait
     */
    private boolean isOrientationPortrait() {
        return orientationMode == OrientationMode.PORTRAIT || orientationMode == OrientationMode.PORTRAIT_UPSIDE_DOWN;
    }


    /**
     * Clean the List with the Result from OCR calls
     */
    public void cleanOcrResultList() {
        ocrResultList.clear();
    }

    /**
     * @return width of video frame from camera PreviewCallback
     */
    public int getFrameWidth() {
        return frameWidth;
    }

    /**
     * @return height of video frame from camera PreviewCallback
     */
    public int getFrameHeight() {
        return frameHeight;
    }

    /**
     * @param resultHandler Handler to send massage to gui back
     */
    public void setResultHandler(Handler resultHandler) {
        this.resultHandler = resultHandler;
    }

    /**
     * @return Bytes Per Pixel of this preview
     */
    public int getPreviewbppBpp() {
        return previewbpp;
    }

    /**
     * Set Bytes Per Pixel
     *
     * @param previewbpp
     */
    public void setPreviewbppBpp(int previewbpp) {
        this.previewbpp = previewbpp;
    }


    public Camera getCamera() {
        return camera;
    }

    public OCR getOcr() {
        return this.ocr;
    }


    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public void setCharacterBoxView(CharacterBoxView characterBoxView) {
        this.characterBoxView = characterBoxView;
    }
}
