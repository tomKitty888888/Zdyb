package com.zdyb.lib_common.utils;


import androidx.annotation.StringRes;

import com.blankj.utilcode.util.ToastUtils;


/**
 * Created by goldze on 2017/5/14.
 * 吐司工具类
 */
public final class MyToastUtils {


    public static void showDefaultToast(@StringRes int msg){
        ToastUtils.showShort(msg);

    }

    public static void showDefaultToast(String msg){
        //KLog.e(msg);
        ToastUtils.showShort(msg);

    }

    public static void cancel(){
        ToastUtils.cancel();
    }
}