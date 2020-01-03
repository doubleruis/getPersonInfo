package com.doubleruis.baduspeech.until;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.webkit.WebView;

public class MyWebviews extends WebView {
    public interface DisplayFinish{
        void After();
    }
    DisplayFinish df;
    public void setDf(DisplayFinish df) {
        this.df = df;
    }
    public MyWebviews(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MyWebviews(Context context) {
        super(context);
    }
    //onDraw表示显示完毕
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        df.After();
    }
}
