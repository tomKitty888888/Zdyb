package com.zdyb.module_obd.activity

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.provider.Settings
import android.text.TextUtils
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.AppUtils
import com.jakewharton.rxbinding3.view.clicks
import com.qmuiteam.qmui.kotlin.onClick
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zdeps.bean.OBDBean
import com.zdyb.lib_common.base.*
import com.zdyb.lib_common.bluetooth.BluetoothManager
import com.zdyb.lib_common.bus.BusEvent
import com.zdyb.lib_common.bus.EventType
import com.zdyb.lib_common.receiver.BluetoothListenerReceiver
import com.zdyb.lib_common.receiver.GPSReceiver
import com.zdyb.lib_common.utils.PreferencesUtils
import com.zdyb.lib_common.utils.RouterUtil
import com.zdyb.lib_common.utils.SharePreferencesConstant
import com.zdyb.lib_common.utils.constant.RouteConstants
import com.zdyb.module_obd.R
import com.zdyb.module_obd.databinding.ActivityVehicleScanningBinding
import com.zdyb.module_obd.dialog.ConnectingDialog
import com.zdyb.module_obd.dialog.DialogBox
import com.zdyb.module_obd.dialog.DialogProgress
import com.zdyb.module_obd.dialog.ScanningDialog
import com.zdyb.module_obd.model.ObdFragmentModel
import io.reactivex.functions.Consumer
import java.util.concurrent.TimeUnit

@Route(path = RouteConstants.Obd.OBD_ACTIVITY_VEHICLE_SCANNING)
class VehicleScanningActivity:BaseActivity<ActivityVehicleScanningBinding, ObdFragmentModel>() {

    companion object{
        fun start(context: Context,absIsIntent: Boolean){
            context.startActivity(Intent(context,VehicleScanningActivity::class.java).putExtra("absIsIntent",absIsIntent))
        }
    }

    lateinit var rxPermissions: RxPermissions
    private lateinit var scanningDialog : ScanningDialog
    private lateinit var dialogProgress : DialogProgress
    private lateinit var connectingDialog : ConnectingDialog
    private lateinit var bleNotElectricityDialog : DialogBox

    var absIsIntent: Boolean = false

    override fun initViewModel(): ObdFragmentModel {
        return ObdFragmentModel()
    }

    override fun initParam() {
        super.initParam()
        absIsIntent = intent.getBooleanExtra("absIsIntent",false)
    }



    override fun initViewObservable() {
        super.initViewObservable()
        connectingDialog = ConnectingDialog().setBox(false)
        bleNotElectricityDialog = DialogBox().setBox(getString(R.string.obd_login_ble_not_electricity))
        rxPermissions = RxPermissions(this)
        if (!absIsIntent){
            BluetoothManager.init()
        }else{
            binding.bleState.setImageResource(R.mipmap.icon_ble_connect)
        }


        binding.testOBDFile.onClick {
            viewModel.loadSoFile(this)
        }

        binding.butSet.setChangeAlphaWhenPress(true)
        addDisposable(binding.butSet.clicks().throttleFirst(1, TimeUnit.SECONDS).subscribe({
            //跳转设置页面 查看信息
            startActivity(Intent(this,OBDSetActivity::class.java))
        },{it.printStackTrace()}))

        binding.butUPObd.setChangeAlphaWhenPress(true)
        addDisposable(binding.butUPObd.clicks().throttleFirst(1, TimeUnit.SECONDS).subscribe({
            dialogProgress = DialogProgress().setBox(getString(R.string.obd_upgrade_hint),false)
            dialogProgress.show(supportFragmentManager,"DialogProgress")
            viewModel.loadObdFile(Consumer {
                dialogProgress.setProgress(it)
                if (it >= 120){
                    dialogProgress.dismiss()
                    viewModel.showToast(getString(R.string.obd_upgrade_ok_hint))
                }
            })
        },{it.printStackTrace()}))

        binding.scan.setChangeAlphaWhenPress(true)
        addDisposable(binding.scan.clicks().throttleFirst(1, TimeUnit.SECONDS).subscribe({
            //判断定位是否打开
            if (!getGpsStatus(this)){
                DialogBox().setBox(getString(R.string.obd_login_ble_location_IsOpen))
                    .setResult(
                        Consumer {
                            if (it){goToOpenGps(this)}
                        }).show(supportFragmentManager,"DialogBox")
                return@subscribe
            }

            viewModel.openPermissions(Consumer {
                if (it){
                    //检查 蓝牙连接 判断也会调用开启蓝牙
                    if (!BluetoothManager.isBluetoothOpened()){
                        //打开蓝牙
                        //BluetoothManager.openBluetooth()
                        return@Consumer
                    }
                    //检查蓝牙是否连接上
                    if(!BluetoothManager.isConnected()){
                        //尝试连接
                        connect()
                        return@Consumer
                    }

                    scanningDialog = ScanningDialog().setBox(false)
                    scanningDialog.show(supportFragmentManager,"scanningDialog")

                    if (absIsIntent){
                        viewModel.obdInfoList(3)
                    }else{
                        if (viewModel.isInitOBD){  //已经初始化
                            viewModel.obdInfoList(3)
                        }else{ //未初始化
                            if (viewModel.initOBD()){
                                viewModel.obdInfoList(1)
                            }else{
                                scanningDialog.dismiss()
                            }
                        }
                    }
                }else{
                    viewModel.showToast("请允许权限后重试")
                }
            })
        },{it.printStackTrace()}))

        binding.butFinish.setChangeAlphaWhenPress(true)
        addDisposable(binding.butFinish.clicks().throttleFirst(1, TimeUnit.SECONDS).subscribe({
              val dialogBox = DialogBox().setBox(getString(R.string.outApp_hint),true).setResult(Consumer {
                  if (it){
                      AppManager.getAppManager().finishAllActivity()
                  }
              })
            dialogBox.show(supportFragmentManager,"finishDialogBox")
        },{it.printStackTrace()}))

        binding.tvVci.text = viewModel.getVCI()

        viewModel.obdInfo.observe(this, Observer {
            val intent = Intent(this,OBDResultActivity::class.java)
            startActivity(intent)
            scanningDialog.dismiss()
        })
        viewModel.emissionInfo.observe(this, Observer {
            //数据准备就绪 跳转显示页面
            val intent = Intent(this,OBDResultActivity::class.java)
            startActivity(intent)
            scanningDialog.dismiss()
        })

        viewModel.upApp(supportFragmentManager, Consumer {
            //检测不需要升级才 正常进行obd扫描
            if (!it){
                belListener() //蓝牙监听
                BluetoothManager.setListener(Consumer { state ->
                    when(state){
                        BluetoothManager.BLEState.CONNECT -> {
                            binding.bleState.setImageResource(R.mipmap.icon_ble_connect)
                            connectingDialog.dismiss()
                            scanningDialog = ScanningDialog().setBox(false)
                            scanningDialog.show(supportFragmentManager,"scanningDialog")
//                            if (viewModel.initOBD()){
//                                viewModel.obdInfoList(1)
//                            }
                            if (absIsIntent){
                                viewModel.obdInfoList(3)
                            }else{
                                if (viewModel.isInitOBD){  //已经初始化
                                    viewModel.obdInfoList(3)
                                }else{ //未初始化
                                    if (viewModel.initOBD()){
                                        viewModel.obdInfoList(1)
                                    }else{
                                        scanningDialog.dismiss()
                                    }
                                }
                            }
                        }
                        BluetoothManager.BLEState.CONNECT_ERROR -> {
                            binding.bleState.setImageResource(R.mipmap.icon_ble_no_connect)
                        }
                        BluetoothManager.BLEState.DISCONNECT -> {
                            connectingDialog.dismiss()
                            //viewModel.showToast(getString(R.string.obd_login_ble_not_electricity))
                            if (!bleNotElectricityDialog.isVisible){
                                bleNotElectricityDialog.show(supportFragmentManager,"msgDialog")
                            }
                            binding.bleState.setImageResource(R.mipmap.icon_ble_no_connect)
                        }
                        BluetoothManager.BLEState.INIT -> {
                            if(!absIsIntent){
                                //蓝牙没有进行连接
                                //主动去连接蓝牙
                                if (BluetoothManager.isBluetoothOpened()){
                                    connect()
                                }

                            }
                        }
                        else -> {}
                    }
                })
            }
        })



    }

