package com.zdyb.module_obd.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.RegexUtils
import com.zdyb.lib_common.utils.RxTimerUtil
import com.jakewharton.rxbinding3.view.clicks
import com.qmuiteam.qmui.kotlin.onClick
import com.trello.rxlifecycle3.android.ActivityEvent
import com.zdyb.lib_common.base.BaseActivity
import com.zdyb.lib_common.bluetooth.BluetoothManager
import com.zdyb.lib_common.http.NetUrl
import com.zdyb.lib_common.receiver.BluetoothListenerReceiver
import com.zdyb.lib_common.receiver.GPSReceiver
import com.zdyb.lib_common.utils.PreferencesUtils
import com.zdyb.lib_common.utils.SharePreferencesConstant
import com.zdyb.lib_common.utils.constant.RouteConstants
import com.zdyb.module_obd.R
import com.zdyb.module_obd.databinding.FragmentObdLoginBinding
import com.zdyb.module_obd.dialog.DialogBox
import com.zdyb.module_obd.model.ObdFragmentModel
import com.zdyb.module_obd.service.OBDInteractor
import io.reactivex.functions.Consumer
import java.util.concurrent.TimeUnit

@Route(path = RouteConstants.Obd.OBD_ACTIVITY_LOGIN)
class OBDLoginActivity: BaseActivity<FragmentObdLoginBinding, ObdFragmentModel>() {


    private var connectAddress = ""

    override fun initViewModel(): ObdFragmentModel {
        return ObdFragmentModel()
    }


