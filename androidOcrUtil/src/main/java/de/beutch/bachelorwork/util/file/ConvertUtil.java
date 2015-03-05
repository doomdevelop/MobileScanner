package de.beutch.bachelorwork.util.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera.Parameters;
import android.util.Log;

public class ConvertUtil {

	public static Bitmap convertToBitmap(byte[] data) {
		// final BitmapFactory.Options opts = new BitmapFactory.Options();
		// opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		// return BitmapFactory.decodeByteArray(data, 0, data.length, opts);
		// byte[] data = getData();
		Bitmap bm = null;
		ByteArrayInputStream bytes = new ByteArrayInputStream(data);
		BitmapDrawable bmd = new BitmapDrawable(bytes);
		bm = bmd.getBitmap();
		return bm;
	}

	public static Bitmap convertToBitmap(File f) {
		Log.d(TAG, "Converting file to bitmap with conf 8888");
		Log.d(TAG, "convertToBitmap().." + f.getAbsolutePath());
		final BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
	}

	/**
	 * @param bmp
	 * @return
	 */
	public static byte[] convertToByteArray(Bitmap bmp) {
		// throwing RuntimeException: Buffer not large enough for pixels
		Log.d(TAG, "convert Bitmap ToByteArray");
		byte[] store = new byte[bmp.getWidth() * bmp.getHeight()];
		ByteBuffer buffer = ByteBuffer.wrap(store);
		bmp.copyPixelsToBuffer(buffer);
		return store;
	}

