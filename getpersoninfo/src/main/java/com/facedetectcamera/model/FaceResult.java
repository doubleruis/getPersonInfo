package com.facedetectcamera.model;

/**
 * Created by sunjunli on 11/20/2018.
 */

public class FaceResult extends Object {

    public float [] mRect;
    public float[] mMarks;
    public String mFaceState;
    private int mHasFace;


    public FaceResult() {
        mHasFace = 0;
        mMarks = new float[212];
        mRect = new float[4];

    }


    public void setFace(int iHasFace, float[] faceRect, float[] marks, String faceState) {
        set(iHasFace, faceRect,marks,faceState);
    }

    public void clear() {
        set(0, mRect,mMarks,mFaceState);
    }

    public synchronized void set(int iHasFace, float[] faceRect, float[] marks, String faceState) {
        this.mHasFace = iHasFace;

        for(int k = 0;k<4;++k) {
            mRect[k] = faceRect[k];
        }

        this.mFaceState = faceState;

    }


    public int getId() {
        return mHasFace;
    }

    public void setId(int id) {
        this.mHasFace = id;
    }


}
