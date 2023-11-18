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
     *  sd卡路径风味路径 末尾不带/
     */
    fun getBasePathNoSep():String{
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
            "com.zdyb.ldjc" -> { //这里是测试的包名 最后都需要更改过来
                return "ldjc"
            }
        }
        return "ldjc"
    }
}