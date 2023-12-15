package com.zdyb.module_diagnosis.model

import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.bean.CartEntity
import com.zdyb.module_diagnosis.utils.AssetsZipUtils
import com.zdyb.module_diagnosis.utils.MyUtils
import com.zdyb.module_diagnosis.utils.Zip7pUtil
import io.reactivex.functions.Consumer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class HomeModel : BaseViewModel() {

    lateinit var rxPermissions : RxPermissions
    private val sort = arrayOf("CA1125","EQ1120","BJ1161","ZZ1167","ZZ2197","CTL161")
    val images = arrayOf(
        R.mipmap.icon_ca1125,
        R.mipmap.icon_eq1120,
        R.mipmap.icon_bj1161,
        R.mipmap.icon_zz1167,
        R.mipmap.icon_zz2197,
        R.mipmap.icon_ctl161)

    val cartLiveList = MutableLiveData<MutableList<CartEntity>>()

    lateinit var waitingDialog : ProgressDialog

    override fun onCreate() {
        super.onCreate()

        //申请权限
        //rxPermissions = RxPermissions(context as FragmentActivity)



//        rxPermissions.request(
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.READ_EXTERNAL_STORAGE
////            ,Manifest.permission.MANAGE_EXTERNAL_STORAGE
//        ).subscribe {
//            if (it){
//
//            }else{
//                println("权限不够")
//            }
//
//        }

        //检查Diagnosis文件是否存在
        val mainFile = File(PathManager.getBasePath()+"Diagnosis/cartType")

        if (mainFile.exists()){
            println("资源文件存在 直接加载")
            getCartData()
        }else{

            showToast("缺少资源文件")
            //showWaitingDialog()
            //copyFilesAssets(BaseApplication.getInstance(),"Diagnosis.7z",PathManager.getBasePrivatePath())
        }

    }

    private fun showWaitingDialog() {
        /* 等待Dialog具有屏蔽其他控件的交互能力
     * @setCancelable 为使屏幕不可点击，设置为不可取消(false)
     * 下载等事件完成后，主动调用函数关闭该Dialog
     */
        waitingDialog = ProgressDialog(context)
        waitingDialog.setTitle("正在准备资源文件")
        waitingDialog.setMessage("请稍后...")
        waitingDialog.setIndeterminate(true)
        waitingDialog.setCancelable(false)
        waitingDialog.show()
    }

    private fun copyFilesAssets(context: Context, assetsFileName: String, newPath: String){

        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            KLog.e("${coroutineContext[CoroutineName].toString()} 处理异常：$throwable")
            throwable.printStackTrace()
        }) {
            launch(Dispatchers.IO) {


                //将assets的资源文件拷贝的私有存储空间中 准备使用
                copyAssetsZipFile(context,assetsFileName,newPath, Consumer {
                    launch(Dispatchers.Main) {
                        waitingDialog.setMessage("文件拷贝中，请稍后... $it%")
                    }
                })

                var file = File("$newPath/$assetsFileName")
                if (file.exists()){
                    println("文件存在")
                }
                launch(Dispatchers.Main) {
                    waitingDialog.setMessage("文件解压中，请稍后...")
                }

                //将zip文件进行解压
//                MyUtils.zipUncompress(file.absolutePath,newPath, Consumer {
//                    launch(Dispatchers.Main) {
//                        waitingDialog.setMessage("$it")
//                    }
//                })

                //诊断的文件 使用7z文件解压，放在应用中体积更小，
                Zip7pUtil.unCompress(file,File("$newPath"), Consumer{
                    println("解压进度=$it")
                    launch(Dispatchers.Main) {
                        waitingDialog.setMessage("文件解压中，请稍后...($it%)")
                    }
                })

                //解压pdf文件，pdf的文件直接进行解压 不需要移动
                launch(Dispatchers.Main) {
                    waitingDialog.setMessage("pdf文件解压中，请稍后...")
                }
                AssetsZipUtils.UnZipAssetsFolder(BaseApplication.getInstance(),"pdf.zip",newPath)

                println("解压完毕")
                launch(Dispatchers.Main) {
                    waitingDialog.setMessage("解压完毕，正在加载")
                }
                launch(Dispatchers.IO) {
                    val data = getCartType()
                    launch(Dispatchers.Main) {
                        waitingDialog.setMessage("准备完毕")
                        waitingDialog.dismiss()
                        cartLiveList.value = data
                        println("准备完毕")
                    }
                }

            }
        }

    }

    /**
     * 将assets zip文件拷贝到项目私有空间
     */
    private fun copyAssetsZipFile(context: Context, assetsDirName: String, sdCardPath: String,consumer :Consumer<String> ){
        try {
            val inputStream = context.assets.open(assetsDirName)
            val mByte = ByteArray(1024*2)
            var bt = 0
            val file = File(sdCardPath + File.separator+ assetsDirName)
            println("拷贝的文件地址="+file.absolutePath)
            if (!file.exists()) {
                Log.w("TAG",file.absolutePath)
                file.createNewFile()
            } else {
                return
            }
            var tempCurr = 0
            val sum = inputStream.available()
            val fos = FileOutputStream(file)
            while ((inputStream.read(mByte).also { bt = it }) != -1) {
                fos.write(mByte, 0, bt)

                ++tempCurr
                val progress = (tempCurr * 2.048f / sum *100)
                val tempProgress = String.format("%.2f",progress*1000)
                consumer.accept(tempProgress)
                println("拷贝zip文件进度=$tempProgress")
            }
            fos.flush()
            inputStream.close()
            fos.close()
        }catch (e :Exception){
            e.printStackTrace()
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


    fun getCartData(){

        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            println("${coroutineContext[CoroutineName].toString()} 处理异常：$throwable")
        }) {
            launch(Dispatchers.IO) {

                val data = getCartType()

                launch(Dispatchers.Main) {
                    cartLiveList.value = data
                    println("获取完毕")
                }
            }

        }

    }
    private fun getCartType() :MutableList<CartEntity>{
        val mainFile = File(PathManager.getBasePath()+"Diagnosis/cartType")
        println("路径=${mainFile.absolutePath}")
        val cartList = mutableListOf<CartEntity>()
        if (!mainFile.exists()){
            showToast("资源文件缺失 ldjc/Diagnosis")
            return cartList
        }
        val tempFiles = mainFile.listFiles()
        if (tempFiles == null) {
            showToast("资源文件缺失 cartType")
            return cartList
        }
        if (tempFiles.isEmpty()){
            showToast("资源文件缺失 cartType")
            return cartList
        }

        //排个序
        tempFiles.sortBy { file -> file.name }


        for (file in tempFiles){
            val cartEntity = CartEntity()
            cartEntity.typeName = file.name
            cartEntity.childAction = mutableListOf()
            val childArray = mutableListOf<CartEntity.ChildAction>()

            if (file.isDirectory){
                val childFiles = file.listFiles()

                for (cf in childFiles){
                    println("获取到的文件---->${cf.name}")
                    if (cf.name == "rm"){
                        continue
                    }
                    if (cf.isDirectory){
                        //再下一层取版本文件
                        val childAction = CartEntity.ChildAction()
                        childAction.name = cf.name

                        val tempVersionFile = cf.listFiles()
                        if (tempVersionFile.isNotEmpty()){
                            val versionFile =  tempVersionFile[0]
                            childAction.versionName = versionFile.name
                            childAction.menuPath = versionFile.absolutePath+File.separator + "menu.txt"
                        }
                        childArray.add(childAction)
                    }else{
                        if (cf.name.endsWith(".png")){
                            cartEntity.typeImgPath = cf.absolutePath //类型图片地址
                        }else if (cf.name.endsWith(".pdf")){
                            cartEntity.instructionsPath = cf.absolutePath //pdf地址
                        }
                    }

                }
                childArray.add(CartEntity.ChildAction("维修保养手册指南","",""))
                childArray.add(CartEntity.ChildAction("辅助维修帮助系统","",""))
                cartEntity.childAction.addAll(childArray)
            }
            cartList.add(cartEntity)
        }

        for (item in cartList){
            println(item.typeName)
        }

        return cartList
    }

}