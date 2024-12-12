package com.zdyb.module_diagnosis.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.zdeps.app.utils.ZipUtils
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.lib_common.utils.PreferencesUtils
import com.zdyb.lib_common.utils.SharePreferencesDiagnosis
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.bean.DataTabBean
import com.zdyb.module_diagnosis.bean.MotorcycleTypeEntity
import com.zdyb.module_diagnosis.netservice.DiagInteractor
import com.zdyb.module_diagnosis.utils.FileUtils
import com.zdyb.module_diagnosis.utils.Zip7pUtil
import io.reactivex.functions.Consumer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets

class DownloadAllModel:BaseViewModel() {

    var childData = MutableLiveData<MutableList<MotorcycleTypeEntity>>()

    override fun onCreate() {
        super.onCreate()
    }


    fun getChildData(dataTabBean: DataTabBean,errConsumer: Consumer<Boolean>){
        val vci = getVCI() //这里测试给的默认值
        addDisposable(DiagInteractor.motorcycleType(dataTabBean.tag,vci).subscribe({
            collatingData(it,dataTabBean.tag)
        },{
            it.printStackTrace()
            errConsumer.accept(true)
        }))

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
                                    item.state = 6
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

    public interface ZipState{
        fun progress(progress :Int,isSuccess :Boolean){}
    }

    fun unzipFile(download: MotorcycleTypeEntity,zipState :ZipState){
        //1 获取文件
        //2 判断文件类型 zip 与 7z文件
        //3 解压
        val zipFile = File(download.downloadSavePath)
        val zipType = Zip7pUtil.fileType(zipFile)
        val endIndex = zipFile.absolutePath.lastIndexOf("/")
        val outPath = zipFile.absolutePath.substring(0,endIndex) //解压目录

        if (zipType == 1){
            viewModelScope.launch(Dispatchers.IO){
                Zip7pUtil.unCompress(zipFile,File(outPath), Consumer {
                    println("解压进度=$it")

                    if (it >= 100){
                        viewModelScope.launch(Dispatchers.Main){
                            zipState.progress(it,true)
                        }
                        zipFile.delete() //清理掉压缩文件
                    }

                })
            }
        }else if (zipType == 2){

            unZip(zipFile.absolutePath,outPath, Consumer {
                if (it){
                    downLoadImage(download)
                }

                viewModelScope.launch(Dispatchers.Main){
                    zipState.progress(100,it)
                }

            })
        }
    }

    /**
     * 解压文件
     */
    private fun unZip(inputFile:String,outFile:String,consumer: Consumer<Boolean>){
        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            println("${coroutineContext[CoroutineName].toString()} 处理异常：$throwable")
        }) {
            launch(Dispatchers.IO){
                ZipUtils.exactFiles(inputFile,outFile, Consumer {
                    consumer.accept(it)
                    File(inputFile).delete() //清理掉压缩文件
                    if (it){
                        //showToast("解压完成")

                        KLog.i("解压完成")
                    }else{
                        launch(Dispatchers.Main){
                            showToast("解压失败,请检查服务器升级文件的压缩报格式是否正确")
                        }
                        KLog.e("解压失败,请检查服务器升级文件的压缩报格式是否正确")
                    }
                })
            }
        }
    }

    /**
     *  下载图片存储到对应的目录 - 为什么不能放在zip文件中一起解压出来呢
     */
    private fun downLoadImage(download: MotorcycleTypeEntity){
        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            println("${coroutineContext[CoroutineName].toString()} 处理异常：$throwable")
        }) {
            launch(Dispatchers.IO){
                println("下载图片--")
                val file = Glide.with(context!!).asFile().load(download.imgUrl).submit(200, 100).get()
                val endIndex = download.downloadSavePath.lastIndexOf("/")
                val outPath = download.downloadSavePath.substring(0,endIndex)
                println("图片文件存放路径=$outPath")
                copyFileUsingFileChannels(file,File("$outPath/car.pngex"))
            }
        }
    }

    /**
     * 文件拷贝
     */
    @Throws(IOException::class)
    private fun copyFileUsingFileChannels(source: File, dest: File) {
        KLog.i("-----copyFileUsingFileChannels-----")
        var inputChannel: FileChannel? = null
        var outputChannel: FileChannel? = null
        try {
            inputChannel = FileInputStream(source).channel
            outputChannel = FileOutputStream(dest).channel
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size())
        } catch (e: Exception) {
            e.printStackTrace()
        }finally {
            inputChannel?.close()
            outputChannel?.close()
        }
    }
}