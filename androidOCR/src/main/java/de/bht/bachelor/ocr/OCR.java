package de.bht.bachelor.ocr;

import java.io.File;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.Pixa;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.Rotate;
import com.googlecode.leptonica.android.WriteFile;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import de.beutch.bachelorwork.util.file.FileUtil;
import de.beutch.bachelorwork.util.file.Path;
import de.bht.bachelor.beans.OcrResult;
import de.bht.bachelor.helper.Timer;
import de.bht.bachelor.language.LanguageManager;
import de.bht.bachelor.message.ServiceMessenger;
import de.bht.bachelor.setting.AppSetting;

/**
 * This class initialising the tesseract API , and is giving access to it.
 * The class is "under construction", has more methods witch are diversely calling api.
 *
 *
 * @author Andrzej Kozlowski
 *
 */
public class OCR {

    private static final String TAG = OCR.class.getSimpleName();
    private Handler resultHandler;
    private TessBaseAPI api;
    private final LanguageManager languageManager;
    private boolean apiCreated = false;
    private boolean cancelResult = false;
    private volatile boolean isRunning = false;

    /**
     *
     * @param languageManager
     * @param handler
     */
    public OCR( ) {
//        this.resultHandler = handler;
        this.languageManager = AppSetting.getInstance().getLanguageManager();
        apiCreated = initAPI();
        prepareTesseract();
    }

    public TessBaseAPI getApi() {
        return api;
    }

    public void prepareTesseract() {
        if (!apiCreated)
            return;

        api.setPageSegMode(TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED);
    }



    public int getMeanConfidences(int[] wordConfidences) {
        int meanConfidences = 0;
        if (wordConfidences.length <= 0) {
            return 0;
        }
        for (int i = 0; i < wordConfidences.length; i++) {
            meanConfidences += wordConfidences[i];
        }
        return meanConfidences / wordConfidences.length;
    }

    private void logRect(Rect rect) {
        int top, bottom, left, right;
        bottom = rect.bottom;
        top = rect.top;
        left = rect.left;
        right = rect.right;
        Log.d(TAG, "The Rect Dimensions are :bottom " + bottom + ",top " + top + ",right " + right + ",left " + left);
    }

    private void testRotatePix(Pix pix) {
        Log.d(TAG, "Start test, the image as PIX will be rotate 360 degree,");
        Log.d(TAG, "and every 10 degree will write statement with a current time and degree in to the Log");
        int degree = 0;
        long startTime = System.currentTimeMillis();
        float tempTime;

        while (degree < 360) {
            rotate(pix, degree, false);
            degree += 10;
            tempTime = System.currentTimeMillis() - startTime;
            if (tempTime > 2000)
                Log.d(TAG, "rotate with degree = " + degree + ", working time = " + tempTime / 1000 + " s");
            else
                Log.d(TAG, "rotate with degree = " + degree + ", working time = " + tempTime + " ms");
        }
        Log.d(TAG, "Start test, the image will be rotate one time 90 degree,");
        startTime = System.currentTimeMillis();
        rotate(pix, degree, false);
        tempTime = System.currentTimeMillis() - startTime;
        Log.d(TAG, "rotate with degree = " + 90 + ", working time = " + tempTime + " ms");
    }

    /*
     * Check the bpp of the given image
     * Return true if has 8 bpp.
     */
    private boolean checkPix(Pix pix) {
        boolean result = true;
        if (pix == null) {
            Log.d(TAG, "rotated pix is null !");
            return false;
        }
        if (pix.getDepth() != 8) {
            Log.d(TAG, "wrong depth");
            result = false;
        }
        if (pix.getNativePix() <= 0) {
            Log.d(TAG, "no pix, is empty !");
            result = false;
        }
        return result;
    }

