package com.zdyb.module_obd.fragment

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.RegexUtils
import com.zdyb.lib_common.utils.RxTimerUtil
import com.qmuiteam.qmui.kotlin.onClick
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.bluetooth.BluetoothManager
import com.zdyb.lib_common.receiver.BluetoothListenerReceiver
import com.zdyb.lib_common.receiver.GPSReceiver
import com.zdyb.module_obd.R
import com.zdyb.module_obd.databinding.FragmentObdLoginBinding
import com.zdyb.module_obd.dialog.DialogBox
import com.zdyb.module_obd.model.ObdFragmentModel
import com.zdyb.module_obd.service.OBDInteractor
import io.reactivex.functions.Consumer

class LoginFragment : BaseNavFragment<FragmentObdLoginBinding, ObdFragmentModel>(){

    override fun initViewModel(): ObdFragmentModel {
        val model: ObdFragmentModel by activityViewModels()
        return model
    }


    override fun initViewObservable() {
        super.initViewObservable()
        binding.bleConnect.onClick {

            if (!getGpsStatus(requireContext())){
                DialogBox().setBox(getString(R.string.obd_login_ble_location_IsOpen))
                    .setResult(
                        Consumer {
                            if (it){goToOpenGps(requireContext())}
                        }).show(childFragmentManager,"DialogBox")
                //viewModel.showToast(getString(R.string.obd_login_ble_location_IsOpen))
                return@onClick
            }

            //连接蓝牙 获取vic码
            viewModel.openPermissions(Consumer {
                if (it){
                    val blueadapter = BluetoothAdapter.getDefaultAdapter()
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@Consumer
                    }
                    if (blueadapter.enable()) { //蓝牙已打开
                        connect()
                    }
                }else{
                    viewModel.showToast("请允许权限后重试")
                }
            })
        }

        binding.tvUserManual.onClick {
            //用户手册
        }

        binding.tvPrivacyPolicy.onClick {
            //隐私协议
        }

        binding.captcha.onClick {
            //获取验证码
            val phone = binding.edPhone.text.toString().trim()

            if (!RegexUtils.isMobileExact(phone)){
                viewModel.showToast(getString(R.string.obd_login_phone_IsNotNull))
                return@onClick
            }
            val vciCode = binding.edVci.text.toString()
            if (TextUtils.isEmpty(vciCode)){
                viewModel.showToast(getString(R.string.obd_login_vci_IsNotNull))
                return@onClick
            }


            OBDInteractor.getReginStart("22222279222T",phone).subscribe({
                binding.captcha.isClickable = false
                binding.captcha.setTextColor(ContextCompat.getColor(requireContext(),R.color.grey))

                RxTimerUtil.timer(60) {
                    binding.captcha.text = "${getString(R.string.obd_login_fetch_code)}(${it})"
                    if (it == 0L) {
                        binding.captcha.isClickable = true
                        binding.captcha.text = getString(R.string.obd_login_fetch_code)
                        binding.captcha.setTextColor(ContextCompat.getColor(requireContext(),R.color.color_theme))
                    }
                }
            },{it.printStackTrace()})
        }

        binding.btnLogin.setChangeAlphaWhenPress(true)
        binding.btnLogin.onClick {

            if (!binding.checkbox.isChecked){
                viewModel.showToast(getString(R.string.obd_login_manuals_and_protocols))
                return@onClick
            }
            val vciCode = binding.edVci.text.toString()
            if (TextUtils.isEmpty(vciCode)){
                viewModel.showToast(getString(R.string.obd_login_vci_IsNotNull))
                return@onClick
            }

            val phone = binding.edPhone.text.toString().trim()
            val code = binding.edCode.text.toString().trim()

            if (!RegexUtils.isMobileExact(phone)){
                viewModel.showToast(getString(R.string.obd_login_phone_IsNotNull))
                return@onClick
            }
            if (TextUtils.isEmpty(code)){
                viewModel.showToast(getString(R.string.obd_login_phone_code_IsNotNull))
                return@onClick
            }

//            LoginInteractor.getReginLogin("22222279222T",phone,code.toInt()).subscribe({
//
//            },{it.printStackTrace()})

            findNavController().navigate(R.id.action_loginFragment_to_vehicleScanningFragment)
        }


        BluetoothManager.bleDevicesData.observe(this){ devices ->
            val names = mutableListOf<String>()
            for (item in devices){
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    viewModel.showToast("缺少BLUETOOTH_CONNECT权限,请授权")
                    return@observe
                }
                item.name?.let { name-> names.add(name) }
            }

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.obd_select_device_to_connect))
            builder.setItems(names.toTypedArray(), DialogInterface.OnClickListener { dialog, which ->
                binding.bleProgressBar.visibility = View.VISIBLE
                BluetoothManager.connect(devices[which].address)
                dialog.dismiss()
            })
            viewModel.dismissLoading()
            builder.show()
        }

        viewModel.vciCode.observe(this, Observer {
            binding.edVci.setText(it)
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
        requireContext().registerReceiver(gpsReceiver, filter)
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
        requireContext().registerReceiver(bleReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    fun connect(){
        viewModel.showLoading()
        BluetoothManager.setListener(consumer = Consumer { state ->
            if (state == BluetoothManager.BLEState.CONNECT){
                binding.bleProgressBar.visibility = View.INVISIBLE
                //初始化 检测 //测试设备10290334014Y
                if (viewModel.initOBD()){
                    viewModel.readVci(1)
                }
                viewModel.dismissLoading()
            }
        }).search(Consumer { devices ->

        })
    }

    override fun onDestroyView() {
        requireContext().unregisterReceiver(gpsReceiver)
        super.onDestroyView()
    }
}