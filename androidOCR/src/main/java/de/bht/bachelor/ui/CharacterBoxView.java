package de.bht.bachelor.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import java.util.Vector;

import de.bht.bachelor.camera.OrientationMode;
import de.bht.bachelor.graphic.PaintCreator;

/**
 * This is a View showing boxes (outlines) of recognised characters contained in the Video-frame.
 * This View will be set on the top of other View witch is displaying the computed video-frame.
 * If the computed video-Frame has different Dimension then the displayed video-frame ,
 * the proportion will be computed so the outlines and characters are fitting together.
 *
 * @author andrzej
 */
public class CharacterBoxView extends View {


    float propW, propH = 0;

    private boolean isInvisible = false;
    /* if is true change color from transparent in to visible */
    private boolean needChangeColor = false;
    private static final String TAG = CharacterBoxView.class.getSimpleName();
    private OrientationMode orientationMode;
    private Vector<Rect> rects;// = new ArrayList<Rect>();
    // computed proportions of sizes
    private Vector<Rect> propRects;
    private Rect rect;
    private Paint strokePaint;
    private int frameWidth;
    private int frameHeight;


    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        Log.d(TAG,"set rotate : "+rotate);
        this.rotate = rotate;
    }

    private volatile int rotate;

    public CharacterBoxView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    /**
     * Draw character outlines from a Frame in to the canvas of this View.
     *
     * @param context App contex
     * @param rects   List of {@link Rect} as character boxes returning after OCR-analysing of the frame
     * @param width   of Frame witch was passing in to OCR
     * @param height  of Frame witch was passing in to OCR
     */
    public CharacterBoxView(Context context, Vector<Rect> rects, int frameWidth, int frameHeight) {
        super(context);
        this.rects = rects;
        this.strokePaint = PaintCreator.createStrokePaintForPreview();
        setWidth(frameWidth);
        setHeight(frameHeight);
        this.frameHeight = frameHeight;
        this.frameWidth = frameWidth;

//        restoreView();
        // TODO Auto-generated constructor stub
    }

    /**
     * Draw character outlines in to the canvas of this View.
     * The Frame witch was passed to OCR could have different dimension than
     * the Frame showing in the displaying View (SurfaceView).
     * Set extra dimension as parameter of the display frame.
     * The proportion will be compute and the dimension of the boxes will be adapt in to the display view.
     *
     * @param context      Application Context
     * @param rects        List with {@link Rect} as character boxes
     * @param width        of Frame witch was passing in to OCR
     * @param height       of Frame witch was passing in to OCR
     * @param displayWidth width of the displaying Frame
     * @param displayHeigt height of the displaying Frame
     */
    public CharacterBoxView(Context context, Vector<Rect> rects, int frameWidth, int frameHeight,  OrientationMode orientationMode) {
        super(context);
        Log.d(TAG, "Constructor CharacterBoxView: width: " + frameWidth + ",height: " + frameHeight );
        this.rects = rects;
        this.strokePaint = PaintCreator.createStrokePaintForPreview();
        setWidth(frameWidth);
        setHeight(frameHeight);
        this.frameHeight = frameHeight;
        this.frameWidth = frameWidth;
        this.orientationMode = orientationMode;
        strokePaint.setStrokeWidth(3);
        computeDifferenze(rects);
//        restoreView();
        setWillNotDraw(false);
        // TODO Auto-generated constructor stub
    }

    /*
     * If the display size is different than computed frame,
     * need to re-scale rect match outline in to the characters in the display view
     */
    private void computeDifferenze(Vector<Rect> rects) {
        Log.d(TAG, "computeDifferenze()...");
        // float propW, propH = 0;
        Vector<Rect> propRects = new Vector<Rect>();
        Rect propRect = null;

        if (rects == null)
            throw new NullPointerException("Could not copute ");
        if (this.frameWidth == 0 || this.frameHeight == 0)
            throw new ArithmeticException("Argument 'divisor' is 0");

//        if (this.displayWidth != this.frameWidth || this.displayHeight != this.frameHeight) {
//            Log.d(TAG, "way of computing: frameWidth / displayWidth,frameWidth: " + frameWidth + ",frameHeight: " + frameHeight + ", displayWidth: " + displayWidth + ",displayHeight: " + displayHeight);
//            propW = (float) frameWidth / displayWidth;
//            propH = (float) frameHeight / displayHeight;
//            /*
//             * variables values width and height to compute the proportional coordinates of character boxes (rects)
//			 */
//            Log.d(TAG, "computed proportion values propW : " + propW + ", height : " + propH);
//            if (propH == 0 || propW == 0)
//                throw new ArithmeticException("Argument 'divisor' is 0");
//
//            for (Rect rect : rects) {
//                propRect = new Rect();
//                if (isOrientationPortrait()) {
//
//                }
//                propRect.left = (int) (rect.left );
//                propRect.right = (int) (rect.right);
//                propRect.top = (int) (rect.top );
//                propRect.bottom = (int) (rect.bottom );
//                propRects.add(propRect);
//            }
//        } else {
			/*
			 * the size is the same, do not need compute proportions ,
			 * but we use propRects for drawing so init it any way
			 */
            this.propRects = new Vector<Rect>(rects);
//        }
        if (propRect != null)
            this.propRects = new Vector<Rect>(propRects);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();

        Log.d(TAG, "canvas size: w= " + canvas.getWidth() + ", h= " + canvas.getHeight() + ", propH =" + propH );
        if (rotate !=0) {
//            positions of the character boxes are wrong, translate and rotate back canvas to fit boxes in to the display
             rotate *=-1;//if image passed in to ocr has been rotated 90 , we must rotate back -90

            Matrix mtx = new Matrix();
            mtx.postRotate(rotate, frameHeight / 2, frameHeight / 2);
            canvas.setMatrix(mtx);

            Log.d(TAG, "ROTATE : canvas rotate "+rotate);
        }else{
            Log.d(TAG, "ROTATE : canvas NOT rotate "+rotate);
        }

        if (propRects == null)
            throw new NullPointerException("Could not draw propRects are NULL !");
        for (Rect rect : propRects) {

            canvas.drawRect(rect, strokePaint);
        }

        super.onDraw(canvas);

        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);

        Log.d(TAG, "onSizeChanged() w: " + w + ",h: " + h + ", old w: " + oldw + ",old h: " + oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure() w: " + widthMeasureSpec + ",h: " + heightMeasureSpec);
        setMeasuredDimension(frameWidth, frameHeight);
        Log.d(TAG, "size after setMeasuredDimension:w: " + this.getWidth() + ",h: " + getHeight());
    }

    public void setRects(Vector<Rect> rects) {
        // if (!isInvisible && needChangeColor) {
        this.strokePaint.setColor(Color.GREEN);
        needChangeColor = false;
        // }
        if (!this.rects.equals(rects)) {
            this.rects = rects;
            computeDifferenze(rects);
        }
    }

    public void makeVisible() {
        isInvisible = false;
        needChangeColor = true;

    }

    public void resetView() {
        this.strokePaint.setColor(Color.TRANSPARENT);
        isInvisible = true;
        restoreView();
        // this.strokePaint.setColor(Color.GREEN);
        // isInvisible = false;
        // restoreView();
    }

    public void cleanView() {
        this.strokePaint.setColor(Color.TRANSPARENT);
        isInvisible = true;
        restoreView();
    }

    public void restoreView() {
        invalidate();
        Log.d(TAG, "restoreView()... View sizes are now: width: " + getWidth() + ", height: " + getHeight());
    }

    public void setWidth(int width) {
        super.setMinimumWidth(width);

    }

    public void setHeight(int height) {
        super.setMinimumHeight(height);
    }

    public void flipSize() {
        int temp = getWidth();
        setWidth(getHeight());
        setHeight(temp);
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }


    public OrientationMode getOrientationMode() {
        return orientationMode;
    }

    public void setOrientationMode(OrientationMode orientationMode) {
        this.orientationMode = orientationMode;
    }

    private boolean isOrientationPortrait() {
        return orientationMode == OrientationMode.PORTRAIT || orientationMode == OrientationMode.PORTRAIT_UPSIDE_DOWN;
    }


}
