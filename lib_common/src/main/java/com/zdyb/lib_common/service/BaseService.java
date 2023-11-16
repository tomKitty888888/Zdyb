package com.zdyb.lib_common.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.zdyb.lib_common.R;
import com.zdyb.lib_common.bus.BusEvent;
import com.zdyb.lib_common.bus.RxBus;
import com.zdyb.lib_common.bus.RxSubscriptions;
import com.zdyb.lib_common.receiver.NetworkConnectChangedReceiver;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public abstract class BaseService extends Service {

    private Disposable mSubscription;

    private boolean isDeBug = true;
    private String tag = this.getClass().getName();
    private int code = AppUtils.getAppVersionCode();

    NetworkConnectChangedReceiver networkConnectChangedReceiver;
    List<Consumer<Integer>> consumers = new ArrayList<>();


    @Override
    public void onCreate() {
        super.onCreate();
        consumers.clear();

        if (registerRxBus()){
            mSubscription = RxBus.getDefault().toObservable(BusEvent.class).subscribe(new Consumer<BusEvent>() {
                @Override
                public void accept(final BusEvent t) throws Exception {
                    eventComing(t);
                }
            });
            //将订阅者加入管理站
            RxSubscriptions.add(mSubscription);
        }
    }


    protected boolean registerRxBus() {
        return false;
    }
    protected void eventComing(BusEvent t) {
    }

    public boolean isNetworkAvailable() {
        return NetworkUtils.isAvailableByPing();
    }



    public void log(String text){
        if(isDeBug){
            //Log.i(tag,code+"(--)"+text);
            LogUtils.i(text);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStartForeground();
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public static final int SERVICE_ID = 136;
    public static final String CHANNEL_ID_STRING ="1";
    public final String TAG = BaseService.class.getSimpleName();
    public void onStartForeground(){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID_STRING, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
                notificationManager.createNotificationChannel(channel);
                Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID_STRING).build();
                startForeground(SERVICE_ID, notification);
                //
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        consumers.clear();
        if (networkConnectChangedReceiver!=null){
            unregisterReceiver(networkConnectChangedReceiver);
        }
        super.onDestroy();
    }

    /**
     * 网络连接状态监听广播
     */
    protected void registerNetworkConnectChangeReceiver(Consumer<Integer> consumer) {
        consumers.add(consumer);

        if (networkConnectChangedReceiver == null){
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
            filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
            //networkConnectChangedReceiver = new NetworkConnectChangedReceiver(consumer);

            networkConnectChangedReceiver = new NetworkConnectChangedReceiver(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    if (integer == 1){

                        for (Consumer<Integer> c :consumers){
                            c.accept(integer);
                        }
                        //连接网络后立马发送5次心跳
//                        TimerUtil.builder().build().setTimeRun(new TimerUtil.TimeRun() {
//                            @Override
//                            public void run() {
//
//                            }
//                        }).start(0,1000,5);
                    }
                }
            });
            registerReceiver(networkConnectChangedReceiver, filter);
        }

    }


}
