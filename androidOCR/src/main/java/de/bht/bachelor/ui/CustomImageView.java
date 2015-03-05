package de.bht.bachelor.ui;

import java.io.Serializable;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import de.beutch.bachelorwork.util.file.ConvertUtil;
import de.bht.bachelor.beans.Dimension;
import de.bht.bachelor.beans.ImageSize;
import de.bht.bachelor.exception.IllegalDimensionException;
import de.bht.bachelor.graphic.ImageHistory;
import de.bht.bachelor.graphic.PaintCreator;
import de.bht.bachelor.graphic.transform.MoveSpace;
import de.bht.bachelor.helper.DimensionHelper;
import de.bht.bachelor.helper.LogHelper;
import de.bht.bachelor.message.ServiceMessenger;

public class CustomImageView extends ImageView implements View.OnTouchListener, Serializable {

	public CustomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.setDrawingCacheEnabled(true);
		this.context = context;
		imageHistory = new ImageHistory(50);
		// onPreDraw();

		setOnTouchListener(this);
	}

	// ********************************************************* On Touch **************************************************
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if (!Corner.MOVE_CIRCLE && !Corner.MOVE_IMAGE_FRAME) {
			onTouchOutOfImageFrame(event);
			return true;
		}
		// if we are here , it mean user is touching circle or frame
		onTouchImageFrame();
		Log.d(TAG, "from this w = " + getWidth() + "; h = " + getHeight());

		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_MOVE:
			onActionMove(event);
			break;
		case MotionEvent.ACTION_UP:
			onActionUp(event);
			break;
		case MotionEvent.ACTION_CANCEL:
			LogHelper.dumpEvent(event);
			Corner.MOVE_CIRCLE = false;
			ImageFrame.TOUCHED_CORNER = null;
			newDiemsion = null;
			break;
		default:
			LogHelper.dumpEvent(event);
			break;
		}
		return true;
	}

	private void onTouchOutOfImageFrame(MotionEvent event) {
		// if touch is out of image frame
		if (!isOnTouch) {
			// make visible / invisible the gui elements
			// by each touch the event is calling more times, make sure we change visible just one time for a touch
			isOnTouch = true;// lock changing visibility
			if (isGuiVisible) {
				setVisibleGuiElemnts(false);
				isGuiVisible = false;
			} else {
				setVisibleGuiElemnts(true);
				isGuiVisible = true;
			}
		}
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_UP:
			// this is last call during touch,
			isOnTouch = false;// unlock changing visibility of GUI
		}
	}

	private void onTouchImageFrame() {
		if (!isOnTouch) {
			// make gui invisible by touching frame or circle
			if (isGuiVisible) {
				wasVisible = true;
				setVisibleGuiElemnts(false);
				isGuiVisible = false;
				isOnTouch = true;
			} else {
				wasVisible = false;
			}
		}
	}

	private void onActionMove(MotionEvent event) {
		LogHelper.dumpEvent(event);
		float x = event.getX();// - Corner.TOUCHED_CIRCLE.getTouchX();
		float y = event.getY();// - Corner.TOUCHED_CIRCLE.getTouchY();
		imageFrame.setMovedX(x);
		imageFrame.setMovedY(y);
		if (Corner.MOVE_IMAGE_FRAME) {
			// if (quad.isTouched((int) x, (int) y)) {
			Log.d(TAG, "frame will try move from X " + imageFrame.getMovedX() + " to x " + x);
			Log.d(TAG, "frame will try move from Y " + imageFrame.getMovedY() + " to y " + y);
			// imageFrame.setMovedX(x);
			// imageFrame.setMovedY(y);
			removeFrame();
			try {
				imageFrame.backUpDimensions();
				imageFrame.moveFrame(imageSize, moveSpace);
			} catch (IllegalDimensionException ex) {
				// the new dimension was out of the move space, do not move (restore dimension)
				imageFrame.restoreDimensions();
				try {
					imageFrame.backUpDimensions();// back up again
					imageFrame.moveFrame(imageSize, moveSpace);// try again, this time with new values of move vector
				} catch (IllegalDimensionException e) {
					Log.d(TAG, "<<<<<<<<<<<<<<<<<<<<<<<< Could not move image frame");
					imageFrame.restoreDimensions();
				}
			}
			addFrame();
			return;
		}
		int id = ImageFrame.TOUCHED_CORNER.getCornerID();

		newDiemsion = new Dimension(x, y);
		removeFrame();
		imageFrame.computeParallelMove(id, newDiemsion, viewGroup, imageSize, moveSpace);
		addFrame();
		return;
	}

	private void onActionUp(MotionEvent event) {
		// finish of touch
		LogHelper.dumpEvent(event);
		// LogSize();
		if (wasVisible) {
			setVisibleGuiElemnts(true);
			isGuiVisible = true;
		} else {
			isGuiVisible = false;
		}
		isOnTouch = false;// Unlock changing visibility of GUI

		if (Corner.MOVE_IMAGE_FRAME) {
			Corner.MOVE_IMAGE_FRAME = false;
			// break;
			return;
		}
		Corner.MOVE_CIRCLE = false;
		ImageFrame.TOUCHED_CORNER = null;
		newDiemsion = null;
	}

	// *************************************************** Init **********************************************************
	/**
	 * Set first time bitmap in to this container
	 * 
	 * @param bitmap
	 */
	public void initImageBitmap(Bitmap bitmap) {
		Log.d(TAG, "initImageBitmap()...");
		this.setDrawingCacheEnabled(true);
		new InitImage().execute(bitmap);
	}

	private class InitImage extends AsyncTask<Bitmap, Integer, Long> {

		@Override
		protected Long doInBackground(Bitmap... b) {
			Log.d(TAG, "InitImage .... doInBackground()...");

			// addFrame();
			setImageBitmapToImageView(b[0]);
			setOrgNoScalImage(b[0]);
			try {
				onPreDrawNewImage();
			} catch (IllegalStateException ex) {
				Log.d(TAG, "Could not run onPreDrawNewImage()", ex);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Log.d(TAG, "sleep has been interrupted", ex);
				}
				onPreDrawNewImage();
			}
			return null;
		}
	}

	private void setImageBitmapToImageView(Bitmap bitmap) {
		this.setImageBitmap(bitmap);
	}

	/**
	 * Crating component {@link ImageFrame} with 4 corners as corners.
	 * 
	 */
	public void createImageFrame(Dimension[] dimensions) {
		// this method is calling just one time during initial of this component
		Log.d(TAG, "createQuad()...");
		if (imageFrame == null)
			if (dimensions == null)
				imageFrame = new ImageFrame(this.context, getDimensions());
			else
				imageFrame = new ImageFrame(this.context, dimensions);

		if (corners == null)
			corners = imageFrame.getCircles();
		for (Corner corner : corners) {
			corner.setImageFrame(imageFrame);
		}
		addFrame();
	}

	/*
	 * Returning calculated Dimensions for the image frame
	 */
	private Dimension[] getDimensions() {
		Log.d(TAG, "getDimensions()...");
		int w;
		int h;
		boolean correction;

		if (this.widthOfInitScaledBitmap > 0 && this.heightOfInitScaledBitmap > 0) {
			Log.d(TAG, "get Dimensions from this width and height ");
			w = widthOfInitScaledBitmap;
			h = heightOfInitScaledBitmap;
			if (imageSize == null)
				imageSize = new ImageSize(widthOfInitScaledBitmap, heightOfInitScaledBitmap);
			correction = false;
		} else {
			Log.d(TAG, "this is the worst option, get Dimensions of Display from WindowManager");
			// if there is no image yet, we use display dimension
			w = getWindowManager().getDefaultDisplay().getWidth();
			h = getWindowManager().getDefaultDisplay().getHeight();
			correction = true;// need to be corrected
		}
		return DimensionHelper.getFrameDimensionsMiddleOfImage(w, h, correction);
	}

	// **************************************************** End of Init *********************************************************
	/**
	 * Remove ImageFrame from the View
	 */
	public void removeFrame() {
		for (Corner corner : corners) {
			viewGroup.removeView(corner);
		}
		noFrame = true;// draw without frames
		draw();

	}

	/**
	 * Add image frame to this component.
	 * The frame will be draw on the top of the image.
	 */
	public void addFrame() {
		Log.d(TAG, "addFrame()...");
		if (corners == null)
			throw new NullPointerException("Could not add Image frame, no corners!");
		try {
			for (Corner corner : corners) {
				Log.d(TAG, "add to view circle with ID " + corner.getCornerID() + ", X=" + corner.getX() + ",Y=" + corner.getY());
				viewGroup.addView(corner);
			}
		} catch (IllegalStateException ex) {
			Log.d(TAG, "Could not add Circles", ex);
			removeFrame();
			for (Corner corner : corners) {
				Log.d(TAG, "add to view circle with ID " + corner.getCornerID() + ", X=" + corner.getX() + ",Y=" + corner.getY());
				viewGroup.addView(corner);
			}
		}
		noFrame = false;
		draw();
		// setChackButtonEnabled(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Log.d(TAG, "onDraw()");
		super.onDraw(canvas);
		canvas.save();
		if (noFrame)
			return;
		if (corners == null)
			return;

		pathL = new Path();
		pathR = new Path();

		RectF bounds = new RectF();
		pathL.computeBounds(bounds, false);

		pathL.moveTo(corners[0].getX(), corners[0].getY());
		pathL.lineTo(corners[1].getX(), corners[1].getY());
		pathL.lineTo(corners[2].getX(), corners[2].getY());
		pathL.lineTo(corners[3].getX(), corners[3].getY());
		pathL.lineTo(corners[0].getX(), corners[0].getY());

		pathR.moveTo(corners[0].getX(), corners[0].getY());
		pathR.lineTo(corners[1].getX(), corners[1].getY());
		pathR.lineTo(corners[2].getX(), corners[2].getY());
		pathR.lineTo(corners[3].getX(), corners[3].getY());
		pathR.lineTo(corners[0].getX(), corners[0].getY());

		// canvas.translate(10 - bounds.left, 10 - bounds.top);
		Paint strokePaint = PaintCreator.createStrokePaint();
		Paint fillPaint = PaintCreator.createFillPaint();
		canvas.drawPath(pathL, strokePaint);
		canvas.drawPath(pathR, fillPaint);
		canvas.restore();
	}

	/**
	 * Call it after throwing {@link IllegalDimensionException} .
	 * Set new Dimension to the corners.
	 * 
	 * @param w
	 * @param h
	 */
	private void checkDimensionAndPlaceFrameInMiddle() {
		Log.d(TAG, "checkDimensionAndPlaceFrameInMiddle()...");
		try {
			if (moveSpace != null && !restoreImageView) {
				moveSpace.placeImageFrameToTheMeddle(corners);
			} else {
				restoreImageView = false;
			}
			noFrame = false;
			draw();
			setChackButtonEnabled(true);
		} catch (NullPointerException ex) {
			Log.d(TAG, "Could not prepare new Dimension for frame", ex);
		}
	}

	/*
	 * return array with width and height of current imege
	 */
	private ImageSize getImageSize() {
		Log.d(TAG, "getImageSize()...");
		int w;
		int h;
		if (imageSize != null)
			return imageSize;
		if (moveSpace != null) {
			Log.d(TAG, "getting size from moveSpace");
			w = (int) moveSpace.getBitmapWidth();
			h = (int) moveSpace.getBitmapHeight();
		} else {
			w = widthOfInitScaledBitmap;
			h = heightOfInitScaledBitmap;
		}
		return new ImageSize(w, h);
	}

	private boolean initMoveSpace() {
		Log.d(TAG, "initMoveSpace()...");
		int w = 0;
		int h = 0;
		ImageSize size;

		if (getWidth() > 0 && getHeight() > 0) {
			Log.d(TAG, "init move space using w, h from imageView");
			size = DimensionHelper.matchAndcomputeSizeWithAspectRatio((int) currentOrgImageSize.getWidth(), (int) currentOrgImageSize.getHeight(), getWidth(), getHeight());
			w = (int) size.getWidth();
			h = (int) size.getHeight();
		} else {
			Log.d(TAG, "could not init move space ");
			return false;
		}

		imageSize = new ImageSize(w, h);

		if (moveSpace == null) {
			moveSpace = new MoveSpace(w, h, getWidth(), getHeight());// by first crop
		} else if (moveSpace.getBitmapWidth() != w || moveSpace.getBitmapHeight() != h) {
			moveSpace = new MoveSpace(w, h, getWidth(), getHeight());

		}
		return true;
	}

	public void onPreDrawNewImage() throws IllegalStateException {
		Log.d(TAG, "onPreDrawNewImage()...");
		this.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				Log.d(TAG, "onPreDraw()...");
				if (initMoveSpace()) {
					// try {
					getViewTreeObserver().removeOnPreDrawListener(this);
					removeFrame();
					checkDimensionAndPlaceFrameInMiddle();
					addFrame();
					setChackButtonEnabled(true);

				}
				return true;
			}
		});
	}

	private void setVisibleGuiElemnts(boolean value) {
		isOnTouch = true;
		serviceMessenger = new ServiceMessenger(handler);
		if (value)
			serviceMessenger.sendMessage(1, 4, null);
		else
			serviceMessenger.sendMessage(0, 4, null);
	}

	private void setChackButtonEnabled(boolean value) {
		Log.d(TAG, "setChackButtonEnabled() with value " + value);
		serviceMessenger = new ServiceMessenger(handler);
		if (value) {
			serviceMessenger.sendMessage(1, 2, null);
			noFrame = false;
		} else {
			serviceMessenger.sendMessage(0, 2, null);
			noFrame = true;
		}
	}

	// ********************************************************

	/**
	 * Set Image Frame to the whole image.
	 */
	public void setFrameToWholeImage() {
		Log.d(TAG, "setFrameToWholeImage()...");
		removeFrame();
		if (moveSpace != null) {
			imageFrame.setFrameToWholeImage(moveSpace, null);
		} else {
			imageFrame.setFrameToWholeImage(null, getImageSize());
		}
		addFrame();
	}

	public void cropToFrame() {
		Log.d(TAG, "cropToFrame()...");
		Log.d(TAG, "The size of the Matrix from History is : " + imageHistory.getMatrixList().size());
		Log.d(TAG, "The size of the Imageframes from History is : " + imageHistory.getQuadList().size());
		this.setDrawingCacheEnabled(true);

		removeFrame();
		Log.d(TAG, "dimension of THIS, w : " + getWidth() + "; h : " + getHeight());
		LogHelper.printBitmapDimension(currentOrgCropBitmap, "currentOrgCropBitmap");
		LogHelper.printCirclesDimensions(corners);

		int newWidth = (int) corners[1].getX() - (int) corners[0].getX();
		int newHeight = (int) corners[3].getY() - (int) corners[0].getY();
		Log.d(TAG, "newWidth : " + newWidth + ", newHeight : " + newHeight);

		try {
			if (currentOrgCropBitmap == null) {
				currentOrgCropBitmap = cropOriginalImageToFrame(orgNoScalImage);// by first crop
			} else {
				currentOrgCropBitmap = cropOriginalImageToFrame(currentOrgCropBitmap);
			}

			imageHistory.addCopyCircles(imageFrame.cloneImageFrameCorners());
			moveSpace = null;
			setImageMatrix(new Matrix());
			this.setImageBitmap(currentOrgCropBitmap);
			this.setDrawingCacheEnabled(true);
			buildDrawingCache(true);

			draw();
			noFrame = true;
			setChackButtonEnabled(false);

			if (scaleType == null)
				scaleType = getScaleType();
			Log.d(TAG, "after crop scaltype : " + scaleType + ", current getScaleType: " + getScaleType());
			setLastImageButtonEnabled();
			onPreDrawNewImage();
			Log.d(TAG, "The size of the Matrix from History is : " + imageHistory.getMatrixList().size());
			Log.d(TAG, "The size of the Imageframes from History is : " + imageHistory.getQuadList().size());
		} catch (IllegalArgumentException ex) {
			Log.e(TAG, "Could not crop image to frame", ex);

			Log.d(TAG, "The size of the Matrix from History is : " + imageHistory.getMatrixList().size());
			Log.d(TAG, "The size of the Imageframes from History is : " + imageHistory.getQuadList().size());
			// remove added matrix
			if (imageHistory.getMatrixList().size() > imageHistory.getQuadList().size())
				imageHistory.getLastMatrix();
			removeFrame();
			addFrame();
		}
	}

	private void testMatrix(float[] values) {
		Log.d(TAG, "matrix values : ");
		for (float v : values) {
			Log.d(TAG, "" + v);
		}
	}

	private Bitmap cropOriginalImageToFrame(Bitmap bitmapToCrop) throws IllegalArgumentException {
		// bitmapToCrop = orgNoScalImage;
		Log.d(TAG, "cropOriginalImageToFrame()....");
		Corner[] circlesLoc = null;
		Matrix invertScal;
		Bitmap orgNoScalToFrame = null;
		float[] values = new float[9];
		circlesLoc = imageFrame.cloneImageFrameCorners();
		Log.d(TAG, "Currnet Matrix values : ");

		invertScal = new Matrix();
		invertScal.reset();// set to identity
		currentScalMatrix = getImageMatrix();
		imageHistory.addMatrix(new Matrix(currentScalMatrix));
		if (!currentScalMatrix.invert(invertScal)) {
			Log.d(TAG, "Could not invert scal matrix");
			throw new IllegalArgumentException("Could not invert scal matrix");
		}
		invertScal.getValues(values);
		Log.d(TAG, "Inverted Matrix values : ");
		testMatrix(values);
		LogHelper.testCircles(circlesLoc);

		mapCirclePoints(invertScal, circlesLoc);
		LogHelper.testCircles(circlesLoc);

		float newWidth = circlesLoc[1].getX() - circlesLoc[0].getX();
		float newHeight = circlesLoc[3].getY() - circlesLoc[0].getY();

		orgNoScalToFrame = crop(bitmapToCrop, circlesLoc);

		currentOrgImageSize = new ImageSize(newWidth, newHeight);
		// new FileUtil().saveBitmapAsFile(orgNoScalToFrame, new File(de.beutch.bachelorwork.util.file.Path.MAIN_PICTURES_PATH + "Cropped_" + count + ".jpg"),
		// Bitmap.CompressFormat.JPEG);
		count++;
		return orgNoScalToFrame;
	}

	/**
	 * 
	 * @return image witch was not scaled down by imageView and is cropped to the frame
	 */
	public Bitmap cropToFrameFromNoScalImage() {
		// if (noFrame) {
		if (this.currentOrgCropBitmap == null)
			return this.orgNoScalImage;
		return this.currentOrgCropBitmap;
	}

	/*
	 * Preparing ImageView for no-scale-image (original image), witch is not displayed but will be send to ocr.
	 * Matrix is the inverted matrix witch processed scaling down original image. This way image could be displayed.
	 * To set current FrameView to the original image we need to scale it back by inverted matrix.
	 * Inverted matrix is processing every X,Y value of frameView
	 */
	private void mapCirclePoints(Matrix invertScal, Corner[] circlesLoc) {
		Log.d(TAG, "mapCirclePoints()");
		float[] dstPoint = new float[2];

		for (Corner corner : circlesLoc) {
			Log.d(TAG, "x= " + corner.toArray()[0] + ",y = " + corner.toArray()[1]);

			invertScal.mapPoints(dstPoint, corner.toArray());
			Log.d(TAG, "invert hx= " + dstPoint[0] + ",y = " + dstPoint[1]);
			if (dstPoint[0] < 0)
				dstPoint[0] = 0;
			if (dstPoint[1] < 0)
				dstPoint[1] = 0;
			corner.setX(dstPoint[0]);
			corner.setY(dstPoint[1]);
		}
	}

	private void setLastImageButtonEnabled() {
		Log.d(TAG, "setLastImageButtonEnabled()");
		if (!imageHistory.isHistoryEmpty()) {
			serviceMessenger = new ServiceMessenger(handler);
			serviceMessenger.sendMessage(1, 1, null);
		}
	}

	/**
	 * This method is setting last image to the imageView
	 * Usually calling during user interaction to set back in to the view last cropped image
	 */
	public void setImageFromHistory() {
		Log.d(TAG, "********************* setImageFromHistory() *************************");
		Log.d(TAG, "The size of the Matrix from History is : " + imageHistory.getMatrixList().size());
		Log.d(TAG, "The size of the Imageframes from History is : " + imageHistory.getQuadList().size());
		Bitmap lastImage = null;
		Matrix matrixtoRemove = null;
		Corner[] circletoRemove = null;
		long start;
		long end;

		removeFrame();
		// this.currentOrgCropBitmap = imageHistory.getLastOrgCropImage();

		this.setDrawingCacheEnabled(true);
		setImageMatrix(new Matrix());
		// remove matrix and Corners of currentOrgCropBitmap (current image ) from the history
		try {
			circletoRemove = imageHistory.getLastCircles();
			matrixtoRemove = imageHistory.getLastMatrix();

			if (imageHistory.isHistoryEmpty()) {
				count--;
				Log.d(TAG, "History is Empty !");
				setImageBitmap(orgNoScalImage);
				this.currentOrgCropBitmap.recycle();
				this.currentOrgCropBitmap = null;
				currentOrgImageSize = new ImageSize(orgNoScalImage.getWidth(), orgNoScalImage.getHeight());
				ServiceMessenger serviceMessenger = new ServiceMessenger(handler);
				serviceMessenger.sendMessage(0, 1, null);
			} else {
				count--;
				start = System.currentTimeMillis();
				lastImage = reconstructLastImage();
				end = System.currentTimeMillis() - start;
				Log.d(TAG, "reconstruction of last image took " + end + " ms");
				if (lastImage != null) {
					currentOrgCropBitmap.recycle();
					currentOrgCropBitmap = lastImage;
					setImageBitmap(currentOrgCropBitmap);
					// new FileUtil().saveBitmapAsFile(lastImage, new File(de.beutch.bachelorwork.util.file.Path.MAIN_PICTURES_PATH + "Cropped_" + count + ".jpg"),
					// Bitmap.CompressFormat.JPEG);
				} else {
					// we could not create last image , set history back
					if (matrixtoRemove != null)
						imageHistory.addMatrix(matrixtoRemove);
					if (circletoRemove != null)
						imageHistory.addCopyCircles(circletoRemove);
				}
			}
		} catch (IllegalStateException ex) {
			// by error during imageHistory.getLastCircles();
			// TODO : User Dialog ??
			if (matrixtoRemove != null)
				imageHistory.addMatrix(matrixtoRemove);
			if (circletoRemove != null)
				imageHistory.addCopyCircles(circletoRemove);
			Log.e(TAG, "Could not set Image from history.", ex);
		} catch (IllegalArgumentException ex) {
			// by error during reconstructLastImage();
			// TODO : User Dialog ??
			if (matrixtoRemove != null)
				imageHistory.addMatrix(matrixtoRemove);
			if (circletoRemove != null)
				imageHistory.addCopyCircles(circletoRemove);
			Log.e(TAG, "Could not set Image from history.", ex);
		}
		draw();
		onPreDrawNewImage();
		Log.d(TAG, "********************* END setImageFromHistory() *************************");
	}

	/*
	 * This method is creating image witch was the parent of current one.
	 * It mean the current visible Image was created by cropping from image,witch we wont to have back now.
	 * The wonted Image is not saved , so we need to reconstruct it
	 * The creating process is using all corners and matrixes used by cropping and witch are saved in History as Lists,
	 * The start point is from very first crop witch was made on the original image
	 * Matrix - matrix for scaling the image in to the ImageView size (creating scale-down image)
	 * Corner - the corners of image frame, in to witch size the scale-down image was cropped
	 * The creating steps are :
	 * 1. Invert matrix
	 * 2. Map all corners points from image frame witch was giving the cropping dimensions in to the invert matrix
	 * 3. This way we get the ImageFrame used to cropping with dimension related to the original image
	 * 4. crop the last image with original size in to the created frame
	 */
	private Bitmap reconstructLastImage() {
		Log.d(TAG, "calculateLastImageToSet().............");
		// first work just on copy
		List<Matrix> matrixList = imageHistory.getMatrixList();
		List<Corner[]> imageFrameList = imageHistory.getQuadList();

		Matrix invertMat;
		int INDEX = 0;
		Corner[] croppingImageFrame;
		Bitmap currentBitmap = null;
		Matrix copyScalDownMatrix;

		if (matrixList == null || imageFrameList == null)
			throw new NullPointerException("Lists from history with Matrixes and corcles can not be null !");

		Log.d(TAG, "The size of the Matrix from History is : " + matrixList.size());
		Log.d(TAG, "The size of the Imageframes from History is : " + imageFrameList.size());

		// quantity must be same
		if (matrixList.size() != imageFrameList.size())
			throw new IllegalArgumentException("Quantity of Imageframes is not equals with the quantity of Matrixes");

		for (Matrix matrix : matrixList) {
			Log.d(TAG, "iterateing through crop history, index : " + INDEX);
			invertMat = new Matrix();
			copyScalDownMatrix = new Matrix(matrix);// work on copy !

			// step 1
			if (!copyScalDownMatrix.invert(invertMat)) {
				// can not invert if det(copyScalDownMatrix) == 0
				throw new IllegalArgumentException("Could not Inver Matrix");
			}

			croppingImageFrame = imageFrame.cloneImageFrameCorners(imageFrameList.get(INDEX));
			// step 2, croppingImageFrame has crop dimensions
			mapCirclePoints(invertMat, croppingImageFrame);
			try {
				if (currentBitmap == null) {
					// the first crop making on the very first original image
					currentBitmap = crop(orgNoScalImage, croppingImageFrame);
				} else {
					currentBitmap = crop(currentBitmap, croppingImageFrame);
				}
			} catch (IllegalArgumentException ex) {
				Log.d(TAG, "Could not get image back by cropping !", ex);
				return null;
			}
			INDEX++;
		}
		return currentBitmap;
	}

	/*
	 * Throw IllegalArgumentException if the width or height of new Bitmap is bigger than width or height than the src birmap
	 */
	private Bitmap crop(Bitmap bitmap, Corner[] corners) throws IllegalArgumentException {
		Log.d(TAG, "crop()............");
		float newWidth;
		float newHeight;
		newWidth = corners[1].getX() - corners[0].getX();
		newHeight = corners[3].getY() - corners[0].getY();
		Log.d(TAG, "Bitmap to crop w = " + bitmap.getWidth() + ", h = " + bitmap.getHeight());
		Log.d(TAG, "new bitmap Width : " + newWidth + ", new Height : " + newHeight);
		// need dimension of current image to calculate movSpace

		currentOrgImageSize = new ImageSize((int) newWidth, (int) newHeight);
		currentRect = new Rect((int) corners[0].getX(), (int) corners[0].getY(), (int) corners[1].getX(), (int) corners[2].getY());
		// return Bitmap.createBitmap(bitmap, (int) corners[0].getX(), (int) corners[0].getY(), (int) newWidth, (int) newHeight);
		return cropToRect8888(currentRect, bitmap, newWidth, newHeight);
	}

	/*
	 * Workaround for creating by crop a bitmap with conf 8888
	 * http://android.amberfog.com/?p=430
	 */
	private Bitmap cropToRect8888(Rect rect, Bitmap src, float width, float hight) {
		// Bitmap bitmap = Bitmap.createBitmap(rect.right - rect.left, rect.bottom - rect.top, Bitmap.Config.ARGB_8888);
		Bitmap bitmap = null;
		try {
			bitmap = ConvertUtil.cropToRect8888(rect, src, width, hight);
		} catch (OutOfMemoryError error) {
			Log.e(TAG, "Could not crop to the rect!", error);
			return src;
		}
		return bitmap;
	}

	/**
	 * Call this Method to get correct bitmap for current Rect.
	 * This is useful by setting Rect on Tessearct API, to map area of current ImageFrame.
	 * If the was more than one crop , will reconstruct last bitmap.
	 * 
	 * @return Bitmap with Org size , if the was more than one crop , will reconstruct last bitmap
	 */
	public Bitmap getOrgBitmapForCurrentRect() {
		Matrix matrixtoRemove = imageHistory.getLastMatrix();
		Corner[] circletoRemove = imageHistory.getLastCircles();
		Bitmap bitmap = null;

		try {
			if (imageHistory.isHistoryEmpty()) {
				return orgNoScalImage;
			} else {
				Rect bacup = new Rect(currentRect);
				bitmap = reconstructLastImage();
				// we need to get bitmap to map the current rect on it-
				// but we already changed dimension of rect, calling reconstructLastImage() and crop()
				// so set it back!
				currentRect = bacup;
				return bitmap;
			}
		} catch (NullPointerException ex) {
			Log.d(TAG, "Could not get Bitmap for current rect ", ex);
		} finally {
			// don't change the history !!
			if (matrixtoRemove != null)
				imageHistory.addMatrix(matrixtoRemove);
			if (circletoRemove != null)
				imageHistory.addCopyCircles(circletoRemove);
		}
		return bitmap;
	}

	/**
	 * Call it to render new this View.
	 */
	public void draw() {
		invalidate();
	}

	public void setViewGroup(ViewGroup viewGroup) {
		this.viewGroup = viewGroup;
	}

	public WindowManager getWindowManager() {
		return windowManager;
	}

	public void setWindowManager(WindowManager windowManager) {
		this.windowManager = windowManager;
	}

	/**
	 * @return {@link Handler}
	 */
	@Override
	public Handler getHandler() {
		return handler;
	}

	/**
	 * Set {@link Handler} to this component.
	 * 
	 * @param handler
	 */
	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	/**
	 * 
	 * @return {@link ImageHistory}, witch contain images before crop.
	 */
	public ImageHistory getImageHistory() {
		return imageHistory;
	}

	/**
	 * Get original no scaled down image, witch is the first image added to this container by initial.
	 * This image came from camera, converted to bitmap has still original dimensions, proportions and property.
	 * 
	 * @return original no scaled bitmap
	 */
	public Bitmap getOrgNoScalImage() {
		return orgNoScalImage;
	}

	/**
	 * Set original no scaled-down image, witch is the first image added to this container by initial
	 * This image came from camera, converted to bitmap has still original dimension, proportions and property.
	 * 
	 * @param orgNoScalImage
	 *            no scaled bitmap
	 */
	public void setOrgNoScalImage(Bitmap orgNoScalImage) {
		currentOrgImageSize = new ImageSize(orgNoScalImage.getWidth(), orgNoScalImage.getHeight());
		this.orgNoScalImage = orgNoScalImage;
	}

	/**
	 * @return true if the image frame is visible.
	 */
	public boolean hasNoFrame() {
		return noFrame;
	}

	public Bitmap getCurrentOrgCropBitmap() {
		return currentOrgCropBitmap;
	}

	public Rect getCurrentRect() {
		return currentRect;
	}

	public void onDestroy() {
		Log.d(TAG, "On Destroy()........");
		if (orgNoScalImage != null) {
			this.orgNoScalImage.recycle();
			this.orgNoScalImage = null;
		}
		if (currentOrgCropBitmap != null) {
			this.currentOrgCropBitmap.recycle();
			this.currentOrgCropBitmap = null;
		}
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Log.d(TAG, "onSaveInstanceState().......");

		Parcelable superState = super.onSaveInstanceState();

		SavedState ss = new SavedState(superState);
		Bundle bundle = new Bundle();

		for (Corner corner : corners) {
			bundle.putSerializable(CORNER_DIMENSION_ + corner.getCornerID(), corner.getDimension());
		}
		ss.bundle = bundle;
		return ss;
		// Bundle bundle = new Bundle();
		// try {
		// bundle.putParcelable("instanceState", super.onSaveInstanceState());
		//
		// // bundle.putSerializable("corners", corners);
		// // bundle.putParcelable("imageFrame", imageFrame.onSaveInstanceState());
		// bundle.putSerializable("newDiemsion", newDiemsion);
		// } catch (RuntimeException ex) {
		// Log.e(TAG, "could not put in to bundle", ex);
		// }
		// return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		Log.d(TAG, "onRestoreInstanceState().......");
		SavedState ss = (SavedState) state;

		super.onRestoreInstanceState(ss.getSuperState());
		Bundle bundle = ss.bundle;

		requestLayout();
		// if (state instanceof Bundle) {
		// Bundle bundle = (Bundle) state;
		// try {
		// // this.corners = (Corner[]) bundle.getSerializable("corners");
		// // this.imageFrame = (ImageFrame) bundle.getSerializable("imageFrame");
		// this.newDiemsion = (Dimension) bundle.getSerializable("newDiemsion");
		// } catch (RuntimeException ex) {
		// Log.e(TAG, "could not put in to bundle", ex);
		// }
		// super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
		// return;
		// }
		// super.onRestoreInstanceState(state);
	}

	static class SavedState extends BaseSavedState {
		Bundle bundle;

		/**
		 * Constructor called from {@link CompoundButton#onSaveInstanceState()}
		 */
		SavedState(Parcelable superState) {
			super(superState);
		}

		/**
		 * Constructor called from {@link #CREATOR}
		 */
		private SavedState(Parcel in) {
			super(in);
			bundle = in.readBundle();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);

			out.writeBundle(bundle);
		}

		@Override
		public String toString() {
			return "CompoundButton.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " corners length=";
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	private static final String TAG = CustomImageView.class.getSimpleName();
	// private final Paint strokePaint;
	// private final Paint fillPaint;
	private ViewGroup viewGroup;
	private Corner[] corners;

	public Corner[] getCorners() {
		return corners;
	}

	private ImageFrame imageFrame;
	private final Context context;
	private Path pathL;
	private Path pathR;
	private boolean noFrame = false;
	private WindowManager windowManager;
	private Dimension newDiemsion = null;

	private final ImageHistory imageHistory;
	private Handler handler;
	private ServiceMessenger serviceMessenger;

	/* Original image with original size- no scale. For OCR process */
	private Bitmap orgNoScalImage;
	/* Cropped image with original size- no scale. For OCR process */
	private Bitmap currentOrgCropBitmap;

	private ScaleType scaleType;
	private Matrix currentScalMatrix;
	private MoveSpace moveSpace;
	private int count = 0;
	private boolean isOnTouch = false;
	private boolean isGuiVisible = false;
	private boolean wasVisible = false;
	private int widthOfInitScaledBitmap;
	private int heightOfInitScaledBitmap;
	private ImageSize imageSize;
	private ImageSize currentOrgImageSize;
	private Rect currentRect;
	public static final String CORNER_DIMENSION_ = "CORNER_DIMENSION_";
	private boolean restoreImageView = false;

	public void setRestoreImageView(boolean restoreImageView) {
		this.restoreImageView = restoreImageView;
	}

}
