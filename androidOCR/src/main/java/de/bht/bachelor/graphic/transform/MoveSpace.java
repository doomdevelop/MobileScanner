package de.bht.bachelor.graphic.transform;

import android.util.Log;
import de.bht.bachelor.beans.Dimension;
import de.bht.bachelor.ui.Corner;

/**
 * This class is representing a space to move.
 * There are calculated values witch are borders of the moving space.
 * Use it for checking if the destination point of move is valid.
 * 
 * @author andrzej kozlowski
 *         kozlowski76@yahoo.de
 */
public class MoveSpace {
	/**
	 * 
	 * @param bitmapWidth
	 *            width of bitmap placed in the image View
	 * @param bitmapHeight
	 *            height of bitmap placed in the image View
	 * @param imageViewWidth
	 *            width of the image View
	 * @param imageViewHeight
	 *            height of the image View
	 */
	public MoveSpace(float bitmapWidth, float bitmapHeight, float imageViewWidth, float imageViewHeight) {
		this.bitmapWidth = bitmapWidth;
		this.bitmapHeight = bitmapHeight;
		this.imageViewWidth = imageViewWidth;
		this.imageViewHeight = imageViewHeight;
		computeMoveSpace();
	}

	/*
	 * Compute the move space and set the values in to the attributes
	 * some ideas of computing are from :
	 * http://stackoverflow.com/questions/6023549/android-how-to-get-the-image-edge-x-y-position-inside-imageview
	 */
	private void computeMoveSpace() {

		float bitmapRatio = bitmapWidth / bitmapHeight;
		float imageViewRatio = imageViewWidth / imageViewHeight;
		if (bitmapRatio == imageViewRatio) {

			drawLeft = 0;
			drawTop = 0;
			drawHeight = imageViewHeight;
			drawWidth = imageViewWidth;
		} else if (bitmapRatio > imageViewRatio) {
			drawLeft = 0;
			drawHeight = (imageViewRatio / bitmapRatio) * imageViewHeight;
			drawTop = (imageViewHeight - drawHeight) / 2;
			drawWidth = imageViewWidth;
		} else {
			drawTop = 0;
			drawWidth = (bitmapRatio / imageViewRatio) * imageViewWidth;
			drawLeft = (imageViewWidth - drawWidth) / 2;
			drawHeight = imageViewHeight;
		}
		minX = drawLeft;
		minY = drawTop;
		maxX = minX + drawWidth;
		maxY = minY + drawHeight;
	}

	/**
	 * 
	 * @param corner
	 * @return true if the circle has Dimension not in the picture Space
	 */
	public boolean fitNotInToTheImage(Corner corner) {
		return fitNotInToTheImage(corner.getX(), corner.getY());
	}