    /*
     * Convert Pix in to the Bitmap , rotate , convert back to pix
     * Return rotated Pix
     */
    private Bitmap testRotateAsBitmap(Bitmap bitmap, float degrees) {
        Log.d(TAG, "--------------------testRotateAsBitmap " + degrees + " degrees----------------");
        long startTime = System.currentTimeMillis();
        float tempTime;
        Bitmap rotated = null;
        Matrix matrix = new Matrix();

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Log.d(TAG, "Dimension of bitmap to rotate : width= " + width + ",height= " + height);
        matrix.postRotate(degrees, width / 2, height / 2);
        try {
            rotated = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            Log.d(TAG, "Dimension of bitmap after rotate: width= " + rotated.getWidth() + ",height= " + rotated.getHeight());
            // bitmap.recycle();
            // new FileUtil().saveBitmapAsFile(rotated, new File(de.beutch.bachelorwork.util.file.Path.MAIN_PICTURES_PATH + "rotate_3_" + System.currentTimeMillis() + ".jpg"),
            // Bitmap.CompressFormat.JPEG);
            // bitmap.recycle();

            // Pix pixRotated = ReadFile.readBitmap(rotated);
            tempTime = System.currentTimeMillis() - startTime;
            if (tempTime > 2000)
                Log.d(TAG, "rotate with degree = " + degrees + ", working time = " + tempTime / 1000 + " s");
            else
                Log.d(TAG, "rotate with degree = " + degrees + ", working time = " + tempTime + " ms");
            // if (checkPix(pix2)) {
            // Log.d(TAG, "Yes !!");
            // }
        } catch (OutOfMemoryError ex) {
            Log.d(TAG, "Could nt rotate image!", ex);
            if (rotated != null)
                rotated.recycle();
            return null;
        }
        // bitmap.recycle();
        testMethod();
        return rotated;
    }

    private void testMethod(){
        Log.d(TAG,"tets");
    }

    /*
     * Test the rotation of 360 degrees of the pix. Write result in to the log , save
     *
     * @param pix
     */
    private void testRotatetBitmap360(Pix pix) {
        Log.d(TAG, "*******************testRotatetBitmap360()*****************");
        long startTime = System.currentTimeMillis();
        float tempTime;
        float degrees = 0f;
        while (degrees < 360) {
            testRotateAsBitmap(WriteFile.writeBitmap(pix), degrees);
            degrees += 10;
        }
        tempTime = System.currentTimeMillis() - startTime;
        if (tempTime > 2000)
            Log.d(TAG, "rotate with degree = " + degrees + ", working time = " + tempTime / 1000 + " s");
        else
            Log.d(TAG, "rotate with degree = " + degrees + ", working time = " + tempTime + " ms");
        Log.d(TAG, "*******************END*****************");
    }

    /**
     *
     * @param data
     *            the picture to analyse
     * @param width
     *            of the picture
     * @param height
     *            of the picture
     * @param bpp
     *            byte per pixel
     * @param bpl
     *            byte per line
     * @return OcrResult witch is a bean witch is containing ocr result, confidences and boxes of the recognised characters
     *
     * @see OcrResult
     */
    public OcrResult runTessWithData(byte[] data, int width, int height, int bpp, int bpl) {
        String result = null;
        isRunning = true;
        int[] wordConfidences = null;
        int meanConfidences = 0;
        Pixa pixa = null;
        ArrayList<Rect> boxes = null;
        OcrResult ocrResult;

        if (!apiCreated)
            return null;
        try {
            Log.d(TAG, "runTessWithData data length: " + data.length + ",w:" + width + ",h: " + height + ",bpp: " + bpp + ",bpl: " + bpl);
            api.setImage(data, width, height, bpp, bpl);
            result = api.getUTF8Text();
            pixa = api.getWords();
            meanConfidences = api.meanConfidence();
            wordConfidences = api.wordConfidences();
            Log.d(TAG, "wordConfidences after set Image= " + wordConfidences.length + ", meanConfidences = " + meanConfidences);

            Log.d(TAG, "Getting result from OCR : ");
            Log.d(TAG, result);

            boxes = pixa.getBoxRects();
        } catch (NullPointerException ex) {
            Log.d(TAG, "Could not run tessearct", ex);
        } finally {
            api.clear();
            isRunning = false;
        }
        ocrResult = new OcrResult(result, wordConfidences, meanConfidences, boxes,data,width,height);
        return ocrResult;
    }

    public static Pix convertToPix(byte[] data, int width, int height){
        Log.d(TAG,"convertToPix() w "+width+" h "+height);
        Pix pix = ReadFile.readBytes8(data,width,height);
        if(pix == null){
            Log.d(TAG," pix is NULL");
        }else{
            Log.d(TAG," pix is OK");
        }
        return pix;
    }

