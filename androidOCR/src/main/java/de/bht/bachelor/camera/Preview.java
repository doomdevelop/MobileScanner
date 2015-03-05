package de.bht.bachelor.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
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
import de.bht.bachelor.activities.CameraActivity;
import de.bht.bachelor.beans.OcrResult;
import de.bht.bachelor.graphic.transform.ImageProcessing;
import de.bht.bachelor.helper.ActivityType;
import de.bht.bachelor.helper.CameraFlashState;
import de.bht.bachelor.helper.ChangeActivityHelper;
import de.bht.bachelor.helper.MenuSettingHelper;
import de.bht.bachelor.language.LanguageManager;
import de.bht.bachelor.manager.OrientationManger;
import de.bht.bachelor.message.ServiceMessenger;
import de.bht.bachelor.ocr.OCR;
import de.bht.bachelor.setting.AppSetting;
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
    private final Context context;
    // ************************************** sizes ******************************************
    // the frame size , could be different than size of view (surfaceView) witch is displaying every each frame
    // the values are NEVER FLIP !
    private int frameWidth, frameHeight;
    // the display size , could be different than size of frame, the values are NEVER FLIP !
    private int displayWidth, displayHeight;
    // the size of frame, if the orientation is changing the values are FLIP w = h, h = w, see : onOrientationChanged()
    private volatile int w = 0, h = 0;
    // ***************************************************************************************
    private static final String TAG = Preview.class.getSimpleName();
    private boolean previewing = false;
    private int previewbpp;// BytesPerPixel
    private final SurfaceHolder surfaceHolder;
    private Camera camera;
    private List<SendFrameToOCRTasc> ocrTacks = new ArrayList<SendFrameToOCRTasc>();

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

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    private ImageView imageView;


    public Preview(Context context, SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        this.context = context;
    }

    public void closeCamera(Camera camera) {
        synchronized (camera) {
            Log.d(TAG, "closeCamera()..");
            camera.stopPreview();
            camera.release();
            camera = null;
        }
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
            camera = Camera.open();

            Log.d(TAG, "seting preview display");
            camera.setPreviewDisplay(holder);
            firstInitVideoCall = true;

        } catch (IOException exception) {
            synchronized (camera) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
            Log.e(TAG, "Could not det preview", exception);
            // TODO: add more exception handling logic here
        } catch (RuntimeException ex) {

            if (camera != null) {

                synchronized (camera) {
                    camera.stopPreview();

                    camera.release();
                }
                camera = null;
            }
            Log.e(TAG, "Could not det preview", ex);
//            if (serviceMessenger == null && resultHandler != null)
//                serviceMessenger = new ServiceMessenger(resultHandler);
//            if (serviceMessenger != null)
//                serviceMessenger.sendMessage(0, 8, null);
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
                synchronized (camera) {
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
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

    private void onSurfaceChanged(Camera camera) {
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
                // params.setPictureFormat(ImageFormat.NV21)
                params.setPictureFormat(PixelFormat.JPEG);// PixelFormat.JPEG
                // camera.setParameters(params);
                buffer = this.frameWidth * this.frameHeight * this.previewbpp / 8;
                Log.d(TAG, "Computed new Buffer: " + buffer);

                camera.setParameters(params);
                camera.startPreview();

                previewing = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "Could not start preview", e);
            }
        }
    }

    /*
     * http://stackoverflow.com/questions/8566008/setting-camera-preview-buffer-size
     */
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
            camera = Camera.open();

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
            camera = Camera.open();
            onSurfaceChanged(camera);
            ocrResultList.clear();
            firstInitVideoCall = true;
        }
        synchronized (camera) {
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
//            isLocked = true;
                Log.d(TAG, "changing from video in to photo mode ,set camera to null, call auto focus");
                camera.stopPreview();
                camera.release();
                camera = null;
                firstInitVideoCall = true;// set it back to correctly initialise next call in video mode
                if (this.characterBoxView != null)
                    this.characterBoxView.cleanView();
            }

            // this.isMakingPhoto = isMakingPhoto;
            this.currentCameraMode = cameraMode;


            camera.autoFocus(myAutoFocusCallback);
        }
    }

    public void resetPreview() {

        Log.d(TAG, "resetPreview()...");
        synchronized (this) {
            if (this.characterBoxView != null) {
                this.characterBoxView.cleanView();
            }
            if (ocrResultList != null) {
                ocrResultList.clear();
            }
            rotate = 0;
            orientationMode = null;
            setControllVariablesToDefault();
        }
    }


    public void stopCamera() {
        synchronized (this) {
            if (camera == null) {
                return;
            }
            camera.stopPreview();
            camera.cancelAutoFocus();
            camera.release();
            camera = null;
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
        synchronized (this) {
            for (SendFrameToOCRTasc ocrTasc : ocrTacks) {
                if (ocrTasc != null && !ocrTasc.isCancelled()) {
                    ocrTasc.cancel(true);
                }
            }
            ocrTacks.clear();
        }
    }

    private void stopVideoCallback() {
        camera.setPreviewCallbackWithBuffer(null);
    }

    private AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean isOnFocus, Camera arg1) {
            // buttonTakePicture.setEnabled(true); 701352
            try {
                if (currentCameraMode == CameraMode.Photo) {
                    Log.d(TAG, "onAutoFocus()....picture mode, focus: " + isOnFocus);

                    if (isOnFocus) {
                        serviceMessenger = new ServiceMessenger(resultHandler);
                        serviceMessenger.sendMessage(1, ServiceMessenger.MSG_TAKE_PICURE, null);// set enable=true to the button and call take photo
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
                            // we start new focus, it could be new picture contend,clean the list and collect new 3 results
                            cleanOcrResultList();

                            if (firstInitVideoCall) {
                                camera.addCallbackBuffer(new byte[buffer]);
                                camera.setPreviewCallbackWithBuffer(previewCallback);


                                firstInitVideoCall = false;
                            }

                            serviceMessenger = new ServiceMessenger(resultHandler);
                            serviceMessenger.sendMessage(1, ServiceMessenger.MSG_CAMERA_ON_FOUCUS, null);// set enable=true to the button and call take photo
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
            serviceMessenger = new ServiceMessenger(resultHandler);
            serviceMessenger.sendMessage(0, 1, ActivityType.PICTURE_VIEW);
//            final BitmapFactory.Options opts = new BitmapFactory.Options();
//            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
//            Bitmap currentOrgBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
//            imageView.setImageBitmap(currentOrgBitmap);
//            imageView.setVisibility(View.VISIBLE);
//            Log.d(TAG, "stoping preview....");
//            camera.stopPreview();
//            camera.release();
//            camera = null;
        }
    };
    /**
     * Using during video modus currentCameraMode == CameraMode.Video
     */
    private PreviewCallback previewCallback = new PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            synchronized (this) {

                if (data == null) {
                    Log.d(TAG, "Data has value null, return");
                    return;
                }
                if (!freeFrame) {
                    Log.e(TAG, "new task-call is locked, freeFrame : " + freeFrame);
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
                boolean rotate = false;
                changeOrientation(OrientationManger.getInstance().getCurrentOrientationMode());
                if (isOrientationPortrait()) {
                    rotate = true;
                    Log.d(TAG, "Orientation is Portrait");

                    if (h < w)
                        onOrientationChanged();
                } else {
                    if (h > w)
                        onOrientationChanged();
                }
                getPreviewValues(parameters);

                Log.d(TAG, "frame width : " + w + ", hight : " + h);
                if (freeFrame && ocrResultList.size() < 3) {

                    SendFrameToOCRTasc sendFrameToOCRTasc = new SendFrameToOCRTasc(w, h, rotate, Preview.this.rotate, orientationMode);
                    sendFrameToOCRTasc.execute(contrastData != null ? contrastData : data);
                    ocrTacks.add(sendFrameToOCRTasc);

                } else {
                    Log.d(TAG, "Can not start OCR task ! freeFrame: " + freeFrame + " ocrResultList size: " + ocrResultList.size());
                }

                camera.addCallbackBuffer(data);
            }
        }
    };

    /*
     * Convert YUV data int to RGB and save as Bitmap
     */
    private void saveOrginalData(byte[] data) {
        FileUtil.saveOrginalData(data, w, h);
    }


    /*
     * Calling OCR with the picture frame as byte[],
     * getting result with boxes with are add in to the preview.
     */
    private class SendFrameToOCRTasc extends AsyncTask<byte[], Void, Vector<Rect>> {
        private final boolean mustRotate;
        private final int rotateValue;
        private final OrientationMode om;

        public SendFrameToOCRTasc(int w, int h, final boolean mustRotate, final int rotateValue, final OrientationMode om) {
            this.mustRotate = mustRotate;
            this.rotateValue = rotateValue;
            this.om = om;
//            imageView.setVisibility(View.INVISIBLE);
            if (Preview.this.isOrientationPortrait()) {
                this.w = h;
                this.h = w;
                Log.d(TAG, "imageView size w: " + imageView.getWidth() + ", h: " + imageView.getHeight());
            } else {
                this.w = w;
                this.h = h;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            freeFrame = false;
            Log.d(TAG, "onPreExecute()...");
        }

        @Override
        protected Vector<Rect> doInBackground(byte[]... arg0) {
            Log.d(TAG, "----- doInBackground() ---- ");
            Vector<Rect> boxes = null;
            OcrResult ocrResult = null;
            Pix pix = null;

            if (arg0[0] == null) {
                Log.e(TAG, "raw picture data is null !");
                return null;
            }
            byte[] data = new byte[arg0[0].length];
            System.arraycopy(arg0[0], 0, data, 0, arg0[0].length);

            if (isCancelled()) {
                return null;
            }
            onStartOCR();
            Bitmap bitmap = null;
            if (mustRotate) {
                Log.d(TAG, "will rotate " + rotateValue + " orientation=  " + Preview.this.orientationMode.name() + " w= " + this.w + " h= " + this.h);
                int width = 0;
                int height = 0;
                if (rotateValue != 0) {
                    pix = ocr.convertToPix(data, this.w, this.h);
                    bitmap = WriteFile.writeBitmap(pix);
//                    int[] rgb = new int[data.length];
//                    ImageProcessing.decodeYUV420RGB(rgb, data, this.w, this.h);
//                    bitmap = Bitmap.createBitmap(rgb,this.w, this.h, Bitmap.Config.ARGB_8888);
                    width = bitmap.getWidth();
                    height = bitmap.getHeight();
                    Log.d(TAG, "bitmap getWidth " + width + " getHeight: " + height);
                    // Setting pre rotate
                    Matrix mtx = new Matrix();
//                    mtx.preRotate(rotateValue);
                    mtx.postRotate(rotateValue, width / 2, height / 2);
                    // Rotating Bitmap & convert to ARGB_8888, required by tess
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, mtx, false);
                    bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Log.d(TAG, "bitmap getWidth " + bitmap.getWidth() + " getHeight: " + bitmap.getHeight());
                }

                final Bitmap finalBitmap = bitmap;//Bitmap.createBitmap(bitmap, 0, 0, w, h);

//                if (finalBitmap == null) {
//                    data = arg0[0];
//                }

                if (ocr != null && ocr.isApiCreated()) {
                    ocrResult = ocr.runTessWithData2(data, finalBitmap, this.w, this.h, bpp, bpp * this.w);
                }
            } else {
//
                if (ocr != null && ocr.isApiCreated()) {
//                    if (data == null) {
//                        data = arg0[0];
//                    }
                    CameraActivity cameraActivity = (CameraActivity) context;
                    cameraActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setVisibility(View.GONE);
                        }
                    });
//                    pix = ocr.convertToPix(data, this.w, this.h);
//                    bitmap = WriteFile.writeBitmap(pix);

                    ocrResult = ocr.runTessWithData2(data, null, this.w, this.h, bpp, bpp * this.w);
                }
                if (ocrResult != null) {
                    if (rotateValue != 0) {
                        ocrResult.setLandscape(false);
                    } else {
                        ocrResult.setLandscape(true);
                    }
                }
            }
            ocrResult.setRotateValue(this.rotateValue);
            ocrResultList.add(ocrResult);
            boxes = new Vector<Rect>(ocrResult.getBoxes());
            if (boxes != null) {
                Log.d(TAG, "returning " + boxes.size() + " boxes");
            }

            return boxes;
        }

        @Override
        protected void onPostExecute(Vector<Rect> boxes) {
            Log.d(TAG, "----- onPostExecute()------");
            if (boxes == null || (boxes != null && boxes.isEmpty())) {
                Log.d(TAG, "Could not get the boxes, run again. THREAD ID: " + Thread.currentThread().getId());

                if (!waitForCameraFocus) {
                    freeFrame = true;
                }
                return;
            }
            Log.d(TAG, "THREAD ID: " + Thread.currentThread().getId());
            if (characterBoxView == null) {
                createCharacterBoxView(new Vector<Rect>(boxes), this.w, this.h);
            }

            characterBoxView.setOrientationMode(om);
            characterBoxView.setRotate(rotateValue);
            characterBoxView.setWidth(w);
            characterBoxView.setHeight(h);
            characterBoxView.setFrameHeight(h);
            characterBoxView.setFrameWidth(w);
            characterBoxView.setRects(new Vector<Rect>(boxes));
            characterBoxView.restoreView();

            if (ocrResultList.size() >= 3) {
                OcrResult ocrResult = findBestResult();
                if (ocrResult != null) {
                    Log.d(TAG, "GET BEST RESULT,WILL FINISHING VIDEO CALLBACK AND START RESULT ACTIVITY !");
                    freeFrame = false;

                    serviceMessenger = new ServiceMessenger(resultHandler);
                    serviceMessenger.sendMessage(1, 4, ocrResult);
                } else {
                    cleanOcrResultList();
                    cancelAllOcrTasks();

                }
            }


            Log.d(TAG, "THREAD ID: " + Thread.currentThread().getId());
            serviceMessenger = new ServiceMessenger(resultHandler);
            serviceMessenger.sendMessage(0, 3, characterBoxView);// set enable=true to the button

            if (!waitForCameraFocus) {
                // is not waiting for camera focus , so can run next task
                freeFrame = true;
            } else {
                Log.d(TAG, "is waiting , do not run a task, give a space to make new focus");
                freeFrame = false;
            }
            Log.d(TAG, "----- return onPostExecute()------");
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
            if (ocrResultBest.getMeanConfidences() < 80 || ocrResultBest.getResult().length() <= 0)
                return null;
            return ocrResultBest;
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "---- On Cancelled -----");
            // isLocked = true;
            // freeFrame = false;
            // waitForCameraFocus = false;
        }

        private final int w;
        private final int h;
    }

    private void getRotateByNewOrientationMode(OrientationMode orientationMode) {
        if (this.orientationMode == null) {
            return;
        }
        Log.d(TAG, "---- getRotateByNewOrientationMode ----- new: " + orientationMode.name + ", old: " + this.orientationMode.name + " rotate: " + rotate);
        if (this.orientationMode == orientationMode) {
            return;
        }
//        switch (this.orientationMode) {
//            case LANDSCAPE_UPSIDE_DOWN:
//                rotate = 180;
//                break;
//            case PORTRAIT:
//                rotate = -90;
//                break;
//            case PORTRAIT_UPSIDE_DOWN:
//                rotate = 90;
//                break;
//        }

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

    /**
     * @param orientationModeNew
     */
    public void changeOrientation(OrientationMode orientationModeNew) {
        int rotateNew;
        if (orientationModeNew == null) {
            return;
        }
//        if (this.orientationMode == null) {
//            this.orientationMode = orientationModeNew;
//            rotate = 0;
//            return;
//        }
        if (this.orientationMode == orientationModeNew) {
            return;
        }
        if ((this.orientationMode == OrientationMode.LANDSCAPE || this.orientationMode == OrientationMode.LANDSCAPE_UPSIDE_DOWN) && (orientationMode == OrientationMode.PORTRAIT || orientationMode == OrientationMode.PORTRAIT_UPSIDE_DOWN)) {
            Log.d(TAG, "change Orintation to PORTRAIT");
        } else if ((this.orientationMode == OrientationMode.PORTRAIT || this.orientationMode == OrientationMode.PORTRAIT_UPSIDE_DOWN) && (orientationMode == OrientationMode.LANDSCAPE || orientationMode == OrientationMode.LANDSCAPE_UPSIDE_DOWN)) {
            Log.d(TAG, "change Orintation to LANDSCAPE");
        }

        getRotateByNewOrientationMode(orientationModeNew);

        this.orientationMode = orientationModeNew;
    }

    /**
     * Control variables are for controlling new task with are calling OCR
     * or for new calls camera focus.
     */
    public void setControllVariablesToDefault() {
        Log.d(TAG, "setControllVariablesToDefault()...");
        freeFrame = true;
        waitForCameraFocus = false;
        firstInitVideoCall = true;
    }

    /*
     * Return true if device is in orientation portrait
     */
    private boolean isOrientationPortrait() {
        return orientationMode == OrientationMode.PORTRAIT || orientationMode == OrientationMode.PORTRAIT_UPSIDE_DOWN;
    }

    /*
     * Switch width , height by orientation changed
     */
    private void onOrientationChanged() {
        Log.d(TAG, "onOrientationChanged()");
        int tempW = w;
        w = h;
        h = tempW;
    }

    /**
     * Clean the List with the Result from OCR calls
     */
    public void cleanOcrResultList() {
        ocrResultList.clear();
    }

    private synchronized void onStartOCR() {
        if (ocr == null || (ocr != null && !ocr.isApiCreated())) {
            LanguageManager languageManager = null;
//            try {
            languageManager = AppSetting.getInstance().getLanguageManager();
//            } catch (NullPointerException ex) {
//                AppSetting.getInstance().initLanguageMenager(context);
//                languageManager = AppSetting.getInstance().getLanguageManager();
//            }
            if (languageManager.checkTraineddataForCurrentLanguage(context))
                ocr = new OCR(resultHandler);
            else
                serviceMessenger.sendMessage(0, 9, null);
        }
    }

    private void createCharacterBoxView(Vector<Rect> rects, int w, int h) {
        this.characterBoxView = new CharacterBoxView(context, rects, w, h, orientationMode);
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

    /**
     * the display size of view (surfaceView) witch is displaying every each frame.
     * Could be different than size of frame
     *
     * @param displayWidth
     */
    public void setDisplayWidth(int displayWidth) {
        this.displayWidth = displayWidth;
    }

    /**
     * the display size of view (surfaceView) witch is displaying every each frame.
     * Could be different than size of frame
     *
     * @param displayHeight
     */
    public void setDisplayHeight(int displayHeight) {
        this.displayHeight = displayHeight;
    }

    public Camera getCamera() {
        return camera;
    }

    public OCR getOcr() {
        return this.ocr;
    }


}
