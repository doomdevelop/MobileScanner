package de.bht.bachelor.ui;

import java.io.Serializable;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import de.bht.bachelor.beans.Dimension;
import de.bht.bachelor.beans.ImageSize;
import de.bht.bachelor.beans.MovingCorners;
import de.bht.bachelor.exception.IllegalDimensionException;
import de.bht.bachelor.graphic.transform.MoveSpace;
import de.bht.bachelor.graphic.transform.Vector;
import de.bht.bachelor.helper.LogHelper;

/**
 * This class represent image frame and provide some methods to check touch and move of this view and other dependent UI components like image frame corners.
 * 
 * @see Corner
 * 
 * @author andrzej kozlowski kozlowski76@yahoo.de
 * 
 */
public class ImageFrame extends View implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8550413559999797624L;

	public ImageFrame(Context context, Dimension[] dimensions) {
		super(context);
		this.context = context;
		create(dimensions);
		// TODO Auto-generated constructor stub
	}

	public ImageFrame(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ImageFrame(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	private void create(Dimension[] dimensions) {
		if (corners.length != dimensions.length)
			throw new IllegalArgumentException("Number of corners is not equals to the number of diemnsions");
		for (int i = 0; i < corners.length; i++) {
			Corner corner = new Corner(context, i, dimensions[i]);

			corners[i] = corner;
		}
	}

	/**
	 * Compute move vector of last touched circle for parallel frame moving.
	 * The new Dimmension is passed, witch is an End Point.
	 * To calculate a vector we need start point.
	 * Also for parallel moving, one, or two more Circles can be moved , this is when
	 * for 2 Circles : Vx != 0 and Vy != 0
	 * for 1 Circles : Vx != 0 or Vy != 0
	 * no move : Vx == 0 and Vy == 0
	 * where V is a Vector.
	 * 
	 * @param ID
	 * @param dimension
	 */
	public void computeParallelMove(int ID, Dimension dimension, ViewGroup viewGroup, ImageSize imageSize, MoveSpace moveSpace) {

		// removeFrame();
		Corner parallel;
		MovingCorners movingCorners;
		try {
			Corner corner = corners[ID];// touched circle
			Vector vector = new Vector(corner.getDimension(), dimension);
			Dimension transDimension = vector.getTranslation();
			// backup dimensions, if move will be invalid the old dimension will be set back
			corner.backUpDimension();
			// transformed parallel circle to touched one
			movingCorners = transforme(ID, transDimension, vector, viewGroup);

			if (movingCorners != null) {
				parallel = movingCorners.getParallelCorner();
				parallel.backUpDimension();

				try {
					moveCorner(corner, vector, imageSize, moveSpace);

					onCorrectMove(corner, parallel, movingCorners);
				} catch (IllegalDimensionException ex) {

					corner.restoreDimension();

					try {
						moveCorner(corner, new Vector(corner.getDimension(), new Dimension(movedX, movedY)), imageSize, moveSpace);

						onCorrectMove(corner, parallel, movingCorners);
					} catch (IllegalDimensionException e) {
						corner.restoreDimension();
						parallel.restoreDimension();
					}
				}

			}
		} catch (NullPointerException ex) {
			Log.d(TAG, "Could not correct compute parallel move", ex);
		} finally {
			// addFrame();
		}
	}

	private void onCorrectMove(Corner corner, Corner parallel, MovingCorners movingCorners) {
		checkMinDistansToNeighbourCircle(corner);
		checkCirclesDimensions(corner, parallel);
		checkSecondCorner(movingCorners, corner);
	}

	private MovingCorners transforme(int ID, Dimension transDimension, Vector vector, ViewGroup viewGroup) {
		Corner parallelX = null;
		Corner parallelY = null;
		Dimension backUpParallelDimesion = null;
		if (transDimension.getX() == 0 && transDimension.getY() == 0) {
			return null;// there was no move
		} else if (transDimension.getX() != 0 && transDimension.getY() == 0) {
			parallelX = getXParallelMovingCircle(ID);
			backUpParallelDimesion = new Dimension(parallelX.getX(), parallelX.getY());
			viewGroup.removeView(parallelX);
			// set new dimension
		} else if (transDimension.getX() == 0 && transDimension.getY() != 0) {
			parallelY = getYParallelMovingCircle(ID);
			backUpParallelDimesion = new Dimension(parallelY.getX(), parallelY.getY());
			viewGroup.removeView(parallelY);
		} else if (transDimension.getX() != 0 && transDimension.getY() != 0) {
			if (Math.abs(transDimension.getX()) >= Math.abs(transDimension.getY())) {
				vector.ignoreY();
				parallelX = getXParallelMovingCircle(ID);
				backUpParallelDimesion = new Dimension(parallelX.getX(), parallelX.getY());
				viewGroup.removeView(parallelX);
			} else {
				vector.ignoreX();
				parallelY = getYParallelMovingCircle(ID);
				backUpParallelDimesion = new Dimension(parallelY.getX(), parallelY.getY());
				viewGroup.removeView(parallelY);
			}
		}
		if (parallelX != null) {
			return new MovingCorners(parallelX, getYParallelMovingCircle(ID), MovingCorners.X_PARALLEL_MOVING_CORNER, backUpParallelDimesion);
		}
		if (parallelY != null) {
			return new MovingCorners(parallelY, getXParallelMovingCircle(ID), MovingCorners.Y_PARALLEL_MOVING_CORNER, backUpParallelDimesion);
		}
		return null;
	}

	private void checkSecondCorner(MovingCorners movingCorners, Corner touchCorner) {
		// Corner parallel;
		Corner second;
		switch (movingCorners.getCoordinateOfparallelMovingCorner()) {
		case MovingCorners.X_PARALLEL_MOVING_CORNER:
			// parallel = movingCorners.getParallelCorner();
			second = movingCorners.getSecondCorner();
			if (touchCorner.getY() != second.getY()) {
				second.setY(touchCorner.getY());
			}
			break;
		case MovingCorners.Y_PARALLEL_MOVING_CORNER:
			second = movingCorners.getSecondCorner();
			if (touchCorner.getX() != second.getX()) {
				second.setX(touchCorner.getX());
			}
			break;
		}
	}

	/**
	 * Check if touched space belong to the Image Frame
	 * 
	 * @param touchedX
	 *            X value of touched image
	 * @param touchedY
	 *            Y value of touched image
	 * @return true if X and Y are part of the Image Frame
	 */
	public boolean isTouched(int touchedX, int touchedY) {
		Log.d(TAG, "isTouched()....");
		if (touchedX >= corners[0].getX() + 20 || touchedX >= corners[3].getX() + 20) {
			Log.d(TAG, "the left Side is ok");
			if (touchedX <= corners[1].getX() - 20 || touchedX <= corners[2].getX() - 20) {
				Log.d(TAG, "the right side is ok");
				if (touchedY >= corners[0].getY() + 20 || touchedY >= corners[1].getY() + 20) {
					Log.d(TAG, "top is ok");
					if (touchedY <= corners[3].getY() - 20 || touchedY <= corners[2].getY() - 20) {
						Log.d(TAG, "down is ok");
						return true;
					}
				}

			}
		}
		return false;
	}

	/**
	 * Back up dimensions of all corners.
	 */
	public void backUpDimensions() {
		Log.d(TAG, "backUpDimensions()...");
		for (Corner corner : corners) {
			this.dimensions[corner.getCornerID()] = corner.getDimension();
		}
	}

	/**
	 * Restore dimensions of all corners.
	 */
	public void restoreDimensions() {
		Log.d(TAG, "restoreDimensions()...");
		if (dimensions == null)
			return;
		for (Corner corner : corners) {
			corner.setDimension(dimensions[corner.getCornerID()]);
		}
	}

	/**
	 * After touch the new Dimension will be calculating.
	 * 
	 * @param size
	 *            The size of the Image (width, height)
	 * @param moveSpace
	 *            The space of moving
	 * @throws IllegalDimensionException
	 * 
	 * @see ImageSize
	 * @see MoveSpace
	 * @see Vector
	 */
	public void moveFrame(ImageSize size, MoveSpace moveSpace) throws IllegalDimensionException {
		Log.d(TAG, "moveQuad()>>>>>>>>>>>>>>>");
		LogHelper.printMoveSpaceDimensions(moveSpace);

		Vector vector = new Vector(new Dimension(touchX, touchY), new Dimension(movedX, movedY));// startPoint EndPoint
		Log.d(TAG, "touchX , touchY: " + touchX + "; " + touchY);
		Log.d(TAG, "movedX , movedY: " + movedX + "; " + movedY);
		for (Corner corner : corners) {
			moveCorner(corner, vector, size, moveSpace);
		}
		// the move was valid ,we moved, set move as start point
		touchX = movedX;
		touchY = movedY;
	}

	/**
	 * 
	 * @param corner
	 *            corner to move
	 * @param vector
	 *            move vector of corenr
	 * @param size
	 *            size of image
	 * @param moveSpace
	 * @throws IllegalDimensionException
	 */
	public void moveCorner(Corner corner, Vector vector, ImageSize size, MoveSpace moveSpace) throws IllegalDimensionException {
		Dimension transaltedDimension;
		Dimension checkedDimension;
		int circleID = corner.getCornerID();
		Log.d(TAG, "move circle " + circleID);

		if (moveSpace != null) {
			transaltedDimension = corner.simulateTranslate(vector);// x + (endPoint-stratPoint)
			Log.d(TAG, "transaltedDimension x,y : " + transaltedDimension.getX() + ", " + transaltedDimension.getY());
			checkedDimension = moveSpace.checkAndCalculateValidEndPointForMoveVector(circleID, vector, transaltedDimension);
			if (checkedDimension.getX() == Integer.MIN_VALUE && checkedDimension.getY() == Integer.MIN_VALUE) {
				Log.d(TAG, "destination move ist valid");
				// translate from last point (touch) in to the new one (move)
				corner.translate(vector);
			} else {
				/*
				 * checkedDimension.getX() is the part of the move witch is not valid any more
				 * to correct the move we subtract the no-valid value from moved attribute (movedX, movedY)
				 * this way we get valid values to compute valid vector
				 */
				if (checkedDimension.getX() != Integer.MIN_VALUE)
					this.movedX = this.movedX - checkedDimension.getX();
				if (checkedDimension.getY() != Integer.MIN_VALUE)
					this.movedY = this.movedY - checkedDimension.getY();
				Log.d(TAG, "set new moved X, Y : " + movedX + "; " + movedY);
				/*
				 * Throw exception , in the catch block the dimension of corners
				 * will be restore. On the end this function will be call again
				 */
				throw new IllegalDimensionException();
			}
		}
	}

	/**
	 * 
	 * @param ID
	 *            circle ID
	 * @return Corner witch is placed parallel to the circle with given ID on the X axis
	 */
	public Corner getXParallelMovingCircle(int ID) {
		Corner parallel = null;
		switch (ID) {
		case 0:
			parallel = corners[3];
			break;
		case 1:
			parallel = corners[2];
			break;
		case 2:
			parallel = corners[1];
			break;
		case 3:
			parallel = corners[0];
			break;
		default:
			break;
		}
		return parallel;
	}

	/**
	 * 
	 * @param ID
	 *            circle ID
	 * @return Corner witch is placed parallel to the circle with given ID on the Y axis
	 */
	public Corner getYParallelMovingCircle(int ID) {
		Corner parallel = null;
		switch (ID) {
		case 0:
			parallel = corners[1];
			break;
		case 1:
			parallel = corners[0];
			break;
		case 2:
			parallel = corners[3];
			break;
		case 3:
			parallel = corners[2];
			break;
		default:
			break;
		}
		return parallel;
	}

	public Corner getCircleByID(int id) {
		if (id < 0 || id >= corners.length)
			throw new IllegalArgumentException("Wrong id, could not get circle");
		for (Corner corner : corners) {
			if (corner.getCornerID() == id)
				return corner;
		}
		return null;
	}

	public void checkMinDistansToNeighbourCircle(Corner corner) {
		int min = MIN_CIRCLE_DISTANCE;
		int id = corner.getCornerID();
		float x = corner.getX();
		float y = corner.getY();
		float distance;
		switch (id) {
		case 0:// check left top
			distance = corners[1].getX() - x;
			if (distance < min) {
				x -= min - distance;
				Log.d(TAG, "old Y : " + corner.getY() + ", old X : " + corner.getX() + ", distance X = " + distance + " new x = " + x + ", new  y = " + y);
				corner.setX(x);
			}
			distance = corners[3].getY() - y;
			if (distance < min) {
				y -= min - distance;
				Log.d(TAG, "old Y : " + corner.getY() + ", old X : " + corner.getX() + ", distance Y = " + distance + " new x = " + x + ", new  y = " + y);
				corner.setY(y);
			}
			break;
		case 1:// check right top
			distance = x - corners[0].getX();
			if (distance < min) {
				x += min - distance;
				Log.d(TAG, "old Y : " + corner.getY() + ", old X : " + corner.getX() + ", distance X = " + distance + " new x = " + x + ", new  y = " + y);
				corner.setX(x);
			}
			distance = corners[2].getY() - y;
			if (distance < min) {
				y -= min - distance;
				Log.d(TAG, "old Y : " + corner.getY() + ", old X : " + corner.getX() + ", distance Y= " + distance + " new x = " + x + ", new  y = " + y);
				corner.setY(y);
			}
			break;
		case 2:// check right bottom
			distance = x - corners[3].getX();
			if (distance < min) {
				x += min - distance;
				Log.d(TAG, "old Y : " + corner.getY() + ", old X : " + corner.getX() + ", distance X= " + distance + " new x = " + x + ", new  y = " + y);
				corner.setX(x);
			}
			distance = y - corners[1].getY();
			if (distance < min) {
				y += min - distance;
				Log.d(TAG, "old Y : " + corner.getY() + ", old X : " + corner.getX() + ", distance Y= " + distance + " new x = " + x + ", new  y = " + y);
				corner.setY(y);
			}
			break;
		case 3:// check left bottom
			distance = corners[2].getX() - x;
			if (distance < min) {
				x -= min - distance;
				Log.d(TAG, "old Y : " + corner.getY() + ", old X : " + corner.getX() + ", distance X= " + distance + " new x = " + x + ", new  y = " + y);
				corner.setX(x);
			}
			distance = y - corners[0].getY();
			if (distance < min) {
				y += min - distance;
				Log.d(TAG, "old Y : " + corner.getY() + ", old X : " + corner.getX() + ", distance Y= " + distance + " new x = " + x + ", new  y = " + y);
				corner.setY(y);
			}
			break;
		default:
			Log.d(TAG, "Wrong given ID !");
			break;
		}
	}

	public void checkCirclesDimensions(Corner master, Corner toCheck) {
		try {
			float masterX = master.getX();
			float masterY = master.getY();
			Log.d(TAG, "X: " + masterX + ", Y: " + masterY);
			int masterId = master.getCornerID();
			int checkID = toCheck.getCornerID();// was null???
			switch (masterId) {
			case 0:
				if (checkID == 1) {
					toCheck.setY(masterY);
				} else if (checkID == 3) {
					toCheck.setX(masterX);
				}
				break;
			case 1:
				if (checkID == 0) {
					toCheck.setY(masterY);
				} else if (checkID == 2) {
					toCheck.setX(masterX);
				}
				break;
			case 2:
				if (checkID == 1) {
					toCheck.setX(masterX);
				} else if (checkID == 3) {
					toCheck.setY(masterY);
				}
				break;
			case 3:
				if (checkID == 0) {
					toCheck.setX(masterX);
				} else if (checkID == 2) {
					toCheck.setY(masterY);
				}
				break;
			default:
				break;
			}
		} catch (NullPointerException ex) {
			Log.d(TAG, "Could not check Circles dimensions", ex);
		}
	}

	/**
	 * This mathod is calculating and setting the frame view to whole image.
	 * One of this 2 parameters could be null.
	 * The better way of calculating positions of frame view relative to image is with the move space.
	 * see also {@link MoveSpace}.
	 * 
	 * @param moveSpace
	 *            The move space for the view frame.
	 * @param size
	 *            the size computed from {@link CustomImageView}
	 */
	public void setFrameToWholeImage(MoveSpace moveSpace, ImageSize size) {
		Log.d(TAG, "setFrameToWholeImage()...");

		float minX;
		float minY;
		float maxX;
		float maxY;

		if (moveSpace != null) {
			minX = moveSpace.getMinX();
			minY = moveSpace.getMinY();
			maxX = moveSpace.getMaxX();
			maxY = moveSpace.getMaxY();
		} else {
			if (size.getWidth() >= 0 && size.getHeight() >= 0) {
				minX = 0;
				minY = 0;
				maxX = size.getWidth();
				maxY = size.getHeight();
			} else {
				Log.d(TAG, "geting size from imageView");
				minX = 0;
				minY = 0;
				maxX = getWidth();
				maxY = getHeight();
			}
		}
		for (Corner corner : corners) {
			int id = corner.getCornerID();
			switch (id) {
			case 0:
				corner.setX(minX);
				corner.setY(minY);
				break;
			case 1:
				corner.setX(maxX);
				corner.setY(minY);
				break;
			case 2:
				corner.setX(maxX);
				corner.setY(maxY);
				break;
			case 3:
				corner.setX(minX);
				corner.setY(maxY);
				break;
			default:
				break;
			}
		}
	}

	public int getMovingCircleID() {
		return movingCircleID;
	}

	public void setMovingCircleID(int movingCircleID) {
		this.movingCircleID = movingCircleID;
	}

	public Corner[] getCircles() {
		return corners;
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

	public float getMovedX() {
		return movedX;
	}

	public void setMovedX(float movedX) {
		this.movedX = movedX;
	}

	public float getMovedY() {
		return movedY;
	}

	public void setMovedY(float movedY) {
		this.movedY = movedY;
	}

	/**
	 * The wide of top and bottom will be not check
	 * 
	 * @return wide of frame
	 */
	public float getWidthFrame() {
		width = corners[1].getX() - corners[0].getX();
		return width;
	}

	/**
	 * The height of Frame will be not check
	 * 
	 * @return height of frame
	 */
	public float getHeightFrame() {
		height = corners[2].getY() - corners[1].getY();
		return height;
	}

	/**
	 * Return cloned corners.
	 * 
	 * @return
	 */
	public Corner[] cloneImageFrameCorners() {
		Corner[] clone = new Corner[4];
		int INDEX = 0;
		Corner c;
		for (Corner corner : corners) {
			c = new Corner(context, INDEX, new Dimension(corner.getX(), corner.getY()));
			c.setImageFrame(corner.getImageFrame());
			c.setTouchX(corner.getTouchX());
			c.setTouchY(corner.getTouchY());
			clone[INDEX] = c;
			INDEX++;
		}
		return clone;
	}

	public Corner[] cloneImageFrameCorners(Corner[] circles) {
		if (circles == null)
			throw new NullPointerException("Could not clone Circles");

		Corner[] clone = new Corner[circles.length];
		int INDEX = 0;
		Corner c;
		for (Corner corner : circles) {
			c = new Corner(context, INDEX, new Dimension(corner.getX(), corner.getY()));
			c.setImageFrame(corner.getImageFrame());
			c.setTouchX(corner.getTouchX());
			c.setTouchY(corner.getTouchY());
			clone[INDEX] = c;
			INDEX++;
		}
		return clone;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Log.d(TAG, "onSaveInstanceState().......");
		Bundle bundle = new Bundle();
		try {
			bundle.putParcelable("instanceState", super.onSaveInstanceState());
			bundle.putFloat("touchX", this.touchX);
			bundle.putFloat("touchY", this.touchY);
			bundle.putFloat("movedX", this.movedX);
			bundle.putFloat("movedY", this.movedY);
			bundle.putFloat("width", this.width);
			bundle.putFloat("height", this.height);

			bundle.putSerializable("dimensions", dimensions);
			// bundle.putSerializable("corners", corners);
			// bundle.putSerializable("TOUCHED_CORNER", ImageFrame.TOUCHED_CORNER);
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
			this.touchX = bundle.getFloat("touchX");
			this.touchY = bundle.getFloat("touchY");
			this.movedX = bundle.getFloat("movedX");
			this.movedY = bundle.getFloat("movedY");
			this.width = bundle.getFloat("width");
			this.height = bundle.getFloat("height");

			// this.corners = (Corner[]) bundle.getSerializable("corners");
			this.dimensions = (Dimension[]) bundle.getSerializable("dimensions");
			// ImageFrame.TOUCHED_CORNER = (Corner) bundle.getSerializable("TOUCHED_CORNER");

			super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
			return;
		}

		super.onRestoreInstanceState(state);
	}

	private static final String TAG = ImageFrame.class.getSimpleName();
	private Context context;
	private Dimension[] dimensions = new Dimension[4];
	private final Corner[] corners = new Corner[4];
	private int movingCircleID;
	private float touchX, touchY, movedX, movedY;// x,y of 4 corners
	public static boolean IS_VISIBLE;
	public static int MIN_CIRCLE_DISTANCE = 50;
	private float width;
	private float height;
	public static Corner TOUCHED_CORNER;

}
