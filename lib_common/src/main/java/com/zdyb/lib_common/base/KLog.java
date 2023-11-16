package com.zdyb.lib_common.base;

import android.util.Log;

import com.zdyb.lib_common.BuildConfig;

public class KLog {

    private static final String tag = "KLog";

    private static final boolean isDebug = BuildConfig.DEBUG;

    public static void i(String tag,String msg){
        if (!isDebug) return;
        Log.i(tag,msg);
    }
    public static void i(String msg){
        if (!isDebug) return;
        Log.i(tag,msg);
    }

    public static void e(String msg){
        if (!isDebug) return;
        Log.e(tag,msg);
    }

    public static void d(String tag,String msg){
        if (!isDebug) return;
        Log.d(tag,msg);
    }
    public static void d(String msg){
        if (!isDebug) return;
        Log.d(tag,msg);
    }
}
