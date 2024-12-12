package com.zdyb.module_diagnosis.model

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.ble.Bluetooth
import com.ble.DeviceCallback
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zdeps.gui.ConnDevices
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.bluetooth.BluetoothManagerNew
import com.zdyb.lib_common.bus.BusEvent
import com.zdyb.lib_common.bus.EventTypeDiagnosis
import com.zdyb.lib_common.bus.RxBus
import com.zdyb.lib_common.utils.PreferencesUtils
import com.zdyb.lib_common.utils.SharePreferencesDiagnosis
import com.zdyb.module_diagnosis.R
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import java.util.ArrayList

class DiagnosisActivityModel() : BaseViewModel() {


    val REQUEST_ENABLE_BT = 1
    lateinit var rxPermissions: RxPermissions
    var isShowList = false //是否显示ble的列表进行选择，或者是直接连接上一次的

    fun init(context : Context):DiagnosisActivityModel{
        super.context = context
        onCreate()
        return this
    }

    override fun onCreate() {
        super.onCreate()
        rxPermissions = RxPermissions(context as FragmentActivity)

        registerUsbReceiver()
    }

    /**
     * 程序打开后，usb如果没插上，主动连接上一次连接过的蓝牙
     */
    fun oneRunConnect(){
        isShowList = false
        showBleList()
    }

    fun showBleList(){
        if (!getGpsStatus(context!!)){
            val normalDialog = AlertDialog.Builder(context!!)
            normalDialog.setMessage(context!!.getString(R.string.ble_location_IsOpen))
            normalDialog.setPositiveButton(context!!.getString(R.string.confirm)) { dialog, which ->
                goToOpenGps(context!!)
                dialog.dismiss()
            }
            normalDialog.setPositiveButton(context!!.getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            normalDialog.show()
            return
        }

        openPermissions(Consumer {
            if (it){
                //蓝牙已打开，获取已适配过的蓝牙列表
                getConnectBleList()
            }else{
                showToast(context!!.getString(R.string.permissions_hint))
            }
        })
    }



    private fun openPermissions(consumer: Consumer<Boolean>){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            addDisposable(rxPermissions.request(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ).subscribe {
                openBle()
                consumer.accept(it)
            })
        }else{
            addDisposable(rxPermissions.request(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ).subscribe {
                openBle()
                consumer.accept(it)
            })
        }
    }

    private fun openBle(){
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            showToast("Device doesn't support Bluetooth")
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

    }


    /**
     * 判断定位是否打开
     */
    fun getGpsStatus(ctx: Context):Boolean{
        //从系统服务中获取定位管理器
        val lm =  ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * 跳转打开定位的界面
     */
    fun goToOpenGps(ctx: Context){
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        ctx.startActivity(intent)
    }


    private fun getConnectBleList(){
        if (ConnDevices.isUSBConnect()){
            return
        }

        BluetoothManagerNew.init().setListener(Consumer {
            when(it){
                BluetoothManagerNew.BLEState.INIT -> {
                    println("初始化完成")
                    //拿已经连接的设备列表
                    BluetoothManagerNew.search(Consumer {

                        //通过蓝牙名称中携带的类型过滤一遍设备，69是1018和pro的板子
                        val data = mutableListOf<BluetoothDevice>()
                        val nameList = arrayListOf<String>()
                        for (item in it){
                            if (ActivityCompat.checkSelfPermission(
                                    context!!,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return@Consumer
                            }
                            println("打印已适配的蓝牙设备=${item.name}")

                            if (item.name.length >= 12){
                                val type = item.name.substring(6,8)
                                if (type == "69"){
                                    data.add(item)
                                    nameList.add(item.name)
                                }
                            }
                        }

                        if (isShowList){
                            //过滤完毕，显示设备列表
                            val listDialog = AlertDialog.Builder(context!!)
                            listDialog.setTitle(context!!.getString(R.string.ble_device_list))
                            listDialog.setItems(nameList.toTypedArray()) { dialog, which ->
                                println("点击了--->${data[which].name}")
                                //连接蓝牙
                                BluetoothManagerNew.connect(data[which].address)
                            }
                            listDialog.show()
                        }else{
                            //直接连接上一次的 用VIC进行匹配
                            val sn = PreferencesUtils.getString(BaseApplication.getInstance(), SharePreferencesDiagnosis.DEVICE_SN)
                            for (item in data){
                                if (item.name == sn){
                                    //连接蓝牙
                                    BluetoothManagerNew.connect(item.address)
                                    break
                                }
                            }
                        }

                    })
                }
                BluetoothManagerNew.BLEState.CONNECT -> {
                    println("设备连接")
                    BaseApplication.connectType = BaseApplication.ConnectType.ble
                    RxBus.getDefault().post(BusEvent(EventTypeDiagnosis.BLE_CONNECT,R.mipmap.icon_d_ble_connected))
                }
                BluetoothManagerNew.BLEState.DISCONNECT -> {
                    println("设备断开")
                    BaseApplication.connectType = BaseApplication.ConnectType.no
                    RxBus.getDefault().post(BusEvent(EventTypeDiagnosis.BlE_OUT,R.mipmap.icon_d_ble_disconnect))
                }
                else -> {}
            }
        })
    }


    private fun registerUsbReceiver(){
        val intentFilter = IntentFilter()
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)//插入
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)//拔出
        context!!.registerReceiver(mUsbReceiver,intentFilter)
    }

    private val mUsbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            intent?.let {
                val action = intent.action
                if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action){

                }else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action){
                    BaseApplication.connectType = BaseApplication.ConnectType.no
                    RxBus.getDefault().post(BusEvent(EventTypeDiagnosis.PORT_OUT))
                }
            }
        }

    }



    override fun onDestroy() {
        super.onDestroy()
        context!!.unregisterReceiver(mUsbReceiver)
    }
}