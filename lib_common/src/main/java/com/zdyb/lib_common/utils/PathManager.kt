package com.zdyb.lib_common.utils

import android.os.Environment
import com.zdyb.lib_common.base.BaseApplication
import java.io.File

object PathManager {

    val STORAGE = Environment.getExternalStorageDirectory().absolutePath + File.separator
    //风味包名

    //sd卡路径风味路径
    //val BASE_PATH: String = STORAGE  + BASE_FOLDER + File.separator

    const val FILE_ECODING = "GBK"

    /**
     *  sd卡路径风味路径
     */
    fun getBasePath():String{
        return STORAGE  + getPackType() + File.separator
    }
    /**
     *  诊断文件路径
     */
    fun getDiagnosisPath():String{
        return STORAGE  + getPackType() + File.separator + "Diagnosis" + File.separator
    }

    /**
     *  sd卡路径风味路径 末尾不带/
     */
    fun getBasePathNoSep():String{
        return STORAGE  + getPackType()
    }

    /**
     *  sd卡路径风味路径
     */
    fun getBasePathNoSeparator():String{
        return STORAGE  + getPackType()
    }

    /**
     * 刷机文件下载位置
     */
    fun getDownloadFolderPath():String{
        return getBasePath() + "Download/webViewDownload"
    }


    /**
     * ecu刷写文件地址
     */
    fun getReflashFilePath():String{
        return getBasePath() + "Diagnosis/Reflash"
    }

    /**
     *后处理诊断文件地址
     */
    fun getATFilePath():String{
        return  getBasePath() + "Diagnosis/AfterTreatment"
    }

    /**
     * 新能能源车系
     */
    fun getEvFilePath():String{
        return  getBasePath() + "Diagnosis/NER"
    }

    /**
     * 天然气车系
     */
    fun getCngFilePath():String{
        return  getBasePath() + "Diagnosis/Natural"
    }


    //柴油车系下面


    /**
     * 电控系统
     */
    fun getElectronicFilePath():String{
        return  getBasePath() + "Diagnosis/Electronic"
    }

    /**
     * 发动机系统
     */
    fun getEngineFilePath():String{
        return  getBasePath() + "Diagnosis/Engine"
    }

    /**
     * 车系系统
     */
    fun getVehicleFilePath():String{
        return  getBasePath() + "Diagnosis/Vehicle"
    }

    /**
     * 工程机械
     */
    fun getMechanicalFilePath():String{
        return  getBasePath() + "Diagnosis/Mechanical"
    }

    /**
     * 高级工具
     */
    fun getObdFilePath():String{
        return  getBasePath() + "Diagnosis/Obd"
    }


    /**
     * 引线说明路径
     */
    fun ecuReportPath():String{
        return getBasePath() + "ECU"
    }

    private fun getPackType():String{
        println("packageName="+BaseApplication.getInstance().packageName)
        when(BaseApplication.getInstance().packageName){
            "com.zdyb.app" -> {
                return "zdeps"
            }
            "com.zdyb.zd" -> {
                return "zdeps"
            }
            "com.zdyb.ldjc" -> { //这里是测试的包名 最后都需要更改过来
                return "ldjc"
            }
        }
        return "ldjc"
    }
}