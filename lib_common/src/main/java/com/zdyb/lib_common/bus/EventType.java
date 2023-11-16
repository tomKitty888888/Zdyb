package com.zdyb.lib_common.bus;

public class EventType {


    /**
     * 命令返回错误
     */
    public final static int CMD_ERR = 201;

    /**
     * 升级obd文件过程中发生异常 关闭升级的窗口
     */
    public final static int UP_OBD_ERR = 202;

    /**
     * 蓝牙连接故障 socket closed 错误的情况，设备未通电 进行提示
     */
    public final static int OBD_BLE_SOCKET_CLOSED_ERR = 203;
}
