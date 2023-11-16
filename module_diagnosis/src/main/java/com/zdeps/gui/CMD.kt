package com.zdeps.gui

object CMD {


    const val PARAM_GUI = 0x10000001L //开启or关闭
    const val PARAM_COM = 0x10000002L //执行数据流


    const val PARAM_GUI_TASK_ID_BEGIN = 0xFFFF0001L ///定义任务ID是否已经启动(发给VDI用来判断执行是menu.txt的菜单还是由诊断程序建立的菜单);
    const val PARAM_GUI_TASK_ID_END : Long = 0xFFFF0002L //消息来自GUI.LIB发过来的 获取VDI程序的根目录(同步);


    const val FORM_GUI_OPEN: Byte = 0x10  //GUI开启
    const val FORM_GUI_CLOSE: Byte = 0x11 //GUI关闭

    const val FORM_QUIT: Byte = 0x70 //退出诊断程序
    const val FORM_COMMAND: Byte = 0x20 //与下位机进行通信，2020.03.06添加
    const val FORM_REFALSE_DATA_DOWNLOAD: Byte = 0x31 //刷写数据在线下载相关

    const val ID_MENU_BACK: Long = 0xFF //所有菜单返回均指定为该ID;


    /**
     * 偏移量
     */

    const val ID_CDS_BACK_OFFSET: Int = 0xFFFF -1 //数据流返回偏移量
    const val ID_ACT_BACK_OFFSET: Int = 0xFFFF - 4 //动作测试偏移量
    const val ID_DIALOG_OFFSET: Int = 3 //弹窗的按键 确认与取消按钮， 有input文本框输入值的情况下是先传递值，然后在调用确认；
    const val ID_DIALOG_VALUE_OFFSET: Int = 6 //传递值的偏移量
    const val ID_DIALOG_VALUE_FILE_NAME: Int = 2000 //传递值的偏移量

    /**
     * EX菜单
     */
    const val FORM_MENU_EX: Byte = 0x0F

    /**
     * 普通菜单
     */
    const val FORM_MENU: Byte = 0x01

    /**
     * DTC
     */
    const val FORM_DTC: Byte = 0x02


    /**
     * 数据流查询
     */
    const val FORM_CDS_SELECT: Byte = 0x03

    /**
     * CDS数据流
     */
    const val FORM_CDS: Byte = 0x04

    /**
     * VER
     */
    const val FORM_VER: Byte = 0x05
    /**
     * ACT
     */
    const val FORM_ACT: Byte = 0x06
    /**
     * dialog List显示版本信息
     */
    const val FORM_LIST: Byte = 0x0D
    /**
     * 2021.10.22诊断系统设置波特率
     */
    const val FORM_BAUD_RATE_CHANGE: Byte = 0x32
    /**
     * DTC多重
     */
    const val FORM_DTC_MULTI: Byte = 0x33
    /**
     * 可控制菜单
     */
    const val FORM_MENU_CTRL: Byte = 0x34


    const val FORM_DATA_INIT: Byte = 0x01 //初始
    const val FORM_DATA_ADD: Byte = 0x02  //添加
    const val FORM_DATA_SHOW: Byte = 0x03 //显示

    const val FORM_DATA_ADD_DTC_ONE: Byte = 0x12 //DTC添加数据的方式1 与正常0x02添加不冲突

    const val FORM_CDS_SELECT_GET_ITEM: Byte = 0x04 //CDS查询读取数据流时回调到java拿需要读取的数据流菜单 此处菜单位置从1起始
    const val FORM_DATA_ADD_CDS_ONE: Byte = 0x04 //CDS添加数据方式2
    const val FORM_ACT_ADD_BUTTON: Byte = 0x04 //act 动作测试添加按钮
    const val FORM_ACT_ADD_PROMPT: Byte = 0x05 //act 动作测试添加提示
    /**
     * 主动发送指令
     *
     * 查看数据流
     */
    const val ID_CDS_VIEW: Long = 0xFD //数据流窗口查看的时候指定的ID


    //弹窗
    /**
     * 该消息可以指定三种窗口(一个确定按钮，是否按钮，无按钮)
     */
    const val FORM_MSG: Byte = 0x40

    /**
     * 输入框窗口消息
     */
    const val FORM_INPUT: Byte = 0x41
    const val FORM_TITLE: Byte = 0x42

    /**
     * 打开或保存窗口
     */
    const val FORM_FILEDIALOG: Byte = 0x60


    //消息框的三种模式(同步VDI)  bMode
    const val MSG_MB_OK: Byte = 0x01 //只有一个[确定]按钮的消息框
    const val MSG_MB_YESNO: Byte = 0x02 //有[确定]和[取消]两个按钮的消息框
    const val MSG_MB_IMAGE: Byte = 0x12 //带图片的消息框
    const val MSG_MB_NOBUTTON: Byte = 0x03 //没有按钮的消息框(用来提示正在操作中，请稍候...之类的窗口)
    const val MSG_MB_ERROR: Byte = -1  //进入系统失败

    //MSG_MB_YESNO这种模式下返回确定还是取消(同步VDI) --inputBox窗口共用此参数返回值
    const val MB_YES: Byte = 0x7E
    const val MB_NO: Byte = 0x7F //也标识 一个取消按钮的弹窗标识

    //带输入框的dialog
    const val INPUT_MODE_DEC: Byte = 0x01 //限制只能输入10进制数字;
    const val INPUT_MODE_HEX: Byte = 0x02 //限制只能输入16进制数字;
    const val INPUT_MODE_VIN: Byte = 0x03 //限制只能输入作为汽车VIN使用的字符和数字;
    const val INPUT_MODE_ALL: Byte = 0x10 //无限制
    const val INPUT_VALUE_END: Char = '\u0000'


    /**
     * 消息窗口关闭
     */
    const val MSG_WAIT_WINDOW_CLOSE: Long = 0x10000002
}