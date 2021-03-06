package de.bht.bachelor.message;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ServiceMessenger {
    public static final int MSG_TAKE_PICURE = 0;
	public static final int MSG_PICTURE_FROM_CAMRA = 1;
    public static final int MSG_CAMERA_ON_FOUCUS = 2;
	public static final int MSG_OCR_BOX_VIEW = 3;
	public static final int MSG_OCR_RESULT = 4;
	public ServiceMessenger(Handler handler) {
		this.handler = handler;
	}

	public void sendMessage(int arg, int what, Object obj) {
		if (handler == null)
			throw new NullPointerException("Could not set message ! handler has value null !");
		Log.d(TAG, "send message ! : "+what);
		Message resultMsg = new Message();
		resultMsg.what = what;
		resultMsg.arg1 = arg;
		if (obj != null)
			resultMsg.obj = obj;
		handler.sendMessage(resultMsg);
	}

	private static final String TAG = ServiceMessenger.class.getSimpleName();
	private final Handler handler;
}
