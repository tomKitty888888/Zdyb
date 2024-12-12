package com.zdyb.module_diagnosis.help

import android.os.SystemClock
import android.text.TextUtils
import com.blankj.utilcode.util.ConvertUtils
import com.zdeps.comm.CommomNative
import com.zdeps.gui.ConnDevices
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.utils.PreferencesUtils
import com.zdyb.lib_common.utils.SharePreferencesDiagnosis
import com.zdyb.module_diagnosis.utils.DisConfig
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object InitDeviceInfo {


    var sn = ""
    var version = ""
    var mCallBack: CommomNative.CallBack? = null

    var isCheckIng = false

    /**
     * 升级过程的回调
     */
    fun registrationCallBack(cb: CommomNative.CallBack) {
        this.mCallBack = cb
        CommomNative.registrationCallBack(cb)
    }

    /**
     * 查询下位机的信息 包括sn与版本信息
     */
    fun getDeviceInfo(){
        isCheckIng = true
        //让下位机停止发送数据
        ConnDevices.sendData(byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x01,0xd1.toByte(), 0x55))
        ConnDevices.timedReadsData(200)
        ConnDevices.purge()
        //关闭流控,发送指令是告诉下位机也关闭
        ConnDevices.setRts(false)
        ConnDevices.sendData(byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x02,0x64.toByte(), 0x00,0x55))
        ConnDevices.timedReadsData(200)

        //查询sn
        ConnDevices.sendData(byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x01, 0xf2.toByte(), 0x55))
        val snResult = ConnDevices.timedReadsData(200)
        if (null != snResult && snResult.size >=17){
            val sn = String(snResult,5,12)
            InitDeviceInfo.sn = sn
            KLog.d("sn=$sn")
            PreferencesUtils.putString(BaseApplication.getInstance(), SharePreferencesDiagnosis.DEVICE_SN,sn)
        }
        ConnDevices.purge()
        //查询版本号
        ConnDevices.sendData(byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x02, 0xf0.toByte(), 0x00, 0x55))
        val versionResult = ConnDevices.timedReadsData(200)
        val resultData = ByteArray(5)
        if (null != versionResult && versionResult.isNotEmpty()){
            for (i in versionResult.indices) {
                if (String(byteArrayOf(versionResult[i]), StandardCharsets.UTF_8) == "V") {
                    resultData[0] = versionResult[i]
                    resultData[1] = versionResult[i + 1]
                    resultData[2] = versionResult[i + 2]
                    resultData[3] = versionResult[i + 3]
                    resultData[4] = versionResult[i + 4]
                }
            }
            val versionString = String(resultData, StandardCharsets.UTF_8)
            version = versionString
            KLog.d("version=$versionString")
            PreferencesUtils.putString(BaseApplication.getInstance(), SharePreferencesDiagnosis.DEVICE_VERSION,versionString)
        }

        if (!TextUtils.isEmpty(sn) && sn.substring(6,8) == "69"){
            //升级下位机
            upDeviceAllData(sn, version)
        }

    }


    /**
     * 升级下位机硬件，或者是蓝牙，一次只升级一个按照旧项目的流程处理 不做更改
     */
    private fun upDeviceAllData(sn:String,version:String){
        //只能通过USB串口升级
        if (!ConnDevices.isUSBConnect()){
            return
        }
        //显示样机
        val com = 0x69.toByte()

        //检查下位机是否需要升级,升级下位机硬件，或者是蓝牙，一次只升级一个按照旧项目的流程处理 不做更改
        if (checkUPDevice(sn, version)){
            return
        }

        //蓝牙检查升级与救活
        upBle()

        //下位机智能配置升级
        upConfig(version)

        this.isCheckIng = false
    }

    /**
     * 检查下位机是否需要升级
     */
    private fun checkUPDevice(sn:String,version:String):Boolean{
        //查看本地文件版本，本地文件也可能不存在
        val binFilePath = BaseApplication.getInstance().cacheDir.absolutePath + File.separator + "config" + File.separator + DisConfig.VCI_BIN_NAME +".bin"
        val binFile = File(binFilePath)

        //查看bin文件的版本号，存在在txt文件中
        val binTxtPath = BaseApplication.getInstance().cacheDir.absolutePath + File.separator + "config" + File.separator + DisConfig.VCI_BIN_NAME +".txt"
        val binTxtFile = File(binTxtPath)

        if (!binFile.exists() || !binTxtFile.exists()){
            return false
        }
        //通过对比串口获取到的版本数据 与本地存储的txt中的版本号进行比较，升级文件的版本号大于下位机的版本号就进行升级
        val binFileVersion = readFileVersion(binTxtFile)
        val tempDeviceVersion = version.replace("v","").replace("V","").replace(".","")

        if (binFileVersion.toInt() > tempDeviceVersion.toInt()){
            KLog.d("开始给下位机升级，本地版本=${binFileVersion},下位机版本=${tempDeviceVersion}")
            //先弹窗
            mCallBack?.onUpdateProgressing("准备升级中...")
            upBottomDevice(binFile.absolutePath,version,0x69.toByte(),1024,true)
            return true
        }
        return false
    }

    /**
     *  下位机升级
     */
    private fun upBottomDevice(filePath:String,vciVersion:String,brand :Byte,dwEachCount:Int,closedialog :Boolean){
        //String filePath,String vciVersion,byte brand,int dwEachCount,boolean closedialog
        KLog.d("filePath=${filePath},vciVersion=${vciVersion},brand=${brand}")
        val result = CommomNative.updateVci1018(filePath, vciVersion, brand, dwEachCount, closedialog)
        if (result){
            mCallBack?.onUpdateSuccess()
        }
    }

    /**
     *  蓝牙升级与救活
     */
    private fun upBle(){
        //重启蓝牙
        val resetBlueTooth = byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x02, 0xf6.toByte(), 0x03, 0x55)
        ConnDevices.sendData(resetBlueTooth)
        val bleOpenResult = ConnDevices.timedReadsData(2000)
        //重启正确就拿版本号 进行对比
        var bleVerSionString = ""
        if (null != bleOpenResult){
            val bleOpenResultString = ConvertUtils.bytes2HexString(bleOpenResult)
            if (bleOpenResultString == "A5A50002F60755"){
                SystemClock.sleep(500)
                KLog.i("重启正确，读取版本号")
                //获取蓝牙版本号
                val getBlueVersion = byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x02, 0xf6.toByte(), 0x00, 0x55)
                ConnDevices.sendData(getBlueVersion)
                val bleVersionResult = ConnDevices.timedReadsData(2000)
                if (null != bleVersionResult && bleVersionResult.size >= 10){
                    bleVerSionString =""+(bleVersionResult[6] -0x30)+(bleVersionResult[8] -0x30)+(bleVersionResult[9] -0x30)
                }
            }else{
                KLog.i("重启失败")
            }
        }else{
            KLog.i("重启失败")
        }

        if (null == bleOpenResult || bleOpenResult.isEmpty() || TextUtils.isEmpty(bleVerSionString)){
            //蓝牙救活

            //在蓝牙复位失败的情况下直接确认芯片是处于串口0下载方式，sp32回复的数据不止10字节，能接收到证明芯片处于下载模式了
            val cmdData =byteArrayOf(0xA5.toByte(), 0xA5.toByte(), 0x00, 0x02, 0xF6.toByte(), 0x05, 0x55)
            ConnDevices.sendData(cmdData)
            val resultData = ConnDevices.timedReadsDataLength(3000,10)
            if (resultData.size >= 10){
                val tempString = ConvertUtils.bytes2HexString(resultData)
                if (tempString == "00000000000000000000"){
                    return
                }
                //串口0的模式可以进行，将uart0复位
                ConnDevices.purge()
                val cmdRepositionData = byteArrayOf(0xA5.toByte(), 0xA5.toByte(), 0x00, 0x02, 0xF1.toByte(), 0x01, 0x55)
                ConnDevices.sendData(cmdRepositionData)
                val resultRepositionData = ConnDevices.timedReadsDataLength(3000,6)
                if (ConvertUtils.bytes2HexString(resultRepositionData) == "A5A50001F155"){
                    KLog.i("uart0关闭-成功-继续确认文件")
                    ConnDevices.purge()
                }

            }else{
                KLog.i("uart0开启-失败")
                ////切换下位机透传
                //切换下位机透传
                val cmd1 =byteArrayOf(0xA5.toByte(), 0xA5.toByte(), 0x00, 0x02, 0xF1.toByte(), 0x01, 0x55)
                ConnDevices.purge()
                ConnDevices.sendData(cmd1)
                val resultData1 = ConnDevices.timedReadsDataLength(3000,6)
                return
            }

            val baseDir = BaseApplication.getInstance().cacheDir.absoluteFile.absolutePath
            val bl = File(baseDir + File.separator + "config" + File.separator + "bootloader.bin")
            val pt = File(baseDir + File.separator + "config" + File.separator + "partition-table.bin")
            val bt = File(baseDir + File.separator + "config" + File.separator + DisConfig.BLE_BIN_NAME + ".bin")

            if (bl.exists() && pt.exists() && bt.exists()){
                mCallBack?.onUpdateProgressing("正在给下位机蓝牙救活升级，请勿操作")
                val result = CommomNative.saveBlueTooth(bl.absolutePath, pt.absolutePath, bt.absolutePath)
                KLog.i("救活流程走完--->$result")
                if (TextUtils.isEmpty(result)){
                    mCallBack?.onUpdateFaild("蓝牙救活成功,请给下位机断电,重新连接下位机")
                }else{
                    mCallBack?.onUpdateFaild(result)
                }
                return
            }else{
                KLog.i("救活文件不存在")
            }

            return
        }else{

            //正常升级
            KLog.i("蓝牙准备升级")
            //对比版本号
            if (contrastVersion(DisConfig.BLE_BIN_NAME,bleVerSionString)){

                mCallBack?.onUpdateProgressing("正在升级蓝牙，请勿操作")
                val binFilePath = BaseApplication.getInstance().cacheDir.absolutePath + File.separator + "config" + File.separator + DisConfig.BLE_BIN_NAME +".bin"
                val binFile = File(binFilePath)
                val b = CommomNative.updateBluetooth(binFile.absolutePath, 1024 * 5)
                if (b){
                    mCallBack?.onUpdateSuccess()
                    KLog.i("蓝牙复位成功")
                }
            }else{
                KLog.i("蓝牙不需要升级")
            }

        }

    }


    /**
     * 下位机智能配置升级
     */
    private fun upConfig(version :String){
        if (!TextUtils.isEmpty(version)){
            val tempDeviceVersion = version.replace("v","").replace("V","").replace(".","")
            if (tempDeviceVersion.toInt() >= 259){
                val getAutoScanVersion = byteArrayOf(
                    0xa5.toByte(),
                    0xa5.toByte(),
                    0x00,
                    0x03,
                    0xfa.toByte(),
                    0x00,
                    0x00,
                    0x55)
                ConnDevices.sendData(getAutoScanVersion)
                val result = ConnDevices.timedReadsData(2000)
                if (result != null && result.isNotEmpty()){
                    if (result.size > 7){
                        //A5A50007FA56 31 2E 30 30 35 55  v1.005
                        var num: Int = (result[6] - 0x30) * 1000
                        num += (result[8] - 0x30) * 100
                        num += (result[9] - 0x30) * 10
                        num += (result[10] - 0x30) * 1

                        if (contrastVersion(DisConfig.AUTO_SCAN_BIN_NAME,num.toString())){
                            mCallBack?.onUpdateProgressing("正在升级智能诊断，请勿操作")
                            val binFilePath = BaseApplication.getInstance().cacheDir.absolutePath + File.separator + "config" + File.separator + DisConfig.AUTO_SCAN_BIN_NAME +".bin"
                            val binFile = File(binFilePath)

                            //查看bin文件的版本号，存在在txt文件中
                            val binTxtPath = BaseApplication.getInstance().cacheDir.absolutePath + File.separator + "config" + File.separator + DisConfig.AUTO_SCAN_BIN_NAME +".txt"
                            val binTxtFile = File(binTxtPath)
                            val binFileVersion = readFileVersion(binTxtFile)
                            CommomNative.updateVciIdentify(binFile.absolutePath,binFileVersion.toInt())
                            mCallBack?.onUpdateSuccess()
                        }else{
                            KLog.i("下位机智能诊断扫描版本不需要升级")
                        }
                    }else{
                        KLog.i("空版本不比较版本 准备刷写")
                        mCallBack?.onUpdateProgressing("正在升级智能诊断，请勿操作")
                        val binFilePath = BaseApplication.getInstance().cacheDir.absolutePath + File.separator + "config" + File.separator + DisConfig.AUTO_SCAN_BIN_NAME +".bin"
                        val binFile = File(binFilePath)

                        //查看bin文件的版本号，存在在txt文件中
                        val binTxtPath = BaseApplication.getInstance().cacheDir.absolutePath + File.separator + "config" + File.separator + DisConfig.AUTO_SCAN_BIN_NAME +".txt"
                        val binTxtFile = File(binTxtPath)
                        val binFileVersion = readFileVersion(binTxtFile)
                        CommomNative.updateVciIdentify(binFile.absolutePath,binFileVersion.toInt())
                        mCallBack?.onUpdateSuccess()
                    }
                }
            }else{
                KLog.i("下位机版本太小不支持升级智能诊断扫描配置")
            }
        }

    }


    /**
     * 传入文件名称，与硬件程序版本号进行对比 检查是否需要升级
     */
    private fun contrastVersion(fileName:String,deviceVersion:String):Boolean{
        //查看本地文件版本，本地文件也可能不存在
        val binFilePath = BaseApplication.getInstance().cacheDir.absolutePath + File.separator + "config" + File.separator + fileName +".bin"
        val binFile = File(binFilePath)

        //查看bin文件的版本号，存在在txt文件中
        val binTxtPath = BaseApplication.getInstance().cacheDir.absolutePath + File.separator + "config" + File.separator + fileName +".txt"
        val binTxtFile = File(binTxtPath)

        if (!binFile.exists() || !binTxtFile.exists()){
            return false
        }
        //通过对比串口获取到的版本数据 与本地存储的txt中的版本号进行比较，升级文件的版本号大于下位机的版本号就进行升级
        val binFileVersion = readFileVersion(binTxtFile)
        val tempDeviceVersion = deviceVersion.replace("v","").replace("V","").replace(".","")

        if (binFileVersion.toInt() > tempDeviceVersion.toInt()){
            return true
        }
        return false
    }



    private fun readFileVersion(file: File): String {
        var ver: String = ""
        try {
            if (file.exists()) {
                ver = BufferedReader(InputStreamReader(FileInputStream(file))).readLine()
                KLog.i("文件读取内容=${ver}")
            } else {
                KLog.d("文件不存在：${file.absolutePath},返回空字符串")
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return ver
    }
}