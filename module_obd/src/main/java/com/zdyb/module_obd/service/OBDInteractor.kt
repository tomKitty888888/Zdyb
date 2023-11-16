package com.zdyb.module_obd.service

import android.text.TextUtils
import com.zdeps.bean.AppUpdateBean
import com.zdeps.bean.LoginBean
import com.zdeps.bean.MotorcycleTypeBean
import com.zdeps.bean.VersionsAllBean
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.IBaseHttpService
import com.zdyb.lib_common.base.NotApiThrowableConsumer
import com.zdyb.lib_common.http.NetWorkManager
import com.zdyb.lib_common.http.response.ResponseTransformer
import com.zdyb.lib_common.utils.PreferencesUtils
import com.zdyb.lib_common.utils.SharePreferencesConstant
import io.reactivex.Observable

object OBDInteractor {

    private val mService = NetWorkManager.getInstance().getService(OBDService::class.java)


    /**
     * 获取验证码
     */
    fun getReginStart(vci: String,phone: String) : Observable<Any> {
        val paramsMap = HashMap<String,Any>()
        paramsMap["vci"] = vci
        paramsMap["phone"] = phone

        return mService.reginStart(IBaseHttpService.mapToFormRequestBody(paramsMap))
                .compose(ResponseTransformer.handleResult())
                .doOnError(NotApiThrowableConsumer())
    }


    /**
     * 验证码登录
     */
    fun getReginLogin(vci: String,phone: String,code: Int) : Observable<LoginBean> {
        val paramsMap = HashMap<String,Any>()
        paramsMap["vci"] = vci
        paramsMap["phone"] = phone
        paramsMap["code"] = code

        return mService.reginLogin(IBaseHttpService.mapToFormRequestBody(paramsMap))
                .compose(ResponseTransformer.handleResult())
                .doOnError(NotApiThrowableConsumer());
    }

    /**
     * 	获取车型分类下所有车型最新版本接口
     */
    fun getMotorcycleType(vci: String) : Observable<MutableList<MotorcycleTypeBean>> {
        val paramsMap = HashMap<String,Any>()
        paramsMap["vci"] = vci
        paramsMap["type"] = "Obd"

        return mService.getMotorcycleType(paramsMap)
                .compose(ResponseTransformer.handleResult())
                .doOnError(NotApiThrowableConsumer());
    }

    /**
     * 	获取指定车型下所有版接口
     */
    fun getVersionsAll(vci: String,type: String) : Observable<MutableList<VersionsAllBean>> {
        val paramsMap = HashMap<String,Any>()
        paramsMap["vci"] = vci
        paramsMap["type"] = type

        return mService.getVersionsAll(IBaseHttpService.mapToFormRequestBody(paramsMap))
                .compose(ResponseTransformer.handleResult())
                .doOnError(NotApiThrowableConsumer());
    }


    /**
     * 	获取APK最新版本接口
     */
    fun appUpdate(vci:String) : Observable<AppUpdateBean> {
        val paramsMap = HashMap<String,Any>()
        paramsMap["vci"] = vci

        return mService.appUpdate(IBaseHttpService.mapToFormRequestBody(paramsMap))
                .compose(ResponseTransformer.handleResult())
                .doOnError(NotApiThrowableConsumer());
    }



}