package de.bht.bachelor.ui;

import android.content.Context;
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


    float propH = 0;

    /* if is true change color from transparent in to visible */
    private static final String TAG = CharacterBoxView.class.getSimpleName();
    private Vector<Rect> rects;// = new ArrayList<Rect>();
    // computed proportions of sizes
    private Vector<Rect> propRects;
    private Paint strokePaint;
    private int frameWidth = 0;
    private int frameHeight = 0;
    private volatile int rotate;


    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        Log.d(TAG, "set rotate : " + rotate);
        this.rotate = rotate;
    }

    public static CharacterBoxView createInstant(Context context) {
        return new CharacterBoxView(context);
    }

    public CharacterBoxView(Context context) {
        super(context);
        this.strokePaint = PaintCreator.createStrokePaintForPreview();
        setWidth(frameWidth);
        setHeight(frameHeight);
        // TODO Auto-generated constructor stub
    }

    /**
     * Draw character outlines in to the canvas of this View.
     * The Frame witch was passed to OCR could have different dimension than
     * the Frame showing in the displaying View (SurfaceView).
     * Set extra dimension as parameter of the display frame.
     * The proportion will be compute and the dimension of the boxes will be adapt in to the display view.
     *
     */
    public CharacterBoxView(Context context, Vector<Rect> rects, int frameWidth, int frameHeight) {
        super(context);
        Log.d(TAG, "Constructor CharacterBoxView: width: " + frameWidth + ",height: " + frameHeight);
        this.rects = rects;
        this.strokePaint = PaintCreator.createStrokePaintForPreview();
        setWidth(frameWidth);
        setHeight(frameHeight);
        this.frameHeight = frameHeight;
        this.frameWidth = frameWidth;
        strokePaint.setStrokeWidth(3);
        computeDifferenze(rects);
        setWillNotDraw(false);
        // TODO Auto-generated constructor stub
    }

    /*
     * If the display size is different than computed frame,
     * need to re-scale rect match outline in to the characters in the display view
     */
    private void computeDifferenze(Vector<Rect> rects) {
        Log.d(TAG, "computeDifferenze()...");

        if (rects == null)
            throw new NullPointerException("Could not copute ");
        if (this.frameWidth == 0 || this.frameHeight == 0)
            throw new ArithmeticException("Argument 'divisor' is 0");

        this.propRects = new Vector<Rect>(rects);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        rotate = 0;
        Log.d(TAG, "canvas size: w= " + canvas.getWidth() + ", h= " + canvas.getHeight() + ", propH =" + propH);

        if (rotate != 0) {
            Matrix mtx = new Matrix();
            mtx.postRotate(rotate, frameHeight / 2, frameHeight / 2);
            canvas.setMatrix(mtx);
            Log.d(TAG, "ROTATE : canvas rotate " + rotate);
        } else {
            Log.d(TAG, "ROTATE : canvas NOT rotate " + rotate);
        }

        if (propRects != null) {
            for (Rect rect : propRects) {

                canvas.drawRect(rect, strokePaint);
            }
        }
        super.onDraw(canvas);
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Log.d(TAG, "onSizeChanged() w: " + w + ",h: " + h + ", old w: " + oldw + ",old h: " + oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure() w: " + widthMeasureSpec + ",h: " + heightMeasureSpec);
        setMeasuredDimension(frameWidth, frameHeight);
        Log.d(TAG, "size after setMeasuredDimension:w: " + this.getWidth() + ",h: " + getHeight());
    }

    public void setRects(Vector<Rect> rects) {
        this.strokePaint.setColor(Color.GREEN);

        if (this.rects != null && this.rects.equals(rects)) {
            return;
        }
        this.rects = rects;
        computeDifferenze(rects);
    }


    public void resetView() {
        this.strokePaint.setColor(Color.TRANSPARENT);
        restoreView();
    }

    public void cleanView() {
        this.strokePaint.setColor(Color.TRANSPARENT);
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

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }


}
