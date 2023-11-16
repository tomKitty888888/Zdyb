package com.zdyb.module_diagnosis.help

import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.bean.CDSSelectEntity
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

object CDSHelp {

    //创建文件
    //写入标题
    //写入数据

    private var currencyFile :RandomAccessFile ?= null

    /**
     * 创建文件
     */
    fun createFile(deviceName:String?){
        try {
            //创建文件
            val date = Date()
            val times = SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(date) + "_" + date.time
            val pathString = if (null == deviceName){
                PathManager.getBasePath() + "/save/" + times + ".csv"
            }else{
                PathManager.getBasePath() + "/save/" + deviceName + times + ".csv"
            }
            val file: File = File(pathString)
            if (file.exists()) {
                file.delete()
            } else {
                file.parentFile?.mkdirs()
            }
            currencyFile = RandomAccessFile(file, "rw")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    /**
     * 写入标题
     */
    fun writeTitle(data :MutableList<CDSSelectEntity>){
        try {
            currencyFile?.let {
                for (s in data) {
                    it.write("${s.value1},".toByteArray(Charset.forName("GBK")))
                }
                it.writeBytes("\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 写入value
     */
    fun writeValue(data :MutableSet<CDSSelectEntity>){
        val thread = Thread {
            synchronized(this){
                try {
                    currencyFile?.let {
                        for (s in data) {
                            it.write("${s.value2},".toByteArray(Charset.forName("GBK")))
                        }
                        it.writeBytes("\n")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        thread.start()
    }


    fun closeFile():Boolean{
        if (null != currencyFile){
            currencyFile?.close()
            currencyFile = null
            return true
        }
        return false
    }

}