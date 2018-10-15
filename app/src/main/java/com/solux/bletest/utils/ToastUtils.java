package com.solux.bletest.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Create by qindl
 * on 2018/9/12
 */
public class ToastUtils {

    private static Toast mToast = null;

    public static void show(Context context, String text) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public static void cancel() {
        if (mToast != null) {
            mToast.cancel();
        }
    }
}