    fun err(bean:OBDBean){
        if (scanningDialog.isVisible){
            scanningDialog.dismiss()
        }
        //提示进行重试
        if (bean.code == "5"){
            //
            //车辆熄火 10S 后请重新启动发动机后点击确定按钮
        }
    }

    override fun registerRxBus(): Boolean {
        return true
    }


    override fun eventComing(t: BusEvent?) {
        super.eventComing(t)
        t?.let {
            when(t.what){
                EventType.CMD_ERR ->{
                    val bean = t.data as OBDBean
                    if (scanningDialog.isVisible){
                        scanningDialog.dismiss()
                    }
                    //提示进行重试
                    if (bean.code == "5"){
                        //车辆熄火 10S 后请重新启动发动机后点击确定按钮
                        DialogBox().setBox(getString(R.string.obd_comm_error_5)).show(supportFragmentManager,"errHintDialog")
                    }else{
                        DialogBox().setBox(getString(R.string.obd_comm_error_retry)).show(supportFragmentManager,"errHintDialog")
                    }
                }
                EventType.UP_OBD_ERR ->{
                    dialogProgress.dismiss()
                }
                EventType.OBD_BLE_SOCKET_CLOSED_ERR ->{
                    if (scanningDialog.isVisible){
                        scanningDialog.dismiss()
                    }
                    //viewModel.showToast(getString(R.string.obd_login_ble_not_electricity_retry))
                    val dialog = DialogBox().setBox(getString(R.string.obd_login_ble_not_electricity_retry)).show(supportFragmentManager,"msgDialog")
                }

            }
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


    private var gpsReceiver : GPSReceiver?= null
    private var bleReceiver: BluetoothListenerReceiver? = null
    private fun belListener() {
        bleReceiver = BluetoothListenerReceiver(Consumer {
            if (it == 2) {
                //开启
                connect()
            } else if (it == 4) {
                //关闭
                binding.bleState.setImageResource(R.mipmap.icon_ble_no_connect)
            }
        })
        this.registerReceiver(bleReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }
    private fun connect(){
        try {
            // 连接上一次登录 缓存的蓝牙
            // 正在连接
            val address =  PreferencesUtils.getString(this, SharePreferencesConstant.ADAPTED_BLE_DEVICES,"")
            if (TextUtils.isEmpty(address)){
                KLog.e("蓝牙设备信息为空，需要重新登录进行确认蓝牙设备")
                PreferencesUtils.putString(BaseApplication.getInstance(), SharePreferencesConstant.USER_TOKEN,"");
                PreferencesUtils.putString(BaseApplication.getInstance(), SharePreferencesConstant.VCI_CODE,"");
                RouterUtil.build(RouteConstants.Obd.OBD_ACTIVITY_LOGIN).launch();
                AppManager.getAppManager().finishAllOBDLoginActivity();
                return
            }
            connectingDialog.show(supportFragmentManager,"ConnectingDialog")
            BluetoothManager.connect(address)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        bleReceiver?.let { unregisterReceiver(it) }
        super.onDestroy()
    }
}