package com.zdyb.module_obd

import android.app.Application
import com.zdeps.obd.DiagAbs
import com.zdyb.lib_common.base.AppConfig
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.bluetooth.BluetoothManager
import io.reactivex.functions.Consumer

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