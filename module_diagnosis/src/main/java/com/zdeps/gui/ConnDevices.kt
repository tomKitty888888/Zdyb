package com.zdeps.gui

import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.bluetooth.BluetoothManagerNew

object ConnDevices {

    fun isConnect():Boolean{
        if (BaseApplication.connectType == BaseApplication.ConnectType.no){
            return false
        }
        return true
    }


    fun isUSBConnect():Boolean{
        if (BaseApplication.usbConn == null){
            //showToast(context?.getString(R.string.no_usb_connect))
            println("usb未连接")
            return false
        }

        if (BaseApplication.usbConn != null && BaseApplication.connectType == BaseApplication.ConnectType.usb){
            return true
        }

        return false
    }

    fun sendData(data: ByteArray) :Int{
        when(BaseApplication.connectType){
            BaseApplication.ConnectType.usb ->{
                if (BaseApplication.usbConn == null){
                    println("usb服务未启动")
                    return 0
                }
                BaseApplication.usbConn?.apply { return sendData(data)}
            }
            BaseApplication.ConnectType.ble ->{
                return BluetoothManagerNew.send(data)
            }else ->{
                println("设备未连接")
            }
        }
        return 0
    }

    fun timedReadsData(outTime:Long): ByteArray? {

        when(BaseApplication.connectType){
            BaseApplication.ConnectType.usb ->{
                if (BaseApplication.usbConn == null){
                    println("usb服务未启动")
                    return ByteArray(0)
                }
                return BaseApplication.usbConn?.timedReadsFrameData(outTime)  //  timedReadsData
            }
            BaseApplication.ConnectType.ble ->{
                return BluetoothManagerNew.timedReadsData(outTime)
            }else ->{
                println("设备未连接")
            }
        }
        return ByteArray(0)
    }

    fun readData(retlen:Int): ByteArray? {

        when(BaseApplication.connectType){
            BaseApplication.ConnectType.usb ->{
                if (BaseApplication.usbConn == null){
                    println("usb服务未启动")
                    return null
                }
                return BaseApplication.usbConn?.readData(retlen)
            }
            BaseApplication.ConnectType.ble ->{
                return BluetoothManagerNew.readData(retlen)
            }else ->{
                println("设备未连接")
            }
        }
        return null
    }

    //升级过程才使用
    fun readFrameData(item:Long): ByteArray? {
        if (BaseApplication.usbConn == null){
            //showToast(context?.getString(R.string.no_usb_connect))
            println("usb未连接")
            return null
        }
        return BaseApplication.usbConn?.readFrameData(item)
    }

    //升级过程才使用
    fun timedReadsDataLength(item:Long,length:Int): ByteArray {
        if (BaseApplication.usbConn == null){
            //showToast(context?.getString(R.string.no_usb_connect))
            println("usb未连接")
            return ByteArray(0)
        }

        return BaseApplication.usbConn?.timedReadsDataLength(item,length) ?: return ByteArray(0)
    }


    fun test() {
        if (BaseApplication.usbConn == null){
            //showToast(context?.getString(R.string.no_usb_connect))
            println("usb未连接")
            return
        }
        BaseApplication.usbConn?.test()
    }

    fun purge(){

        when(BaseApplication.connectType){
            BaseApplication.ConnectType.usb ->{
                if (BaseApplication.usbConn == null){
                    println("usb服务未启动")
                    return
                }
                BaseApplication.usbConn?.purge()
            }
            BaseApplication.ConnectType.ble ->{
                return BluetoothManagerNew.purgeData()
            }else ->{
                println("设备未连接")
            }
        }
    }

    fun setRts(boolean: Boolean){
        if (BaseApplication.usbConn == null){
            //showToast(context?.getString(R.string.no_usb_connect))
            println("usb未连接")
            return
        }
        BaseApplication.usbConn?.setRts(boolean)
    }

    /**
     * 设置波特率
     */
    fun setUsbBaudRate(baudRate :Int):Boolean{
        if (BaseApplication.usbConn == null){
            println("usb未连接")
            return false
        }
        if (BaseApplication.usbConn?.setBaudRate(baudRate) == true){
            return true
        }
        return false
    }

    fun closeSerialPort(){
        if (BaseApplication.usbConn == null){
            println("usb未连接")
            return
        }
        BaseApplication.usbConn?.closeSerialPort()
    }

    fun openSerialPort(){
        if (BaseApplication.usbConn == null){
            println("usb未连接")
            return
        }
        BaseApplication.usbConn?.initSerial()
    }

}