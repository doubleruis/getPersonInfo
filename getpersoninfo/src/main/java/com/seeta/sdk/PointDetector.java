package com.seeta.sdk;

public class PointDetector {
    static{
        System.loadLibrary("PointDetectorJni");
    }

    public long impl = 0;
    private native void construct(String seetaModel);
    public PointDetector(String seetaModel){
        this.construct(seetaModel);
    }

    public native void dispose();
    protected void finalize()throws Throwable {
        super.finalize();
        this.dispose();
    }

    public static native int SetLogLevel(int level);

    public native int GetLandmarkNumber();

    public native boolean Detect(SeetaImageData image, SeetaRect face, SeetaPointF[] points);

    public native boolean Detect(SeetaImageData image, SeetaRect face, SeetaPointF[] points, int[] masks);
}
