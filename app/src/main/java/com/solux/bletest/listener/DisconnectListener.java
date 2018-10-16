package com.solux.bletest.listener;

import android.content.DialogInterface;

/**
 * Create by qindl
 * on 2018/10/16
 */
public interface DisconnectListener {
    public void cancel(DialogInterface dialog);
    public void retryConnect(DialogInterface dialog);
}
