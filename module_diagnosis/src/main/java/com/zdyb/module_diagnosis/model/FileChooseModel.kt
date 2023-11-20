package com.zdyb.module_diagnosis.model

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.bean.ProductsEntity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.jvm.internal.Intrinsics.Kotlin

class FileChooseModel:BaseViewModel() {


    var fileListLiveData = MutableLiveData<MutableList<File>>()
    var currentFilePath = MutableLiveData<String>()

    override fun onCreate() {
        super.onCreate()
    }


    fun getFileArray(mFilePath :String){
        KLog.i("文件路径=$mFilePath")
        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            println("${coroutineContext[CoroutineName].toString()} 处理异常：$throwable")
        }) {
            launch(Dispatchers.IO) {
                var file = File(mFilePath)
                if (!file.exists()){
                    //文件不存在则获取sd卡根目录 让用户自己去选择
                    file = File(PathManager.getBasePathNoSeparator())
                    currentFilePath.value = PathManager.getBasePathNoSeparator()
                }
                val fileArray = file.listFiles().toMutableList()
                launch(Dispatchers.Main) {
                    fileListLiveData.value = fileArray
                }

            }

        }

    }
}