    override fun initViewObservable() {
        super.initViewObservable()
        //PreferencesUtils.putString(this, SharePreferencesConstant.VCI_CODE,"20230379019T")
        //检查token是否登录过 vci码同样不允许为空
        val userToken = PreferencesUtils.getString(this,SharePreferencesConstant.USER_TOKEN)
        val vciCode = PreferencesUtils.getString(this,SharePreferencesConstant.VCI_CODE)

        if (!TextUtils.isEmpty(userToken) && !TextUtils.isEmpty(vciCode)){
            NetUrl.TOKEN = userToken
            VehicleScanningActivity.start(this,false)
            finish()
            return
        }

        BluetoothManager.init()

        addDisposable(binding.bleConnect.clicks()
            .compose(this.bindUntilEvent(ActivityEvent.DESTROY))
            .throttleFirst(1, TimeUnit.SECONDS).subscribe({
                if (!getGpsStatus(this)){
                    DialogBox().setBox(getString(R.string.obd_login_ble_location_IsOpen))
                        .setResult(
                            Consumer {
                                if (it){goToOpenGps(this)}
                            }).show(supportFragmentManager,"DialogBox")
                    //viewModel.showToast(getString(R.string.obd_login_ble_location_IsOpen))
                    return@subscribe
                }

                //连接蓝牙 获取vic码
                viewModel.openPermissions(Consumer {
                    if (it){
//                        val blueadapter = BluetoothAdapter.getDefaultAdapter()
//                        if (ActivityCompat.checkSelfPermission(
//                                this,
//                                Manifest.permission.BLUETOOTH_CONNECT
//                            ) != PackageManager.PERMISSION_GRANTED
//                        ) {
//
//                            println("蓝牙已打开，")
//                            return@Consumer
//                        }
                        //if (blueadapter.enable()) { //蓝牙已打开
                            //connect()
                        //}
                        if (BluetoothManager.isBluetoothOpened()){
                            connect()
                        }
                    }else{
                        viewModel.showToast("请允许权限后重试")
                    }
                })
            },{it.printStackTrace()}))


        binding.tvUserManual.onClick {
            //用户手册
        }

        binding.tvPrivacyPolicy.onClick {
            //隐私协议
        }

        addDisposable(binding.captcha.clicks().throttleFirst(1, TimeUnit.SECONDS).subscribe({
            //获取验证码
            val phone = binding.edPhone.text.toString().trim()

            if (!RegexUtils.isMobileExact(phone)){
                viewModel.showToast(getString(R.string.obd_login_phone_IsNotNull))
                return@subscribe
            }
            val vciCode = binding.edVci.text.toString()
            if (TextUtils.isEmpty(vciCode)){
                viewModel.showToast(getString(R.string.obd_login_vci_IsNotNull))
                return@subscribe
            }

            addDisposable(OBDInteractor.getReginStart(vciCode,phone).subscribe({
                binding.captcha.isClickable = false
                binding.captcha.setTextColor(ContextCompat.getColor(this, R.color.grey))

                RxTimerUtil.timer(60) {
                    binding.captcha.text = "${getString(R.string.obd_login_fetch_code)}(${it})"
                    if (it == 0L) {
                        binding.captcha.isClickable = true
                        binding.captcha.text = getString(R.string.obd_login_fetch_code)
                        binding.captcha.setTextColor(ContextCompat.getColor(this, R.color.color_theme))
                    }
                }
            },{it.printStackTrace()}))
        },{it.printStackTrace()}))

        binding.btnLogin.setChangeAlphaWhenPress(true)
        addDisposable(binding.btnLogin.clicks().throttleFirst(1, TimeUnit.SECONDS).subscribe({
            if (!binding.checkbox.isChecked){
                viewModel.showToast(getString(R.string.obd_login_manuals_and_protocols))
                return@subscribe
            }
            val vciCode = binding.edVci.text.toString()
            if (TextUtils.isEmpty(vciCode)){
                viewModel.showToast(getString(R.string.obd_login_vci_IsNotNull))
                return@subscribe
            }

            val phone = binding.edPhone.text.toString().trim()
            val code = binding.edCode.text.toString().trim()

            if (!RegexUtils.isMobileExact(phone)){
                viewModel.showToast(getString(R.string.obd_login_phone_IsNotNull))
                return@subscribe
            }
            if (TextUtils.isEmpty(code)){
                viewModel.showToast(getString(R.string.obd_login_phone_code_IsNotNull))
                return@subscribe
            }

            addDisposable(OBDInteractor.getReginLogin(vciCode,phone,code.toInt()).subscribe({
                PreferencesUtils.putString(this, SharePreferencesConstant.USER_TOKEN,it.token)
                NetUrl.TOKEN = it.token
                VehicleScanningActivity.start(this,true)
                finish()
            },{it.printStackTrace()}))
        },{it.printStackTrace()}))


//        BluetoothManager.bleDevicesData.observe(this){ devices ->
//            val names = mutableListOf<String>()
//            for (item in devices){
//                if (ActivityCompat.checkSelfPermission(
//                        this,
//                        Manifest.permission.BLUETOOTH_CONNECT
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    viewModel.showToast("缺少BLUETOOTH_CONNECT权限,请授权")
//                    return@observe
//                }
//                item.name?.let { name-> names.add(name) }
//            }
//
//            val builder = AlertDialog.Builder(this)
//            builder.setTitle(getString(R.string.obd_select_device_to_connect))
//            builder.setItems(names.toTypedArray(), DialogInterface.OnClickListener { dialog, which ->
//                binding.bleProgressBar.visibility = View.VISIBLE
//                BluetoothManager.connect(devices[which].address)
//                dialog.dismiss()
//            })
//            viewModel.dismissLoading()
//            builder.show()
//        }

        viewModel.vciCode.observe(this, Observer {
            binding.edVci.setText(it)
            PreferencesUtils.putString(this, SharePreferencesConstant.VCI_CODE,it)
        })

        belListener()
        gpsRegisterReceiver()

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

    private fun gpsRegisterReceiver() {
        val filter = IntentFilter()
        filter.addAction(LocationManager.MODE_CHANGED_ACTION)
        //filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        gpsReceiver = GPSReceiver()
        gpsReceiver!!.setListener(object : GPSReceiver.CallBack {
            override fun gpsState(b: Boolean) {
                if (b) {
                    println("gps 开启")
                }else{
                    println("gps 关闭")
                }
            }
        })
        registerReceiver(gpsReceiver, filter)
    }

    private fun belListener() {
        bleReceiver = BluetoothListenerReceiver(Consumer {
            if (it == 2) {
                //开启
                connect()
            } else if (it == 4) {
                //关闭
            }
        })
        this.registerReceiver(bleReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    fun connect(){
        //viewModel.showLoading()
        //判断蓝牙是否连接
        //未连接走这里 已连接直接初始化去

        BluetoothManager.setListener(consumer = Consumer { state ->
            if (state == BluetoothManager.BLEState.CONNECT){
                //记录当前连接的蓝牙设备
                PreferencesUtils.putString(this, SharePreferencesConstant.ADAPTED_BLE_DEVICES,connectAddress)
                binding.bleProgressBar.visibility = View.INVISIBLE
                //初始化 检测 //测试设备10290334014Y
                if (viewModel.initOBD()){
                    viewModel.readVci(1)
                }
                viewModel.dismissLoading()
            }
        }).search2(Consumer { devices ->
            val names = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    viewModel.showToast("缺少BLUETOOTH_CONNECT权限,请授权")
                    return@Consumer
                }
            }
            for (item in devices){
                item.name?.let { name-> names.add(name) }
            }

            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.obd_select_device_to_connect))
            builder.setItems(names.toTypedArray(), DialogInterface.OnClickListener { dialog, which ->
                binding.bleProgressBar.visibility = View.VISIBLE
                connectAddress = devices[which].address //记录当前连接，连接成功后进行存储
                BluetoothManager.connect(devices[which].address)
                dialog.dismiss()
            })
            viewModel.dismissLoading()
            builder.show()
        }, Consumer {
            when(it){
                BluetoothManager.BLESearchState.START -> {
                    binding.bleProgressBar.visibility = View.VISIBLE
                }
                BluetoothManager.BLESearchState.STOP -> {
                    binding.bleProgressBar.visibility = View.INVISIBLE
                }
                else -> {

                }
            }
        })
    }


    override fun onDestroy() {
        gpsReceiver?.let { unregisterReceiver(it) }
        bleReceiver?.let { unregisterReceiver(it) }
        super.onDestroy()
    }

}