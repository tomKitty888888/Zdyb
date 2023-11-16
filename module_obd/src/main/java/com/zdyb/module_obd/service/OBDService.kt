package com.zdyb.module_obd.service

import com.zdeps.bean.AppUpdateBean
import com.zdeps.bean.LoginBean
import com.zdeps.bean.MotorcycleTypeBean
import com.zdeps.bean.VersionsAllBean
import com.zdyb.lib_common.http.response.Response
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface OBDService {


    /**
     * 获取登录验证码
     */
    @POST("/InspectAnnually/sendMsg")
    fun reginStart(@Body account: RequestBody) : Observable<Response<Any>>

    /**
     * 验证码登录
     */
    @POST("/InspectAnnually/login")
    fun reginLogin(@Body account: RequestBody) : Observable<Response<LoginBean>>


    /**
     * •	获取车型分类下所有车型最新版本接口
     */
    @GET("/InspectAnnually/getMotorcycleType")
    fun getMotorcycleType(@QueryMap paramsMap: HashMap<String,Any>) : Observable<Response<MutableList<MotorcycleTypeBean>>>

    /**
     * •	获取指定车型下所有版接口
     */
    @GET("/InspectAnnually/getVersionsAll")
    fun getVersionsAll(@Body account: RequestBody) : Observable<Response<MutableList<VersionsAllBean>>>


    /**
     * 获取APK最新版本接口
     */
    @POST("/InspectAnnually/appUpdate")
    fun appUpdate(@Body account: RequestBody) : Observable<Response<AppUpdateBean>>

    /**
     * 上传错误信息
     */
    @POST("/mobile/Appexmsg/upload_exmsg")
    fun loadErrLog(@Body account: RequestBody) : Observable<Response<Any>>

}