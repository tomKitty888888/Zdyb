package com.zdyb.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.utils.RouterUtil
import com.zdyb.lib_common.utils.constant.RouteConstants

class EntranceActivity :AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrance)
        BaseApplication.FLAVOR = BuildConfig.FLAVOR
        when(BuildConfig.FLAVOR){
            AppType.zdyb.name -> {
                RouterUtil.build(RouteConstants.Diagnosis.DIAGNOSIS_ACTIVITY).launch()
            }
            AppType.obd.name -> {
                RouterUtil.build(RouteConstants.Obd.OBD_ACTIVITY_LOGIN).launch()
            }
        }
        finish()
    }

    enum class AppType{
        zdyb,obd
    }

}