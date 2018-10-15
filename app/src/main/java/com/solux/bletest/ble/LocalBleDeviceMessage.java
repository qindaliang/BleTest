package com.solux.bletest.ble;

import android.bluetooth.BluetoothGatt;

import com.clj.fastble.data.BleDevice;

public class LocalBleDeviceMessage {
    private static LocalBleDeviceMessage instance;
    private BleDevice bleDevice;
    private BluetoothGatt gatt;

    public static LocalBleDeviceMessage getInstance() {
        if (instance == null) {
            synchronized (LocalBleDeviceMessage.class) {
                if (instance == null) {
                    instance = new LocalBleDeviceMessage();
                }
            }
        }
        return instance;
    }

    public BleDevice getBleDevice() {
        return bleDevice;
    }

    public void setBleDevice(BleDevice bleDevice) {
        this.bleDevice = bleDevice;
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }
}
