package com.zdeps.gui

import android.content.Context
import android.os.Build
import com.blankj.utilcode.util.ConvertUtils
import com.zdeps.obd.AssetUtils
import com.zdeps.obd.ContextUtils
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.bluetooth.BluetoothManager
import com.zdyb.module_obd.FilePathManager
import java.io.File
import java.io.IOException

/**
 * Created by Administrator on 2017/1/7.
 * soPath:数据库data.vdi文件路径。
 * “call from jni”代表此方法由诊断库回调此方法。
 */
object OBDComJni {



    /**
     * call from jni
     * 获取路径
     *
     * @return
     */
    var soPath = (ContextUtils.getContext().filesDir.path + File.separator + "data.vdi").toByteArray()

    var vdiPath = FilePathManager.getOBDVdiDataPath().toByteArray()

    fun init(){

    }

    init {
        //如果本地JNILibs中有so优先加载JNILibs中的，每次下载诊断会同步更新到该文件中
        val dir = BaseApplication.getInstance().getDir("jniLibs", Context.MODE_PRIVATE)
        val currentFiles = dir.listFiles()
        if (jniLibsIsNotSOFile(currentFiles)){
            for (f in currentFiles){
                if (f.isDirectory){
                    continue
                }
                if (f.name == "libOBDDiagnosis.so"){
                    System.load(f.absolutePath)
                    KLog.i("动态加载--libOBDDiagnosis")
                }else if (f.name == "libAndroidOBDDiagnoseBase.so"){
                    System.load(f.absolutePath)
                    KLog.i("动态加载--libAndroidOBDDiagnoseBase")
                }
            }
        }else{
            System.loadLibrary("OBDDiagnosis")
        }

        copyAssets()
        nativeSetup()
        OBDDiagnosisInit()
    }

    /**
     * 检查jniLibs文件中 obd环检的so文件是否存在
     */
    private fun jniLibsIsNotSOFile(currentFiles: Array<File>):Boolean{
        if (currentFiles.isEmpty()){
            return false
        }
        var libOBDDiagnosis = false
        var libAndroidOBDDiagnoseBase = false
        for (f in currentFiles){
            if (f.isDirectory){
                continue
            }
            if (f.name == "libOBDDiagnosis.so"){
                libOBDDiagnosis = true
            }else if (f.name == "libAndroidOBDDiagnoseBase.so"){
                libAndroidOBDDiagnoseBase = true
            }
        }
        if (libOBDDiagnosis && libAndroidOBDDiagnoseBase){
            return true
        }
        return false
    }

    fun getLastPath(path: String): String {
        val ret = path.lastIndexOf("/")
        return if (ret <= 0) path else path.substring(0, ret)
    }
    /**
     * call from jni
     *
     * @param path
     * @return
     */
    fun setExePath(path: String): Boolean {
        println("调用到setExePath()")
        var path = path
        soPath = path.toByteArray()
        vdiPath = path.toByteArray()
        for (i in 0..4) {
            path = getLastPath(path)
        }
        return true
    }

    fun getExePath():ByteArray{
        //println("C获取时候的路径=" + (ContextUtils.getContext().filesDir.path + File.separator + "data.vdi"))
        KLog.i("C获取时候的路径=" + FilePathManager.getOBDVdiDataPath())

        return vdiPath
    }

    /**
     * call from jni
     * 清空串口数据
     *
     * @return
     */
    fun PurgeComm() {
        BluetoothManager.purgeData()
    }


    private fun send(dat: ByteArray):Int {
        if (BaseApplication.usbConn != null) {

            return BaseApplication.usbConn.sendData(dat)
        }else{
            println("BaseApplication.usbConn=null")
        }
        return 0
    }
    private fun read(retlen:Int):ByteArray?{
        if (BaseApplication.usbConn != null) {
            return BaseApplication.usbConn.readData(retlen)
        }
        return ByteArray(0)
    }

    /**
     * call from jni
     *
     * @param dat 发送的数据
     * @param len 发送的长度
     * @return
     */
    fun sendData(dat: ByteArray, len: Int): Int {

        //蓝牙
        BluetoothManager.send(dat)
        return dat.size

        //串口
//        var res = 0
//        res = send(dat)
//        return res
    }

    /**
     * call from jni
     *
     * @param retlen 接收的数据长度
     * @return
     */
    fun recvData(retlen: Int): ByteArray {

        //蓝牙
        val data = BluetoothManager.readData(retlen)
        if (data != null && data.isNotEmpty()){
            KLog.i("recvData:"+ ConvertUtils.bytes2HexString(data))
            return data
        }

        //串口
//        val data = read(retlen)
//        if (data != null){
//            println("recvData="+ConvertUtils.bytes2HexString(data))
//            return data
//        }else{
//            //println("值为null??")
//        }

        return ByteArray(0)
    }

    private fun copyAssets() {
        try {
            //如果私有空间的文件不为空 则进行拷贝，检测两个目标源 一个是assets文件里的，还有一个是下载解压的文件的，优先使用下载解压的文件里的。
            //直接将assets文件拷贝到解压后的vdi目录 这样少一个流程

            val tempFile = File(FilePathManager.getOBDVdiDataPath())
            if (tempFile.exists()){
                println("vdi文件已存在")
            }else{
                println("vdi文件不存在 -- 等待拷贝")

                val filesName = ContextUtils.getContext().assets.list("")
                for (file in filesName!!) {
                    if (file == "data.vdi") {
                        AssetUtils.copyAssetsFile2Phone("data.vdi",FilePathManager.getOBDVdiDataPath())
                    }
                    /* if (file.equals("mcu.bin")) {
                        AssetUtils.copyAssetsFile2Phone("mcu.bin");
                    }*/
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * call from jni
     * 获取version
     *
     * @return
     */
    val sysVer: Int
        get() = Build.VERSION.SDK_INT

    external fun nativeSetup()
    external fun OBDDiagnosisInit()
    external fun CommInit(): Int
    external fun upgradePath(path: String?): ByteArray?
    external fun RequestByCmdType(command: Long): ByteArray?
    external fun RequestByXMLString(command: String?): ByteArray?



    //com.zdybs.gui.OBDComJni.nativeSetup()
    //Java_com_zdybs_gui_OBDComJni_nativeSetup
    //Java_com_zdybs_gui_OBDComJni_nativeSetup__
}