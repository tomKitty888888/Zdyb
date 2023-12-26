package com.zdyb.module_diagnosis.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.lib_common.utils.PreferencesUtils
import com.zdyb.lib_common.utils.SharePreferencesDiagnosis
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.bean.DataTabBean
import com.zdyb.module_diagnosis.bean.MotorcycleTypeEntity
import com.zdyb.module_diagnosis.netservice.DiagInteractor
import com.zdyb.module_diagnosis.utils.FileUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

class DownloadAllModel:BaseViewModel() {

    var childData = MutableLiveData<MutableList<MotorcycleTypeEntity>>()

    override fun onCreate() {
        super.onCreate()
    }


    fun getChildData(dataTabBean: DataTabBean){
        val vci = PreferencesUtils.getString(context,SharePreferencesDiagnosis.DEVICE_SN,"20221185052T") //这里测试给的默认值
        DiagInteractor.motorcycleType(dataTabBean.tag,vci).subscribe({
            collatingData(it,dataTabBean.tag)
        },{it.printStackTrace()})
    }


    /**
     * 整理数据
     */
    private fun collatingData(mtData :MutableList<MotorcycleTypeEntity>,tag:String){
        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            println("${coroutineContext[CoroutineName].toString()} 处理异常：$throwable")
        }) {
            launch(Dispatchers.IO) {
                //与本地的文件对比 初始化一些下载的路径，本地存储的路径 zdeps特殊处理
                val data = FileUtils.readItemFolder(PathManager.getDiagnosisPath()+tag)
                for (item in mtData){

                    //图片链接
                    item.imgUrl = context!!.getString(R.string.base_img_url)+item.brand_logo
                    //下载地址
                    item.downloadUrl = context!!.getString(R.string.download_url)+item.patch_url

                    if (tag == "Zdeps"){ //系统数据的存放路径不一样 只能按照旧项目的写 兼容 真乱
                        //父目录
                        item.basePath = PathManager.getBasePath() + item.brand_name
                        //本地存放路径
                        val temp = item.patch_url.substring(item.patch_url.indexOf("/"))
                        item.downloadSavePath = PathManager.getBasePath()+temp
                        //是否需要更新下载
                        item.isDownload = checkUpdate(item)
                    }else{
                        //父目录
                        item.basePath = PathManager.getDiagnosisPath() + item.brand_name
                        //下载存放路径
                        item.downloadSavePath = PathManager.getDiagnosisPath()+item.patch_url
                        //过滤已经有的升级文件
                        for (d in data){
                            if (item.brand_name == d.name){
                                if (FileUtils.formatVersionCode(item.versions) > d.verName) {
                                    //是否需要更新下载
                                    item.isDownload = true
                                    item.state = 5
                                }
                            }
                        }
                    }
                }
                //排个序
                mtData.sortByDescending { it.state }
                launch(Dispatchers.Main) {
                    childData.value = mtData
                }
            }

        }

    }

    /**
     * 判断系统数据是否需要更新
     *
     * @param item
     * @return
     */
    private fun checkUpdate(item: MotorcycleTypeEntity): Boolean {
        val path = item.basePath + File.separator + item.brand_name +".txt"

        val file: File = File(path)
        if (!file.exists()) {
            try {
                org.apache.commons.io.FileUtils.touch(file)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return true
        }
        if (FileUtils.formatVersionCode(item.versions) > readVersion(file, "version").toFloat()) {
            return true
        }
        return false
    }

    /**
     *  读取txt文件内容
     * @param file
     * @param params
     */
    fun readVersion(file: File?, params: String?): String {
        var value = ""
        try {
            val lineList = org.apache.commons.io.FileUtils.readLines(file, StandardCharsets.UTF_8)
            for (line in lineList) {
                if (line.startsWith(params!!)) {
                    val values = line.split("=".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    if (values.size > 1) {
                        value = values[1]
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return if (value.isEmpty()) "0" else value
    }
}