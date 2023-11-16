package com.zdyb.module_diagnosis.service;

import android.util.Log;

import com.zdeps.diag.DiagJni;
import com.zdeps.gui.ComJni;

public class ServiceRunnablee implements Runnable {
    public volatile boolean isRunning = true;
    private ComJni comJni;
    private String libName;
    private DiagJni diagJni;

    public ServiceRunnablee(ComJni comJni,String libName) {
        this.diagJni = DiagJni.INSTANCE;
        this.comJni=comJni;
        this.libName=libName;
    }

    @Override
    public void run() {
        diagJni.load(libName);
        comJni.nativeSetup();
        diagJni.run();
        Log.v("DiagService","nativeSetup3 ");
    }
}