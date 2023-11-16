package com.zdeps.gui

import com.zdyb.lib_common.R
import com.zdyb.lib_common.base.BaseApplication

object ConnDevices {

    fun isConnect():Boolean{
        if (BaseApplication.usbConn == null){
            //showToast(context?.getString(R.string.no_usb_connect))
            println("usb未连接")
            return false
        }
        return true
    }

    fun sendData(data: ByteArray) :Int{
        if (BaseApplication.usbConn == null){
            //showToast(context?.getString(R.string.no_usb_connect))
            println("usb未连接")
            return 0
        }
        BaseApplication.usbConn?.apply { return sendData(data)}
        return 0
    }

    fun outTimeReadData(outTime:Long): ByteArray? {
        if (BaseApplication.usbConn == null){
            //showToast(context?.getString(R.string.no_usb_connect))
            println("usb未连接")
            return null
        }
        return BaseApplication.usbConn?.readData(outTime)
    }

    fun readData(retlen:Int): ByteArray? {
        if (BaseApplication.usbConn == null){
            //showToast(context?.getString(R.string.no_usb_connect))
            println("usb未连接")
            return null
        }
        return BaseApplication.usbConn?.readData(retlen)
    }

    fun purge(){
        if (BaseApplication.usbConn == null){
            //showToast(context?.getString(R.string.no_usb_connect))
            println("usb未连接")
            return
        }
        BaseApplication.usbConn?.purge()
    }
}