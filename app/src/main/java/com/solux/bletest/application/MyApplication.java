package com.solux.bletest.application;

import android.app.Application;

import com.clj.fastble.BleManager;

/**
 * Create by qindl
 * on 2018/9/13
 */
public class MyApplication extends Application {
    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        BleManager.getInstance().init(this);
        BleManager
                .getInstance()
                .enableLog(true)//打开库中的运行日志
                .setReConnectCount(3, 5000)//设置连接时重连次数和重连间隔（毫秒）
                .setSplitWriteNum(20)//设置分包发送的时候，每一包的数据长度，默认20个字节
                .setConnectOverTime(5000)//设置连接超时时间（毫秒），默认10秒
                .setOperateTimeout(5000);//设置readRssi、setMtu、write、read、notify、indicate的超时时间（毫秒），默认5秒
    }
}
