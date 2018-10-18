package com.solux.bletest.ui;

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
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.kyleduo.switchbutton.SwitchButton;
import com.solux.bletest.R;
import com.solux.bletest.constants.Constants;
import com.solux.bletest.data.BleData;
import com.solux.bletest.data.MsgReciveData;
import com.solux.bletest.data.MsgSendData;
import com.solux.bletest.listener.BleStateChangeListener;
import com.solux.bletest.listener.DisconnectListener;
import com.solux.bletest.listener.GpsStateListener;
import com.solux.bletest.listener.SoftKeyBoardListener;
import com.solux.bletest.permission.PermissionUtils;
import com.solux.bletest.receiver.BleStateReceiver;
import com.solux.bletest.receiver.DisConnectReceiver;
import com.solux.bletest.receiver.GpsBroadcastReceiver;
import com.solux.bletest.utils.DateUtils;
import com.solux.bletest.utils.DensityUtils;
import com.solux.bletest.utils.HexUtils;
import com.solux.bletest.utils.StringUtils;
import com.solux.bletest.utils.ToastUtils;
import com.solux.bletest.view.dialog.WaitDialog;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.jessyan.autosize.internal.CustomAdapt;

public class MainActivity extends AppCompatActivity implements CustomAdapt {

    private static final String TAG = "MainActivity";
    private static final int RETRY_CONNECT = 100;
    private static final int SCAN_DELAY = 101;
    public static final String SEND_DATA = "200";
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
    @BindView(R.id.sb_auto)
    SwitchButton sbAuto;
    @BindView(R.id.sb_open)
    SwitchButton sbOpen;
    @BindView(R.id.container)
    LinearLayout container;
    @BindView(R.id.tv_send)
    TextView tvSend;
    @BindView(R.id.tv_recive)
    TextView tvRecive;
    @BindView(R.id.tv_background)
    TextView tvBackground;
    @BindView(R.id.et_data)
    EditText etData;
    @BindView(R.id.ll_data)
    LinearLayout llData;

