package com.solux.bletest.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.solux.bletest.listener.BleStateChangeListener;

/**
 * Create by qindl
 * on 2018/9/14
 */
public class BleStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 100);
        switch (state) {
            case BluetoothAdapter.STATE_ON:
                Log.i("msg", "打开");
                mBleStateChangeListener.on();
                break;
            case BluetoothAdapter.STATE_OFF:
                Log.i("msg", "关闭");
                mBleStateChangeListener.off();
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                Log.i("msg", "正在打开");

                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                Log.i("msg", "正在关闭");

                break;
            default:
                Log.i("msg", "未知状态");
                break;

        }
    }

    private BleStateChangeListener mBleStateChangeListener;

    public void setBleStateChangeListener(BleStateChangeListener bleStateChangeListener) {
        this.mBleStateChangeListener = bleStateChangeListener;
    }
}
