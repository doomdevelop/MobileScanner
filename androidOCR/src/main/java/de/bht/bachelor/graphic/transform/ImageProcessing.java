package de.bht.bachelor.graphic.transform;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android.view.View;

import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.WriteFile;

import java.io.File;

import de.beutch.bachelorwork.util.file.ConvertUtil;
import de.beutch.bachelorwork.util.file.FileUtil;
import de.beutch.bachelorwork.util.file.Path;

public class ImageProcessing {

	public static void decodeYUV420RGB(int[] rgb, byte[] yuv420sp, int width, int height) {
		// Convert YUV to RGB
		final int frameSize = width * height;
		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & (yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}

				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;
				// rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
	}

	private static void decodeYUV420RGBContrastEnhance(int[] rgb, byte[] yuv420sp, int width, int height) {
		// Compute histogram for Y
		int[] mGrayHistogram = new int[256];
		final int frameSize = width * height;
		int clipLimit = frameSize / 10;
		for (int bin = 0; bin < 256; bin++)
			mGrayHistogram[bin] = 0;
		for (int j = 0, yp = 0; j < height; j++) {
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & (yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if (mGrayHistogram[y] < clipLimit)
					mGrayHistogram[y]++;
			}
		}
		double sumCDF = 0;
		for (int bin = 0; bin < 256; bin++) {
			sumCDF += (double) mGrayHistogram[bin] / frameSize;
			mGrayCDF[bin] = sumCDF;
		}

		// Convert YUV to RGB
		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & (yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}
				y = (int) (mGrayCDF[y] * 255 + 0.5);

				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
	}

	public static void setContrastEnhance(byte[] yuv420sp, byte[] contrastData, int width, int height, float contrastFactor) {
		Log.d(TAG, "setContrastEnhance: decode data of picture, width : " + width + ", hight : " + height);
		final int frameSize = width * height;

		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & (yuv420sp[yp])) - 16;
				float yConstr = y * contrastFactor;
				y = (int) yConstr;
				y = ((y < 0) ? 0 : ((y > 255) ? 255 : y));
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;

					contrastData[yp] = (byte) ((0xff & y) + 16);
					contrastData[uvp - 2] = (byte) ((0xff & v) + 128);
					contrastData[uvp - 1] = (byte) ((0xff & u) + 128);

				} else {
					contrastData[yp] = (byte) ((0xff & y) + 16);
				}
			}
		}
	}

	public static Bitmap rotate(Bitmap b, int degrees) {
		Bitmap b2 = null;
		if (degrees != 0 && b != null) {
			Matrix m = new Matrix();

			m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
			try {
				b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
				if (b2 != null)
					b2 = ConvertUtil.convetrToBitmap8888(b2);
			} catch (OutOfMemoryError ex) {
				Log.e(TAG, "Could not rotate bitmap !", ex);
			}
		}
		return b2;
	}

    public static void rotateData(byte[] data,int frameWidth, int frameHeight ){
        Log.d(TAG,"rotateData w: "+frameWidth+",h: "+frameHeight);
        byte[] rotatedData =  new byte[data.length];

        for (int y = 0; y < frameHeight; y++) {
            for (int x = 0; x < frameWidth; x++)
                rotatedData[x * frameHeight + frameHeight - y - 1] = data[x + y * frameWidth];
        }
        data = rotatedData;
    }

    public static Pix binarizeData(byte[] data, byte[] contrastData,int w , int h){
//                    saveOrginalData(data);
        Log.d(TAG,"binarizeData() w "+w+" h "+h);
        Pix pix = Pix.createFromPix(data,w,h,8);
        Pix pixBin = Binarize.otsuAdaptiveThreshold(pix, 32, 32, 2, 2, 0.1f);
        contrastData = pixBin.getData();
        return pixBin;
    }

    public static Bitmap enhanceDataToBitmap(byte[] data, byte[] contrastData, int w , int h){
        Log.d(TAG, "processAndSaveDataAsBitmap().......");
        int[] rgb = new int[w * h];
        Bitmap bitmapRGB = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);

        ImageProcessing.setContrastEnhance(data, contrastData, w, h, 1.3f);
        ConvertUtil.decodeYUV420SP(rgb, contrastData, w, h);
        bitmapRGB.setPixels(rgb, 0, w, 0, 0, w, h);
        return bitmapRGB;
    }
    public static void enhanceDataToFile(byte[] data, byte[] contrastData,int w , int h) {
        Log.d(TAG, "processAndSaveDataAsBitmap().......");
        int[] rgb = new int[w * h];
        Bitmap bitmapRGB = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);

        ImageProcessing.setContrastEnhance(data, contrastData, w, h, 1.3f);
        // imageProcessing.setContrastEnhance(data, contrastData, w, h, 1.4f);
        // imageProcessing.decodeYUV420RGB(rgb, contrastData, w, h);
        ConvertUtil.decodeYUV420SP(rgb, contrastData, w, h);
        bitmapRGB.setPixels(rgb, 0, w, 0, 0, w, h);
        new FileUtil().saveBitmapAsFile(bitmapRGB, new File(Path.MAIN_PICTURES_PATH + "contrastBitmap.jpeg"), Bitmap.CompressFormat.JPEG);

    }

	public static Bitmap convertAndRotate(byte[] data, int rotate, int w , int h){
		int[] rgb = new int[data.length];
		ImageProcessing.decodeYUV420RGB(rgb, data, w, h);
		Bitmap bmp = Bitmap.createBitmap(rgb, w, h, Bitmap.Config.ARGB_8888);
//		int rotateValue = ocrResult.getRotateValue();
		Matrix matrix = new Matrix();
		matrix.postRotate(rotate, w / 2, h / 2);
		bmp = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, false);
		return bmp;
	}

    private final static double[] mGrayCDF = new double[256];
	private static final String TAG = ImageProcessing.class.getSimpleName();
}
