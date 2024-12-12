package com.zdyb.lib_common.bus;

public class EventTypeDiagnosis {


    /**
     * 文件选择
     */
    public final static int CMD_SELECT_FILE = 301;

    /**
     * 串口连接
     */
    public final static int PORT_CONNECT = 302;
    public final static int PORT_OUT = 303;


    /**
     * 蓝牙连接与断开
     */
    public final static int BLE_CONNECT = 304;
    public final static int BlE_OUT = 305;

    /**
     *  usb串口没插，去连接蓝牙
     */
    public final static int CONNECT_BLE = 306;





}
