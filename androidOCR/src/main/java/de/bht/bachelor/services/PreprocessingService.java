package de.bht.bachelor.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.googlecode.leptonica.android.Convert;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;

import de.beutch.bachelorwork.util.color.ColorUtil;
import de.beutch.bachelorwork.util.file.ConvertUtil;
import de.bht.bachelor.camera.OrientationMode;
import de.bht.bachelor.helper.ChangeActivityHelper;
import de.bht.bachelor.helper.Timer;
import de.bht.bachelor.language.LanguageManager;
import de.bht.bachelor.message.ServiceMessenger;
import de.bht.bachelor.ocr.OCR;

/**
 * This Class extend a {@link Service} and this way has a Service Structure.
 * It prepare Picture from camera and call OCR.
 * 
 * @author Andrzej Kozlowski
 * 
 */
public class PreprocessingService extends Service {
	/*
	 * Source for the code archritecture :
	 * http://developer.android.com/guide/topics/fundamentals/services.html
	 */
	@Override
	public void onCreate() {
		// The service is being created
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// The service is starting, due to a call to startService()
		return mStartMode;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// A client is binding to the service with bindService()
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// All clients have unbound with unbindService()
		return mAllowRebind;
	}

	@Override
	public void onRebind(Intent intent) {
		// A client is binding to the service with bindService(),
		// after onUnbind() has already been called
	}

	@Override
	public void onDestroy() {
		// The service is no longer used and is being destroyed
	}

	Runnable mTask = new Runnable() {

		@Override
		public void run() {
			// prepareDataForOCR(data);

		}

	};

	public class PreprocessingBinder extends Binder {

		public void cancelOcr() {
			Log.d(TAG, "cancelOcr()...");
			ocr.setCancelResult(true);
		}

		public void createPix(Bitmap orgBitmap, Bitmap bm, Rect rect) {
			ServiceMessenger serviceMessenger;
			Timer timer;

			OrientationMode orientationMode;
			if (bm == null) {
				Log.d(TAG, "No cropped bitmap, run with original image ");
				bm = orgBitmap;
			}
			if (rect != null)
				Log.d(TAG, "Rect dimesions width = " + ((rect.right) - (rect.left)) + "hight = " + (rect.bottom - rect.top));
			Log.d(TAG, "org bitmap width = " + orgBitmap.getWidth() + ", height = " + orgBitmap.getHeight());
			Log.d(TAG, "org bitmap conf :" + orgBitmap.getConfig().name());
			Log.d(TAG, "croped bitmap width = " + bm.getWidth() + ", height = " + bm.getHeight());
			Log.d(TAG, "croped bitmap conf :" + bm.getConfig().name());
			timer = new Timer();
			try {
				// ****************** test************
				// runTestsOCR(orgBitmap, bm, rect);
				// **********************************

				ocr = new OCR(handler);
				timer.start();
				orientationMode = ChangeActivityHelper.getInstance().getOrientationMode();
				if (orientationMode == OrientationMode.PORTRAIT || orientationMode == OrientationMode.PORTRAIT_UPSIDE_DOWN) {
					ocr.runWithCropped8888(rotate(bm, 90));
				} else {
					ocr.runWithCropped8888(bm);
				}
				timer.stop();
				Log.d(TAG, "The recognising by cropped converted image took " + timer.getRunTime() + " ms");
			} catch (NullPointerException ex) {
				Log.d(TAG, "Could not run OCTR!", ex);
				// there was an error , go back
				serviceMessenger = new ServiceMessenger(handler);
				serviceMessenger.sendMessage(0, 0, new String(""));
			} catch (RuntimeException e) {
				Log.d(TAG, "Could not convert : pix = ReadFile.readBytes8(data, width, height)", e);
			}
		}

		public Bitmap rotate(Bitmap b, int degrees) {
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

		private Bitmap setContrast(Bitmap bitmap) {
			return ColorUtil.setContrast(bitmap, 0.7f);
		}

		private Pix preparePixForOCR(byte[] data, int width, int hight) {
			Log.d(TAG, "Try read data to Pix");
			Pix pix = null;
			try {
				pix = ReadFile.readBytes8(data, width, hight);
				if (pix.getDepth() != 8) {
					Log.d(TAG, "Need convert depth of Pix to 8bp");
					pix = Convert.convertTo8(pix);
				}
			} catch (Exception ex) {
				Log.d(TAG, "Cold not run OCR !", ex);
			}
			return pix;
		}

		/*
		 * This method run ocr in 3 different ways to test runtime and quality of the result
		 */
		private void runTestsOCR(Bitmap orgBitmap, Bitmap bm, Rect rect) throws NullPointerException {
			// ************************************* Tests *******************************************
			byte[] data;
			ServiceMessenger serviceMessenger;
			Timer timer = null;
			OCR ocr;

			timer = new Timer();
			ocr = new OCR(handler);
			timer.start();
			ocr.testWithRect(orgBitmap, rect);
			timer.stop();
			Log.d(TAG, "The recognising by rect and org image took " + timer.getRunTime() + " ms");

			ocr = new OCR(handler);
			timer.start();
			data = ConvertUtil.getAvarage(bm);
			ocr.testWithCruppedBitmapAsPix(preparePixForOCR(data, bm.getWidth(), bm.getHeight()));
			timer.stop();
			Log.d(TAG, "The recognising by cropped converted image took " + timer.getRunTime() + " ms");

			ocr = new OCR(handler);
			timer.start();
			ocr.testWithCropped8888(bm);
			timer.stop();
			Log.d(TAG, "The recognising by cropped converted image took " + timer.getRunTime() + " ms");

			serviceMessenger = new ServiceMessenger(handler);
			serviceMessenger.sendMessage(0, 0, new String(""));
			// ************************************* End Tests *******************************************
		}

		public PreprocessingService getService() {
			return PreprocessingService.this;
		}

		public byte[] getData() {
			return data;
		}

		public void setData(byte[] data) {
			this.data = data;
		}

		public void setHandler(Handler handler) {
			this.handler = handler;
		}

		public LanguageManager getLanguageManager() {
			return languageManager;
		}

		public void setLanguageManager(LanguageManager languageManager) {
			this.languageManager = languageManager;
		}

		private LanguageManager languageManager;
		private Handler handler;
		private byte[] data;
		private byte[] b;
		private OCR ocr;

	}

	private static final String TAG = PreprocessingService.class.getSimpleName();
	private Handler handler;
	private int mStartMode; // indicates how to behave if the service is killed
	private final IBinder mBinder = new PreprocessingBinder(); // interface for clients that bind
	private boolean mAllowRebind; // indicates whether onRebind should be used

}
