package com.zdyb.module_diagnosis.model

import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ConvertUtils
import com.zdeps.gui.ConnDevices
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.module_diagnosis.bean.VoltageBean
import io.reactivex.functions.Consumer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.experimental.and

class VoltageModel :BaseViewModel(){

    private val voltageIds = byteArrayOf(
        0x01,
        0x02,
        0x03,
        0x04,
        0x05,
        0x06,
        0x07,
        0x08,
        0x09,
        0x0A,
        0x0B,
        0x0C,
        0x0D,
        0x0E,
        0x0F,
        0x10
    )



    override fun onCreate() {
        super.onCreate()
    }

    var isStop = false

    fun scan(consumer: Consumer<VoltageBean>){
        //while (!isStop){

            select(consumer)
        //}
    }

    private fun select(consumer: Consumer<VoltageBean>){

        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            println("${coroutineContext[CoroutineName].toString()} 处理异常：$throwable")
        }) {
            launch(Dispatchers.IO) {

                for ((i, id) in voltageIds.withIndex()){

                    if (i == 3 || i == 4){ //不需要查询
                        continue
                    }

                    if (i == voltageIds.size-1){ //最后一个
                        //16号针脚获取数据:A5 A5 00 01 E4 55
                        val set16Pin = byteArrayOf(
                            0xA5.toByte(),
                            0xA5.toByte(),
                            0x00,
                            0x01,
                            0xE4.toByte(),
                            0x55)
                        ConnDevices.sendData(set16Pin)
                        val result2 = ConnDevices.timedReadsData(500)
                        val hexString = ConvertUtils.bytes2HexString(result2)
                        val voltageBean = jiexi(id,hexString) //这里固定给0x10最后一位
                        launch(Dispatchers.Main) {
                            consumer.accept(voltageBean)
                        }
                        break
                    }
                    //设置针脚
                    val setPin = byteArrayOf(
                        0xA5.toByte(),
                        0xA5.toByte(),
                        0x00,
                        0x02,
                        0xE6.toByte(),
                        id,
                        0x55)
                    ConnDevices.sendData(setPin)
                    val result = ConnDevices.timedReadsData(500)
                    //不处理

                    // 获取电压
                    val voltageByte = byteArrayOf(
                        0xA5.toByte(),
                        0xA5.toByte(),
                        0x00,
                        0x01,
                        0xE5.toByte(),
                        0x55,
                        0x55)
                    ConnDevices.sendData(voltageByte)
                    val result2 = ConnDevices.timedReadsData(500)
                    val hexString = ConvertUtils.bytes2HexString(result2)
                    val voltageBean = jiexi(id,hexString)
                    launch(Dispatchers.Main) {
                        consumer.accept(voltageBean)
                    }
                }


            }

        }
    }


    private fun jiexi(id:Byte, hexString :String):VoltageBean{
        if (hexString.contains("A5A50003E5") || hexString.contains("A5A50003E4")){
            val index: Int = (id and 0xff.toByte()).toInt()
            println("测试下标=$index")

            //解析电压值
            //YY
            val hex1: String = hexString.substring(10, 12)
            val i1 = hex1.toInt(16)
            //ZZ
            val hex2: String = hexString.substring(12, 14)
            val i2 = hex2.toInt(16)
            val dy = if (i1 == 0 && i2 <= 80) {
                "$i1.00V"
            }else{
                if(i2 < 10){
                    i1.toString() + "." + "0" + i2 + "V"
                }else{
                    i1.toString() + "." + i2 + "V"
                }
            }
            println("测试电压=$dy")
            return VoltageBean(index,dy)
        }
        return VoltageBean()
    }

}