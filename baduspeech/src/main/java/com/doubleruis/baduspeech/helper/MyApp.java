package com.doubleruis.baduspeech.helper;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import cn.finedo.adcore.common.AndroidApplication;

public class MyApp extends AndroidApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        AppFrontBackHelper helper = new AppFrontBackHelper();
        helper.register(MyApp.this, new AppFrontBackHelper.OnAppStatusListener() {
            @Override
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onFront() {
                //应用切到前台处理

            }

            @Override
            public void onBack() {
                //应用切到后台处理

            }
        });
    }

    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        //MultiDex.install(this);
    }
}
