package com.zdyb.module_diagnosis.bean

import java.io.Serializable


class DeviceEntity : Serializable {


    companion object{
        const val tag :String = "DeviceEntity"
    }

    constructor(id: Long, path: String, name: String) {
        this.id = id
        this.path = path
        this.name = name
    }

    var id :Long
    var path :String  //Diagnosis\Electronic\01Bosch
    var name :String  //博世EDC17CV44/54系统 P903_V762(锡柴国四国五发动机) CAN线
    lateinit var soPath :String  ///storage/emulated/0/zdeps/Diagnosis/Electronic/05Cummins/V4.008/diag/armeabi-v7a/libdiag-lib.so
    lateinit var versionPath :String //  /storage/emulated/0/zdeps/Diagnosis/Electronic/05Cummins/V4.008/ 注意最后的/


}