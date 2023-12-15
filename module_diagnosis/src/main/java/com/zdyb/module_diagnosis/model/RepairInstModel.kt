package com.zdyb.module_diagnosis.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.bean.RepairInstBean
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class RepairInstModel:BaseViewModel() {

    var dataList = MutableLiveData<MutableList<RepairInstBean>>()

    override fun onCreate() {
        super.onCreate()
    }


    fun getMenuList(cartType :String){


        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            KLog.e("${coroutineContext[CoroutineName].toString()} 处理异常：$throwable")
            throwable.printStackTrace()
        }) {
            launch(Dispatchers.IO) {
                val mainFile = File(PathManager.getBasePath()+"Diagnosis/cartType/${cartType}/rm")
                if (!mainFile.exists()){
                    return@launch
                }

                val data = mutableListOf<RepairInstBean>()

                val tempList = mainFile.listFiles()
                tempList?.sortBy { it.name }
                for (item in tempList!!){
                    val rib = RepairInstBean()
                    rib.menu = item.name
                    rib.helpPath = item.absolutePath+"/help.pdf"
                    rib.videoPath = item.absolutePath+"/device.mp4"
                    rib.errorCodePath = item.absolutePath+"/errorCode.pdf"
                    data.add(rib)
                }
                launch(Dispatchers.Main) {
                    dataList.value = data
                }
            }
        }

    }
}