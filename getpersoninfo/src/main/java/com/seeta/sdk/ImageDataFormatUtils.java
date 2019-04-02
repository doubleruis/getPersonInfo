package com.seeta.sdk;

/**
 * Created by mimi on 2018/5/29.
 */

public class ImageDataFormatUtils {

    static {
        System.loadLibrary("ImageDataFormatUtils");
    }
    public static native  byte[] Nv21ToARGB(byte[] data, int width, int height);

    public static native byte[] ARGBToBGR(byte[] data, int width, int height);
}
