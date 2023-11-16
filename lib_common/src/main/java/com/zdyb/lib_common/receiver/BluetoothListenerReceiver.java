package com.zdyb.lib_common.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.zdyb.lib_common.base.KLog;

import io.reactivex.functions.Consumer;


public class BluetoothListenerReceiver extends BroadcastReceiver {

    private Consumer<Integer> consumer;

    public BluetoothListenerReceiver(Consumer<Integer> consumer){
        this.consumer = consumer;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                switch (blueState) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        KLog.i("onReceive---------蓝牙正在打开中");
                        callBack(1);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        KLog.i("onReceive---------蓝牙已经打开");
                        callBack(2);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        KLog.i("onReceive---------蓝牙正在关闭中");
                        callBack(3);
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        KLog.i("onReceive---------蓝牙已经关闭");
                        callBack(4);
                        break;
                }
                break;
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                KLog.i("onReceive---------蓝牙已经连接");
                callBack(5);
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                KLog.i("onReceive---------蓝牙已经断开");
                callBack(6);
                break;
        }
    }

    private void callBack(int state){
        try {
            if (consumer!= null){
                consumer.accept(state);
            }
        }catch (Exception e){

        }
    }
}
