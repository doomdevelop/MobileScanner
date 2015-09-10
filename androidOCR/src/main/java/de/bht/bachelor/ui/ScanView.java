package de.bht.bachelor.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by and on 21.06.15.
 */
public class ScanView extends ViewGroup {
    private Paint paint;
    private static final int LASER_W = 40;
    private int position = 0;
    private boolean isScanning = false;


    public ScanView(Context context) {
        super(context);
        init();
    }

    public ScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScanView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

    }

    private void init(){
        initPaint();
    }

    public void startScan(){

    }

    public void stopScan(){

    }

    public void moveLaser(int pixels){

    }
    private void initPaint(){
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setColor(Color.LTGRAY);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(!isScanning){
            return;
        }

        canvas.drawLine(position,0, position+LASER_W,getHeight(),paint);
    }
}
