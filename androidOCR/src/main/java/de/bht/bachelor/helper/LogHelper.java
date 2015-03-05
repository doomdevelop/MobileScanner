package de.bht.bachelor.helper;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.MotionEvent;
import de.bht.bachelor.graphic.transform.MoveSpace;
import de.bht.bachelor.ui.Corner;

public class LogHelper {
	public static void dumpEvent(MotionEvent event) {
		Log.d(TAG, "-------------------------------------------");
		try {
			String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
			StringBuilder sb = new StringBuilder();
			int action = event.getAction();
			int actionCode = action & MotionEvent.ACTION_MASK;
			sb.append("event ACTION_").append(names[actionCode]);
			if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP) {
				sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
				sb.append(")");
			}
			sb.append("[");
			for (int i = 0; i < event.getPointerCount(); i++) {
				sb.append("#").append(i);
				sb.append("(pid ").append(event.getPointerId(i));
				sb.append(")=").append((int) event.getX(i));
				sb.append(",").append((int) event.getY(i));
				if (i + 1 < event.getPointerCount())
					sb.append(";");
			}
			sb.append("]");
			Log.d(TAG, sb.toString());
			Log.d(TAG, "-------------------------------------------");
		} catch (NullPointerException ex) {
			Log.d(TAG, "Could not print event info.", ex);
		}
	}

	public static void printBitmapDimension(Bitmap bitmap, String refName) {
		try {
			Log.d(TAG, "printBitmapDimension of " + refName);
			Log.d(TAG, "w : " + bitmap.getWidth());
			Log.d(TAG, "h : " + bitmap.getHeight());
		} catch (NullPointerException ex) {
			Log.d(TAG, "Could not print bitmap size.");
		}
	}

	public static void printCirclesDimensions(Corner[] circles) {
		Log.d(TAG, "printCirclesDimensions()...");
		try {
			for (Corner corner : circles) {
				Log.d(TAG, "x : " + corner.getX() + ", y : " + corner.getY());
			}
		} catch (NullPointerException ex) {
			Log.d(TAG, "Could not print circle dimensions.", ex);
		}
	}

	public static void testCircles(Corner[] c) {
		Log.d(TAG, "The circles test : ");
		try {
			for (Corner corner : c) {
				Log.d(TAG, "x= " + corner.getX() + ", y=" + corner.getY());
			}
		} catch (NullPointerException ex) {
			Log.d(TAG, "Could not print circle dimensions.", ex);
		}
	}

	public static void printMoveSpaceDimensions(MoveSpace moveSpace) {
		Log.d(TAG, "printMoveSpaceDimensions()..");
		try {
			Log.d(TAG, "minX: " + moveSpace.getMinX() + "; maxX: " + moveSpace.getMaxX());
			Log.d(TAG, "minY: " + moveSpace.getMinY() + "; maxY: " + moveSpace.getMaxY());
		} catch (NullPointerException ex) {
			Log.d(TAG, "Could not print dimensions.", ex);
		}
	}

	private static final String TAG = LogHelper.class.getSimpleName();
}