    public static Pix rotate(Pix pix, float rotateValue, boolean quality) {
        Log.d(TAG, "image will be rotated ");
        Pix temp = Rotate.rotate(pix, rotateValue, quality);
        if (temp != null && temp.getNativePix() > 0)
            Log.d(TAG, "Successfully rotaded image !");
        return temp;
    }

    public synchronized OcrResult runTessWithData2(byte[] data,final Bitmap bitmap,int width, int height, int bpp, int bpl){
        String result = null;
        isRunning = true;
        int[] wordConfidences = null;
        int meanConfidences = 0;
        ArrayList<Rect> boxes = new ArrayList<Rect>();
        OcrResult ocrResult;
        Rect rect;
        int[] lastBoundingBox = null;

        if (!apiCreated)
            return null;
        try {
            Log.d(TAG, "runTessWithData data length: " + data.length + ",w:" + width + ",h: " + height + ",bpp: " + bpp + ",bpl: " + bpl);
           if(bitmap != null) {
               api.setImage(bitmap);
           }else {
               api.setImage(data, width, height, bpp, bpl);
           }
               result = api.getUTF8Text();

            final ResultIterator iterator = api.getResultIterator();
            do{
                lastBoundingBox = iterator.getBoundingBox(TessBaseAPI.PageIteratorLevel.RIL_SYMBOL);
                rect = new Rect(lastBoundingBox[0],lastBoundingBox[1],lastBoundingBox[2],lastBoundingBox[3]);
                boxes.add(rect);
            }while(iterator.next(TessBaseAPI.PageIteratorLevel.RIL_SYMBOL));

            meanConfidences = api.meanConfidence();
            wordConfidences = api.wordConfidences();
            Log.d(TAG, "wordConfidences after set Image= " + wordConfidences.length + ", meanConfidences = " + meanConfidences);

            Log.d(TAG, "Getting result from OCR : ");
            Log.d(TAG, result);
        } catch (NullPointerException ex) {
            Log.d(TAG, "Could not run tessearct", ex);
        } finally {
            api.clear();
            isRunning = false;
        }
        return new OcrResult(result, wordConfidences, meanConfidences, boxes,data,width,height);
    }

    public ArrayList<Rect> runTessWithBitmap(Bitmap bitmap) {
        try {
            Log.d(TAG, "runTessWithData Bitmap w: " + bitmap.getWidth() + ",h: " + bitmap.getHeight());
            String result;
            int[] wordConfidences;
            int meanConfidences;
            Pixa pixa = null;

            api.setImage(bitmap);
            result = api.getUTF8Text();
            pixa = api.getWords();
            wordConfidences = api.wordConfidences();
            meanConfidences = api.meanConfidence();
            Log.d(TAG, "wordConfidences after set Image= " + wordConfidences.length + ", meanConfidences = " + meanConfidences);

            Log.d(TAG, "Getting result from OCR : ");
            Log.d(TAG, result);
            if (meanConfidences > 75)
                return pixa.getBoxRects();
            else
                return null;
        } catch (NullPointerException ex) {
            Log.d(TAG, "Could not run tessearct", ex);
        }
        return null;
    }

    /**
     * Binarize giving image
     *
     * @param pix
     * @return
     */
    private Bitmap binarize(Pix pix) {

        Pix pixBin = Binarize.otsuAdaptiveThreshold(pix, 32, 32, 2, 2, 0.1f);
        Bitmap bitmapBin = WriteFile.writeBitmap(pixBin);
        api.clear();
        api.setImage(bitmapBin);
        // new FileUtil().saveBitmapAsFile(bitmapBin, new File(de.beutch.bachelorwork.util.file.Path.MAIN_PICTURES_PATH + "Binarize_" + System.currentTimeMillis() + ".jpg"),
        // Bitmap.CompressFormat.JPEG);
        return bitmapBin;
    }

