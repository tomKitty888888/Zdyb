package com.zdyb.module_obd.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ThreadUtils;
import com.zdyb.module_obd.R;

public class ScanningView extends View {

    private int mImgHeight = 40;
    private int mImgWidth = 100;
    private int mProgressHeight = 40;

    private int mProgressColor = Color.rgb(54,187,76);
    private int mBackgroundColor = Color.rgb(235,235,235);
    private int mIcon = R.mipmap.icon_truck;;
    private Paint mProgressPaint;
    private Paint mBackgroundPaint;
    private Paint mIconPaint;
    float mProgress = 40;  //进度
    private Bitmap mBitmap;
    private Boolean isRun = true;


    public ScanningView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public ScanningView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public ScanningView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public ScanningView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        //setWillNotDraw(true);

        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr){
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,R.styleable.ScanningView,0,0);

        try {
            mImgHeight = a.getInt(R.styleable.ScanningView_img_height,mImgHeight);
            mImgWidth = a.getInt(R.styleable.ScanningView_img_width,mImgWidth);
            mProgressHeight = a.getColor(R.styleable.ScanningView_progress_height, mProgressHeight);
            mProgressColor = a.getColor(R.styleable.ScanningView_progress_colors, mProgressColor);
            mIcon = a.getInt(R.styleable.ScanningView_progress_icon,R.mipmap.icon_truck);
        }finally {
            a.recycle();
        }

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStrokeWidth(mProgressHeight);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStrokeWidth(mProgressHeight);
        mBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        mIconPaint = new Paint();
        mBitmap = BitmapFactory.decodeResource(context.getResources(),mIcon);

    }


    public void setToX(float x) {
        this.mProgress = x;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvasBackground(canvas);
        canvasProgress(canvas);
        canvasIcon(canvas);
    }

    private void canvasProgress(Canvas canvas){
        canvas.drawLine(mProgressHeight /2,getHeight()/2,mProgress- mProgressHeight /2,getHeight()/2,mProgressPaint);
    }
    private void canvasBackground(Canvas canvas){
        canvas.drawLine(mProgressHeight /2,getHeight()/2,getWidth()- mProgressHeight /2,getHeight()/2,mBackgroundPaint);
    }

    private void canvasIcon(Canvas canvas){
        //int mBitmapWidth = mBitmap.getWidth();   // 图片的宽 640
        int mBitmapHeight = mBitmap.getHeight(); // 图片的高 360
        //Rect dstRect = new Rect(mImgHeight,mImgHeight,mImgWidth,mImgWidth);
        canvas.drawBitmap(mBitmap,mProgress,getHeight()/2-(mBitmapHeight/2),mIconPaint);

    }

    public void start(){
        int width = getWidth();
        System.out.println("width="+width);
        //while (this.mProgress < width && isRun){
            this.mProgress+=10;
            if (this.mProgress > (width- mProgressHeight /2)){
                this.mProgress = mProgressHeight;
            }
            invalidate();


            try {
                Thread.sleep(20);
            }catch (Exception e){

            }
        //}

//        mProgress = p;
//        invalidate();
//        forceLayout();
//        requestLayout();
    }


    public void restore(){
        this.mProgress = mProgressHeight;
    }
    public void startOpen(int progress){
        this.mProgress = progress;
        invalidate();
        //postInvalidate();
        System.out.println("progress="+progress);
    }


    public void onDestroy(){
        isRun = false;
        mBitmap.recycle();
        mBitmap = null;
    }

}
