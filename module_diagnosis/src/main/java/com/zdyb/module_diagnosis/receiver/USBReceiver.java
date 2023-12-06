package com.zdyb.module_diagnosis.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

import com.zdyb.lib_common.base.BaseApplication;
import com.zdyb.lib_common.bus.BusEvent;
import com.zdyb.lib_common.bus.EventTypeDiagnosis;
import com.zdyb.lib_common.bus.RxBus;
import com.zdyb.module_diagnosis.MyApplication;

public class USBReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            // U盘根目录
            String mountPath = intent.getData().getPath();
            if (!TextUtils.isEmpty(mountPath)) {
                Log.d("TAG", "U盘挂载:" + mountPath);
                RxBus.getDefault().post(new BusEvent(EventTypeDiagnosis.USB_PATH,mountPath));
                BaseApplication.USBPath = mountPath;
            }
        } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED) || action.equals(Intent.ACTION_MEDIA_EJECT)) {
            Log.d("TAG", "U盘移除");
            RxBus.getDefault().post(new BusEvent(EventTypeDiagnosis.USB_OUT));
            BaseApplication.USBPath = null;
        } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {

        }


    }
}
