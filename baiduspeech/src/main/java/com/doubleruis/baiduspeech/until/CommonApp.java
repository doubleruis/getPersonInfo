package com.doubleruis.baiduspeech.until;

import android.app.Application;
import android.content.Context;

/**
 * Created by chenxf on 17-7-14.
 */

public class CommonApp extends Application {
    private static Application mApplication = null;

    public static Context getContext() {
        return mApplication.getApplicationContext();
    }

    @Override
    public void onCreate() {
        if (mApplication == null) {
            PPLog.d("Application has not been set, extends from Application now." );
            mApplication = this;
            super.onCreate();
        }
    }
}