    /**
     * This method check if the given parameter should be rotated before getting result from Tessearct
     *
     * @param pix
     */
    public void recognisableOfRotatedImage(Pix pix) {
        Log.d(TAG, "------------------- recognisableOfRotatedImage() ------------------------");
        int degrees = 0;
        int[] wordConfidences;
        int meanConfidences;
        Bitmap bitmapRotated = null;
        Bitmap orgBitmap;
        String result = "";
        try {
            api.setImage(pix);
            wordConfidences = api.wordConfidences();
            meanConfidences = api.meanConfidence();
            Log.d(TAG, "rotate degrees= " + degrees + ", wordConfidences= " + wordConfidences.length + ", meanConfidences = " + meanConfidences);
            result = api.getUTF8Text();
            Log.d(TAG, "OCR Text result : ");
            Log.d(TAG, result);
            orgBitmap = WriteFile.writeBitmap(pix);

            pix.recycle();

            while (degrees <= 40) {
                if (degrees >= 10)
                    degrees += 1;
                else
                    degrees += 5;
                if (bitmapRotated != null)
                    bitmapRotated.recycle();
                api.clear();
                bitmapRotated = testRotateAsBitmap(orgBitmap, degrees);
                api.setImage(bitmapRotated);
                wordConfidences = api.wordConfidences();
                meanConfidences = api.meanConfidence();
                Log.d(TAG, "rotate degrees= " + degrees + ", wordConfidences= " + wordConfidences.length + ", meanConfidences = " + meanConfidences);
                result = api.getUTF8Text();
                Log.d(TAG, "OCR Text result : ");
                Log.d(TAG, result);

                // new FileUtil().saveBitmapAsFile(bitmapRotated, new File(de.beutch.bachelorwork.util.file.Path.MAIN_PICTURES_PATH + "Rotated_" + degrees + "_" +
                // System.currentTimeMillis() + ".jpg"), Bitmap.CompressFormat.JPEG);
                saveBitmapAsFile(bitmapRotated, degrees);
                Thread.sleep(2000);
            }
        } catch (Exception ex) {
            Log.d(TAG, "Could not finish recognise of rotated image  ", ex);
        } finally {
            ServiceMessenger serviceMessenger = new ServiceMessenger(resultHandler);
            serviceMessenger.sendMessage(0, 0, result);
            pix.recycle();
            api.clear();
            api.end();
        }
        Log.d(TAG, "------------------- END OF recognisableOfRotatedImage() ------------------------");
    }

