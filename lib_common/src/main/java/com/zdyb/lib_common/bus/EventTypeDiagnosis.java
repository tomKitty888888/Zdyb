package com.zdyb.lib_common.bus;

public class EventTypeDiagnosis {


    /**
     * 文件选择
     */
    public final static int CMD_SELECT_FILE = 301;


    /**
     * USB插入后 发送获取的路径
     */
    public final static int USB_PATH = 302;

    /**
     * USB拔出
     */
    public final static int USB_OUT = 303;

    /**
     * 串口链接
     */
    public final static int PORT_CONNECT = 304;

    /**
     * 串口断开
     */
    public final static int PORT_OUT = 305;

    /**
     * 串口断开--息屏时间过长引起的
     */
    public final static int PORT_IS_NULL = 306;

}
