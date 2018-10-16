package com.solux.bletest.listener;

import android.bluetooth.BluetoothDevice;


/**
 * Create by qindl
 * on 2018/9/14
 */
public interface BleStateConnectListener {
    void onConnected(BluetoothDevice device);
    void onDisConnected(BluetoothDevice device);
}
