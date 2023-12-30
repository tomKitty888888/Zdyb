package com.zdyb.module_diagnosis.netservice

import com.zdyb.lib_common.http.response.Response
import com.zdyb.module_diagnosis.bean.ItemVersionEntity
import com.zdyb.module_diagnosis.bean.MotorcycleTypeEntity
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface DiagService {


    /**
     * 根据类型 获取品牌的诊断文件进行下载
     */
    @POST("/Upgradenew/motorcycle_type")
    fun motorcycleType(@Body account: RequestBody) : Observable<Response<MutableList<MotorcycleTypeEntity>>>

    /**
     * 根据类型 获取品牌的全部版本列表
     */
    @POST("/Upgradenew/versions_all")
    fun versionsAll(@Body account: RequestBody) : Observable<Response<MutableList<ItemVersionEntity>>>
}