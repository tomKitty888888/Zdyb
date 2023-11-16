package com.zdyb.module_obd

import com.zdyb.lib_common.base.BaseApplication
import java.io.File

object FilePathManager {


    /**
     * 从服务器下载环检 7z压缩文件的存放地址
     */
    fun getDownloadOBD7zPath():String{
        return BaseApplication.getInstance().filesDir.absoluteFile.toString() + File.separator  + "OBD.7z"
    }

    /**
     * 环检 压缩文件解压的地址
     */
    fun getUnOBDZipPath():String{
        return BaseApplication.getInstance().filesDir.absoluteFile.toString() + File.separator + "diagnostic"
    }


    /**
     * 解压后
     * 环检 so文件的地址 armeabi-v7a文件下的
     */
    fun getOBDSoPath():String{
        return BaseApplication.getInstance().filesDir.absoluteFile.toString() + File.separator + "diagnostic/obd/obd/armeabi-v7a"
    }


    /**
     * 解压后
     * 环检 vdi数据库文件地址
     */
    fun getOBDVdiDataPath():String{

        return BaseApplication.getInstance().filesDir.absoluteFile.toString() + File.separator + "diagnostic/obd/obd/data.vdi"
    }

}