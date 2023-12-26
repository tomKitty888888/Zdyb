package com.zdyb.module_diagnosis.netservice

import com.zdyb.lib_common.http.response.Response
import com.zdyb.module_diagnosis.bean.MotorcycleTypeEntity
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface DiagService {


    /**
     * 上传错误信息
     */
    @POST("/Upgradenew/motorcycle_type")
    fun motorcycleType(@Body account: RequestBody) : Observable<Response<MutableList<MotorcycleTypeEntity>>>

}