    private WaitDialog mConnectDialog;
    private BleStateReceiver mBleStateReceiver;
    private PermissionUtils mPermission;
    private GpsBroadcastReceiver mGpsBroadcastReceiver;
    private DisConnectReceiver mDisConnectReceiver;
    private StringBuilder mSendBuilder = new StringBuilder();
    private StringBuilder mReciveBuilder = new StringBuilder();
    private BleData mBleData = new BleData();
    private MyHandler mMyHandler;
    private int retryCount;
    public boolean mAuto;
    private boolean isSend = true;
    private int recivered;
    private int scanCount = 0;
    private List<Long> mTimes = new ArrayList<>();
    private Map<BleDevice, Integer> selectMap;
    private int mBtnTestX;
    private int mBtnTestY;
    private int mBtnConnectX;
    private int mBtnConnectY;
    private int tvBackgroundX;
    private SendFragment mFragment;
    private FragmentTransaction mTransaction;
    private ReciverFragment mReciverFragment;
    private long end;
    private WaitDialog mSendDialog;
    private boolean isHandDisconnect;
    private int mLlDataTop;
    private int mTop;
    private boolean isShowKeyBoard;
    private String[] dataArray = new String[]{"986610ad", "986620ad"};
    private int postion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        checkPermission();
        initConnectDialog();
        initSendDialog();
        initListener();
        initBleConfig();
        registerBleReceiver();
        initPostion();
        etData.setText(dataArray[postion]);
        mTransaction = getSupportFragmentManager().beginTransaction();
        showFragment(false);
        showFragment(true);
        mMyHandler = new MyHandler(this);
    }

    private void initListener() {
        container.animate().scaleY(0);
        sbAuto.setOnCheckedChangeListener((buttonView, isChecked) -> setAuto(isChecked));
        sbOpen.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (judgeDoubleClickTime(500)) {
//                sbOpen.setCheckedNoEvent(true);
//                return;
//            }
            if (isChecked) {
                isShowKeyBoard = true;
                llData.animate().y(DensityUtils.dip2px(this, 392));

                btnConnect.animate().scaleX(0.7f).scaleY(0.7f)
                        .x(DensityUtils.dip2px(this, 40))
                        .y(getBtnConnectPostion());
                btnTest.animate().scaleX(0.7f).scaleY(0.7f)
                        .x(getBtnTestPostionX())
                        .y(getBtnTestPostionY());
                container.animate().scaleY(1);
                container.setVisibility(View.VISIBLE);
                showFragment(true);
                mTop = llData.getTop();
            } else {
                isShowKeyBoard = false;
                llData.animate().y(llData.getTop() - mLlDataTop);
                btnConnect.animate().scaleX(1f).scaleY(1f)
                        .x(btnConnect.getLeft() - mBtnConnectX)
                        .y(btnConnect.getTop() - mBtnConnectY);
                btnTest.animate().scaleX(1f).scaleY(1f)
                        .x(btnTest.getLeft() - mBtnTestX)
                        .y(btnTest.getTop() - mBtnTestY);
                container.animate().scaleY(0);
            }
        });

        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                if (isShowKeyBoard)
                    llData.animate().y(DensityUtils.dip2px(MainActivity.this, 100));
            }

            @Override
            public void keyBoardHide(int height) {
                if (isShowKeyBoard)
                    llData.animate().y(llData.getTop() + DensityUtils.dip2px(MainActivity.this, 70));
            }
        });
    }

    private void showFragment(boolean isChecked) {
        mTransaction = getSupportFragmentManager().beginTransaction();
        if (mFragment != null) {
            mTransaction.hide(mFragment);
        }
        if (mReciverFragment != null) {
            mTransaction.hide(mReciverFragment);
        }
        if (isChecked) {
            if (mFragment != null)
                mTransaction.show(mFragment);
            else {
                mFragment = new SendFragment();
                mTransaction.add(R.id.content, mFragment);
            }
        } else {
            if (mReciverFragment != null)
                mTransaction.show(mReciverFragment);
            else {
                mReciverFragment = new ReciverFragment();
                mTransaction.add(R.id.content, mReciverFragment);
            }
        }
        mTransaction.commit();
    }

    public float getBtnConnectPostion() {
        return DensityUtils.getScreenHeight(this) - btnConnect.getBottom() - btnConnect.getHeight() - DensityUtils.dip2px(this, 50);
    }

    public float getBtnTestPostionY() {
        return DensityUtils.getScreenHeight(this) - btnTest.getBottom() - DensityUtils.dip2px(this, 20);
    }

    public float getBtnTestPostionX() {
        return DensityUtils.getScreenWidth(this) - btnTest.getWidth() - DensityUtils.dip2px(this, 40);
    }

    public void initPostion() {
        mBtnTestX = btnTest.getLeft();
        mBtnTestY = btnTest.getTop();
        mBtnConnectX = btnConnect.getLeft();
        mBtnConnectY = btnConnect.getTop();
        tvBackgroundX = tvBackground.getLeft();
        mLlDataTop = llData.getTop();
    }

    public void setAuto(boolean auto) {
        this.mAuto = auto;
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
                hideDialog(mConnectDialog);
                BleManager.getInstance().disconnect(mBleData.getBleDevice());
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
        //   intent.putExtra(Constants.SEND_RECIVERED,recivered);
        sendBroadcast(intent);
    }

    public void openBle() {
        retryCount = 0;
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

    public void scanBleName() {
        if (!isConnectBleName(mBleData.getBleDevice())) {
            BleManager.getInstance().scan(new BleScanCallback() {
                @Override
                public void onScanFinished(List<BleDevice> scanResultList) {
                    if (scanResultList.size() <= 0) {
                        ToastUtils.show(MainActivity.this, "没有搜索到蓝牙设备");
                        hideDialog(mConnectDialog);
                        setDisVisiable();
                    } else if (scanResultList.size() == 1) {
                        setBleText(scanResultList.get(0));
                        Log.i(TAG, "onScanFinished: 单个设备连接");
                        connectBle(scanResultList.get(0).getMac());
                    } else {
                        BleDevice bleDevice = getMaxRssi(scanResultList);
                        setBleText(bleDevice);
                        Log.i(TAG, "onScanFinished: 多设备选择设备连接");
                        connectBle(bleDevice.getMac());
                    }
                }

                private void setBleText(BleDevice scanResultList) {
                    setVisiable();
                    tvMac.setText(scanResultList.getMac());
                    String name = scanResultList.getName();
                    if (TextUtils.isEmpty(name))
                        tvName.setText("未知设备");
                    else
                        tvName.setText(name);
                    tvRssi.setText(String.valueOf(scanResultList.getRssi()));
                    tvState.setText("连接中...");
                }

                @Override
                public void onScanStarted(boolean success) {
                    showDialog(mConnectDialog);
                    scanCount = 0;
                    mTimes.clear();
                    EventBus.getDefault().post(new MsgSendData(null));
                    EventBus.getDefault().post(new MsgReciveData(null));
                }

                @Override
                public void onScanning(BleDevice bleDevice) {
                    if (null != bleDevice) {
                        scanCount++;
                        Log.i(TAG, "onScanning: " + bleDevice.getMac());
                        long time = System.currentTimeMillis();
                        mTimes.add(time);
                        mMyHandler.sendEmptyMessageDelayed(SCAN_DELAY, 500);
                        if (mTimes.size() >= 2) {
                            mMyHandler.removeCallbacksAndMessages(null);
                            long dis = mTimes.get(scanCount - 1) - mTimes.get(scanCount - 2);
                            Log.i(TAG, "onScanning: 时间间隔：" + dis);
                            if (dis > 100) {
                                stopScan();
                            }
                        }
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

    public void connectBle(String mac) {
        if (!isConnectBleName(mBleData.getBleDevice())) {
            BleManager.getInstance().connect(mac, new BleGattCallback() {
                @Override
                public void onStartConnect() {
                    Log.i(TAG, "onStartConnect: 开始连接");
                    tvState.setText("连接中...");
                }

                @Override
                public void onConnectFail(BleDevice bleDevice, BleException exception) {
                    ToastUtils.show(MainActivity.this, "连接失败");
                    Log.i(TAG, "onConnectFail: 连接失败" + exception.getDescription() + exception.getCode());
                    tvState.setText("连接失败");
                    tvState.setTextColor(Color.RED);
                    Message message = Message.obtain();
                    message.what = RETRY_CONNECT;
                    message.obj = bleDevice;
                    mMyHandler.sendMessageDelayed(message, 500);
                }

                @Override
                public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                    ToastUtils.show(MainActivity.this, "连接成功");
                    Log.i(TAG, "onConnectSuccess: 连接成功");
                    hideDialog(mConnectDialog);
                    setVisiable();
                    tvState.setText("连接成功");
                    tvState.setTextColor(Color.GREEN);
                    recivered = 0;
                    mBleData = new BleData();
                    mBleData.setBleDevice(bleDevice);
                    bleNotify(bleDevice);
                }

                @Override
                public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                    ToastUtils.show(MainActivity.this, "已断开连接");
                    Log.i(TAG, "onDisConnected: 已断开连接");
                    tvState.setText("已断开连接");
                    tvState.setTextColor(Color.GRAY);
                    if (isHandDisconnect) {
                        isHandDisconnect = false;
                        return;
                    }
                    if (!mAuto) {
                        recivered++;
                        if (recivered == 1) {
                            sendDisConnectBroadcast();
                        }
                    } else {
                        scanBleName();
                    }
                }
            });
        }
    }

    public void sendBleData(BleDevice bleDevice, byte[] bytes) {
        if (isConnectBleName(mBleData.getBleDevice())) {
            BleManager.getInstance().write(bleDevice,
                    "00008957-786e-4340-8bbb-2201c8699534",
                    "89560002-b5a3-f393-e0a9-e50e24dcca9e", bytes, new BleWriteCallback() {
                        @Override
                        public void onWriteSuccess(int current, int total, byte[] justWrite) {
                            hideDialog(mSendDialog);
                            mSendBuilder.append(DateUtils.getCurrentTime() + "发送成功: " + Arrays.toString(justWrite));
                            mSendBuilder.append("\n");
                            Log.i(TAG, "发送成功: " + mSendBuilder.toString());
                            String hexStr = HexUtils.encodeHexStr(justWrite);
                            String xHexString = StringUtils.get0XHexString(hexStr);
                            String send = DateUtils.getCurrentTime() + "发送成功: " + xHexString
                                    + "\n" + DateUtils.getCurrentTime() + "发送成功: " + hexStr;
                            EventBus.getDefault().post(new MsgSendData(send));
                            postion++;
                            if (postion < dataArray.length) {
                                etData.setText(dataArray[postion]);

                                byte[] toBytes = HexUtils.hexStringToBytes(dataArray[postion]);
                                sendBleData(mBleData.getBleDevice(), toBytes);
                            }
                        }

                        @Override
                        public void onWriteFailure(BleException exception) {
                            hideDialog(mSendDialog);
                            Log.i(TAG, "onWriteFailure: " + exception.getDescription());
                        }
                    });
        }
    }

    private void bleNotify(BleDevice bleDevice) {
        if (isConnectBleName(mBleData.getBleDevice())) {
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
                            String xHexString = StringUtils.get0XHexString(mHexStr);
                            Log.i(TAG, "接收到的数据:" + xHexString);
                            String reciveData = DateUtils.getCurrentTime() + "接收成功: " + xHexString + "\n" +
                                    DateUtils.getCurrentTime() + "接收成功: " + mHexStr;
                            EventBus.getDefault().post(new MsgReciveData(reciveData));

                            if ("981cad".equals(mHexStr)||"982cad".equals(mHexStr)) {
                                String success = DateUtils.getCurrentTime() + "测试成功，数据收发正常";
                                EventBus.getDefault().post(new MsgReciveData(success));
                                EventBus.getDefault().post(new MsgSendData(success));
                                ToastUtils.show(getApplicationContext(), "测试成功，数据收发正常");
                            }else {
                                String fail = DateUtils.getCurrentTime() + "测试失败，数据收发异常";
                                EventBus.getDefault().post(new MsgReciveData(fail));
                                EventBus.getDefault().post(new MsgSendData(fail));
                                ToastUtils.show(getApplicationContext(), "测试失败，数据收发异常");
                            }
                        }
                    });
        }
    }

    private void initBleConfig() {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setDeviceName(true, Constants.BLE_NAME)
                //  .setDeviceMac(Constants.BLE_MAC)
                .setScanTimeOut(20000)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    public void initConnectDialog() {
        mConnectDialog = new WaitDialog();
        mConnectDialog.setMsg("正在连接中");
    }

    public void initSendDialog() {
        mSendDialog = new WaitDialog();
        mSendDialog.setMsg("数据发送中");
    }

    public void showDialog(WaitDialog dialog) {
        if (dialog.isAdded()) {
            getSupportFragmentManager().beginTransaction().remove(dialog).commit();
        }
        dialog.show(getSupportFragmentManager());
    }

    public void hideDialog(WaitDialog dialog) {
        dialog.hide();
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
        if (null != mMyHandler) {
            mMyHandler.removeCallbacksAndMessages(null);
            mMyHandler = null;
        }
        unregisterReceiver(mBleStateReceiver);
        unregisterReceiver(mGpsBroadcastReceiver);
        unregisterReceiver(mDisConnectReceiver);
        BleManager.getInstance().destroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
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

    public BleDevice getMaxRssi(List<BleDevice> scanResultList) {
        int maxRssi = -100;
        BleDevice device = null;
        selectMap = new HashMap<>(scanResultList.size());
        for (BleDevice bleDevice : scanResultList) {
            selectMap.put(bleDevice, bleDevice.getRssi());
        }
        for (Map.Entry<BleDevice, Integer> entry : selectMap.entrySet()) {
            if (maxRssi < entry.getValue()) {
                maxRssi = entry.getValue();
                device = entry.getKey();
            }
        }
        Log.i(TAG, "getMaxRssi: " + maxRssi);
        Log.i(TAG, "getMaxRssi: " + device.getMac());
        return device;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.detail:
                if (isConnectBleName(mBleData.getBleDevice())) {
                    isHandDisconnect = true;
                    BleManager.getInstance().disconnect(mBleData.getBleDevice());
                } else {
                    ToastUtils.show(MainActivity.this, "请先连接设备！");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.btn_connect, R.id.btn_test, R.id.tv_send, R.id.tv_recive})
    public void onViewClicked(View view) {
        if (judgeDoubleClickTime(500)) {
            return;
        }
        switch (view.getId()) {
            case R.id.btn_connect:
                openBle();
                // scanAndConect();
                break;
            case R.id.btn_test:
                if (isConnectBleName(mBleData.getBleDevice())) {
                    showDialog(mSendDialog);
                    byte[] bytes = processData(0X10);
                    String xHexString = StringUtils.get0XHexString(HexUtils.encodeHexStr(bytes));
                    Log.i(TAG, "0X16进制: " + xHexString);
                    String trim = etData.getText().toString().trim();
                    if (TextUtils.isEmpty(trim)) {
                        sendBleData(mBleData.getBleDevice(), bytes);
                    } else {
                        byte[] toBytes = HexUtils.hexStringToBytes(trim);
                        sendBleData(mBleData.getBleDevice(), toBytes);
                        Log.i(TAG, "0X16进制: " + trim);
                    }
                } else {
                    ToastUtils.show(MainActivity.this, "请先连接设备！");
                }
                break;
            case R.id.tv_send:
                showFragment(true);
                tvBackground.animate().x(tvBackgroundX - tvBackground.getLeft());
                tvSend.setTextColor(getResources().getColor(R.color.red));
                tvRecive.setTextColor(getResources().getColor(R.color.black));
                break;
            case R.id.tv_recive:
                showFragment(false);
                tvBackground.animate().x(DensityUtils.getScreenWidth(this) / 2 - DensityUtils.dip2px(this, 5));
                tvRecive.setTextColor(getResources().getColor(R.color.red));
                tvSend.setTextColor(getResources().getColor(R.color.black));
                break;
        }
    }

    public boolean judgeDoubleClickTime(long time) {
        long start = System.currentTimeMillis();
        if (start - end > time) {
            end = System.currentTimeMillis();
            return false;
        }
        return true;
    }

    public byte[] processData(int a) {
        List<Integer> list = new ArrayList<>();
        list.add(0X98);
        list.add(0X66);
        list.add(a);
        list.add(0XAD);
        byte[] datas = new byte[list.size()];
        for (int i = 0; i < datas.length; i++) {
            datas[i] = list.get(i).byteValue();
        }
        return datas;
    }

    @Override
    public boolean isBaseOnWidth() {
        return false;
    }

    @Override
    public float getSizeInDp() {
        return 667;
    }

    private class MyHandler extends Handler {

        private WeakReference<Activity> mMReference;

        public MyHandler(Activity activity) {
            mMReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Activity activity = mMReference.get();
            if (null == activity) {
                return;
            }
            switch (msg.what) {
                case RETRY_CONNECT:
                    retryCount++;
                    BleDevice bleDevice = (BleDevice) msg.obj;
                    if (retryCount < 6) {
                        connectBle(bleDevice.getMac());
                        Log.i(TAG, "handleMessage: " + retryCount);
                    } else {
                        hideDialog(mConnectDialog);
                    }
                    break;
                case SCAN_DELAY:
                    stopScan();
                    Log.i(TAG, "handleMessage: ");
                    break;
            }
        }
    }
}
