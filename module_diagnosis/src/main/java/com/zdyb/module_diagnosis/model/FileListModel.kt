package com.zdyb.module_diagnosis.model

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zdeps.app.utils.ZipUtils
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.base.KLog
import com.zdyb.module_diagnosis.bean.ItemVersionEntity
import com.zdyb.module_diagnosis.bean.MotorcycleTypeEntity
import com.zdyb.module_diagnosis.bean.ProductsEntity
import com.zdyb.module_diagnosis.netservice.DiagInteractor
import com.zdyb.module_diagnosis.utils.FileUtils
import com.zdyb.module_diagnosis.utils.Zip7pUtil
import io.reactivex.functions.Consumer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.jvm.internal.Intrinsics.Kotlin

class FileListModel:BaseViewModel() {


    var fileListLiveData = MutableLiveData<MutableList<ProductsEntity>>()
    var versionListLiveData = MutableLiveData<MutableList<ItemVersionEntity>>()

    override fun onCreate() {
        super.onCreate()
    }


    fun getData(mFilePath :String){
        KLog.i("文件路径=$mFilePath")
        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            println("${coroutineContext[CoroutineName].toString()} 处理异常：$throwable")
        }) {
            launch(Dispatchers.IO) {

                val data = mutableListOf<ProductsEntity>()
                val file = File(mFilePath)
                if (!file.exists()){
                    launch(Dispatchers.Main) {
                        showToast("文件不存在，请先下载数据")
                    }
                    return@launch
                }

                val files = file.listFiles()
                files.sortBy { file -> file.name }

                for (f in files){
                    if (!f.isDirectory){
                        continue
                    }
                    val versionFiles = f.listFiles() //版本文件
                    val productsEntity = ProductsEntity()
                    productsEntity.versionList = arrayListOf()
                    productsEntity.path = f.absolutePath

                    for (versionFile in versionFiles){
                        if (versionFile.isDirectory){
                            productsEntity.versionList.add(ItemVersionEntity(versionFile.name))
                        }else{
                            val fileName = versionFile.name.toLowerCase()
                            if (fileName.endsWith("pngex") || fileName.endsWith("png") ||fileName.endsWith("bmp")){
                                productsEntity.imagePath = versionFile.absolutePath
                            }
                        }
                    }
                    data.add(productsEntity)
                }
                launch(Dispatchers.Main) {
                    fileListLiveData.value = data
                }
            }

        }

    }

    //获取网络版本列表
    fun getVersionList(pe: ProductsEntity){
        val tempStringArray = pe.path.split("/")
        val childType = tempStringArray[tempStringArray.size-1]
        val type = tempStringArray[tempStringArray.size-2]

        addDisposable(DiagInteractor.versionsAll(type,getVCI(),childType).subscribe({

            it.removeAll(pe.versionList) //过滤本地版本
            versionListLiveData.value = it

        },{it.printStackTrace()}))
    }


    public interface ZipState{
        fun progress(progress :Int,isSuccess :Boolean){}
    }

    fun unzipFile(zipFilePath: String,unzipPath:String, zipState :ZipState){
        //1 获取文件
        //2 判断文件类型 zip 与 7z文件
        //3 解压
        val zipFile = File(zipFilePath)
        val zipType = Zip7pUtil.fileType(zipFile)
        if (zipType == 1){
            viewModelScope.launch(Dispatchers.IO){
                Zip7pUtil.unCompress(zipFile,File(unzipPath), Consumer {
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

            unZip(zipFile.absolutePath,unzipPath, Consumer {
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

    fun deleteVersionFile(path:String){
        println("删除文件路径=$path")
        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            println("${coroutineContext[CoroutineName].toString()} 处理异常：$throwable")
        }) {
            launch(Dispatchers.IO){
                FileUtils.deleteFileAndContents(File(path))
            }
        }
    }

}