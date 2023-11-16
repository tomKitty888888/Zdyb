/*
 * Copyright (c) 18-8-13 上午9:48. create by User,email:godmarvin@163.com.
 */

package com.zdeps.app.utils;


import android.util.Log;

import com.zdyb.lib_common.http.MyObserver;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by godmarvin on 2017/8/13.
 */

public class ZipUtils {

    //单独解压文件
    public static void exactFiles(final String inputFile, final String outFolder, Consumer<Boolean> subscriber) throws Throwable {
        final File file = new File(inputFile);
        final File folder = new File(outFolder);
        if (!file.exists()) {
            subscriber.accept(false);
            throw new Throwable("inputFile is not exists");
        }
        if (!folder.isDirectory()) {
            subscriber.accept(false);
            throw new Throwable("outFolder is not exists");
        }

        Log.v("UpdateModuleActivity","开始解压文件 ");
        int result = P7ZipApi.executeCommand(ZipCommand.getExtractCmd(inputFile, outFolder));
        Log.v("UpdateModuleActivity","解压完成 "+(result==0)+" "+result);
        if (result==0) {
            subscriber.accept(true);
        }else {
            subscriber.accept(false);
        }
    }

}
