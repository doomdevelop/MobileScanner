package de.beutch.bachelorwork.util.color;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;
import de.beutch.bachelorwork.util.file.ConvertUtil;

public class ColorUtil {
	public static void setContrast(ColorMatrix cm, float contrast) {
		Log.i(TAG, "setContrast");
		float scale = contrast + 1.f;
		float translate = (-.5f * scale + .5f) * 255.f;
		cm.set(new float[] { scale, 0, 0, 0, translate, 0, scale, 0, 0, translate, 0, 0, scale, 0, translate, 0, 0, 0, 1, 0 });
		Log.i(TAG, "ready");
	}

	public static void setContrastTranslateOnly(ColorMatrix cm, float contrast) {
		float scale = contrast + 1.f;
		float translate = (-.5f * scale + .5f) * 255.f;
		cm.set(new float[] { 1, 0, 0, 0, translate, 0, 1, 0, 0, translate, 0, 0, 1, 0, translate, 0, 0, 0, 1, 0 });
	}

	/**
	 * 
	 * @param file
	 * @param contrast
	 * @return
	 */
	public static Bitmap setContrast(File file, float contrast) {
		Log.i(TAG, "Start preparing for setting contrast ");

		Bitmap bitmap = ConvertUtil.convertToBitmap(file);
		return setContrast(bitmap, contrast);
	}

	public static Bitmap setContrast(Bitmap bitmap, float contrast) {
		Log.d(TAG, "Setting contrast " + contrast);
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		// Bitmap bmpContrast = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		// cm.setSaturation(0f);
		float con = contrast;
		if (contrast < -1) {
			con = -1;
		}
		if (contrast > 1) {
			con = 1;
		}
		setContrast(cm, con);
		Bitmap resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height - 1);
		// Bitmap resultBitmap = null;
		try {
			// bitmap.recycle();
			// resultBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			Canvas c = new Canvas(resultBitmap);
			paint.setColorFilter(new ColorMatrixColorFilter(cm));
			c.drawBitmap(resultBitmap, 0, 0, paint);
		} catch (OutOfMemoryError e) {
			Log.e(TAG, "Out of memory error :");
		}
		// paint.setColorFilter(new ColorMatrixColorFilter(cm));
		bitmap.recycle();
		return resultBitmap;
	}

	public static Bitmap setColor(File file) {
		Bitmap sourceBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
		int width = sourceBitmap.getWidth();
		int height = sourceBitmap.getHeight();
		float[] colorTransform = { 0, 1f, 0, 0, 0, 0, 0, 0f, 0, 0, 0, 0, 0, 0f, 0, 0, 0, 0, 1f, 0 };

		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.setSaturation(0f); // Remove Colour
		colorMatrix.set(colorTransform); // Apply the Red

		ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
		Paint paint = new Paint();
		paint.setColorFilter(colorFilter);

		// Display display = getWindowManager().getDefaultDisplay();

		Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap, 0, (int) (height * 0.15), width, (int) (height * 0.75));

		// image.setImageBitmap(resultBitmap);

		Canvas canvas = new Canvas(resultBitmap);
		canvas.drawBitmap(resultBitmap, 0, 0, paint);
		return resultBitmap;
	}

	/**
	 * Create new greyscal Bitmap
	 * 
	 * @param bmpOriginal
	 * @return greyscal bitmap
	 */
	public static Bitmap toGrayscale(Bitmap bmpOriginal, BitmapFactory.Options opts) {
		Log.d(TAG, "toGrayscale()..." + bmpOriginal.toString());
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();

		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, opts.inPreferredConfig);
		Canvas c = new Canvas(bmpGrayscale);

		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);// remove color
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}

	private static float mAngle;
	private static final String TAG = ColorUtil.class.getSimpleName();
}
