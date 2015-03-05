package de.beutch.bachelorwork.util.pixel;

import android.graphics.Bitmap;

public class PixelUtil {
	public boolean hasBlackPixel(Bitmap bitmap) throws OutOfMemoryError {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		int[] pixels = new int[w * h];
		bitmap.getPixels(pixels, 0, w, 0, 0, w, h);// exeption wrong argument
		if (index == -1)
			index = 0;
		for (int i = index; i < pixels.length; i++) {

			int c = pixels[i];

			int r = (c & 0xff0000) >> 16;
			int g = (c & 0x00ff00) >> 8;
			int b = (c & 0x0000ff);

			if (r + g + b == 0) {
				index = i;
				punktH = index / pixels.length / w;
				punktW = index - (punktH * w);
				return true;
			}
		}

		return false;
	}

	public int getIndex() {
		return index;
	}

	public int getPunktW() {
		return punktW;
	}

	public int getPunktH() {
		return punktH;
	}

	public void resetPunkts() {
		punktH = 0;
		punktW = 0;
	}

	public void resetAll() {
		index = -1;
		punktH = 0;
		punktW = 0;
	}

	private static final String TAG = PixelUtil.class.getSimpleName();

	private int index = -1;
	private int punktW;
	private int punktH;
}
