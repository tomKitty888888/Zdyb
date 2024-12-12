package com.zdyb.module_diagnosis.model

import androidx.lifecycle.viewModelScope
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.http.OKHttpManager
import com.zdyb.lib_common.utils.PreferencesUtils
import com.zdyb.lib_common.utils.SharePreferencesDiagnosis
import com.zdyb.module_diagnosis.netservice.DiagInteractor
import com.zdyb.module_diagnosis.utils.DisConfig
import com.zdyb.module_diagnosis.utils.Zip7pUtil
import io.reactivex.functions.Consumer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*

class HomeModel :BaseViewModel(){

    override fun onCreate() {
        super.onCreate()

        checkFile()


    }

    private fun checkFile(){
        try {
            val vci = PreferencesUtils.getString(BaseApplication.getInstance(), SharePreferencesDiagnosis.DEVICE_SN)
            if ("69" == vci.substring(6, 8)){
                //检查Vci.bin文件是否需要下载
                checkForBinDownloads()
                //检查蓝牙文件是否需要下载
                checkForBleDownloads()
                //检查自动识别文件是否需要下载
                checkForAutoScanDownloads()
                //PS:在usb连接上之后在检查是否需要升级，这里是下载升级的文件，不改动旧项目的流程。
            }
        }catch (e :Exception){
            e.printStackTrace()
        }
    }

    /**
     * 检查下位机文件是否需要下载
     */
    @Throws(IOException::class)
    private fun checkForBinDownloads(){
        val vci = PreferencesUtils.getString(BaseApplication.getInstance(), SharePreferencesDiagnosis.DEVICE_SN)
        addDisposable(DiagInteractor.version(vci).subscribe({
            KLog.i("下位机最新版本=${it.version}")
            //查看本地文件版本，本地文件也可能不存在
            val binFilePath = context!!.cacheDir.absolutePath +File.separator + "config" +File.separator +DisConfig.VCI_BIN_NAME +".bin"
            val binFile = File(binFilePath)

            //查看bin文件的版本号，存在在txt文件中
            val binTxtPath = context!!.cacheDir.absolutePath +File.separator + "config" +File.separator +DisConfig.VCI_BIN_NAME +".txt"
            val binTxtFile = File(binTxtPath)

            //处理一下版本号
            val tempVersion = it.version.replace("V","").replace("v","").replace(".","")

            //文件不存在或者 版本号未等于 直接下载最新
            if (!binFile.exists() || !binTxtFile.exists() || readFileVersion(binTxtFile) != tempVersion){
                val zipFilePath = context!!.cacheDir.absoluteFile.toString() + File.separator + DisConfig.VCI_BIN_NAME + ".7z"
                //直接下载
                addDisposable(OKHttpManager.getInstance().downloadFile(it.lower_machine_url,zipFilePath, Consumer {
                    //KLog.i("下载进度：$it")
                }).subscribe({
                    KLog.i("准备解压")
                    viewModelScope.launch(Dispatchers.IO){
                        val zipFile = File(it)
                        Zip7pUtil.unCompress(zipFile,File(zipFile.parentFile.absolutePath), Consumer {
                            println("升级文件解压进度=$it")
                            if (it >= 100){
                                zipFile.delete() //清理掉压缩文件
                            }
                        })
                    }
                },{it.printStackTrace()}))
            }else{
                KLog.i("下位机文件无需下载")
            }
        },{it.printStackTrace()}))

    }


    /**
     * 检查下位机蓝牙文件是否需要下载
     */
    @Throws(IOException::class)
    private fun checkForBleDownloads(){
        val vci = PreferencesUtils.getString(BaseApplication.getInstance(), SharePreferencesDiagnosis.DEVICE_SN)
        addDisposable(DiagInteractor.bluetooth(vci,1).subscribe({
            KLog.i("ble最新版本=${it.version}")
            //查看本地文件版本，本地文件也可能不存在
            val binFilePath = context!!.cacheDir.absolutePath +File.separator + "config" +File.separator +DisConfig.BLE_BIN_NAME +".bin"
            val binFile = File(binFilePath)

            //查看bin文件的版本号，存在在txt文件中
            val binTxtPath = context!!.cacheDir.absolutePath +File.separator + "config" +File.separator +DisConfig.BLE_BIN_NAME +".txt"
            val binTxtFile = File(binTxtPath)

            //处理一下版本号
            val tempVersion = it.version.replace("V","").replace("v","").replace(".","")

            //文件不存在或者 版本号未等于 直接下载最新
            if (!binFile.exists() || !binTxtFile.exists() || readFileVersion(binTxtFile) != tempVersion){
                val zipFilePath = context!!.cacheDir.absoluteFile.toString() + File.separator + DisConfig.BLE_BIN_NAME + ".7z"
                //直接下载
                addDisposable(OKHttpManager.getInstance().downloadFile(it.bluetooth_url,zipFilePath, Consumer {
                    //KLog.i("下载进度：$it")
                }).subscribe({
                    KLog.i("准备解压")
                    viewModelScope.launch(Dispatchers.IO){
                        val zipFile = File(it)
                        Zip7pUtil.unCompress(zipFile,File(zipFile.parentFile.absolutePath), Consumer {
                            println("ble升级文件解压进度=$it")
                            if (it >= 100){
                                zipFile.delete() //清理掉压缩文件
                            }
                        })
                    }
                },{it.printStackTrace()}))
            }else{
                KLog.i("ble文件无需下载")
            }
        },{it.printStackTrace()}))

    }



    /**
     * 检查自动识别文件是否需要下载
     */
    @Throws(IOException::class)
    private fun checkForAutoScanDownloads(){
        val vci = PreferencesUtils.getString(BaseApplication.getInstance(), SharePreferencesDiagnosis.DEVICE_SN)
        addDisposable(DiagInteractor.downResource(vci).subscribe({
            KLog.i("自动识别最新版本=${it.version}")
            //查看本地文件版本，本地文件也可能不存在
            val binFilePath = context!!.cacheDir.absolutePath +File.separator + "config" +File.separator +DisConfig.AUTO_SCAN_BIN_NAME +".bin"
            val binFile = File(binFilePath)

            //查看bin文件的版本号，存在在txt文件中
            val binTxtPath = context!!.cacheDir.absolutePath +File.separator + "config" +File.separator +DisConfig.AUTO_SCAN_BIN_NAME +".txt"
            val binTxtFile = File(binTxtPath)

            //处理一下版本号
            val tempVersion = it.version.replace("V","").replace("v","").replace(".","")

            //文件不存在或者 版本号未等于 直接下载最新
            if (!binFile.exists() || !binTxtFile.exists() || readFileVersion(binTxtFile) != tempVersion){
                val zipFilePath = context!!.cacheDir.absoluteFile.toString() + File.separator + DisConfig.BLE_BIN_NAME + ".7z"
                //直接下载
                addDisposable(OKHttpManager.getInstance().downloadFile(it.patch_url,zipFilePath, Consumer {
                    //KLog.i("下载进度：$it")
                }).subscribe({
                    KLog.i("准备解压")
                    viewModelScope.launch(Dispatchers.IO){
                        val zipFile = File(it)
                        Zip7pUtil.unCompress(zipFile,File(zipFile.parentFile.absolutePath), Consumer {
                            println("自动识别文件解压进度=$it")
                            if (it >= 100){
                                zipFile.delete() //清理掉压缩文件
                            }
                        })
                    }
                },{it.printStackTrace()}))
            }else{
                KLog.i("自动识别文件无需下载")
            }
        },{it.printStackTrace()}))

    }


    //@Throws(IOException::class)
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