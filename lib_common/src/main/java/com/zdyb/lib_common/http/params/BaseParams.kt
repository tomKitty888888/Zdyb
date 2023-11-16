package com.example.lib_common.http.params

import com.blankj.utilcode.util.TimeUtils
import java.util.*

/**
 * 公共参数
 */
object BaseParams {

    fun params(data: HashMap<String, out Any>): HashMap<String, Any> {
        val map: HashMap<String, Any> = HashMap()
//        map["messageId"] = TimeUtils.getNowMills()
//        map["payLoadVersion"] = 1
        map["data"] = data
        return map
    }
}