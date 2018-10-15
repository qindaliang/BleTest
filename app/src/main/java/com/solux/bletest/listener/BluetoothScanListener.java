package com.solux.bletest.listener;

import android.bluetooth.le.ScanResult;

import java.util.List;

/**
 * Create by qindl
 * on 2018/9/13
 */
public interface BluetoothScanListener {
    void onScanResult(int callbackType, ScanResult result);
    void onBatchScanResults(List<ScanResult> results);
    void onScanFailed(int errorCode);
}
