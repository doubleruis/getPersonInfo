package com.doubleruis.baiduspeech.until;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by chenxf on 17-7-14.
 */

public class PaoPaoTips {
    public static void showDefault(Context context, String tips) {
        Toast.makeText(context, tips, Toast.LENGTH_SHORT);
    }
}