	public static byte[] covnertToByteArray(File file) {
		Log.d(TAG, "covnert File ToByteArray");
		if (file == null)
			return null;
		byte[] b = new byte[(int) file.length()];
		FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(b);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "can not read file!");
		} catch (RuntimeException ex) {
			Log.d(TAG, "can not read file!");
		}
		return b;
	}

	/**
	 * Convert bitmap with 4/2 or 1 channel to byte[] with 8 bits for each pixel, 2^8=256 colors
	 * 
	 * 
	 * @param bmp
	 * @return byte[]
	 */
	public static byte[] get8bpp(Bitmap bmp) {
		Log.d(TAG, "get8bpp()");
		// the issue of getting pix direct from byte[] is not solved
		// workaround :
		// http://code.google.com/p/tesseract-android-tools/issues/detail?id=5#c43

		int chanels = getNumberOfPixelChanels(bmp);
		if (chanels <= 0)
			throw new IllegalArgumentException("wrong number of channels in the pixel ");
		byte[] store = new byte[bmp.getWidth() * bmp.getHeight() * chanels];
		ByteBuffer buffer = ByteBuffer.wrap(store);
		bmp.copyPixelsToBuffer(buffer);

		byte[] bmpData = null;
		try {
			bmpData = new byte[store.length / chanels];
			int c = 0;
			for (int i = 0; i < store.length; i++) {
				if (i % chanels == 1) {
					bmpData[c] = store[i];
					c++;
				}
			}
			return bmpData;
		} catch (ArrayIndexOutOfBoundsException ex) {
			Log.d(TAG, "Could not get data !", ex);
		}
		return null;
	}

	/**
	 * The pixel can has different number of color channels. Full RGB has 4 channels.
	 * This function check configuration of passed parameter bitmap.
	 * Returning 2 for Bitmap.Config.RGB_565.
	 * Returning 4 for Bitmap.Config.ARGB_8888.
	 * Check for more {@link Bitmap.Config}.
	 * 
	 * @param bitmap
	 *            to computing number of channels in the pixel
	 * @return number of channels in the pixel
	 */
	private static int getNumberOfPixelChanels(Bitmap bitmap) {
		Log.d(TAG, "getNumberOfPixelChanels");

		Config conf = bitmap.getConfig();
		if (conf.equals(Bitmap.Config.ALPHA_8)) {
			Log.d(TAG, "ALPHA_8, return 1");
			return 1;
		} else if (conf.equals(Bitmap.Config.RGB_565)) {
			Log.d(TAG, "RGB_565, return 2");
			return 2;
		} else if (conf.equals(Bitmap.Config.ARGB_8888)) {
			Log.d(TAG, "ARGB_8888, return 4");
			return 4;
		}
		Log.d(TAG, "No conf, return 0");
		return 0;
	}

	/**
	 * Extract bytes from 8bpp bitmap.
	 * 
	 * @param bmp
	 *            8bpp bitmap
	 * @return data 8bpp or null
	 */
	public static byte[] bmp8ToData8(Bitmap bmp) {
		Log.d(TAG, "bmp8ToData8()");

		byte[] store = null;

		int chanels = getNumberOfPixelChanels(bmp);
		if (chanels <= 0)
			throw new IllegalArgumentException("wrong number of channels in the pixel ");

		try {
			store = new byte[bmp.getWidth() * bmp.getHeight() * chanels];
			ByteBuffer buffer = ByteBuffer.wrap(store);
			bmp.copyPixelsToBuffer(buffer);
		} catch (ArrayIndexOutOfBoundsException ex) {
			Log.d(TAG, "Could not get data !", ex);
		}
		return store;
	}

	/**
	 * Convert full color data to 8bpp data
	 * 
	 * @param full
	 *            color data
	 * @return 8 bpp data or nul
	 */
	public static byte[] get8bpp(byte[] data, int depth) {
		Log.d(TAG, "get8bpp()");
		byte[] bmpData = null;

		if (depth < 0)
			throw new IllegalArgumentException("Cann not comupute with depth " + depth);
		if (data == null)
			throw new IllegalArgumentException("Cann not comupute, data is null");

		try {
			bmpData = new byte[data.length / depth];

			int c = 0;
			for (int i = 0; i < data.length; i++) {
				if (i % depth == 1) {
					bmpData[c] = data[i];
					c++;
				}
			}
			return bmpData;
		} catch (ArrayIndexOutOfBoundsException ex) {
			Log.d(TAG, "Could not get data !", ex);
		}
		return null;
	}

	public static byte[] getAvarage(Bitmap bmp) {
		Log.d(TAG, "convert getAvarage() .... ");
		// the issue of getting pix from byte[] is not solved
		// workaround :
		// http://code.google.com/p/tesseract-android-tools/issues/detail?id=5#c43
		// http://books.google.de/books?id=3u5hboQaJAYC&lpg=PA240&ots=4AGzZt2UjB&dq=java%2Br%20%3D%20%200xff0000)%20%3E%3E%2016%3B&hl=de&pg=PA244#v=onepage&q=java+r%20=%20%200xff0000)%20%3E%3E%2016;&f=true

		int r, g, b, fourBytes, ii = 0, sum;
		int chanels = getNumberOfPixelChanels(bmp);
		long start = System.currentTimeMillis();
		long stop;

		if (chanels <= 0)
			throw new IllegalArgumentException("wrong number of channels in the pixel ");
		/**
		 * byte == 8 bit , so storing all 4 channels of rgb(32bit) in byte[] mean ,every channel is storing separately
		 **/
		// if (chanels == 3 || chanels == 4)
		// return toGreyscal(bmp, chanels);

		byte[] store = new byte[bmp.getWidth() * bmp.getHeight() * chanels];
		ByteBuffer buffer = ByteBuffer.wrap(store);
		bmp.copyPixelsToBuffer(buffer);

		byte[] bmpData = new byte[store.length / chanels];
		try {
			for (int i = 0; i < store.length; i++) {
				if (i % chanels == 1) {
					fourBytes = store[i];

					r = (fourBytes & 0xff0000) >> 16;
					g = (fourBytes & 0x00ff00) >> 8;
					b = (fourBytes & 0x0000ff);
					sum = (int) (r * 0.299 + g * 0.587 + b + 0.114);// ((r + g + b) / 3);
					if (sum < 0)
						sum = 0;
					if (sum > 255)
						sum = 255;
					bmpData[ii] = (byte) sum;
					ii++;
				}
			}
			stop = System.currentTimeMillis() - start;
			Log.d(TAG, "converted in " + stop + " ms");
			return bmpData;
		} catch (Exception e) {
			Log.d(TAG, "Could not convert rgb to 8", e);
		}
		return null;
	}

	private static byte[] toGreyscal(Bitmap bmp, int chanels) {
		Log.d(TAG, "convert toGreyscal() .... ");
		int ii = 0, index = 0, sum;
		byte r, g, b;
		long start = System.currentTimeMillis();
		long stop;

		if (chanels != 3 && chanels != 4)
			throw new IllegalArgumentException("wrong number of channels in the pixel. Supert just 4 chanles ARGB and 3 chanels RGB ");

		byte[] store = new byte[bmp.getWidth() * bmp.getHeight() * 4];
		ByteBuffer buffer = ByteBuffer.wrap(store);
		bmp.copyPixelsToBuffer(buffer);
		byte[] bmpData = new byte[store.length / 4];

		for (int i = 0; i < store.length; i++) {

			// if four channels, skip alpha
			if (chanels == 4)
				index++;

			// read R,G,B,
			r = store[index++];
			g = store[index++];
			b = store[index++];

			sum = (int) (r * 0.299 + g * 0.587 + b + 0.114);

			bmpData[ii++] = (byte) sum;
		}
		stop = System.currentTimeMillis() + start;
		Log.d(TAG, "converted in " + stop + " ms");
		return bmpData;
	}

	public static Bitmap fileToBitmap(File file) {
		Log.i("showImage", "loading:" + file.getAbsolutePath());
		BitmapFactory.Options bfOptions = new BitmapFactory.Options();
		bfOptions.inDither = false; // Disable Dithering mode
		bfOptions.inPurgeable = true; // Tell to gc that whether it needs free memory, the Bitmap can be cleared
		bfOptions.inInputShareable = true; // Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
		bfOptions.inTempStorage = new byte[32 * 1024];

		FileInputStream fs = null;
		Bitmap bm = null;
		try {
			fs = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO do something intelligent
			Log.e(TAG, "Could not found file !", e);
		}

		try {
			if (fs != null)
				bm = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
		} catch (IOException e) {
			// TODO do something intelligent
			Log.e(TAG, "Could not load file !", e);
		} finally {
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "Could not close fileStream !", e);
				}
			}
		}
		// bm=BitmapFactory.decodeFile(path, bfOptions); This one causes error: java.lang.OutOfMemoryError: bitmap size exceeds VM budget

		// im.setImageBitmap(bm);
		// bm.recycle();
		return bm;
	}

	/**
	 * 
	 * @param bitmap
	 *            src bitmap to convert
	 * @return bitmap with conf ARGB_8888
	 */
	public static Bitmap convetrToBitmap8888(Bitmap bitmap) {

		if (bitmap == null)
			throw new NullPointerException("Bitmap to convert has value null !");
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Bitmap b8888 = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		int[] pixels = new int[w * h];
		bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
		b8888.setPixels(pixels, 0, w, 0, 0, w, h);
		return b8888;
	}

	/**
	 * Workaround for creating by crop a bitmap with conf 8888
	 * http://android.amberfog.com/?p=430
	 * 
	 * @param rect
	 *            rect to witch size the src bitmap will be cropped
	 * @param src
	 *            source bitmap
	 * @param width
	 *            of new cropped bitmap
	 * @param hight
	 *            of new cropped bitmap
	 * @return new cropped bitmap with size equal rect size and conf ARGB_8888
	 */
	public static Bitmap cropToRect8888(Rect rect, Bitmap src, float width, float hight) {
		int w = (int) width;
		int h = (int) hight;
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		int[] pixels = new int[w * h];
		src.getPixels(pixels, 0, w, rect.left, rect.top, w, h);
		bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
		return bitmap;
	}

	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

		int width = bm.getWidth();

		int height = bm.getHeight();

		float scaleWidth = ((float) newWidth) / width;

		float scaleHeight = ((float) newHeight) / height;

		// create a matrix for the manipulation

		Matrix matrix = new Matrix();

		// resize the bit map

		matrix.postScale(scaleWidth, scaleHeight);

		// recreate the new Bitmap

		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

		return resizedBitmap;

	}

	public Bitmap getResizeFromData(byte[] data) {
		// http://stackoverflow.com/questions/3331527/android-resize-a-large-bitmap-file-to-scaled-output-file
		BitmapFactory.Options options = new BitmapFactory.Options();
		InputStream is = null;
		is = new ByteArrayInputStream(data);// new FileInputStream(path_to_file);

		// here w and h are the desired width and height
		options.inSampleSize = Math.max(options.outWidth / 640, options.outHeight / 480);
		// bitmap is the resized bitmap
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
		return bitmap;
	}

	/********************************************** Decode Data *****************************************************/

	private Bitmap convertDataToBitmap(byte[] data, Parameters parameters, boolean useAndroidAPI) {
		Bitmap bitmap = null;
		int w = 0, h = 0;
		int[] rgb = new int[w * h];
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		w = parameters.getPreviewSize().width;
		h = parameters.getPreviewSize().height;
		if (useAndroidAPI) {
			decodeYUVAndroidAPI(data, outputStream, parameters);
			bitmap = BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size());
		} else {
			decodeYUV420SP(rgb, data, w, h);
			// TODO : get bitmap from int[] (pixels)
		}
		// new FileUtil().saveBitmapAsFile(bitmap, new File(de.beutch.bachelorwork.util.file.Path.MAIN_PICTURES_PATH + "RealTimeBimtap_" + System.currentTimeMillis() + ".jpg"),
		// Bitmap.CompressFormat.JPEG);
		return bitmap;
	}

	/*
	 * This method was a bit faster than decodeYUV420SP(), but need more test !
	 */
	public static void decodeYUVAndroidAPI(byte[] data, ByteArrayOutputStream outputStream, Parameters parameters) {
		int w, h, imageFormat;
		Rect rect;
		imageFormat = parameters.getPreviewFormat();
		w = parameters.getPreviewSize().width;
		h = parameters.getPreviewSize().height;
		Log.d(TAG, "decodeYUV: decode data of picture, width : " + w + ", hight : " + h);
		YuvImage yuvImage = new YuvImage(data.clone(), imageFormat, w, h, null);
		rect = new Rect(0, 0, w, h);
		yuvImage.compressToJpeg(rect, 100, outputStream);
	}

    /**
     * Converts YUV420 NV21 to RGB8888
     *
     * @param data byte array on YUV420 NV21 format.
     * @param width pixels width
     * @param height pixels height
     * @return a RGB8888 pixels int array. Where each int is a pixels ARGB.
     */
    public static int[] convertYUV420_NV21toRGB8888(byte [] data, int width, int height) {
        int size = width*height;
        int offset = size;
        int[] pixels = new int[size];
        int u, v, y1, y2, y3, y4;

        // i percorre os Y and the final pixels
        // k percorre os pixles U e V
        for(int i=0, k=0; i < size; i+=2, k+=2) {
            y1 = data[i  ]&0xff;
            y2 = data[i+1]&0xff;
            y3 = data[width+i  ]&0xff;
            y4 = data[width+i+1]&0xff;

            u = data[offset+k  ]&0xff;
            v = data[offset+k+1]&0xff;
            u = u-128;
            v = v-128;

            pixels[i  ] = convertYUVtoRGB(y1, u, v);
            pixels[i+1] = convertYUVtoRGB(y2, u, v);
            pixels[width+i  ] = convertYUVtoRGB(y3, u, v);
            pixels[width+i+1] = convertYUVtoRGB(y4, u, v);

            if (i!=0 && (i+2)%width==0)
                i+=width;
        }

        return pixels;
    }
    private static int convertYUVtoRGB(int y, int u, int v) {
        int r,g,b;

        r = y + (int)1.402f*v;
        g = y - (int)(0.344f*u +0.714f*v);
        b = y + (int)1.772f*u;
        r = r>255? 255 : r<0 ? 0 : r;
        g = g>255? 255 : g<0 ? 0 : g;
        b = b>255? 255 : b<0 ? 0 : b;
        return 0xff000000 | (b<<16) | (g<<8) | r;
    }

	/**
	 * 
	 * @param rgb
	 *            pixels decoded from yuv data
	 * @param yuv420sp
	 *            data of the picture
	 * @param width
	 * @param height
	 */
	public static void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
		// Pulled directly from:
		// http://ketai.googlecode.com/svn/trunk/ketai/src/edu/uic/ketai/inputService/KetaiCamera.java
		Log.d(TAG, "decodeYUV420SP: decode data of picture, width : " + width + ", hight : " + height);
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

				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
	}

	private static final String TAG = ConvertUtil.class.getSimpleName();
}
