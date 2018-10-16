package com.solux.bletest.view.dialog;

import android.app.AlertDialog;
import android.content.Context;

import com.solux.bletest.listener.DisconnectListener;
import com.solux.bletest.permission.PermissionUtils;

/**
 * Create by qindl
 * on 2018/10/16
 */
public class DialogUtils {
    public static void showSetting(Context context){
        new AlertDialog.Builder(context)
                .setTitle("温馨提示")
                .setMessage("此应用需要获取定位权限才能正常使用！")
                .setNegativeButton("取消",
                        (dialog, which) -> mPermissionListener.onCancel())
                .setPositiveButton("前往设置",
                        (dialog, which) -> mPermissionListener.onSetting())
                .setCancelable(false)
                .show();
    }
    private static PermissionUtils.PermissionListener mPermissionListener;

    public interface PermissionListener {
        void onCancel();

        void onSetting();
    }

    public void setPermissionListener(PermissionUtils.PermissionListener permissionListener) {
        this.mPermissionListener = permissionListener;
    }

}