	/**
	 * 
	 * @param circle
	 * @return true if the circle has Dimension not in the picture Space
	 */
	public boolean fitNotInToTheImage(float x, float y) {
		return x < minX || y < minY || x > maxX || y > maxY;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return true if one of given parameters are equals with the border value
	 */
	public boolean isOnTheBorder(float x, float y) {
		return x == minX || y == minY || x == maxX || y == maxY;
	}

	/**
	 * Call it to check destination of moving.
	 * If the destination point is out of moving space, create new valid dimension with new valid value X/Y.
	 * If the destination was not valid, the new Dimension will move circle of given ID to the border of move space.
	 * 
	 * @param circleID
	 *            id of moving circle
	 * @param x
	 *            destination value x
	 * @param y
	 *            destination value y
	 * @return new Dimension or Dimension with given x/y if was valid
	 * 
	 * @see MoveSpace
	 */
	public Dimension moveToTheBorder(int circleID, float x, float y) {
		Log.d(TAG, "moveToTheBorder()....");
		float newX = 0;
		float newY = 0;
		switch (circleID) {
		case 0:
			newX = x < minX ? minX : x;
			newY = y < minY ? minY : y;
			break;
		case 1:
			newX = x > maxX ? maxX : x;
			newY = y < minY ? minY : y;
			break;
		case 2:
			newX = x > maxX ? maxX : x;
			newY = y > maxY ? maxY : y;
			break;
		case 3:
			newX = x < minX ? minX : x;
			newY = y > maxY ? maxY : y;
			break;
		}
		return new Dimension(newX, newY);
	}

	/**
	 * This function check if the given vector will move circle with given ID not out of {@link MoveSpace}.
	 * If the move is going to be invalid , the value to correct vector will be calculating.
	 * 
	 * @param circleID
	 *            id of moving circle
	 * @param vector
	 *            move vector
	 * @param transaltedDimension
	 *            the dimension of circle with given ID translated by given vector
	 * @return Dimension with {@link Integer.MIN_VALUE} as value if move is valid or with value witch must be subtracted from move value
	 */
	public Dimension checkAndCalculateValidEndPointForMoveVector(int circleID, Vector vector, Dimension transaltedDimension) {
		Log.d(TAG, "checkAndCalculateValidEndPointForMoveVector()....");
		float newX = 0;
		float newY = 0;
		float translatedX = transaltedDimension.getX();
		float translatedY = transaltedDimension.getY();

		Log.d(TAG, "translatedX, translatedX before: " + translatedX + "; " + translatedX);
		switch (circleID) {
		case 0:
			newX = translatedX < minX ? translatedX - minX : Integer.MIN_VALUE;
			newY = translatedY < minY ? translatedY - minY : Integer.MIN_VALUE;
			break;
		case 1:
			newX = translatedX > maxX ? translatedX - maxX : Integer.MIN_VALUE;
			newY = translatedY < minY ? translatedY - minY : Integer.MIN_VALUE;
			break;
		case 2:
			newX = translatedX > maxX ? translatedX - maxX : Integer.MIN_VALUE;
			newY = translatedY > maxY ? translatedY - maxY : Integer.MIN_VALUE;
			break;
		case 3:
			newX = translatedX < minX ? translatedX - minX : Integer.MIN_VALUE;
			newY = translatedY > maxY ? translatedY - maxY : Integer.MIN_VALUE;
			break;
		}
		Log.d(TAG, "no valid values to sustract from moveX and moveY: " + newX + "; " + newY);
		return new Dimension(newX, newY);
	}

	/**
	 * 
	 * @param corner
	 * @return true if the X of the given circle is in the picture Space
	 */
	public boolean hasXRightPosition(Corner corner) {
		return corner.getX() > minX && corner.getX() < maxX;
	}

	/**
	 * 
	 * @param corner
	 * @return true if the Y of the given circle is in the picture Space
	 */
	public boolean hasYRightPosition(Corner corner) {
		return corner.getY() > minY && corner.getY() < maxY;
	}

	/**
	 * 
	 * @param circles
	 * @return
	 */
	public boolean moveSideToRightPosition(Corner[] circles) {

		float toMoveX = 0;
		float toMoveY = 0;
		float circlePosX = circles[0].getX();
		float circlePosY = circles[0].getY();
		// distance for each side (left write)
		float distanceFrameSideToImageX = (this.bitmapWidth - circles[1].getX() - circlePosX) / 2;
		// distance for each side (top bottom)
		float distanceFrameSideToImageY = (this.bitmapHeight - circles[3].getY() - circlePosY) / 2;
		// check left side
		if (drawLeft > circlePosX) {
			toMoveX = drawLeft - circlePosX + distanceFrameSideToImageX;

		}
		if (drawTop > circlePosY) {
			toMoveY = drawTop - circlePosY + distanceFrameSideToImageY;
		}
		if (toMoveX < 0)
			toMoveX = 0;
		if (toMoveY < 0)
			toMoveY = 0;
		for (Corner corner : circles) {
			corner.setX(toMoveX);
			corner.setY(toMoveY);
		}
		return false;
	}

	/**
	 * Place ImageFrame on the middle of image
	 * 
	 * @param circles
	 *            given corners of ImageFrmae
	 */
	public void placeImageFrameToTheMeddle(Corner[] circles) {
		int distanceX = (int) (bitmapWidth / 4);
		int distanceY = (int) (bitmapHeight / 4);

		for (Corner corner : circles) {
			int id = corner.getCornerID();
			switch (id) {
			case 0:
				corner.setX(minX + distanceX);
				corner.setY(minY + distanceY);
				break;
			case 1:
				corner.setX(maxX - distanceX);
				corner.setY(minY + distanceY);
				break;
			case 2:
				corner.setX(maxX - distanceX);
				corner.setY(maxY - distanceY);
				break;
			case 3:
				corner.setX(minX + distanceX);
				corner.setY(maxY - distanceY);
				break;
			default:
				break;
			}
		}
	}

	public float getBitmapWidth() {
		return bitmapWidth;
	}

	public float getBitmapHeight() {
		return bitmapHeight;
	}

	public float getImageViewWidth() {
		return imageViewWidth;
	}

	public float getImageViewHeight() {
		return imageViewHeight;
	}

	/**
	 * 
	 * @return height of the bitmap placed in the imageView
	 */
	public float getDrawHeight() {
		return drawHeight;
	}

	/**
	 * 
	 * @return y value of placed bitmap
	 */
	public float getDrawTop() {
		return drawTop;
	}

	/**
	 * 
	 * @return width of the bitmap placed in the imageView
	 */
	public float getDrawWidth() {

		return drawWidth;
	}

	/**
	 * 
	 * @return x value of placed bitmap in the imageView
	 */
	public float getDrawLeft() {
		return drawLeft;
	}

	public float getMinX() {
		return minX;
	}

	public float getMaxX() {
		return maxX;
	}

	public float getMinY() {
		return minY;
	}

	public float getMaxY() {
		return maxY;
	}

	private static final String TAG = MoveSpace.class.getSimpleName();
	private final float bitmapWidth, bitmapHeight, imageViewWidth,
			imageViewHeight;
	/**
	 * x value of placed bitmap in the imageView. This x value for the left side
	 */
	private float drawLeft;
	/**
	 * the y value of placed bitmap in the imageView. This is top side.
	 */
	private float drawTop;
	/**
	 * height of the bitmap placed in the imageView
	 */
	private float drawHeight = -1;
	/**
	 * width of the bitmap placed in the imageView
	 */
	private float drawWidth = -1;
	private float minX;
	private float maxX;
	private float minY;
	private float maxY;
}
