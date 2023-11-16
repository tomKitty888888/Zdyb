package com.zdyb.module_diagnosis.model

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.base.KLog
import com.zdyb.module_diagnosis.bean.ProductsEntity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.jvm.internal.Intrinsics.Kotlin

class FileListModel:BaseViewModel() {


    var fileListLiveData = MutableLiveData<MutableList<ProductsEntity>>()

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
                            productsEntity.versionList.add(versionFile.name)
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
}