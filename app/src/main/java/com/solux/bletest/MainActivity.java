package com.solux.bletest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanAndConnectCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.solux.bletest.constants.Constants;
import com.solux.bletest.data.BleData;
import com.solux.bletest.listener.BleStateChangeListener;
import com.solux.bletest.listener.DisconnectListener;
import com.solux.bletest.listener.GpsStateListener;
import com.solux.bletest.permission.PermissionUtils;
import com.solux.bletest.receiver.BleStateReceiver;
import com.solux.bletest.receiver.DisConnectReceiver;
import com.solux.bletest.receiver.GpsBroadcastReceiver;
import com.solux.bletest.utils.DateUtils;
import com.solux.bletest.utils.HexUtils;
import com.solux.bletest.utils.StringUtils;
import com.solux.bletest.utils.ToastUtils;
import com.solux.bletest.view.dialog.WaitDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RETRY_CONNECT = 100;
    @BindView(R.id.btn_test)
    Button btnTest;
    @BindView(R.id.btn_connect)
    Button btnConnect;
    @BindView(R.id.tv_nodevice)
    TextView tvNodevice;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_rssi)
    TextView tvRssi;
    @BindView(R.id.ll_top)
    LinearLayout llTop;
    @BindView(R.id.tv_mac)
    TextView tvMac;
    @BindView(R.id.tv_state)
    TextView tvState;
    @BindView(R.id.ll_bottom)
    LinearLayout llBottom;
    private WaitDialog mDialog;
    private BleStateReceiver mBleStateReceiver;
    private PermissionUtils mPermission;
    private GpsBroadcastReceiver mGpsBroadcastReceiver;
    private DisConnectReceiver mDisConnectReceiver;
    private StringBuilder mBuilder = new StringBuilder();
    private BleData mBleData = new BleData();
    private MyHandler mMyHandler;
    private int retryCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        checkPermission();
        initDialog();
        initBleConfig();
        registerBleReceiver();
        mMyHandler = new MyHandler(this);
    }

    public void registerBleReceiver() {
        IntentFilter filterg = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        mGpsBroadcastReceiver = new GpsBroadcastReceiver();
        registerReceiver(mGpsBroadcastReceiver, filterg);
        mGpsBroadcastReceiver.setGpsStateListener(new GpsStateListener() {
            @Override
            public void on() {
                Log.i(TAG, "on: GPS已打开");
                ToastUtils.show(MainActivity.this, "GPS已打开");
            }

            @Override
            public void off() {
                Log.i(TAG, "off: GPS已关闭");
                ToastUtils.show(MainActivity.this, "GPS已关闭");
            }
        });

        mBleStateReceiver = new BleStateReceiver();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBleStateReceiver, filter);
        mBleStateReceiver.setBleStateChangeListener(new BleStateChangeListener() {
            @Override
            public void on() {
                ToastUtils.show(MainActivity.this, "蓝牙已打开");
            }

            @Override
            public void off() {
                ToastUtils.show(MainActivity.this, "蓝牙已关闭");
            }
        });

        mDisConnectReceiver = new DisConnectReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("DisConnectReceiver");
        registerReceiver(mDisConnectReceiver, intentFilter);
        mDisConnectReceiver.setDisconnectListener(new DisconnectListener() {

            @Override
            public void cancel(DialogInterface dialog) {
                dialog.dismiss();
            }

            @Override
            public void retryConnect(DialogInterface dialog) {
                openBle();
            }
        });
    }

    private void checkPermission() {
        mPermission = new PermissionUtils(this);
        mPermission.checkPermissions();
        mPermission.setPermissionListener(new PermissionUtils.PermissionListener() {
            @Override
            public void onCancel() {
                finish();
            }

            @Override
            public void onSetting() {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 100);
            }
        });
    }

    public void sendDisConnectBroadcast() {
        Intent intent = new Intent("DisConnectReceiver");
        sendBroadcast(intent);
    }

    public void openBle() {
        if (isEnable() && isGpsEnabled()) {
            scanBleName();
        } else {
            if (!isGpsEnabled()) {
                checkPermission();
            }
            BleManager.getInstance().enableBluetooth();
        }
    }

    public boolean isSupport() {
        return BleManager.getInstance().isSupportBle();
    }

    public boolean isEnable() {
        return isSupport() && BleManager.getInstance().isBlueEnable();
    }

    private boolean isConnectBleName(BleDevice bleDevice) {
        return BleManager.getInstance().isConnected(bleDevice);
    }

    public boolean isGpsEnabled() {
        LocationManager locationManager = ((LocationManager) this.getSystemService(Context.LOCATION_SERVICE));
        return Objects.requireNonNull(locationManager).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * 多个设备连接
     */
    public void scanBleName() {
        if (!isConnectBleName(mBleData.getBleDevice())) {
            BleManager.getInstance().scan(new BleScanCallback() {
                @Override
                public void onScanFinished(List<BleDevice> scanResultList) {
                    if (scanResultList.size() <= 0) {
                        ToastUtils.show(MainActivity.this, "没有搜索到蓝牙设备");
                        hideDialog();
                        setDisVisiable();
                    } else {
                        long startTime = System.currentTimeMillis();
                        if (scanResultList.size() > 0) {
                            tvMac.setText(scanResultList.get(0).getMac());
                            String name = scanResultList.get(0).getName();
                            if (TextUtils.isEmpty(name))
                                tvName.setText("未知设备");
                            else
                                tvName.setText(name);
                            tvRssi.setText(String.valueOf(scanResultList.get(0).getRssi()));
                            tvState.setText("连接中...");
                            connectBle(scanResultList.get(0).getMac());
//                            long endTime = System.currentTimeMillis();
//                            long distinct = endTime - startTime;
//                            Log.i(TAG, "onScanFinished: " + distinct);
//                            if (distinct < 1000 && scanResultList.size() < 2) {
//                                stopScan();
//                                Log.i(TAG, "onScanFinished: 一个设备");
//                                connectBle(scanResultList.get(0).getMac());
//                            } else {
//                                Log.i(TAG, "onScanFinished: 多个设备");
//                                connectBle(getMax(scanResultList).getMac());
//                            }
                        }
                    }
                }

                @Override
                public void onScanStarted(boolean success) {
                    showDialog();
                }

                @Override
                public void onScanning(BleDevice bleDevice) {
                    if (null!=bleDevice){
                        setVisiable();
                        stopScan();
                    }
                }
            });
        } else {
            ToastUtils.show(MainActivity.this, "已连接");
        }
    }

    public void stopScan() {
        BleManager.getInstance().cancelScan();
    }

    public void connectBle(BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.i(TAG, "onStartConnect: 开始连接");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                ToastUtils.show(MainActivity.this, "连接失败");
                Log.i(TAG, "onConnectFail: 连接失败" + exception.getDescription() + bleDevice.getMac());
                hideDialog();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.i(TAG, "onConnectSuccess: 连接成功");
                ToastUtils.show(MainActivity.this, "连接成功");
                hideDialog();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                ToastUtils.show(MainActivity.this, "已断开连接");
                sendDisConnectBroadcast();
            }
        });
    }

    public void connectBle(String mac) {
        BleManager.getInstance().connect(mac, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.i(TAG, "onStartConnect: 开始连接");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                ToastUtils.show(MainActivity.this, "连接失败");
                Log.i(TAG, "onConnectFail: 连接失败" + exception.getDescription() + bleDevice.getMac());
                connectBle(bleDevice.getMac());
                tvState.setText("连接失败");
                tvState.setTextColor(Color.RED);
                Message message = Message.obtain();
                message.what = RETRY_CONNECT;
                message.obj = bleDevice;
                mMyHandler.sendMessageDelayed(message,300);
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                ToastUtils.show(MainActivity.this, "连接成功");
                Log.i(TAG, "onConnectSuccess: 连接成功");
                hideDialog();
                setVisiable();
                tvState.setText("连接成功");
                tvState.setTextColor(Color.GREEN);
                mBleData = new BleData();
                mBleData.setBleDevice(bleDevice);
                bleNotify(bleDevice);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                ToastUtils.show(MainActivity.this, "已断开连接");
                sendDisConnectBroadcast();
                tvState.setText("已断开连接");
                tvState.setTextColor(Color.GRAY);
            }
        });
    }

    public void sendBleData(BleDevice bleDevice, byte[] bytes) {
        BleManager.getInstance().write(bleDevice,
                "00008957-786e-4340-8bbb-2201c8699534",
                "89560002-b5a3-f393-e0a9-e50e24dcca9e", bytes, new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        mBuilder.append(DateUtils.getCurrentTime() + "写入成功: " + Arrays.toString(justWrite));
                        mBuilder.append("\n");
                        Log.i(TAG, "写入成功: " + mBuilder.toString());
                        bleNotify(bleDevice);
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.i(TAG, "onWriteFailure: " + exception.getDescription());
                    }
                });
    }

    private void bleNotify(BleDevice bleDevice) {
        BleManager.getInstance().notify(bleDevice,
                "00008957-786e-4340-8bbb-2201c8699534",
                "89560001-b5a3-f393-e0a9-e50e24dcca9e", new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        Log.i(TAG, "onNotifySuccess: notify成功");
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        Log.i(TAG, "onNotifyFailure: " + exception.getDescription());
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        String mHexStr = HexUtils.encodeHexStr(data);
                        Log.i(TAG, "接收到的数据:" + mHexStr);
                        String reciveData = DateUtils.getCurrentTime() + "接收到的数据:" + mHexStr;
                        ToastUtils.show(getApplicationContext(), "成功");
                    }
                });
    }

    public void scanAndConect() {
        BleManager.getInstance().scanAndConnect(new BleScanAndConnectCallback() {
            @Override
            public void onScanFinished(BleDevice scanResult) {

            }

            @Override
            public void onStartConnect() {

            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                ToastUtils.show(MainActivity.this, "连接失败");
                hideDialog();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                ToastUtils.show(MainActivity.this, "连接成功");
                hideDialog();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                ToastUtils.show(MainActivity.this, "已断开连接");
                sendDisConnectBroadcast();
            }

            @Override
            public void onScanStarted(boolean success) {
                showDialog();
            }

            @Override
            public void onScanning(BleDevice bleDevice) {

            }
        });
    }

    private void initBleConfig() {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                // .setDeviceName(true, Constants.BLE_NAME)
                //  .setDeviceMac(Constants.BLE_MAC)
                .setScanTimeOut(10000)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    public void initDialog() {
        mDialog = new WaitDialog();
        mDialog.setMsg("正在连接中...");
    }

    public void showDialog() {
        if (mDialog.isAdded()) {
            getSupportFragmentManager().beginTransaction().remove(mDialog).commit();
        }
        mDialog.show(getSupportFragmentManager());
    }

    public void hideDialog() {
        mDialog.hide();
    }

    public void setVisiable() {
        llBottom.setVisibility(View.VISIBLE);
        llTop.setVisibility(View.VISIBLE);
        tvNodevice.setVisibility(View.INVISIBLE);
    }

    public void setDisVisiable() {
        llBottom.setVisibility(View.INVISIBLE);
        llTop.setVisibility(View.INVISIBLE);
        tvNodevice.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null!=mMyHandler){
            mMyHandler.removeCallbacksAndMessages(null);
            mMyHandler=null;
        }
        unregisterReceiver(mBleStateReceiver);
        unregisterReceiver(mGpsBroadcastReceiver);
        unregisterReceiver(mDisConnectReceiver);
        BleManager.getInstance().destroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            Log.i("EXCEPTION", "onActivityResult");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                        } else {
                            mPermission.onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;
        }
    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 1500) {
                ToastUtils.show(MainActivity.this, "再按一次退出程序");
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public BleDevice getMax(List<BleDevice> scanResultList) {
        int maxRssi = scanResultList.get(0).getRssi();
        BleDevice bleDevice = null;
        for (int i = 0; i < scanResultList.size(); i++) {
            if (maxRssi <= scanResultList.get(i).getRssi()) {
                maxRssi = scanResultList.get(i).getRssi();
                bleDevice = scanResultList.get(i);
            }
        }
        return bleDevice == null ? scanResultList.get(0) : bleDevice;
    }

    @OnClick({R.id.btn_connect, R.id.btn_test})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_connect:
                openBle();
                // scanAndConect();
                break;
            case R.id.btn_test:
                if (isConnectBleName(mBleData.getBleDevice())) {
                    byte[] bytes = processData();
                    Log.i(TAG, "onViewClicked: " + Arrays.toString(bytes));
                    sendBleData(mBleData.getBleDevice(), bytes);
                }else {
                    ToastUtils.show(MainActivity.this, "请先连接设备！");
                }
                break;
        }
    }

    public byte[] processData() {
        List<Integer> list = new ArrayList<>();
        list.add(0X98);
        list.add(0X66);
        list.add(0X10);
        list.add(0XAD);
        byte[] datas = new byte[list.size()];
        for (int i = 0; i < datas.length; i++) {
            datas[i] = list.get(i).byteValue();
        }
        return datas;
    }

    private class MyHandler extends Handler{

        private  WeakReference<Activity> mMReference;

        public MyHandler(Activity activity) {
            mMReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Activity activity = mMReference.get();
            if (null==activity){
                return;
            }
            switch (msg.what){
                case RETRY_CONNECT:
                    retryCount++;
                    BleDevice bleDevice = (BleDevice) msg.obj;
                    if (retryCount<6){
                        connectBle(bleDevice.getMac());
                        Log.i(TAG, "handleMessage: "+retryCount);
                    }
                    break;
            }
        }
    }
}
