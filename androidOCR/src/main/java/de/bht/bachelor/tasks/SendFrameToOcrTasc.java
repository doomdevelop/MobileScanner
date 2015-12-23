package de.bht.bachelor.tasks;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.WriteFile;

import java.util.Vector;

import de.bht.bachelor.activities.CameraActivity;
import de.bht.bachelor.beans.OcrResult;
import de.bht.bachelor.camera.OrientationMode;
import de.bht.bachelor.language.LanguageManager;
import de.bht.bachelor.message.ServiceMessenger;
import de.bht.bachelor.ocr.OCR;
import de.bht.bachelor.setting.AppSetting;

/**
 * Created by and on 23.12.15.
 *
 * Calling OCR with the picture frame as byte[],
 * getting result with boxes with are add in to the preview.
 */
public class SendFrameToOcrTasc extends AsyncTask<byte[], Void, OcrResult> {
    private int rotateValue;
    private final OrientationMode om;
    private int w;
    private int h;
    private static final String TAG = SendFrameToOcrTasc.class.getSimpleName();
    private OcrTaskResultCallback ocrTaskResultCallback;
    private OCR ocr;
    private int bpp;
    private Activity context;

    public SendFrameToOcrTasc(int w, int h,int bpp, final int rotateValue,Activity context, final OrientationMode om,OcrTaskResultCallback ocrTaskResultCallback) {
        this.rotateValue = rotateValue;
        this.om = om;
        this.bpp = bpp;
        this.context = context;
        this.ocrTaskResultCallback = ocrTaskResultCallback;
        if (om == OrientationMode.PORTRAIT || om == OrientationMode.PORTRAIT_UPSIDE_DOWN) {
            this.w = h;
            this.h = w;
        } else {
            this.w = w;
            this.h = h;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ocrTaskResultCallback.onOcrTaskPreExecute();
        onStartOCR();
        Log.d(TAG, "onPreExecute()...");
    }

    @Override
    protected OcrResult doInBackground(byte[]... arg0) {
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
//        onStartOCR();
        Bitmap bitmap = null;
//        Log.d(TAG, "will rotate " + rotateValue + " orientation=  " + Preview.this.orientationMode.name() + " w= " + this.w + " h= " + this.h);
        if (rotateValue != 0) {

            pix = ocr.convertToPix(data, this.w, this.h);
            bitmap = WriteFile.writeBitmap(pix);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Log.d(TAG, "bitmap getWidth " + width + " getHeight: " + height);
            // Setting pre rotate
            Matrix mtx = new Matrix();
            mtx.postRotate(rotateValue, width / 2, height / 2);
            // Rotating Bitmap & convert to ARGB_8888, required by tess
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, mtx, false);
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Log.d(TAG, "bitmap getWidth " + bitmap.getWidth() + " getHeight: " + bitmap.getHeight());

            final Bitmap finalBitmap = bitmap;//Bitmap.createBitmap(bitmap, 0, 0, w, h);

            if (ocr != null && ocr.isApiCreated()) {
                ocrResult = ocr.runTessWithData2(data, finalBitmap, this.w, this.h, bpp, bpp * this.w);
            }
        } else {
            if (ocr != null && ocr.isApiCreated()) {
                ocrResult = ocr.runTessWithData2(data, null, this.w, this.h, bpp, bpp * this.w);
            }
        }
        ocrResult.setRotateValue(this.rotateValue);

        return ocrResult;
    }

    @Override
    protected void onPostExecute(OcrResult ocrResult) {
        Log.d(TAG, "----- onPostExecute()------");
        this.ocrTaskResultCallback.onOcrTaskResultCallback(ocrResult);

    }
    private  void onStartOCR() {
        if (ocr == null || (ocr != null && !ocr.isApiCreated())) {
            LanguageManager languageManager = null;
            languageManager = AppSetting.getInstance().getLanguageManager();
            if (languageManager.checkTraineddataForCurrentLanguage(context))
                ocr = new OCR();
            else
                ocrTaskResultCallback.onTrainDataerror();
        }
    }
}
