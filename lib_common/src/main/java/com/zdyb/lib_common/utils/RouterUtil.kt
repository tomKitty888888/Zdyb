package com.zdyb.lib_common.utils

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Nullable
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavigationCallback
import com.alibaba.android.arouter.launcher.ARouter
//import com.alibaba.fastjson.JSON
import java.io.Serializable

object RouterUtil {

    var postcard: Postcard? = null

    @JvmStatic
    fun inject(thiz: Any) {
        ARouter.getInstance().inject(thiz)
    }

    @JvmStatic
    fun build(path: String): RouterUtil {
        postcard = ARouter.getInstance().build(path)
        return this
    }

    fun with(bundle: Bundle?): RouterUtil {
        postcard?.with(bundle)
        return this
    }

    fun withFlags(flg: Int): RouterUtil {
        postcard?.withFlags(flg)
        return this
    }

    fun addFlags(flg: Int): RouterUtil {
        postcard?.addFlags(flg)
        return this
    }

    fun withString(@Nullable key: String, values: String): RouterUtil {
        postcard?.withString(key, values)
        return this
    }

    fun withLong(@Nullable key: String, values: Long): RouterUtil {
        postcard?.withLong(key, values)
        return this
    }

    fun withDouble(@Nullable key: String, values: Double): RouterUtil {
        postcard?.withDouble(key, values)
        return this
    }

    fun withInt(@Nullable key: String, values: Int): RouterUtil {
        postcard?.withInt(key, values)
        return this
    }

    fun withBoolean(@Nullable key: String, values: Boolean): RouterUtil {
        postcard?.withBoolean(key, values)
        return this
    }

    fun withSerializable(@Nullable key: String, @Nullable values: Serializable): RouterUtil {
        postcard?.withSerializable(key, values)
        return this
    }

    fun withParcelable(@Nullable key: String, @Nullable values: Parcelable): RouterUtil {
        postcard?.withParcelable(key, values)
        return this
    }

    fun withObject(@Nullable key: String, @Nullable values: Any): RouterUtil {
        postcard?.withObject(key, values)
        return this
    }

    fun greenChannel(): RouterUtil {
        postcard?.greenChannel()
        return this
    }

    fun launch(): Any? {
        return launch(null)
    }

    fun launch(context: Context?): Any? {
        return postcard?.navigation(context)
    }

    fun launch(callback: NavigationCallback): Any? {
        return postcard?.navigation(null, callback)
    }

    fun launch(context: Activity?, callback: NavigationCallback): Any? {
        return postcard?.navigation(context, callback)
    }

    fun launch(context: Activity?, requestCode: Int) {
        postcard?.navigation(context, requestCode)
    }

    @JvmStatic
    fun <T> load(service: Class<out T>): T? {
        return ARouter.getInstance().navigation(service)
    }

}