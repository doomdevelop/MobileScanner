package de.bht.bachelor.helper;

import android.util.Log;
import de.bht.bachelor.beans.Dimension;
import de.bht.bachelor.beans.ImageSize;
import de.bht.bachelor.graphic.transform.MoveSpace;
import de.bht.bachelor.ui.Corner;
import de.bht.bachelor.ui.CustomImageView;
import de.bht.bachelor.ui.ImageFrame;

public class DimensionHelper {

	/**
	 * Returning Dimension for Circles ,
	 * so the Image Frame will be located on the middle of the Image added in to the {@link CustomImageView}.
	 * 
	 * @param w
	 *            width of the image added to the {@link CustomImageView}
	 * @param h
	 *            height of the image added to the {@link CustomImageView}
	 * @param correction
	 *            has value true if there was no image added , in this case dimension came from display
	 * @return calculated dimension for circles
	 */
	public static Dimension[] getFrameDimensionsMiddleOfImage(int w, int h, boolean correction) {
		Log.d(TAG, "getFrameDimensionsMiddleOfImage()...");
		float corr;
		Dimension dimension;
		Dimension[] dimensions = new Dimension[4];
		float middleX = w / 2;
		float middleY = h / 2;
		float x = 0;
		float y = 0;
		if (correction)
			corr = 100;
		else
			corr = 0;
		for (int i = 0; i < dimensions.length; i++) {
			switch (i) {
			case 0:// Left top
				x = (middleX / 2);
				y = middleY / 2;
				break;
			case 1:// right top
				x = middleX + (middleX / 2) - corr;
				y = middleY / 2;
				break;
			case 2:// left bottom
				x = middleX + (middleX / 2) - corr;
				y = middleY + (middleY / 2);
				break;
			case 3:// right bottom
				x = middleX / 2;
				y = middleY + (middleY / 2);
				break;
			}
			dimension = new Dimension(x, y);
			dimensions[i] = dimension;
		}
		return dimensions;
	}

	/**
	 * Call it if the position of frame is out of image area.
	 * This method will move or / and scale frame to the right position.
	 * 
	 * @param circles
	 *            corners of the frame
	 * @param moveSpace
	 *            image area see {@link MoveSpace}
	 */
	public static void checkAndCorrectFramePosition(Corner[] circles, MoveSpace moveSpace) {

		int scaleValue;
		if (moveSpace.getDrawLeft() == 0) {
			if (frameLengthOK(moveSpace.getBitmapHeight())) {
				// setFrameToImageSize(circles, moveSpace);
				scaleValue = getScaleValueLength(moveSpace.getBitmapHeight());// by first crop
				// setFrameToImage(circles, moveSpace, scaleValue);
			}
		} else if (moveSpace.getDrawTop() == 0) {
			if (frameWideOK(moveSpace.getBitmapWidth())) {
				// setFrameToImageSize(circles, moveSpace);
				scaleValue = getScaleValueWIde(moveSpace.getBitmapWidth());
				// setFrameToImage(circles, moveSpace, scaleValue);
			}
		}
	}

	/**
	 * 
	 * @param bitmapHeight
	 * @return
	 */
	public static boolean frameLengthOK(float bitmapHeight) {
		if (bitmapHeight < ImageFrame.MIN_CIRCLE_DISTANCE)
			return false;
		return true;
	}

	/**
	 * 
	 * @param bitmapWidth
	 * @return
	 */
	public static boolean frameWideOK(float bitmapWidth) {
		if (bitmapWidth < ImageFrame.MIN_CIRCLE_DISTANCE)
			return false;
		return true;
	}

	private static int getScaleValueLength(float bitmapHeight) {
		if (frameLengthOK(bitmapHeight + 100))
			return 100;
		else if (frameLengthOK(bitmapHeight + 50))
			return 50;
		else if (frameLengthOK(bitmapHeight + 25))
			return 25;
		return 0;
	}

	private static int getScaleValueWIde(float bitmapWidth) {
		if (frameWideOK(bitmapWidth + 100))
			return 100;
		else if (frameWideOK(bitmapWidth + 50))
			return 50;
		else if (frameWideOK(bitmapWidth + 25))
			return 25;
		return 0;
	}

	/**
	 * 
	 * @param circles
	 * @param moveSpace
	 * @param scaleValue
	 */
	public static void setFrameToImage(Corner[] circles, MoveSpace moveSpace, int scaleValue) {
		int i;
		float minX = moveSpace.getMinX();
		float minY = moveSpace.getMinY();
		float maxX = moveSpace.getMaxX();
		float maxY = moveSpace.getMaxY();
		for (Corner corner : circles) {
			i = corner.getCornerID();
			switch (i) {
			case 0:// Left top
				corner.setX(minX);
				corner.setY(minY);
				break;
			case 1:// right top
				corner.setX(maxX);
				corner.setY(minY);
				break;
			case 2:// left bottom
				corner.setX(maxX);
				corner.setY(maxY);
				break;
			case 3:// right bottom
				corner.setX(minX);
				corner.setY(maxY);
				break;
			}

		}
	}

	/**
	 * The returned size is array , where first components is a width and second height.
	 * 
	 * @param orgBtmap
	 *            Bitmap to scale down
	 * @param newWidth
	 *            the new width after scale
	 * @param newHeight
	 *            the new height after scale
	 * @return size of given bitmap scaled in to given newWidth and newHight with computed aslect ratio
	 */
	public static ImageSize matchAndcomputeSizeWithAspectRatio(int imageWidth, int imageHight, int newWidth, int newHeight) {
		// http://www.coderanch.com/t/467505/java/java/Resize-image-while-maintaing-its

		int[] size = new int[2];
		float scaleWidth;
		float scaleHeight;
		float scaleFactor;
		long start;
		long end;
		start = System.nanoTime();

		scaleWidth = (float) newWidth / imageWidth;
		scaleHeight = (float) newHeight / imageHight;
		scaleFactor = Math.min(scaleWidth, scaleHeight);

		size[0] = Math.round(imageWidth * scaleFactor);
		size[1] = Math.round(imageHight * scaleFactor);

		end = System.nanoTime();
		Log.e("TIME", "" + (end - start));
		return new ImageSize(size[0], size[1]);
	}

	private static final String TAG = DimensionHelper.class.getSimpleName();
}
