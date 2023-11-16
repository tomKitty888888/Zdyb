package com.zdeps.diag


import android.content.Context
import android.text.TextUtils
import com.zdeps.gui.ComJni
import com.zdyb.lib_common.base.KLog
import java.io.File
import java.lang.reflect.Field
import java.util.*

object DiagJni {

    private const val DEBUG = false
    var loadPathing = "" //加载中的so路径

    fun setLoadPath(path :String){
        this.loadPathing = path
        println("DiagJni进程ID="+ android.os.Process.myPid())
    }

    fun init():DiagJni{
        return this
    }

    fun load(){
        try {
            println("DiagJni进程ID="+ android.os.Process.myPid())
            println("loadPathing=$loadPathing")
            if (!DEBUG) {
                if (loadPathing.isNotEmpty()) {
                    System.load(loadPathing)
                    KLog.i("load完成")
                }else{
                    KLog.i("loadPathing = null")
                }
            } else {
                System.loadLibrary("diag-lib")
            }
        } catch (e: Exception) {
            KLog.e(e.message)
            e.printStackTrace()
        }
    }

    fun load(libName:String){
        try {
            if (!DEBUG) {
                if (libName.isNotEmpty()) {
                    System.load(libName)
                    loadPathing = libName
                }
            } else {
                System.loadLibrary("diag-lib")
            }
        } catch (e: Exception) {
            KLog.e(e.message)
            e.printStackTrace()
        }
    }


    fun unloadSo() { //context: Context, soName: String,soPath :String
        try {
            if (TextUtils.isEmpty(loadPathing)){
                KLog.i("未加载，无需unload")
                return
            }
            val field: Field = ClassLoader::class.java.getDeclaredField("loadedLibraryNames")
            field.isAccessible = true
            val libraries: Vector<String> =
                field.get(ClassLoader.getSystemClassLoader()) as Vector<String>
            //val soPath: String = getSoFile(context,soName).absolutePath
            val index: Int = libraries.indexOf(loadPathing) //soPath
            if (index != -1) {
                libraries.removeAt(index)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            KLog.d("卸载SO:${e.message}")
        }
    }

    private fun getSoFile(context: Context,soName: String): File {
        val dir: File = context.getDir("jniLibs", Context.MODE_PRIVATE)
        return File(dir, soName)
    }



    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun run(): Int
}