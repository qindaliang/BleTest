package com.solux.bletest.receiver;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.solux.bletest.constants.Constants;
import com.solux.bletest.listener.DisconnectListener;

public class DisConnectReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      //  boolean isSend = intent.getBooleanExtra(Constants.SEND_RECIVERED, false);

        new AlertDialog.Builder(context)
                .setTitle("温馨提示")
                .setMessage("连接已断开，请重新连接或扫描！")
                .setNegativeButton("取消",
                        (dialog, which) -> mDisconnectListener.cancel(dialog))
                .setPositiveButton("重连",
                        (dialog, which) -> mDisconnectListener.retryConnect(dialog))
                .setCancelable(false)
                .show();
    }

    private DisconnectListener mDisconnectListener;

    public void setDisconnectListener(DisconnectListener disconnectListener) {
        this.mDisconnectListener = disconnectListener;
    }
}
