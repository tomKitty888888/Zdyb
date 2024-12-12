package com.zdyb.lib_common.netserice

import com.zdyb.lib_common.base.IBaseHttpService
import com.zdyb.lib_common.base.NotApiThrowableConsumer
import com.zdyb.lib_common.http.NetWorkManager
import com.zdyb.lib_common.http.response.ResponseTransformer
import io.reactivex.Observable

object BasicsInteractor {

    private val mService = NetWorkManager.getInstance().getService(BaseService::class.java)

    /**
     * 	上传错误信息
     */
    fun loadErrLog(title:String,msg:String,vci:String) : Observable<Any> {
        val paramsMap = HashMap<String,Any>()
        paramsMap["title"] = title
        paramsMap["msg"] = msg
        paramsMap["vci"] = vci

        return mService.loadErrLog(IBaseHttpService.mapToFormRequestBody(paramsMap))
            .compose(ResponseTransformer.handleResult())
            .doOnError(NotApiThrowableConsumer());
    }
}