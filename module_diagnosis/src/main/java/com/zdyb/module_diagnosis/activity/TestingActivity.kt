package com.zdyb.module_diagnosis.activity

import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ThreadUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.qmuiteam.qmui.kotlin.onClick
import com.zdeps.gui.ConnDevices
import com.zdyb.lib_common.base.BaseActivity
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.lib_common.utils.RxTimerUtil
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.bean.DeviceEntity
import com.zdyb.module_diagnosis.bean.TestingBean
import com.zdyb.module_diagnosis.bean.Transfer
import com.zdyb.module_diagnosis.databinding.ActivityTestingBinding
import com.zdyb.module_diagnosis.utils.FileUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit

class TestingActivity :BaseActivity<ActivityTestingBinding,BaseViewModel>() {
    private var dis1: Disposable? = null
    private var dis2: Disposable? = null

    override fun initViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initViewObservable() {
        super.initViewObservable()

        binding.tvTitle.text = "检测诊断设备"
        binding.tvBack.onClick { finish() }

        binding.recyclerView.adapter = mAdapter

        val list = mutableListOf<TestingBean>()
        list.add(TestingBean("USB/MCU通讯测试",0,false,"结果:正常"))
        list.add(TestingBean("总驱动测试",0,false,"结果:正常"))
        list.add(TestingBean("CAN驱动设置测试",0,false,"结果:正常"))
        list.add(TestingBean("K线驱动设置测试",0,false,"结果:正常"))
        list.add(TestingBean("SAEJ1939/ISO15031/ISO27145/ISO14229/协议设置测试",0,false,"结果:正常"))
        list.add(TestingBean("ISO9141/ISO14230/协议设置测试",0,false,"结果:正常"))
        mAdapter.setList(list)
    }
    private val mAdapter: BaseQuickAdapter<TestingBean, BaseViewHolder> =
        object : BaseQuickAdapter<TestingBean, BaseViewHolder>(R.layout.item_test) {

            override fun convert(holder: BaseViewHolder, item: TestingBean) {

                holder.setText(R.id.name, item.name)
                val progressBar = holder.getView<ProgressBar>(R.id.progressBar1)

                var statString = ""
                when(item.state){
                    1 -> {
                        statString = "等待检测"
                    }
                    2 -> {
                        statString = "检测中"
                        progressBar.visibility = View.VISIBLE
                    }
                    3 -> {
                        statString = "检测结束"
                        progressBar.visibility = View.GONE

                        val resultText = holder.getView<TextView>(R.id.tv_t1_result)
                        if (item.result){
                            resultText.setTextColor(ContextCompat.getColor(context,R.color.color_theme))
                            resultText.text = item.resultString
                        }else{
                            resultText.setTextColor(ContextCompat.getColor(context,R.color.red))
                            resultText.text = item.resultString
                        }
                    }
                }
                holder.setText(R.id.tv_t1_state, statString)



                if (holder.layoutPosition %2 != 0){
                    holder.getView<ConstraintLayout>(R.id.itemLayout).setBackgroundResource(R.color.item_bg)
                }else{
                    holder.getView<ConstraintLayout>(R.id.itemLayout).setBackgroundResource(R.color.white)
                }

            }
        }

