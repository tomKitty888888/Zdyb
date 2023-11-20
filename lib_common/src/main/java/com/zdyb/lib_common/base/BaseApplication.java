package com.zdyb.lib_common.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;

import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDex;

import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;
import com.zdyb.lib_common.BuildConfig;
import com.zdyb.lib_common.R;
import com.zdyb.lib_common.cockroach.Cockroach;
import com.zdyb.lib_common.cockroach.CrashLog;
import com.zdyb.lib_common.cockroach.ExceptionHandler;
import com.zdyb.lib_common.http.NetWorkManager;
import com.zdyb.lib_common.service.UsbSerialPortService;
import com.zdyb.lib_common.utils.file.FileUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import kotlin.jvm.internal.Intrinsics;

/**
 * Created by goldze on 2017/6/15.
 */

public abstract class BaseApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private static BaseApplication sInstance;

    /**
     * 是否退出诊断服务关闭多进程
     */
    private boolean isOutDiagnosisService = false;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        String processName = getProcessName(android.os.Process.myPid());
        if (!TextUtils.isEmpty(processName) && processName.equals(this.getPackageName())) {
            //主进程初始化逻辑
            CrashReport.initCrashReport(this, "212f48c9bc", false);

            if (BuildConfig.DEBUG) {
                // 这两行必须写在init之前，否则这些配置在init过程中将无效
                // 打印日志
                ARouter.openLog();
                // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
                ARouter.openDebug();
                ARouter.printStackTrace();
            }
            ARouter.init(this);

            installCockroach();



            //注册监听每个activity的生命周期,便于堆栈式管理
            registerActivityLifecycleCallbacks(this);
            //Context context = getApplicationContext();

            // 获取当前包名
            //String packageName = context.getPackageName();
            // 获取当前进程名
            //String processName = getProcessName(android.os.Process.myPid());

            //KLog.i("当前包名=" + packageName);
            //KLog.i("当前进程名=" + processName);

            //网络监听变化
            //IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            //registerReceiver(NetworkChange.getInstance(), filter);
            //startAlarm(0);

            //inhibitionAndroidPAlert();

            NetWorkManager.getInstance().init(this);
            initToast();
            //odb年检预检是关闭的 未打开usb
            bingUsbService();
            //FileUtil.isARMv7Compatible();

            KLog.i("初始化了");
            KLog.i("进程ID="+android.os.Process.myPid());
        }

    }

    private void initToast() {
        ToastUtils.getDefaultMaker().setGravity(Gravity.CENTER, 0, 0);
        ToastUtils.getDefaultMaker().setBgColor(ContextCompat.getColor(this, R.color.black));
        ToastUtils.getDefaultMaker().setTextColor(ContextCompat.getColor(this, R.color.white));
    }



    public void bingUsbService(){
        Intent intent = new Intent(this, UsbSerialPortService.class);
        intent.setAction("android.intent.action.UsbService");
        bindService(intent,mUsbConnection,Context.BIND_AUTO_CREATE);
    }

    public void unBingUsbService(){
        if(usbConn != null){
            unbindService(mUsbConnection);
        }
    }




    public static UsbSerialPortService usbConn;
    private ServiceConnection mUsbConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UsbSerialPortService.MyBinder myBinder = (UsbSerialPortService.MyBinder) service;
            usbConn = myBinder.getService();
            LogUtils.i("usb服务绑定成功");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.i("usb服务断开--重新绑定");
            bingUsbService();
        }
    };


    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Application 初始化
     */
    public abstract void initModuleApp(Application application);

    /**
     * 所有 Application 初始化后的自定义操作
     */
    public abstract void initModuleData(Application application);

    /**
     * 获得当前app运行的AppContext
     */
    public static BaseApplication getInstance() {
        return sInstance;
    }

    /**
     * 是否退出诊断服务关闭多进程
     * @return
     */
    public void setOutDiagnosisService(boolean bo){
        isOutDiagnosisService = bo;
    }

    /**
     * 是否退出诊断服务关闭多进程
     * @return
     */
    public boolean getOutDiagnosisService(){
        return isOutDiagnosisService;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        AppManager.getAppManager().addActivity(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {


    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        AppManager.getAppManager().removeActivity(activity);
    }


    PowerManager.WakeLock wakeLock;

    @SuppressLint("InvalidWakeLockTag")
    private void powerService() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "sm_hooroo");
        wakeLock.acquire();
    }

    private void inhibitionAndroidPAlert() {
        if (Build.VERSION.SDK_INT >= 27) {
        }
        try {
            Class.forName("android.content.pm.PackageParser$Package").getDeclaredConstructor(new Class[]{String.class}).setAccessible(true);
            Object localObject1 = Class.forName("android.app.ActivityThread");
            Object localObject2 = ((Class) localObject1).getDeclaredMethod("currentActivityThread", new Class[0]);
            ((Method) localObject2).setAccessible(true);
            localObject2 = ((Method) localObject2).invoke(null, new Object[0]);
            localObject1 = ((Class) localObject1).getDeclaredField("mHiddenApiWarningShown");
            ((Field) localObject1).setAccessible(true);
            ((Field) localObject1).setBoolean(localObject2, true);
            return;
        } catch (Throwable localThrowable) {
        }
    }


    /**
     * app运行过程中异常处理
     */
    private void installCockroach() {
        final Thread.UncaughtExceptionHandler sysExcepHandler = Thread.getDefaultUncaughtExceptionHandler();
        Cockroach.install(this, new ExceptionHandler() {
            @Override
            protected void onUncaughtExceptionHappened(Thread thread, Throwable throwable) {
                Log.e("AndroidRuntime", "--->onUncaughtExceptionHappened:" + thread + "<---", throwable);
                //上传异常日志
                CrashLog.saveCrashLog(getApplicationContext(), throwable);
            }

            @Override
            protected void onBandageExceptionHappened(Throwable throwable) {
                throwable.printStackTrace();//打印警告级别log，该throwable可能是最开始的bug导致的，无需关心
            }

            @Override
            protected void onEnterSafeMode() {
//                int tips = R.string.safe_mode_tips;
//                Toast.makeText(MyApplication.this, getResources().getString(tips), Toast.LENGTH_LONG).show();
//                DebugSafeModeUI.showSafeModeUI();
//                if (BuildConfig.DEBUG) {
//                    Intent intent = new Intent(MyApplication.this, DebugSafeModeTipActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                }
            }

            @Override
            protected void onMayBeBlackScreen(Throwable e) {
                Thread thread = Looper.getMainLooper().getThread();
                Log.e("AndroidRuntime", "--->onUncaughtExceptionHappened:" + thread + "<---", e);
                if (BuildConfig.DEBUG) {
                    //黑屏时建议直接杀死app
                    sysExcepHandler.uncaughtException(thread, new RuntimeException("black screen"));
                }
            }

        });

    }

}
