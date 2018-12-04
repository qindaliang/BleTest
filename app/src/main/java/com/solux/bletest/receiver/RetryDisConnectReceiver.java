package com.solux.bletest.receiver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;

import com.solux.bletest.R;
import com.solux.bletest.listener.DisconnectListener;
import com.solux.bletest.view.dialog.WaitDialog;

public class RetryDisConnectReceiver extends BroadcastReceiver {


    private Dialog mDialog;

    @Override
    public void onReceive(Context context, Intent intent) {
        //  boolean isSend = intent.getBooleanExtra(Constants.SEND_RECIVERED, false);

//        new AlertDialog.Builder(context)
//                .setTitle("温馨提示")
//                .setMessage("连接已断开，正在尝试重新扫描连接！")
//                .setView(LayoutInflater.from(context).inflate(R.layout.dialog_wait, null))
//                .setCancelable(false)
//                .show();

        mDialog = new Dialog(context);
        mDialog.setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_wait_retry, null));
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public void hide() {
        if (mDialog != null) {

                mDialog.dismiss();

        }
    }

    private DisconnectListener mDisconnectListener;

    public void setDisconnectListener(DisconnectListener disconnectListener) {
        this.mDisconnectListener = disconnectListener;
    }
}
