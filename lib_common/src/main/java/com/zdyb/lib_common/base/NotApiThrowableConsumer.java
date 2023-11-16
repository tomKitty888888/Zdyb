package com.zdyb.lib_common.base;

import android.text.TextUtils;


import com.zdyb.lib_common.R;
import com.zdyb.lib_common.utils.MyToastUtils;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;

import io.reactivex.functions.Consumer;
import retrofit2.HttpException;

public class NotApiThrowableConsumer implements Consumer<Throwable> {

    @Override
    public void accept(Throwable t) throws Exception {
        t.printStackTrace();
        if (t instanceof HttpException) {
            MyToastUtils.showDefaultToast(R.string.throw_server_abnormal);
        } else if (t instanceof NoRouteToHostException) {
            MyToastUtils.showDefaultToast(R.string.throw_network_unavailable);
        } else if (t instanceof SocketTimeoutException) {
            MyToastUtils.showDefaultToast(R.string.throw_request_time_out);
        } else if (t instanceof ConnectException) {
            MyToastUtils.showDefaultToast(R.string.throw_request_time_out);
        } else {
            if(!TextUtils.isEmpty(t.getMessage())) {
                MyToastUtils.showDefaultToast(t.getMessage());
            }
        }
    }
}
