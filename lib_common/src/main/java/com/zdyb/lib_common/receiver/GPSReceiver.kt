package com.zdyb.lib_common.receiver;

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.core.content.ContextCompat.getSystemService


class GPSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        //val usbDevice: UsbDevice? = intent!!.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        if (intent != null) {
            when(intent.action){
                //LocationManager.MODE_CHANGED_ACTION,LocationManager.PROVIDERS_CHANGED_ACTION
                LocationManager.MODE_CHANGED_ACTION  ->{

                    val manager = context?.getSystemService(Service.LOCATION_SERVICE) as LocationManager?
                    manager?.let {
                        val gpsEnabled = it.isProviderEnabled(LocationManager.GPS_PROVIDER)
                        callBack?.gpsState(!gpsEnabled)
                    }

                }
            }
        }
    }

    private var callBack :CallBack? = null
    fun setListener(callBack :CallBack){
        this.callBack = callBack
    }

    public interface CallBack{
        fun gpsState(boolean: Boolean)
    }

}