    override fun onResume() {
        super.onResume()

//        setState(0,false,2)
//        test()



        dis2 = Observable.just("")
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io()).subscribe {
                test2()
            }


    }


    fun setState(index:Int,result: Boolean,state :Int) {

        if (result){
            mAdapter.data[index].state = state
            mAdapter.data[index].result = true
            mAdapter.data[index].resultString = "结果:正常"
            runOnUiThread{
                mAdapter.notifyItemChanged(index)
            }
        }else{
            mAdapter.data[index].state = state
            mAdapter.data[index].result = false
            mAdapter.data[index].resultString = "结果:故障"
            runOnUiThread{
                mAdapter.notifyItemChanged(index)
            }
        }
    }


    fun test2(){
        if (!BaseApplication.usbConnect){
            viewModel.showToast("请将诊断接头连接到平板电脑")
            return
        }

        //USB通讯测试
        setState(0,true,2)
        ConnDevices.sendData(byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x02, 0xff.toByte(), 0x00, 0x55))
        val result = ConnDevices.outTimeReadData(500)
        val stringResult = ConvertUtils.bytes2HexString(result)
        if ("A5A50002FF0155" == stringResult){
            setState(0,true,3)
        }else{
            setState(0,false,3)
        }
        Thread.sleep(1000)

        //总驱动测试
        setState(1,true,2)
        ConnDevices.sendData(byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x04, 0x00, 0xff.toByte(), 0xff.toByte(), 0x00, 0x55))
        val result2 = ConnDevices.outTimeReadData(500)
        val stringResult2 = ConvertUtils.bytes2HexString(result2)
        if ("A5A5000500FFFF000155" == stringResult2){
            setState(1,true,3)
        }else{
            setState(1,false,3)
        }

        Thread.sleep(1000)
        //CAN驱动测试
        setState(2,true,2)
        ConnDevices.sendData(byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x04, 0x00, 0xff.toByte(), 0xff.toByte(), 0x01, 0x55))
        val result3 = ConnDevices.outTimeReadData(500)
        val stringResult3 = ConvertUtils.bytes2HexString(result3)
        if ("A5A5000500FFFF01FF55" == stringResult3){
            setState(2,true,3)
        }else{
            //倒数第二个字节是具体第几路失败
            setState(2,false,3)
        }
        Thread.sleep(1000)
        //K线驱动测试
        setState(3,true,2)
        ConnDevices.sendData(byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x04, 0x00, 0xff.toByte(), 0xff.toByte(), 0x02, 0x55))
        val result4 = ConnDevices.outTimeReadData(500)
        val stringResult4 = ConvertUtils.bytes2HexString(result4)
        if ("A5A5000500FFFF020155" == stringResult4){
            setState(3,true,3)
        }else{
            //倒数第二个字节是具体第几路失败
            setState(3,false,3)
        }

        Thread.sleep(1000)
        //SAEJ1939协议  和can是一样的
        //ISO15031协议
        //ISO27145协议
        setState(4,true,2)
        ConnDevices.sendData(byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x04, 0x00, 0xff.toByte(), 0xff.toByte(), 0x01, 0x55))
        val result5 = ConnDevices.outTimeReadData(500)
        val stringResult5 = ConvertUtils.bytes2HexString(result5)
        if ("A5A5000500FFFF01FF55" == stringResult5){
            setState(4,true,3)
        }else{
            //倒数第二个字节是具体第几路失败
            setState(4,false,3)
        }

        Thread.sleep(1000)
        //ISO9141协议  和K线是一样的
        //ISO14230协议
        //K线驱动测试
        setState(5,true,2)
        ConnDevices.sendData(byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x04, 0x00, 0xff.toByte(), 0xff.toByte(), 0x02, 0x55))
        val result6 = ConnDevices.outTimeReadData(500)
        val stringResult6 = ConvertUtils.bytes2HexString(result6)
        if ("A5A5000500FFFF020155" == stringResult6){
            setState(5,true,3)
        }else{
            //倒数第二个字节是具体第几路失败
            setState(5,false,3)
        }
    }



    fun test():Boolean{

        if (!BaseApplication.usbConnect){
            viewModel.showToast("请将诊断接头连接到平板电脑")
            return false
        }

        ConnDevices.sendData(byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x01,0xd1.toByte(), 0x55))
        ConnDevices.outTimeReadData(200)
        ConnDevices.purge()
        //流控 开关，这里展示不用

        //查询sn
        ConnDevices.sendData(byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x01, 0xf2.toByte(), 0x55))
        val snResult = ConnDevices.outTimeReadData(200)
        if (null != snResult && snResult.size >=17){
            val sn = String(snResult,5,12)
            KLog.d("sn=$sn")
            return true
        }

        return false
    }



    fun ss(b: Boolean){
        dis1 =
        Observable.interval(2000, TimeUnit.MILLISECONDS).repeat(10)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                //consumer.accept(it)
                if (it >= 6) {
                    dis1?.dispose()
                }
                println("测试="+it)

                when(it){
                    1L ->{
                        setState(0,b,3)
                        setState(1,b,2)
                    }
                    2L ->{
                        setState(1,b,3)
                        setState(2,b,2)
                    }
                    3L ->{
                        setState(2,b,3)
                        setState(3,b,2)
                    }
                    4L ->{
                        setState(3,b,3)
                        setState(4,b,2)
                    }
                    5L ->{
                        setState(4,b,3)
                    }
                    6L ->{

                    }
                }
            },{it.printStackTrace()})

    }

    override fun onDestroy() {
        super.onDestroy()
        dis1?.dispose()
        dis2?.dispose()
    }
}