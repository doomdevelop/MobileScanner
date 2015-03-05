package de.bht.bachelor.beans;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.googlecode.leptonica.android.WriteFile;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class OcrResult implements Parcelable {

    private byte[] imageData;
    private String result;
    private int[] wordConfidences;
    private int meanConfidences;
    private ArrayList<Rect> boxes = null;
    private static final String TAG = OcrResult.class.getSimpleName();
    private boolean isLandscape;
    private boolean hasImageData = false;
    private int w;
    private int h;
    private int rotateValue;

    public void setW(int w) {
        this.w = w;
    }

    public int getRotateValue() {
        return rotateValue;
    }

    public void setRotateValue(int rotateValue) {
        this.rotateValue = rotateValue;
    }

    public boolean isLandscape() {
        return isLandscape;
    }

    public void setLandscape(boolean isLandscape) {
        this.isLandscape = isLandscape;
    }


    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }


    public void setHasImageData(boolean hasImageData) {
        this.hasImageData = hasImageData;
    }

    public boolean isHasImageData() {
        return hasImageData;
    }




    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public OcrResult(String result, int[] wordConfidences, int meanConfidences, ArrayList<Rect> boxes,byte[] imageData,int w , int h) {
        this.result = result;
        this.wordConfidences = wordConfidences;
        this.meanConfidences = meanConfidences;
        this.boxes = boxes;
        if( this.imageData == null){
            this.imageData = imageData;
        }
        this.w = w;
        this.h = h;
    }

    public OcrResult(String result, int meanConfidences) {
        this.result = result;
        this.meanConfidences = meanConfidences;
    }

    public static byte[] convertBitmapToData(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();

    }

    public static Bitmap convertDataToBitmap(byte[] bitmapData) {
        Log.d(TAG,"convertDataToBitmap()");

        Bitmap bmp = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);

        return bmp;

    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int[] getWordConfidences() {
        return wordConfidences;
    }

    public void setWordConfidences(int[] wordConfidences) {
        this.wordConfidences = wordConfidences;
    }

    public int getMeanConfidences() {
        return meanConfidences;
    }

    public void setMeanConfidences(int meanConfidences) {
        this.meanConfidences = meanConfidences;
    }

    public ArrayList<Rect> getBoxes() {
        return boxes;
    }


    public void setBoxes(ArrayList<Rect> boxes) {
        this.boxes = boxes;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.result);
        dest.writeIntArray(this.wordConfidences);
        dest.writeInt(this.meanConfidences);
        this.boxes = new ArrayList<Rect>();
        dest.writeSerializable(this.boxes);
        dest.writeByte(hasImageData ? (byte) 1 : (byte)0);
        dest.writeByte(isLandscape ? (byte) 1 : (byte) 0);
        dest.writeInt(w);
        dest.writeInt(h);
        dest.writeInt(rotateValue);
    }

    private OcrResult(Parcel in) {
        this.result = in.readString();
        this.wordConfidences = in.createIntArray();
        this.meanConfidences = in.readInt();
        this.boxes = (ArrayList<Rect>) in.readSerializable();
        this.hasImageData = in.readByte() != 0;
        this.isLandscape =  in.readByte() != 0;
        this.w = in.readInt();
        this.h = in.readInt();
        this.rotateValue = in.readInt();
    }

    public static final Parcelable.Creator<OcrResult> CREATOR = new Parcelable.Creator<OcrResult>() {
        public OcrResult createFromParcel(Parcel source) {
            return new OcrResult(source);
        }

        public OcrResult[] newArray(int size) {
            return new OcrResult[size];
        }
    };
}
