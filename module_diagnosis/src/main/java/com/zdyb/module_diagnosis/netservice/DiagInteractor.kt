package com.zdyb.module_diagnosis.netservice

import com.blankj.utilcode.util.EncryptUtils
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.IBaseHttpService
import com.zdyb.lib_common.base.NotApiThrowableConsumer
import com.zdyb.lib_common.bean.NoValueBean
import com.zdyb.lib_common.http.NetWorkManager
import com.zdyb.lib_common.http.response.ResponseTransformer
import com.zdyb.module_diagnosis.bean.ItemVersionEntity
import com.zdyb.module_diagnosis.bean.LoginResultBean
import com.zdyb.module_diagnosis.bean.MotorcycleTypeEntity
import com.zdyb.module_diagnosis.bean.VciUpdateInfoBean
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
        paramsMap["iphering"] = EncryptUtils.encryptMD5ToString(times + vci + BaseApplication.ENTRANCE)

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


    /**
     * 	获取验证码
     */
    fun sendSms(vci:String,phone:String) : Observable<NoValueBean> {
        val paramsMap = HashMap<String,Any>()
        paramsMap["vci"] = vci
        paramsMap["phone"] = phone

        return mService.sendSms(IBaseHttpService.mapToFormRequestBody(paramsMap))
            .compose(ResponseTransformer.handleResult())
            .doOnError(NotApiThrowableConsumer());
    }


    /**
     * 	登录
     */
    fun zdybValidate(vci:String,phone:String,code:String) : Observable<LoginResultBean> {
        val paramsMap = HashMap<String,Any>()
        paramsMap["vci"] = vci
        paramsMap["phone"] = phone
        paramsMap["code"] = code

        val times = (Date().time / 1000).toString()
        paramsMap["times"] = times
        paramsMap["iphering"] = EncryptUtils.encryptMD5ToString(times + phone + vci + BaseApplication.ENTRANCE)

        return mService.zdybValidate(IBaseHttpService.mapToFormRequestBody(paramsMap))
            .compose(ResponseTransformer.handleResult())
            .doOnError(NotApiThrowableConsumer());
    }

    /**
     * 	获取下位机升级信息
     */
    fun version(vci:String) : Observable<VciUpdateInfoBean> {
        val paramsMap = HashMap<String,Any>()
        paramsMap["vci"] = vci

        return mService.version(IBaseHttpService.mapToFormRequestBody(paramsMap))
            .compose(ResponseTransformer.handleResult())
            .doOnError(NotApiThrowableConsumer());
    }

    /**
     * 获取下位机蓝牙升级信息
     */
    fun bluetooth(vci:String,flag:Int) : Observable<VciUpdateInfoBean> {
        val paramsMap = HashMap<String,Any>()
        paramsMap["vci"] = vci
        paramsMap["flag"] = flag
        return mService.bluetooth(IBaseHttpService.mapToFormRequestBody(paramsMap))
            .compose(ResponseTransformer.handleResult())
            .doOnError(NotApiThrowableConsumer());
    }

    /**
     * 获取智能诊断配置文件
     */
    fun downResource(vci:String) : Observable<VciUpdateInfoBean> {
        val paramsMap = HashMap<String,Any>()
        paramsMap["vci"] = vci

        val times = (Date().time / 1000).toString()
        paramsMap["times"] = times
        paramsMap["iphering"] = EncryptUtils.encryptMD5ToString(times + vci + BaseApplication.ENTRANCE)
        paramsMap["language"] = ""

        return mService.downResource(IBaseHttpService.mapToFormRequestBody(paramsMap))
            .compose(ResponseTransformer.handleResult())
            .doOnError(NotApiThrowableConsumer());
    }


}