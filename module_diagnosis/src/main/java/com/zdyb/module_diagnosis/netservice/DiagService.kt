package com.zdyb.module_diagnosis.netservice

import com.zdyb.lib_common.bean.NoValueBean
import com.zdyb.lib_common.http.response.Response
import com.zdyb.module_diagnosis.bean.*
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


    /**
     * 登录获取验证码
     */
    @POST("/Index/sendSms")
    fun sendSms(@Body account: RequestBody) : Observable<Response<NoValueBean>>

    /**
     * 登录
     */
    @POST("/Index/zdybValidate")
    fun zdybValidate(@Body account: RequestBody) : Observable<Response<LoginResultBean>>



    /**
     * 获取下位机升级信息
     */
    //@POST("/LowerMachine/datapacket") 旧接口数据格式不统一 改换新接口
    @POST("/LowerMachine/version")
    fun version(@Body account: RequestBody) : Observable<Response<VciUpdateInfoBean>>


    /**
     * 获取下位机蓝牙升级信息
     */
    @POST("/Winprocedure/bluetoothVsersion")
    fun bluetooth(@Body account: RequestBody) : Observable<Response<VciUpdateInfoBean>>


    /**
     * 获取智能诊断配置文件
     */
    @POST("/mobile/SelfResource/downResource")
    fun downResource(@Body account: RequestBody) : Observable<Response<VciUpdateInfoBean>>

}