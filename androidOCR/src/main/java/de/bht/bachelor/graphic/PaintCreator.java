package de.bht.bachelor.graphic;

import android.graphics.Color;
import android.graphics.Paint;

public class PaintCreator {
	/**
	 * 
	 * @return return stroke paint
	 */
	public static Paint createStrokePaint() {
		if (strokePaint != null)
			return strokePaint;

		strokePaint = new Paint();
		strokePaint.setAntiAlias(true);
		strokePaint.setStyle(Paint.Style.STROKE);
		strokePaint.setColor(Color.RED);
		strokePaint.setAlpha(0x80);
		return strokePaint;
	}

	/**
	 * 
	 * @return fill paint for the circle
	 */
	public static Paint createCircleFillPaint() {

		if (fillCirclePaint != null)
			return fillCirclePaint;

		fillCirclePaint = new Paint();
		fillCirclePaint.setAntiAlias(true);
		fillCirclePaint.setStyle(Paint.Style.FILL);// Stroke just regions
		fillCirclePaint.setColor(Color.RED);
		fillCirclePaint.setAlpha(0x80);
		return fillCirclePaint;
	}

	/**
	 * 
	 * @return fill paint for the inside of image frame
	 */
	public static Paint createFillPaint() {
		if (fillPaint != null)
			return fillPaint;

		fillPaint = new Paint();
		fillPaint.setAntiAlias(false);
		fillPaint.setStyle(Paint.Style.FILL);
		fillPaint.setColor(Color.YELLOW);
		fillPaint.setAlpha(0x20);
		return fillPaint;
	}

	/************************************* paint for Preview *************************************************/

	/**
	 * 
	 * @return return stroke paint
	 */
	public static Paint createStrokePaintForPreview() {

		Paint strokePaint = new Paint();
		strokePaint.setAntiAlias(true);
		strokePaint.setStyle(Paint.Style.STROKE);
		strokePaint.setColor(Color.GREEN);
		strokePaint.setAlpha(0x80);
		return strokePaint;
	}

	/**
	 * 
	 * @return return stroke paint
	 */
	public static Paint createStrokePaintForArc() {

		Paint paintArc = new Paint();
		// paintArc.setAntiAlias(true);
		paintArc.setStyle(Paint.Style.STROKE);
		paintArc.setColor(Color.GREEN);
		paintArc.setStrokeWidth(5);
		// strokePaint.setAlpha(0x80);
		return paintArc;
	}

	public static Paint createFillPaintArc() {
		Paint mBgPaints = new Paint();
		// mBgPaints.setAntiAlias(true);
		mBgPaints.setStyle(Paint.Style.STROKE);
		mBgPaints.setColor(Color.BLUE);
		mBgPaints.setStrokeWidth(0.5f);
		return mBgPaints;
	}

	/**
	 * 
	 * @return fill paint for the inside of image frame
	 */
	public static Paint createFillPaintForPreview() {

		Paint fillPaint = new Paint();
		fillPaint.setAntiAlias(false);
		fillPaint.setStyle(Paint.Style.FILL);
		fillPaint.setColor(Color.YELLOW);
		fillPaint.setAlpha(0x20);
		return fillPaint;
	}

	/**********************************************************************************************************/
	private static Paint strokePaint;
	private static Paint fillCirclePaint;
	private static Paint fillPaint;
}
