package com.solux.bletest.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.solux.bletest.listener.BleStateConnectListener;

/**
 * Create by qindl
 * on 2018/9/14
 */
public class BleConnectReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        switch (action) {
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                Log.i("msg", "onReceive: 已连接");
                mBleStateConnectListener.onConnected(device);
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                Log.i("msg", "onReceive: 断开连接");
                mBleStateConnectListener.onDisConnected(device);
                break;
            default:
                break;
        }
    }

    private BleStateConnectListener mBleStateConnectListener;

    public void setBleStateConnectListener(BleStateConnectListener bleStateConnectListener){
        this.mBleStateConnectListener = bleStateConnectListener;
    }
}
