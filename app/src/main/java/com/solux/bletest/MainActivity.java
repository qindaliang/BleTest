package com.solux.bletest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanAndConnectCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.solux.bletest.constants.Constants;
import com.solux.bletest.listener.BleStateChangeListener;
import com.solux.bletest.listener.GpsStateListener;
import com.solux.bletest.permission.PermissionUtils;
import com.solux.bletest.receiver.BleStateReceiver;
import com.solux.bletest.receiver.DisConnectReceiver;
import com.solux.bletest.receiver.GpsBroadcastReceiver;
import com.solux.bletest.utils.ToastUtils;
import com.solux.bletest.view.dialog.WaitDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    @BindView(R.id.btn_test)
    Button btnTest;
    @BindView(R.id.btn_connect)
    Button btnConnect;
    private WaitDialog mDialog;
    private BleStateReceiver mBleStateReceiver;
    private PermissionUtils mPermission;
    private GpsBroadcastReceiver mGpsBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initDialog();
        initBleConfig();
        registerBleReceiver();
    }

    public void registerBleReceiver() {
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
    }

    public void registerDisconnectReceiver() {
        Intent intent = new Intent("com.solux.bletest.receiver.DisConnectReceiver");
        sendBroadcast(intent);
    }

    public void openBle() {
        Log.i(TAG, "openBle: " + isGpsEnabled());
        if (isEnable() && isGpsEnabled()) {
            scanBleName();
        } else {
            if (!isGpsEnabled()) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 100);
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

    private boolean isConnectBleName() {
        return BleManager.getInstance().isConnected(Constants.BLE_NAME);
    }

    public boolean isGpsEnabled() {
        LocationManager locationManager = ((LocationManager) this.getSystemService(Context.LOCATION_SERVICE));
        return Objects.requireNonNull(locationManager).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * 多个设备连接
     */
    public void scanBleName() {
        if (!isConnectBleName()) {
            BleManager.getInstance().scan(new BleScanCallback() {
                @Override
                public void onScanFinished(List<BleDevice> scanResultList) {
                    if (scanResultList.size() <= 0) {
                        ToastUtils.show(MainActivity.this, "没有搜索到蓝牙设备");
                        hideDialog();
                    } else if (scanResultList.size() == 1) {
                        connectBle(scanResultList.get(0));
                    } else {
                        connectBle(getMax(scanResultList));
                    }
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
    }

    public void connectBle(BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                mDialog.setMsg("正在连接中...");
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
                registerDisconnectReceiver();
            }
        });
    }

    public void scanAndConect() {
        BleManager.getInstance().scanAndConnect(new BleScanAndConnectCallback() {
            @Override
            public void onScanFinished(BleDevice scanResult) {
                mDialog.setMsg("正在连接中...");
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
                registerDisconnectReceiver();
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
                .setDeviceName(true, Constants.BLE_NAME)
                //  .setDeviceMac(Constants.BLE_MAC)
                .setScanTimeOut(5000)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    public void initDialog() {
        mDialog = new WaitDialog();

    }

    public void showDialog() {
        mDialog.show(getSupportFragmentManager());
    }

    public void hideDialog() {
        mDialog.hide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBleStateReceiver);
        unregisterReceiver(mGpsBroadcastReceiver);
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

    @OnClick({R.id.btn_connect, R.id.btn_test})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_connect:
                openBle();
                break;
            case R.id.btn_test:
                registerDisconnectReceiver();
                break;
        }
    }

    public BleDevice getMax(List<BleDevice> scanResultList) {
        int maxRssi = scanResultList.get(0).getRssi();
        BleDevice bleDevice = null;
        for (int i = 0; i < scanResultList.size(); i++) {
            if (maxRssi < scanResultList.get(i).getRssi()) {
                maxRssi = scanResultList.get(i).getRssi();
                bleDevice = scanResultList.get(i);
            }
        }
        return bleDevice;
    }
}
