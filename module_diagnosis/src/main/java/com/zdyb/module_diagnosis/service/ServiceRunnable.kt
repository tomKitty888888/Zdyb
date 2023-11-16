package com.zdyb.module_diagnosis.service

import com.zdeps.diag.DiagJni
import com.zdeps.gui.ComJni


class ServiceRunnable : Runnable{

    lateinit var jniLibsPath :String
    constructor(path:String){
        this.jniLibsPath = path
    }

    override fun run() {
        DiagJni.load(jniLibsPath)
        ComJni.nativeSetup()
        DiagJni.run()
    }
}