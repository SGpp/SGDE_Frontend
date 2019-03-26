package com.example.jamal.sgde;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class myCanvas extends View {
    private Paint mPaint; //set color of drawing
    private Path mPath; //Path of current drawing
    private int mDrawColor; //choose color of drawing
    private int mBackgroundColor; //choose background color
    private Canvas mExtraCanvas;
    private Bitmap mExtraBitmap;
    private Rect mFrame;

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;


    myCanvas(Context context) {
        this(context, null);
    }

    public myCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBackgroundColor = ResourcesCompat.getColor(getResources(),android.R.color.white,null);
        mDrawColor = ResourcesCompat.getColor(getResources(),android.R.color.black,null);

        mPath = new Path();

        mPaint = new Paint();
        mPaint.setColor(mDrawColor);

        // Smoothes out edges of what is drawn without affecting shape.
        mPaint.setAntiAlias(true);
        // Dithering affects how colors with higher-precision device
        // than the are down-sampled.
        //mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE); // default: FILL
        mPaint.setStrokeJoin(Paint.Join.ROUND); // default: MITER
        mPaint.setStrokeCap(Paint.Cap.ROUND); // default: BUTT
        mPaint.setStrokeWidth(200); // default: Hairline-width (really thin)

    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        // Create bitmap, create canvas with bitmap, fill canvas with color.
        mExtraBitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        mExtraCanvas = new Canvas(mExtraBitmap);
        // Fill the Bitmap with the background color.
        mExtraCanvas.drawColor(mBackgroundColor);
        Log.d("VALUES", width +" " +height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw the bitmap that stores the path the user has drawn.
        // Initially the user has not drawn anything
        // so we see only the colored bitmap.
        canvas.drawBitmap(mExtraBitmap, 0, 0, null);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        float x = event.getX();
        float y = event.getY();


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                // No need to invalidate because we are not drawing anything.
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                // No need to invalidate because we are not drawing anything.
                break;
            default:
                // Do nothing.
        }
        invalidate();
        return true;
    }

    private void touchStart(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            // Reset mX and mY to the last drawn point.
            mX = x;
            mY = y;
            // Save the path in the extra bitmap,
            // which we access through its canvas.
            mExtraCanvas.drawPath(mPath, mPaint);
        }
    }

    private void touchUp() {
        // Reset the path so it doesn't get drawn again.
        mPath.reset();
    }

    public void clearBitmap() {
        mExtraBitmap.eraseColor(Color.TRANSPARENT);
        mExtraCanvas.drawColor(mBackgroundColor);
    }

    public Bitmap getBitmap(){
        return mExtraBitmap;
    }


}
