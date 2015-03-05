package de.bht.bachelor.ocr;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import de.bht.bachelor.camera.CameraMode;
import de.bht.bachelor.language.LanguageManager;
import de.bht.bachelor.services.PreprocessingService;

public class PreprocessingServiceConnection {
	/**
	 * Constructor for calling service during photo mode
	 * 
	 * @param resultHandler
	 * @param languageManager
	 * @param cameraMode
	 * @param orgBitmapForCurrentRect
	 * @param currentOrgCropBitmap
	 * @param currentRect
	 */
	public PreprocessingServiceConnection(Handler resultHandler, LanguageManager languageManager, CameraMode cameraMode, Bitmap orgBitmapForCurrentRect, Bitmap currentOrgCropBitmap, Rect currentRect) {
		this.resultHandler = resultHandler;
		this.languageManager = languageManager;
		this.orgBitmapForCurrentRect = orgBitmapForCurrentRect;
		this.currentOrgCropBitmap = currentOrgCropBitmap;
		this.currentRect = currentRect;
		this.cameraMode = cameraMode;
	}

	/**
	 * Constructor for calling service during video mode
	 * 
	 * @param resultHandler
	 * @param languageManager
	 * @param cameraMode
	 * @param data
	 */
	public PreprocessingServiceConnection(Handler resultHandler, LanguageManager languageManager, CameraMode cameraMode, byte[] data) {
		this.resultHandler = resultHandler;
		this.languageManager = languageManager;
		this.data = data;
		this.cameraMode = cameraMode;
	}

	private final ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			try {
				Log.d(TAG, "onServiceConnected()");
				// This is called when the connection with the service has been
				// established, giving us the service object we can use to
				// interact with the service. Because we have bound to a explicit
				// service that we know is running in our own process, we can
				// cast its IBinder to a concrete class and directly access it.
				procesBinder = (PreprocessingService.PreprocessingBinder) service;
				procesBinder.setHandler(resultHandler);

				startTime = System.currentTimeMillis();
				new Thread(new Runnable() {
					@Override
					public void run() {
						procesBinder.createPix(orgBitmapForCurrentRect, currentOrgCropBitmap, currentRect);
					}
				}).start();
				procesBinder.setLanguageManager(languageManager);
				preprocessingService = procesBinder.getService();

			} catch (NullPointerException ex) {
				Log.d(TAG, "Error ", ex);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			preprocessingService = null;
			// Toast.makeText(PixProcessActivity.this, R.string.local_service_disconnected, Toast.LENGTH_SHORT).show();
		}
	};

	public ServiceConnection getmConnection() {
		return mConnection;
	}

	private final CameraMode cameraMode;
	private final LanguageManager languageManager;
	private Bitmap orgBitmapForCurrentRect = null;
	private Bitmap currentOrgCropBitmap = null;
	private Rect currentRect = null;
	private PreprocessingService preprocessingService;
	private PreprocessingService.PreprocessingBinder procesBinder;
	private long startTime;
	private boolean mIsBound;
	private static final String TAG = PreprocessingServiceConnection.class.getSimpleName();
	private final Handler resultHandler;
	private byte[] data;
}
