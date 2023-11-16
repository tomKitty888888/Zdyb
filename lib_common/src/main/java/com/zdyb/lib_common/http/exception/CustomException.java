package com.zdyb.lib_common.http.exception;

import android.net.ParseException;

import com.blankj.utilcode.util.NetworkUtils;
import com.google.gson.JsonParseException;
import com.zdyb.lib_common.R;
import com.zdyb.lib_common.base.BaseApplication;
import com.zdyb.lib_common.utils.MyToastUtils;

import org.json.JSONException;

import java.io.EOFException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class CustomException {

    public static final int SUCCESS = 1;


    /**
     * 请先登录后再请求数据
     */
    public static final int NO_LOGIN = -1;

    /**
     * 无操作权限
     */
    public static final int NO_Permissions = -2;

    /**
     * 查询不到用户信息
     */
    public static final int NO_USER_INFO = -7;

//
//    /**
//     * 设备未加网
//     */
//    public static final String NO_ADD_NET = "10";



    /**
     * 未知错误
     */
    public static final String UNKNOWN = "1000";

    /**
     * 解析错误
     */
    public static final String PARSE_ERROR = "1001";

    /**
     * 网络错误
     */
    public static final String NETWORK_ERROR = "1002";

    /**
     * 协议错误
     */
    public static final String HTTP_ERROR = "1003";

    public static ApiException handleException(Throwable e) {
        ApiException ex;
        if (e instanceof JsonParseException
                || e instanceof JSONException
                || e instanceof ParseException) {
            //解析错误
            ex = new ApiException(PARSE_ERROR, BaseApplication.getInstance().getString(R.string.exception_parse_failed));
            e.printStackTrace();
            ex.printStackTrace();
            MyToastUtils.showDefaultToast(BaseApplication.getInstance().getString(R.string.exception_parse_failed));
            return ex;
        } else if (e instanceof ConnectException) {
            //网络错误
            ex = new ApiException(NETWORK_ERROR, e.getMessage());
            MyToastUtils.showDefaultToast(BaseApplication.getInstance().getString(R.string.exception_network_error));
            return ex;
        } else if (e instanceof UnknownHostException) {
            //连接错误
            ex = new ApiException(NETWORK_ERROR, e.getMessage());
            MyToastUtils.showDefaultToast(BaseApplication.getInstance().getString(R.string.exception_connect_error));
            return ex;
        } else if (e instanceof SocketTimeoutException) {
            //请求超时
            ex = new ApiException(NETWORK_ERROR, e.getMessage());
            MyToastUtils.showDefaultToast(BaseApplication.getInstance().getString(R.string.exception_connect_timeout));
            return ex;
        } else if (e instanceof EOFException) {
            ex = new ApiException(NETWORK_ERROR, e.getMessage());
            MyToastUtils.showDefaultToast("body null");
            return ex;
        }
        else {
            //无网络 提示连接网络 (OKHttp走缓存没有缓存时的提示)
            if(!NetworkUtils.isConnected()) {
                ex = new ApiException(UNKNOWN, e.getMessage());
                MyToastUtils.showDefaultToast(BaseApplication.getInstance().getString(R.string.exception_please_connect));
                return ex;
            }
            //未知错误
            ex = new ApiException(UNKNOWN, e.getMessage());
            MyToastUtils.showDefaultToast(BaseApplication.getInstance().getString(R.string.exception_unknow_error));
            return ex;
        }
    }
}
