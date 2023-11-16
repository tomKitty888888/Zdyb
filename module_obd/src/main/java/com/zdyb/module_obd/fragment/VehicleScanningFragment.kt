package com.zdyb.module_obd.fragment

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding3.view.clicks
import com.zdeps.bean.OBDBean
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.bus.BusEvent
import com.zdyb.lib_common.bus.EventType
import com.zdyb.module_obd.R
import com.zdyb.module_obd.databinding.ActivityVehicleScanningBinding
import com.zdyb.module_obd.dialog.ScanningDialog
import com.zdyb.module_obd.model.ObdFragmentModel
import io.reactivex.functions.Consumer
import java.util.concurrent.TimeUnit

class VehicleScanningFragment : BaseNavFragment<ActivityVehicleScanningBinding, ObdFragmentModel>(){

    private lateinit var scanningDialog : ScanningDialog


    override fun initViewModel(): ObdFragmentModel {
        val model: ObdFragmentModel by activityViewModels()
        return model
    }


    override fun initViewObservable() {
        super.initViewObservable()



        binding.scan.clicks().throttleFirst(1, TimeUnit.SECONDS).subscribe({
            scanningDialog = ScanningDialog()
            scanningDialog.show(childFragmentManager,"scanningDialog")

            viewModel.obdInfoList(3)

//            if (BluetoothController.isConnecting()){
//                //已连接
//                viewModel.obdInfoList()
//            }else{
//                BluetoothManager.init(consumer = Consumer { state ->
//                    if (state == BluetoothManager.BLEState.CONNECT){
//                        //初始化 检测 //测试设备10290334014Y
//                        //viewModel.initOBD()
//                    }
//                }).search(Consumer { devices ->
//
//                })
//            }
        },{it.printStackTrace()})

        viewModel.obdInfo.observe(this, Observer {


            findNavController().navigate(R.id.action_vehicleScanningFragment_to_OBDResultFragment)
            scanningDialog.dismiss()
        })
        viewModel.emissionInfo.observe(this, Observer {
            //数据准备就绪 跳转显示页面
            //val intent = Intent(activity, OBDResultActivity::class.java)
           // startActivity(intent)

            //findNavController().navigate(R.id.action_vehicleScanningFragment_to_OBDResultFragment)
            //scanningDialog.dismiss()
        })

    }

    private fun err(bean: OBDBean){
        if (scanningDialog != null && scanningDialog.isVisible){
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
                    err(t.data as OBDBean)
                }
            }
        }
    }
}