    /**
     * Save giving bitmap in to the file.
     *
     * @param b
     * @param degrees
     *            use it as a filename
     */
    public void saveBitmapAsFile(final Bitmap b, final int degrees) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new FileUtil().saveBitmapAsFile(b, new File(de.beutch.bachelorwork.util.file.Path.MAIN_PICTURES_PATH + "recognise_rotate_+" + degrees + "_" + System.currentTimeMillis() + ".jpg"), Bitmap.CompressFormat.JPEG);
            }
        }).start();
    }

    private void printResult(String result) {
        Log.d(TAG, "***************** ocr result **********************");
        Log.d(TAG, result);
        Log.d(TAG, "***************************************************");
    }

    /**
     * This Method is calling tesseract with giving parameter pix.
     * It set the Picture right time run time and results in to the log.
     *
     * @param pix
     *            image
     */
    public void testWithCruppedBitmapAsPix(Pix pix) {
        Log.d(TAG, "testWithCruppedBitmapAsPix().........");
        int[] wordConfidences;
        int meanConfidences;
        String result = "";
        Timer timer = new Timer();
        try {
            timer.start();
            api.setImage(pix);
            wordConfidences = api.wordConfidences();
            meanConfidences = api.meanConfidence();
            Log.d(TAG, "wordConfidences= " + wordConfidences.length + ", meanConfidences = " + meanConfidences);
            result = api.getUTF8Text();
            printResult(result);
            timer.stop();

            Log.d(TAG, "timer from set image till result : " + timer.getRunTime() + " ms");
        } catch (RuntimeException ex) {
            Log.d(TAG, "Could not set Pix.", ex);
        } finally {
            api.clear();
            api.end();
        }
    }

    /**
     * Call it to run tesseract with Bitmap
     *
     * @param bitmap8888
     *            bitmap with configuration 8888
     * @see Bitmap.Config.ARGB_8888
     */
    public String runWithCropped8888(Bitmap bitmap8888) {
        Log.d(TAG, "runWithCropped8888().........");
        int[] wordConfidences;
        int meanConfidences;
        String result = "";
        Timer timer = new Timer();
        try {
            timer.start();
            api.setImage(bitmap8888);
            wordConfidences = api.wordConfidences();
            meanConfidences = api.meanConfidence();
            Log.d(TAG, "wordConfidences= " + wordConfidences.length + ", meanConfidences = " + meanConfidences);
            result = api.getUTF8Text();
            printResult(result);
            timer.stop();
            Log.d(TAG, "timer from set image till result : " + timer.getRunTime() + " ms");
        } catch (RuntimeException ex) {
            Log.d(TAG, "Could not set Bitmap.", ex);
        } finally {

            clearAndCloseApi();
        }
        return result;
    }

    public void clearAndCloseApi() {
        if (api != null) {
            Log.d(TAG, "clearAndCloseApi");

            api.clear();
            api.end();
            apiCreated = false;
        }
    }

    public void testWithCropped8888(Bitmap bitmap8888) {
        Log.d(TAG, "testWithCropped8888().........");
        int[] wordConfidences;
        int meanConfidences;
        String result = "";
        Timer timer = new Timer();
        try {
            timer.start();
            api.setImage(bitmap8888);
            wordConfidences = api.wordConfidences();
            meanConfidences = api.meanConfidence();
            Log.d(TAG, "wordConfidences= " + wordConfidences.length + ", meanConfidences = " + meanConfidences);
            result = api.getUTF8Text();
            printResult(result);
            timer.stop();
            Log.d(TAG, "timer from set image till result : " + timer.getRunTime() + " ms");
        } catch (RuntimeException ex) {
            Log.d(TAG, "Could not set Bitmap.", ex);
        } finally {
            clearAndCloseApi();
        }
    }

    public void testWithRect(Bitmap bitmap, Rect rect) {
        Log.d(TAG, "testWithRect().........");
        int[] wordConfidences;
        int meanConfidences;
        String result = "";
        Timer timer = new Timer();
        try {
            timer.start();
            api.setImage(bitmap);

            if (rect != null) {

                api.setRectangle(rect);
                wordConfidences = api.wordConfidences();
                meanConfidences = api.meanConfidence();
                Log.d(TAG, "wordConfidences= " + wordConfidences.length + ", meanConfidences = " + meanConfidences);
            }
            // Thread.sleep(200);
            result = api.getUTF8Text();
            printResult(result);
            timer.stop();

            Log.d(TAG, "timer from set image till result : " + timer.getRunTime() + " ms");
        } catch (RuntimeException ex) {
            Log.d(TAG, "Could not set Bitmap.", ex);

        } finally {
            api.clear();
            api.end();
        }
    }

    public void testSetImage(byte[] data, Bitmap bitmap, Pix pix, File file) {
        String result = "";

        try {
            api.setImage(bitmap);
            result = api.getUTF8Text();
            Log.d(TAG, "result : " + result);
        } catch (RuntimeException ex) {
            Log.d(TAG, "Could not set Bitmap.", ex);
        }
        api.clear();

        ServiceMessenger serviceMessenger = new ServiceMessenger(resultHandler);
        serviceMessenger.sendMessage(0, 0, result);
        pix.recycle();
        api.clear();
        api.end();
    }

    /*
     * return true if the api is successfully initialised
     */
    private boolean initAPI() {
        Log.d(TAG, "init...");
        Log.d(TAG, "Environment.getExternalStorageDirectory() : " + Environment.getExternalStorageDirectory());
        api = new TessBaseAPI();
        // boolean b = api.setVariable(TessBaseAPI.VAR_ACCURACYVSPEED, "100");
        boolean resultInit = api.init(Path.MAIN_LANGUAGE_PATH, languageManager.getCurrentOcrLanguage());
        // boolean resultInit = api.init("/mnt/sdcard/tesseract/", "deu-frak");
        return resultInit;
    }

    public Handler getResultHandler() {
        return resultHandler;
    }

    public void setResultHandler(Handler resultHandler) {
        this.resultHandler = resultHandler;
    }

    public void setCancelResult(boolean cancelResult) {
        this.cancelResult = cancelResult;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isApiCreated() {
        return apiCreated;
    }



}
