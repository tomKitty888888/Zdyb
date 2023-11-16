package com.zdyb.module_diagnosis

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.blankj.utilcode.util.LogUtils
import com.zdyb.lib_common.base.AppConfig
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.service.UsbSerialPortService
import com.zdyb.lib_common.service.UsbSerialPortService.MyBinder

class MyApplication : BaseApplication() {

    override fun onCreate() {
        super.onCreate()


    }


    override fun initModuleApp(application: Application) {
        for (moduleApp in AppConfig.moduleApps) {
            try {
                val clazz = Class.forName(moduleApp)
                val baseApp = clazz.newInstance() as BaseApplication
                baseApp.initModuleApp(this)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InstantiationException) {
                e.printStackTrace()
            }
        }
    }

    override fun initModuleData(application: Application) {
        for (moduleApp in AppConfig.moduleApps) {
            try {
                val clazz = Class.forName(moduleApp)
                val baseApp = clazz.newInstance() as BaseApplication
                baseApp.initModuleData(this)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InstantiationException) {
                e.printStackTrace()
            }
        }
    }
}