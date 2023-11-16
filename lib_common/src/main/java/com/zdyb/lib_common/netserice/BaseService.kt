package com.zdyb.lib_common.netserice

import com.zdyb.lib_common.http.response.Response
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface BaseService {


    /**
     * 上传错误信息
     */
    @POST("/mobile/Appexmsg/upload_exmsg")
    fun loadErrLog(@Body account: RequestBody) : Observable<Response<Any>>

}