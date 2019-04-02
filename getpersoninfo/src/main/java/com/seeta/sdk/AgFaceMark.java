package com.seeta.sdk;

public class AgFaceMark
{
	static{
		System.loadLibrary("FaceAntiSpoofingJni");
		System.loadLibrary("AgileFace");
	}

	public enum Status{
		REAL,
		SPOOF,
		FUZZY,
		DETECTING,
	};
	public long impl = 0;

	private native void construct(String model1, String model2);
	private native void construct(String model);
	public native void dispose();

	public AgFaceMark(String model1, String model2) {this.construct(model1, model2);}
	public AgFaceMark(String model){this.construct(model);}
	protected void finalize() throws Throwable {
		super.finalize();
		this.dispose();
	}
	public static native boolean AddAuthor(String ahtorcode);
	//Camera nv12 type data  to  BGR type,we can change dst's  w, h
	public static native int Nv12toBGR(byte[] nv12data,int w,int h,byte[] dstBGRdata,int dstW,int dstH);

	//when it is back camera,we should change vertical
	//Camera nv12 type data  to  BGR type,we can change dst's  w, h
	public static native int Nv12toBGRVer(byte[] nv12data,int w,int h,byte[] dstBGRdata,int dstW,int dstH);

	public static native int SetLogLevel(int level);
	public static native void SetSingleCalculationThreads(int num);

	private native int DoPredictCore(SeetaImageData image, SeetaRect face, SeetaPointF[] landmarks);
	private native int DoPredictVideoCore(SeetaImageData image, SeetaRect face, SeetaPointF[] landmarks);
	public Status Predict(SeetaImageData image, SeetaRect face, SeetaPointF[] landmarks)
	{
		int status_num = this.DoPredictCore(image, face, landmarks);
		if(status_num == 0)
		{
			return Status.REAL;
		}
		if(status_num == 1)
		{
			return Status.SPOOF;
		}
		if(status_num == 2)
		{
			return Status.FUZZY;
		}

		return Status.DETECTING;
	}

	public Status PredictVideo(SeetaImageData image, SeetaRect face, SeetaPointF[] landmarks)
	{
		int status_num = this.DoPredictVideoCore(image, face, landmarks);
		if(status_num == 0)
		{
			return Status.REAL;
		}
		if(status_num == 1)
		{
			return Status.SPOOF;
		}
		if(status_num == 2)
		{
			return Status.FUZZY;
		}

		return Status.DETECTING;
	}

	public native void ResetVideo();
	public native void GetPreFrameScore(float[] clarity, float[] reality);
	public native void SetVideoFrameCount(int number);
	public native int GetVideoFrameCount();
	public native void SetThreshold(float clarity, float reality);
	public native void GetThreshold(float[] clarity, float[] reality);
}