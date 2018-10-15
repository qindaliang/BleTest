package com.solux.bletest.ble;

import android.bluetooth.BluetoothGatt;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.solux.bletest.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ClosestoolBleUtils {

    private static int write_index = 1;

    /**
     * 编辑发送的蓝牙数据
     * 字节	1	2	3	4	5	6	7	8	9	10	11	12	13	14	15	16	备注
     * 帧	帧头		序号		命令	数据1	数据2	数据3	数据4	数据5	数据6	数据7	数据8	数据9	数据10	SUM
     * APP --> 蓝牙
     * 心跳	0x51	0x98	0xXX	0xXX	0xA1
     * 设置获取	0x51	0x98	0xXX	0xXX	0xA2
     * 设置推送	0x51	0x98	0xXX	0xXX	0xA3	"臀洗清洗水温01-05"	"臀洗强度档位01-05"	"臀洗清洗位置01-05"	"妇洗清洗水温01-05"	"妇洗强度档位01-05"	"妇洗清洗位置01-05"	"烘干温度档位01-05"	"座圈温度档位     01-05"	"夜灯模式01-03"
     * 妇洗	0x51	0x98	0xXX	0xXX	0x01
     * 臀洗	0x51	0x98	0xXX	0xXX	0x02
     * 烘干	0x51	0x98	0xXX	0xXX	0x03
     * 小冲	0x51	0x98	0xXX	0xXX	0x04
     * 大冲	0x51	0x98	0xXX	0xXX	0x05
     * 自洁	0x51	0x98	0xXX	0xXX	0x06
     * 坐温	0x51	0x98	0xXX	0xXX	0x07
     * 夜灯	0x51	0x98	0xXX	0xXX	0x08
     */
    public static int[] editSendBleData(List<Integer> command, boolean isSendHeatBeat) {
        List<Integer> data = new ArrayList<>(16);
        for (int i = 0; i < 16; i++) {
            data.add(0);
        }
        //前两位固定
        data.set(0, 0x51);
        data.set(1, 0x98);
        //第三、四字节：数据包不一样时序号自动累加1。编号在0000~ffff间循环累加。
        if (write_index > 0xffff) {
            write_index = 0;
        }
        write_index++;
        //高位
        int mHeight = write_index / 0xff;
        String mHeightStr = Integer.toHexString(mHeight);
        data.set(2, mHeight);
        //低位
        int mLow = write_index % 0xff;
        String mLowStr = Integer.toHexString(mLow);
        if(!isSendHeatBeat) {
//            MyApplication.getInstance().setHeight(mHeightStr);
//            MyApplication.getInstance().setLow(mLowStr);
        }
        data.set(3, mLow);
        //第五字节开始：命令
        for (int i = 0; i < command.size(); i++) {
            data.set(4 + i, command.get(i));
        }

        //第十六位校验，将第三位加到第十五位，和除以255或ff求余
        int num = 0;
        for (int i = 2; i < 15; i++) {
            num += data.get(i);
        }
        data.set(15, num % 0xff);

        //把list转数组
        int[] intData = new int[data.size()];
        for (int i = 0; i < data.size(); i++) {
            intData[i] = data.get(i);
        }
        return intData;
    }

    /**
     * 连接蓝牙
     */
    public static void connectBle(BleDevice bleDevice) {

        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                onConnectBleListener.onStartConnect();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                onConnectBleListener.onConnectFail(bleDevice, exception);
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                LocalBleDeviceMessage.getInstance().setBleDevice(bleDevice);
                LocalBleDeviceMessage.getInstance().setGatt(gatt);
                onConnectBleListener.onConnectSuccess(bleDevice, gatt, status);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                onConnectBleListener.onDisConnected(isActiveDisConnected, device, gatt, status);
            }
        });
    }

    public static OnConnectBleListener onConnectBleListener;

    public static void setOnConnectBleListener(OnConnectBleListener onConnectBleListener) {
        ClosestoolBleUtils.onConnectBleListener = onConnectBleListener;
    }

    public interface OnConnectBleListener {
        void onStartConnect();

        void onConnectFail(BleDevice bleDevice, BleException exception);

        void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status);

        void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status);
    }

    private static List<Integer> bleSendData = new ArrayList<>();

    public static void sendBleData(Integer command, boolean isSendHeartBeat) {
        bleSendData.clear();
        bleSendData.add(command);
        sendBleData(LocalBleDeviceMessage.getInstance().getBleDevice(), LocalBleDeviceMessage.getInstance().getGatt(), bleSendData,isSendHeartBeat);
    }

    public static void sendBleData(List<Integer> command, boolean isSendHeartBeat) {
        bleSendData.clear();
        bleSendData.addAll(command);
        sendBleData(LocalBleDeviceMessage.getInstance().getBleDevice(), LocalBleDeviceMessage.getInstance().getGatt(), bleSendData,isSendHeartBeat);
    }

    public static void sendBleData(BleDevice bleDevice, BluetoothGatt gatt, Integer command, boolean isSendHeartBeat) {
        bleSendData.clear();
        bleSendData.add(command);
        sendBleData(bleDevice, gatt, bleSendData,isSendHeartBeat);
    }

    /**
     * 发送蓝牙数据
     * uuid_service:00008957-786e-4340-8bbb-2201c8699534
     * uuid_chara:89560002-b5a3-f393-e0a9-e50e24dcca9e
     */
    public static void sendBleData(BleDevice bleDevice, BluetoothGatt gatt, List<Integer> command, boolean isSendHeartBeat) {

        int[] data_int = ClosestoolBleUtils.editSendBleData(command,isSendHeartBeat);
        String[] data_string_encode = StringUtils.intArrayTo0xStringArray(data_int);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < data_string_encode.length; i++) {
            if (data_string_encode[i].length() == 1) {
                stringBuilder.append("0" + data_string_encode[i]);
            } else {
                stringBuilder.append(data_string_encode[i]);
            }
        }
        for (int i = 0; i < 3; i++) {
            BleManager.getInstance().write(
                    bleDevice,
                    "00008957-786e-4340-8bbb-2201c8699534",
                    "89560002-b5a3-f393-e0a9-e50e24dcca9e",
                    HexUtil.hexStringToBytes(stringBuilder.toString()),
                    new BleWriteCallback() {

                        @Override
                        public void onWriteSuccess(int current, int total, byte[] justWrite) {
                            Log.i("msg", "写入成功: "+new String(justWrite));

                        }

                        @Override
                        public void onWriteFailure(BleException exception) {
                            Log.i("msg", "onWriteFailure: "+exception.getDescription());
                        }
                    });
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送心跳
     */
    public static void sendHeartbeat(boolean isSendHearBeat) {
        sendBleData(0xA1,isSendHearBeat);
    }

    /**
     * 发送配对握手
     */
    public static void sendConjugate() {
        sendBleData(0xA3,true);
    }
}