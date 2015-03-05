package de.bht.bachelor.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.tesseract.android.TessBaseAPI;

import de.beutch.bachelorwork.util.file.Path;
import de.bht.bachelor.language.LanguageManager;
import de.bht.bachelor.message.ServiceMessenger;
import de.bht.bachelor.setting.AppSetting;

public class OCRService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public class OCRBinder extends Binder {
		public OCRService getService() {
			return OCRService.this;
		}

		private boolean initAPI() {
			Log.d(TAG, "init...");
			Log.d(TAG, "Environment.getExternalStorageDirectory() : " + Environment.getExternalStorageDirectory());
			api = new TessBaseAPI();
			// boolean b = api.setVariable(TessBaseAPI.VAR_ACCURACYVSPEED, "100");
			boolean resultInit = api.init(Path.MAIN_LANGUAGE_PATH, languageManager.getCurrentOcrLanguage());
			// boolean resultInit = api.init("/mnt/sdcard/tesseract/", "deu-frak");
			return resultInit;
		}

		public void runTesseract(Pix pix) {
			String result;
			int[] wordConfidences;
			int meanConfidences;
			int bestConfidences = 0;

			try {
				if (initAPI() && pix != null && pix.getNativePix() > 0) {

					api.setImage(pix);
					wordConfidences = api.wordConfidences();
					meanConfidences = api.meanConfidence();
					Log.d(TAG, "wordConfidences size = " + wordConfidences.length + ", meanConfidences = " + meanConfidences);
					Log.d(TAG, "best confidences = " + bestConfidences);
					result = api.getUTF8Text();
					ServiceMessenger serviceMessenger = new ServiceMessenger(handler);
					serviceMessenger.sendMessage(0, 0, result);
				}
			} catch (NullPointerException e) {
				Log.e(TAG, "Could not run tesseract !", e);
			} catch (RuntimeException e) {
				Log.e(TAG, "Could not run tesseract !", e);
			} catch (Exception e) {
				Log.e(TAG, "Could not run tesseract !", e);
			} finally {
				pix.recycle();
				api.clear();
				api.end();
			}
		}

		public void setHandler(Handler handler) {
			this.handler = handler;
		}

		private final LanguageManager languageManager = AppSetting.getInstance().getLanguageManager();;
		private TessBaseAPI api;
		private Handler handler;
	}

	private static final String TAG = OCRService.class.getSimpleName();
}
