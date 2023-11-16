package com.zdyb.lib_common.receiver;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

class USBReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        //val usbDevice: UsbDevice? = intent!!.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        if (intent != null) {
            when(intent.action){
                UsbManager.ACTION_USB_DEVICE_ATTACHED ->{
                    // 插入USB设备
                    callBack?.usbState(true)
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED ->{
                    // 拔出USB设备
                    callBack?.usbState(false)
                }
            }
        }
    }

    private var callBack :CallBack? = null
    fun setListener(callBack :CallBack){
        this.callBack = callBack
    }

    public interface CallBack{
        fun usbState(boolean: Boolean)
    }

}