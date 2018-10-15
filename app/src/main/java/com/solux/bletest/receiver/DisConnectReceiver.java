package com.solux.bletest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.solux.bletest.utils.ToastUtils;

public class DisConnectReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ToastUtils.show(context,"连接已断开，请重新连接或扫描！");
        Log.i("msg", "onReceive: 请重新连接或扫描");
    }
}
