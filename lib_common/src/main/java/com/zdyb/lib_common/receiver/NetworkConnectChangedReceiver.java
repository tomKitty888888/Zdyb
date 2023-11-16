package com.zdyb.lib_common.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;

import io.reactivex.functions.Consumer;


public class NetworkConnectChangedReceiver extends BroadcastReceiver {

    private Consumer<Integer> consumer;
    public NetworkConnectChangedReceiver(Consumer<Integer> consumer){
        this.consumer = consumer;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {//这个监听wifi的打开与关闭，与wifi的连接无关
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            log("WIFI状态：wifiState=" + wifiState);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    log("WIFI状态：wifiState:WIFI_STATE_DISABLED");
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    log("WIFI状态：wifiState:WIFI_STATE_DISABLING");
                    break;
                case WifiManager.WIFI_STATE_ENABLED: //启用
                    log("WIFI状态：wifiState:WIFI_STATE_ENABLED");
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    log("WIFI状态：wifiState:WIFI_STATE_ENABLING");
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    log("WIFI状态：wifiState:WIFI_STATE_UNKNOWN");
                    break;
                //
            }
        }
        // 这个监听wifi的连接状态即是否连上了一个有效无线路由，当上边广播的状态是WifiManager.WIFI_STATE_DISABLING，和WIFI_STATE_DISABLED的时候，根本不会接到这个广播。
        // 在上边广播接到广播是WifiManager.WIFI_STATE_ENABLED状态的同时也会接到这个广播，当然刚打开wifi肯定还没有连接到有效的无线
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            //WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
            //String bssid = intent.getStringExtra(WifiManager.EXTRA_BSSID);
            if (parcelableExtra != null){
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                if(state==NetworkInfo.State.DISCONNECTED){

                    //LedManager.getInstance().slowFlash();
                    log("WIFI状态：断开连接");
                    sendState(0);
                }else if(state== NetworkInfo.State.CONNECTED){
                    log("WIFI状态：已连接");
                    log("WIFI状态：网络连接后更新时间");
                    //LedManager.getInstance().close();
                    sendState(1);
                }
            }
        }

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)){
            NetworkType networkType = NetworkUtil.getNetworkType(context);
            log("NetworkType网络状态"+ networkType);

            if (networkType == NetworkType.NETWORK_ETHERNET){

            }else if (networkType == NetworkType.NETWORK_NO){

            }
        }

    }

    public void sendState(Integer state){
        try {
            if (consumer != null){
                consumer.accept(state);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void log(String text){
        Log.i("tag",text);
        LogUtils.i(text);
    }
}
