package com.zdyb.module_diagnosis.netservice

import com.blankj.utilcode.util.EncryptUtils
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.IBaseHttpService
import com.zdyb.lib_common.base.NotApiThrowableConsumer
import com.zdyb.lib_common.http.NetWorkManager
import com.zdyb.lib_common.http.response.ResponseTransformer
import com.zdyb.module_diagnosis.bean.ItemVersionEntity
import com.zdyb.module_diagnosis.bean.MotorcycleTypeEntity
import io.reactivex.Observable
import java.util.*

object DiagInteractor {

    private val mService = NetWorkManager.getInstance().getService(DiagService::class.java)

    /**
     * 	获取类型接口
     */
    fun motorcycleType(type:String,vci:String) : Observable<MutableList<MotorcycleTypeEntity>> {
        val paramsMap = HashMap<String,Any>()
        paramsMap["type"] = type
        paramsMap["vci"] = vci

        val times = (Date().time / 1000).toString()
        paramsMap["times"] = times
        paramsMap["iphering"] = EncryptUtils.encryptMD5ToString(times + vci + BaseApplication.FLAVOR)

        return mService.motorcycleType(IBaseHttpService.mapToFormRequestBody(paramsMap))
            .compose(ResponseTransformer.handleResult())
            .doOnError(NotApiThrowableConsumer());
    }

    /**
     * 	获取全部的版本
     */
    fun versionsAll(type:String,vci:String,mt:String) : Observable<MutableList<ItemVersionEntity>> {
        val paramsMap = HashMap<String,Any>()
        paramsMap["type"] = type
        paramsMap["vci"] = vci
        paramsMap["motorcycle_type"] = mt

        return mService.versionsAll(IBaseHttpService.mapToFormRequestBody(paramsMap))
            .compose(ResponseTransformer.handleResult())
            .doOnError(NotApiThrowableConsumer());
    }
}