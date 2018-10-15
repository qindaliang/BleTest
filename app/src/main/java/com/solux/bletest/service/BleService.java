package com.solux.bletest.service;

import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.solux.bletest.ble.LocalBleDeviceMessage;
import com.solux.bletest.utils.DateUtils;
import com.solux.bletest.utils.HexUtils;
import com.solux.bletest.utils.ToastUtils;

public class BleService extends Service {

    private String mNotifyData;
    private String mWriteData;

    @Override
    public void onCreate() {
        super.onCreate();
        bleNotify(LocalBleDeviceMessage.getInstance().getBleDevice());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public BleService getService(){
            return BleService.this;
        }
    }

    public void sendBleData(BleDevice bleDevice, BluetoothGatt gatt, byte[] bytes, boolean isSendHeartBeat) {
        BleManager.getInstance().write(
                bleDevice,
                "00008957-786e-4340-8bbb-2201c8699534",
                "89560002-b5a3-f393-e0a9-e50e24dcca9e",
                bytes,
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        Log.i("msg", "写入成功: " + new String(justWrite));
                        mWriteData = new String(justWrite);
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.i("msg", "onWriteFailure: " + exception.getDescription());
                    }
                });
    }

    public void read(BleDevice bleDevice, String uuid_service, String uuid_read) {
        BleManager.getInstance().read(bleDevice, uuid_service, uuid_read, new BleReadCallback() {
            @Override
            public void onReadSuccess(byte[] data) {
                // 读特征值数据成功
            }

            @Override
            public void onReadFailure(BleException exception) {
                // 读特征值数据失败
            }
        });
    }

    private void bleNotify(BleDevice bleDevice) {
        BleManager.getInstance().notify(bleDevice, "00008957-786e-4340-8bbb-2201c8699534", "89560001-b5a3-f393-e0a9-e50e24dcca9e", new BleNotifyCallback() {
            @Override
            public void onNotifySuccess() {

            }

            @Override
            public void onNotifyFailure(BleException exception) {
                ToastUtils.show(getApplicationContext(),"失败" );
            }

            @Override
            public void onCharacteristicChanged(byte[] data) {
                String mHexStr = HexUtils.encodeHexStr(data);
                Log.i("msg", "接收到的数据:" + mHexStr);

                mNotifyData = DateUtils.getCurrentTime() + "接收到的数据:"+ mHexStr;
                ToastUtils.show(getApplicationContext(),"成功" );
            }
        });
    }

    public String getNotifyData(){
        return mNotifyData;
    }

    public String getWriteData(){
        return mWriteData;
    }
}
