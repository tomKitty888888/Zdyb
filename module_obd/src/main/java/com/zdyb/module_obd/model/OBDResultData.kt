package com.zdyb.module_obd.model

import androidx.lifecycle.MutableLiveData
import com.zdeps.bean.OBDBean

object OBDResultData {


    //车辆信息
    var cartInfo = mutableListOf<OBDBean.ObdData>()
    //obd检测信息
    var obdInfo = mutableListOf<OBDBean.ObdData>()
    //排放相关数据流
    var emissionInfo = mutableListOf<OBDBean.ObdData>()

    val KEY_CURRENT = "Current" //故障数量key
    val KEY_READINESSSTATUS = "ReadinessStatus" //就绪状态的key

    /**
     * 故障数据
     */
    val failureData = mutableListOf<OBDBean.ObdData>()

    /**
     * 就绪状态
     */
    val readinessStatus = mutableListOf<OBDBean.ObdData>()

    /**
     * 记录的故障码数量
     */
    var failureSum = 0

    /**
     * 记录的就绪状态未完成项目
     */
    var readinessStatusSum = 0

    /**
     * 记录故障灯状态 0是关 1是开，默认关
     */
    var lampState = 0

}