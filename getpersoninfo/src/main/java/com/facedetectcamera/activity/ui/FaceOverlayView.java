// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.facedetectcamera.activity.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.facedetectcamera.model.FaceResult;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;

/**
 * Created by Nguyen on 5/20/2016.
 */

/**
 * This class is a simple View to display the faces.
 */
public class FaceOverlayView extends View {

    private Paint mPaint;
    private Paint mTextPaint;
    private int mDisplayOrientation;
    private int mOrientation;
    private int previewWidth;
    private int previewHeight;
    private FaceResult mFace;
    private double fps;
    private boolean isFront = false;


    //private Rect mMarkRect;
    Bitmap bitmapMark;

    public FaceOverlayView(Context context) {
        super(context);
        initialize();
    }

    private void initialize()  {
        // We want a green box around the face:
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int stroke = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(stroke);
        mPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, metrics);
        mTextPaint.setTextSize(size);
        mTextPaint.setColor(Color.GREEN);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);


//        try {
            String path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/model/mark.png";
//            FileInputStream fis = new FileInputStream(android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/model/mark.png");
//            bitmapMark= BitmapFactory.decodeStream(fis);
            bitmapMark= BitmapFactory.decodeFile(path);
            //mMarkRect = new Rect(0,0,bitmapMark.getWidth(),bitmapMark.getHeight());
//        } catch (FileNotFoundException e) {
//
//            e.printStackTrace();
//
//        }



    }

    public void setFPS(double fps) {
        this.fps = fps;
    }

    public void setFace(FaceResult face) {
        mFace = face;
        invalidate();

    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFace != null)
        {

            float scaleX = (float) getWidth() / (float) previewWidth;
            float scaleY = (float) getHeight() / (float) previewHeight;

            switch (mDisplayOrientation) {
                case 90:
                case 270:
                    scaleX = (float) getWidth() / (float) previewHeight;
                    scaleY = (float) getHeight() / (float) previewWidth;
                    break;
            }




            canvas.save();
            canvas.rotate(-mOrientation);
            RectF rectF = new RectF();
          // canvas.drawBitmap(bitmapMark,mMarkRect,new RectF(0,0,getWidth(),getHeight()),mPaint);

                if (mFace.getId() != 0)
                {

                   rectF.set(new RectF(
                           mFace.mRect[0]* getWidth(),
                           mFace.mRect[1] * getHeight() ,
                           mFace.mRect[2] * getWidth(),
                           mFace.mRect[3] * getHeight() ));


                    if (isFront)
                    {
                       float left = rectF.left;
                       float right = rectF.right;
                     // rectF.left = getWidth() - right;
                      //rectF.right = getWidth() - left;
                    }
                    //canvas.drawRect(rectF, mPaint);
                    /*canvas.drawLine(rectF.left,rectF.top,rectF.left+rectF.width()*0.2f, rectF.top,mPaint);
                    canvas.drawLine(rectF.left,rectF.top,rectF.left, rectF.top+rectF.height()*0.2f,mPaint);

                    canvas.drawLine(rectF.left+rectF.width()*0.8f,rectF.top,rectF.left+rectF.width(), rectF.top,mPaint);
                    canvas.drawLine(rectF.left+rectF.width(),rectF.top,rectF.left+rectF.width(), rectF.top+rectF.height()*0.2f,mPaint);


                    canvas.drawLine(rectF.left,rectF.top+rectF.height(),rectF.left+rectF.width()*0.2f, rectF.top+rectF.height(),mPaint);
                    canvas.drawLine(rectF.left,rectF.top+rectF.height()*0.8f,rectF.left, rectF.top+rectF.height(),mPaint);


                    canvas.drawLine(rectF.left+rectF.width()*0.8f,rectF.top+rectF.height(),rectF.left+rectF.width(), rectF.top+rectF.height(),mPaint);
                    canvas.drawLine(rectF.left+rectF.width(),rectF.top+rectF.height()*0.8f,rectF.left+rectF.width(), rectF.top+rectF.height(),mPaint);*/

                    for(int k = 0;k<68;++k)
                    {
                     //  canvas.drawCircle(mFace.mMarks[2*k]* getWidth(), mFace.mMarks[2*k+1]* getHeight(),1,mPaint);
                    }

            }

            try {
//                canvas.drawText(mFace.mFaceState,rectF.left,rectF.top,mTextPaint);
                canvas.restore();
            } catch (Exception e) {
            }

        }

        DecimalFormat df2 = new DecimalFormat(".##");
       // canvas.drawText("Detected_Frame/s: " + df2.format(fps) + " @ " + previewWidth + "x" + previewHeight, mTextPaint.getTextSize(), mTextPaint.getTextSize(), mTextPaint);
    }


    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    public void setFront(boolean front) {
        isFront = front;
    }
}