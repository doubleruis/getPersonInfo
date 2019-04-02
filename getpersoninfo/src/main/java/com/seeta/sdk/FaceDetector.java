package com.seeta.sdk;

public class FaceDetector
{
    static{
        System.loadLibrary("FaceDetectorJni");
    }

    public long impl = 0;

    private native void construct(String seetaModel);
    private  void construct(String seetaModel, SeetaSize coreSize){
        this.constructCore(seetaModel, coreSize.width, coreSize.height);
    }
    private native void constructCore(String seetaModel, int width, int height);
    public FaceDetector(String seetaModel) {
        this.construct(seetaModel);
    }
    public FaceDetector(String seetaModel, SeetaSize coreSize) {
        this.construct(seetaModel, coreSize);
    }

    public native void dispose();
    protected void finalize()throws Throwable {
        super.finalize();
        this.dispose();
    }

    public static native int SetLogLevel(int level);

    public static native void SetSingleCalculationThreads(int num);

    public native SeetaRect[] Detect(SeetaImageData image);

    public native void SetMinFaceSize(int size);
    public native int GetMinFaceSize();

    public native void SetImagePyramidScaleFactor(float factor);
    public native float GetImagePyramidScaleFactor();

    public native void SetScoreThresh(float thresh1, float thresh2, float thresh3);
    public native void GetScoreThresh(float[] thresh);

    public native void SetVideoStable(boolean stable);
    public native boolean GetVideoStable();
}
