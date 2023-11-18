package com.zdyb.module_diagnosis.model

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.utils.PathManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class HomeModel : BaseViewModel() {

    lateinit var rxPermissions : RxPermissions
    override fun onCreate() {
        super.onCreate()

        //申请权限
        rxPermissions = RxPermissions(context as FragmentActivity)


        rxPermissions.request(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
            //,Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
        ).subscribe {
            if (it){
                copyFilesAssets(BaseApplication.getInstance(),"Diagnosis",PathManager.getBasePathNoSep())
            }else{
                println("权限不够")
            }

        }


    }

    private fun copyFilesAssets(context: Context, oldPath: String, newPath: String){
        println("开始拷贝")
        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            println("${coroutineContext[CoroutineName].toString()} 处理异常：$throwable")
        }) {
            launch(Dispatchers.IO) {

                copyAssetsDirToSDCard(context,oldPath,newPath)

                launch(Dispatchers.Main) {
                    showToast("拷贝完毕")
                    println("拷贝完毕")
                }
            }

        }

    }


    fun copyAssetsDirToSDCard(context: Context, assetsDirName: String, sdCardPath: String) {
        var sdCardPath = sdCardPath
        Log.d(
            "TAG",
            "copyAssetsDirToSDCard() called with: context = [$context], assetsDirName = [$assetsDirName], sdCardPath = [$sdCardPath]"
        )
        try {
            val list = context.assets.list(assetsDirName)
            if (list!!.isEmpty()) {
                val inputStream = context.assets.open(assetsDirName)
                val mByte = ByteArray(1024*2)
                var bt = 0
                val file = File(
                    sdCardPath + File.separator
                            + assetsDirName.substring(assetsDirName.lastIndexOf('/'))
                )
                if (!file.exists()) {
                    Log.w("TAG",file.absolutePath)
                    file.createNewFile()
                } else {
                    return
                }
                val fos = FileOutputStream(file)
                while ((inputStream.read(mByte).also { bt = it }) != -1) {
                    fos.write(mByte, 0, bt)
                }
                fos.flush()
                inputStream.close()
                fos.close()
            } else {
                var subDirName = assetsDirName
                if (assetsDirName.contains("/")) {
                    subDirName = assetsDirName.substring(assetsDirName.lastIndexOf('/') + 1)
                }
                sdCardPath = sdCardPath + File.separator + subDirName
                val file = File(sdCardPath)
                if (!file.exists()) file.mkdirs()
                for (s: String in list) {
                    copyAssetsDirToSDCard(context, assetsDirName + File.separator + s, sdCardPath)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    //
}