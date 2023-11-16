package com.zdyb.lib_common.base

import com.zdyb.lib_common.http.PostFileBody
import com.zdyb.lib_common.http.PostFormBody
import com.zdyb.lib_common.http.PostJsonBody
import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

object IBaseHttpService {

    @JvmStatic
    fun mapToFormRequestBody(paramsMap: HashMap<String,  out Any?>): FormBody {
        return PostFormBody.create(paramsMap)
    }

    @JvmStatic
    fun mapToRequestBody(paramsMap: HashMap<String, out Any?>): RequestBody {
        return PostJsonBody.create(Gson().toJson(paramsMap))
    }

    @JvmStatic
    fun listToRequestBody(paramsList: List<Any>): RequestBody {
        return PostJsonBody.create(Gson().toJson(paramsList))
    }

    @JvmStatic
    fun mapToRequestBody(json:String): RequestBody {
        return PostJsonBody.create(json)
    }

    @JvmStatic
    fun mapToMulipartRequestBody(params: HashMap<String, Any>, file: File, fileKey:String ): MultipartBody {
        return PostFileBody.create(params,file,fileKey)
    }

}