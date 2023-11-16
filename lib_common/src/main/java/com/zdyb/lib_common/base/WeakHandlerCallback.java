package com.zdyb.lib_common.base;

import android.os.Message;

/**
 * 弱引用handler消息回调
 */
public interface WeakHandlerCallback {
    /**
     * 消息队列回调
     * @param msg
     * @return
     */
    boolean handleMessage(Message msg);
}
