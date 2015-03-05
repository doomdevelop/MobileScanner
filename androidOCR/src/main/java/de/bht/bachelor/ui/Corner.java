package de.bht.bachelor.ui;

import java.io.Serializable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import de.bht.bachelor.beans.Dimension;
import de.bht.bachelor.graphic.PaintCreator;
import de.bht.bachelor.graphic.transform.Vector;
import de.bht.bachelor.helper.LogHelper;

public class Corner extends View implements View.OnTouchListener, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2121288435873267422L;

	public Corner(Context context) {
		super(context);
	}

	public Corner(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public Corner(Context context, AttributeSet attrs, int defStyle, int cornerID) {
		super(context, attrs, defStyle);
	}

	public Corner(Context context, final int cornerID, Dimension dimension) {
		super(context);
		this.cornerID = cornerID;
		setDimension(dimension);
		setOnTouchListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		touchX = event.getX();
		touchY = event.getY();

		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			if (MOVE_CIRCLE || MOVE_IMAGE_FRAME) {
				Log.d(TAG, "MOVE is true , break !");
				break;
			}
			if (imageFrame != null && imageFrame.isTouched((int) touchX, (int) touchY)) {
				MOVE_IMAGE_FRAME = true;
				imageFrame.setTouchX(touchX);
				imageFrame.setTouchY(touchY);
				break;
			}
			if (v instanceof Corner) {
				if (!isTouched((int) touchX, (int) touchY))
					return false;
				Log.d(TAG, "onTouch() Action.DOWN ; Corner id " + cornerID + ", x = " + x + ", y = " + y);
				LogHelper.dumpEvent(event);
				ImageFrame.TOUCHED_CORNER = (Corner) v;
				x = touchX;
				y = touchY;
				MOVE_CIRCLE = true;
			}
			break;
		case MotionEvent.ACTION_UP:
			Log.d(TAG, "up");
		case MotionEvent.ACTION_CANCEL:
			Log.d(TAG, "up");
		default:
			break;
		}
		return false;
	}

	public boolean isTouched(int touchX, int touchY) {
		if ((x + 20 < touchX || x - 20 > touchX) || (y + 20 < touchY || y - 20 > touchY)) {
			return false;// didn't touch this circle and around 20 px
		}
		return true;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// canvas.save();
		// not working
		canvas.drawArc(getCircleRect(), 0, 270, true, PaintCreator.createStrokePaintForArc());
		// working well
		// canvas.drawRect(getCircleRect(), PaintCreator.createStrokePaintForArc());
		// working well
		canvas.drawCircle(x, y, RADIUS, PaintCreator.createCircleFillPaint());
		move = false;
		// canvas.restore();
	}

	private RectF getCircleRect() {
		float left, top, right, bottom;
		left = x - RADIUS;
		right = x + RADIUS;
		top = y + RADIUS;
		bottom = y - RADIUS;
		RectF rect = new RectF();
		rect.set(left, top, right, bottom);
		return rect;

	}

	public void draw() {
		invalidate();
		requestLayout();
	}

	/**
	 * The way of calculating is :
	 * 
	 * x = (x + vector.getTranslation().getX());
	 * y = (y + vector.getTranslation().getY());
	 * 
	 * @param vector
	 *            witch will translate this circle
	 */
	public void translate(Vector vector) {
		x += vector.getTranslation().getX();
		y += vector.getTranslation().getY();
	}

	/**
	 * Calculating translation but not changing values of this circle
	 * 
	 * The way of calculating is :
	 * 
	 * x = (x + vector.getTranslation().getX());
	 * 
	 * y = (y + vector.getTranslation().getY());
	 * 
	 * @param vector
	 *            witch will translate this circle
	 * @return Dimension with new calculated x / y
	 */
	public Dimension simulateTranslate(Vector vector) {
		float xTemp = (x + vector.getTranslation().getX());
		float yTemp = (y + vector.getTranslation().getY());
		return new Dimension(xTemp, yTemp);
	}

	public void backUpDimension() {
		this.backUpDimension = new Dimension(getDimension());
	}

	public void restoreDimension() {
		this.x = backUpDimension.getX();
		this.y = backUpDimension.getY();
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getTouchX() {
		return touchX;
	}

	public void setTouchX(float touchX) {
		this.touchX = touchX;
	}

	public float getTouchY() {
		return touchY;
	}

	public void setTouchY(float touchY) {
		this.touchY = touchY;
	}

	public void setMove(boolean move) {
		this.move = move;
	}

	public boolean isMove() {
		return move;
	}

	public int getCornerID() {
		return cornerID;
	}

	public ImageFrame getImageFrame() {
		return imageFrame;
	}

	public void setImageFrame(ImageFrame imageFrame) {
		this.imageFrame = imageFrame;
	}

	public Dimension getDimension() {
		return new Dimension(this.x, this.y);
	}

	public void setDimension(Dimension dimension) {
		this.x = dimension.getX();
		this.y = dimension.getY();
	}

	public float[] toArray() {
		float[] arr = new float[2];
		arr[0] = x;
		arr[1] = y;
		return arr;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Log.d(TAG, "onSaveInstanceState().......");
		Bundle bundle = new Bundle();
		try {
			bundle.putParcelable("instanceState", super.onSaveInstanceState());
			bundle.putFloat("x", this.x);
			bundle.putFloat("y", this.y);
			bundle.putInt("circleID", cornerID);
			bundle.putFloat("touchX", this.touchX);
			bundle.putFloat("touchY", this.touchY);
			bundle.putBoolean("move", move);
			bundle.putBoolean("MOVE_CIRCLE", MOVE_CIRCLE);
			bundle.putBoolean("MOVE_IMAGE_FRAME", MOVE_IMAGE_FRAME);

			bundle.putSerializable("backUpDimension", backUpDimension);
		} catch (RuntimeException ex) {
			Log.e(TAG, "could not put in to bundle", ex);
		}
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		Log.d(TAG, "onRestoreInstanceState().......");
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			this.x = bundle.getFloat("x");
			this.y = bundle.getFloat("y");
			this.cornerID = bundle.getInt("circleID");
			this.touchX = bundle.getFloat("touchX");
			this.touchY = bundle.getFloat("touchY");
			this.move = bundle.getBoolean("move");
			MOVE_CIRCLE = bundle.getBoolean("MOVE_CIRCLE");
			MOVE_IMAGE_FRAME = bundle.getBoolean("MOVE_IMAGE_FRAME");

			this.backUpDimension = (Dimension) bundle.getSerializable("backUpDimension");

			super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
			return;
		}

		super.onRestoreInstanceState(state);
	}

	private ImageFrame imageFrame;
	private float x;
	private float y;
	private static final float RADIUS = 20f;
	private int cornerID;
	private static final String TAG = Corner.class.getSimpleName();
	private float touchX;
	private float touchY;
	private boolean move = false;
	public static boolean MOVE_CIRCLE = false;
	public static boolean MOVE_IMAGE_FRAME = false;
	private Dimension backUpDimension;

}
