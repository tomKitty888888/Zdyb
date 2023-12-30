package com.zdyb.module_obd.model

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.TimeUtils
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zdeps.app.utils.ZipUtils
import com.zdeps.bean.OBDBean
import com.zdeps.obd.CommandUtils
import com.zdeps.obd.DiagAbs
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.bus.BusEvent
import com.zdyb.lib_common.bus.EventType
import com.zdyb.lib_common.bus.RxBus
import com.zdyb.lib_common.http.OKHttpManager
import com.zdyb.lib_common.utils.PreferencesUtils
import com.zdyb.lib_common.utils.SharePreferencesConstant
import com.zdyb.lib_common.utils.file.FileUtil
import com.zdyb.module_obd.DiagXmlUtils
import com.zdyb.module_obd.FilePathManager
import com.zdyb.module_obd.R
import com.zdyb.module_obd.dialog.DialogUPApp
import com.zdyb.module_obd.service.OBDInteractor
import io.reactivex.functions.Consumer
import kotlinx.coroutines.*
import java.io.File

class ObdFragmentModel:BaseViewModel() {

    val REQUEST_ENABLE_BT = 1
    public lateinit var rxPermissions: RxPermissions
    lateinit var abs : DiagAbs

    //车辆信息
    var cartInfo = MutableLiveData<MutableList<OBDBean.ObdData>>()
    //obd检测信息
    var obdInfo = MutableLiveData<MutableList<OBDBean.ObdData>>()
    //排放相关数据流
    var emissionInfo = MutableLiveData<MutableList<OBDBean.ObdData>>()

    var vciCode = MutableLiveData<String>()
    /**
     * obd 数据组装
     */
    var obdTempList = mutableListOf<OBDBean.ObdData>()

    /**
     * 故障数据
     */
    val failureData = mutableListOf<OBDBean.ObdData>()

    /**
     * 就绪状态
     */
    val readinessStatus = mutableListOf<OBDBean.ObdData>()

    var failureSum = 0
    var readinessStatusSum = 0

    val KEY_CURRENT = "Current" //故障数量key
    val KEY_READINESSSTATUS = "ReadinessStatus" //就绪状态的key

    /**
     * 持续进行实时检测
     */
    var startGetRTData = true

    /**
     * 下载文件进度100 拷贝so文件到jinLibs 20 总进度120
     */
    var loadObdFileProgress = MutableLiveData<Int>()


    override fun onCreate() {
        super.onCreate()
        rxPermissions = RxPermissions(context as FragmentActivity)
        //openPermissions()
        abs = DiagAbs.getInstance()
    }


    fun openPermissions(consumer: Consumer<Boolean>){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            addDisposable(rxPermissions.request(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ).subscribe {
                openBle()
                consumer.accept(it)
            })
        }else{
            addDisposable(rxPermissions.request(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ).subscribe {
                openBle()
                consumer.accept(it)
            })
        }
    }

    private fun openBle(){
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            showToast("Device doesn't support Bluetooth")
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

    }

    var isInitOBD = false

    fun initOBD():Boolean{
        if (abs.commInit() == -1) {
            showToast(context!!.getString(R.string.obd_init_error))
            println("初始化失败")
            return false
        }
        isInitOBD = true
        return true
    }

    fun obdInfoList(type: Int){
        //1取vci连接状态
        //2取软件版本
        //3开始OBD检测
        //4取车辆信息
        //15取故障灯状态
        //11取故障里程
        //6取故障和故障代码
        //7取就绪状态未完成项目
        //9取实时数据

        failureSum = 0
        readinessStatusSum = 0
        OBDResultData.failureSum = 0
        OBDResultData.readinessStatusSum = 0
        OBDResultData.lampState = 0
        cmd(abs,type)
    }

    fun readVci(type: Int){
        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            println("${coroutineContext[CoroutineName].toString()} 处理异常：$throwable")
        }) {
            launch(Dispatchers.IO){
                withContext(Dispatchers.IO) {
                    Thread.sleep(200)
                    //println("延时")
                }
                abs.DiagRequestByXMLString(DiagXmlUtils.createCommDom(CommandUtils.numToCommand(type)), Consumer {
                    if (!isOk(it)){
                        return@Consumer
                    }
                    when(type){
                        1 -> {
                            println("vci连接成功")
                            readVci(100)
                        }
                        100 ->{
                            println("读取VCI：${it.msg}")
                            launch(Dispatchers.Main) {
                                vciCode.value = it.getData()[0].value.toString()
                            }
                            readVci(2)
                        }
                        2 -> {
                            println("获取软件版本成功：${it.data[0].value}")
                            val code = it.data[0].value.toString()
                            PreferencesUtils.putString(context, SharePreferencesConstant.BOTTOM_DEVICE_CODE,code)
                        }

                    }
                })
            }
        }
    }

    /**
     * 重复读取实时数据
     */
    fun readGetRTData(type: Int){
        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            println("${coroutineContext[CoroutineName].toString()} 处理异常：$throwable")
        }) {
            launch(Dispatchers.IO){
                withContext(Dispatchers.IO) {
                    Thread.sleep(1000)
                    //println("延时")
                }
                abs.DiagRequestByXMLString(DiagXmlUtils.createCommDom(CommandUtils.numToCommand(type)), Consumer {

                    when(type){
                        9 ->{
                            if (!isOk(it)){
                                return@Consumer
                            }
                            println("读取实时数据：${it.msg}")
                            launch(Dispatchers.Main) {
                                emissionInfo.value = it.getData()
                                OBDResultData.emissionInfo = it.getData()
                            }
                            if (startGetRTData){
                                readGetRTData( 9)
                            }

                        }
                        16 ->{
                            println("停止检测：${it.msg}")
                        }
                    }
                })
            }
        }
    }

    private fun cmd(abs: DiagAbs, type: Int){

        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            println("${coroutineContext[CoroutineName].toString()} 处理异常：$throwable")
        }) {
            launch(Dispatchers.IO){
                withContext(Dispatchers.IO) {
                    Thread.sleep(200)
                    //println("延时")
                }
                abs.DiagRequestByXMLString(DiagXmlUtils.createCommDom(CommandUtils.numToCommand(type)), Consumer {
                    if (!isOk(it)){
                        return@Consumer
                    }
                    // println("获取软件版本成功：${it.data[0].value}")
                    when(type){
                        1 ->{
                            println("vci连接成功")
                            cmd(abs, 2)
                        }
                        2 ->{
                            println("获取软件版本成功：${it.data[0].value}")
                            cmd(abs, 3)
                        }
                        3 ->{
                            println("OBD检测：${it.msg}")
                            cmd(abs, 4)
                        }
                        4 ->{
                            println("取车辆信息：${it.msg}")
                            //记录车架号
                            val cartInfoList = mutableListOf<OBDBean.ObdData>()
                            cartInfoList.add(OBDBean.ObdData("",TimeUtils.getNowString(),context!!.getString(R.string.obd_key_obdTime),""))
                            cartInfoList.addAll(it.getData())
                            launch(Dispatchers.Main) {
                                cartInfo.value = cartInfoList
                                OBDResultData.cartInfo = cartInfoList
                            }

                            cmd(abs, 15)
                        }
                        15 ->{
                            println("取故障灯状态：${it.msg}")
                            obdTempList.clear()
                            if (it.getData().isNotEmpty()){
                                it.getData()[0].contextType = 1
                                obdTempList.add(it.getData()[0])
                                //记录故障灯状态
                                OBDResultData.lampState = it.getData()[0].value.toString().toInt()
                            }
                            cmd(abs, 11)
                        }
                        11 ->{
                            println("取故障里程：${it.msg}")
                            if (it.getData().isNotEmpty()){
                                obdTempList.add(it.getData()[0])
                            }
                            cmd(abs, 6)
                        }
                        6 ->{
                            println("取故障和故障代码：${it.msg}")
                            failureData.clear()
                            OBDResultData.failureData.clear()
                            if (it.getData().isNotEmpty()){
                                failureData.addAll(it.getData())
                                OBDResultData.failureData.addAll(it.getData())
                                for (item in it.getData()){
                                    if (item.key == KEY_CURRENT){
                                        //记录故障码数量
                                        if (item.value != null){
                                            failureSum = item.value.toString().toInt()
                                            OBDResultData.failureSum = failureSum
                                        }
                                        item.isError = failureSum >= 1 //当前故障数量>=1显示红色， 就绪大于2显示红色
                                        item.chevron = true //可下一步
                                        obdTempList.add(item)
                                        break
                                    }
                                }
                            }
                            cmd(abs, 7)
                        }
                        7 ->{
                            println("7取就绪状态未完成项目：${it.msg}")
                            readinessStatus.clear()
                            OBDResultData.readinessStatus.clear()
                            if (it.getData().isNotEmpty()){
                                readinessStatus.addAll(it.getData())
                                OBDResultData.readinessStatus.addAll(it.getData())

                                var sum = 0 //记录未完成的总数
                                for (item in it.getData()){
                                    if (item.value.toString() == "2"){
                                        sum+=1
                                    }
                                }
                                readinessStatusSum = sum //记录故障码数量
                                OBDResultData.readinessStatusSum = sum
                                val isError = OBDResultData.readinessStatusSum > 2
                                obdTempList.add(OBDBean.ObdData(KEY_READINESSSTATUS,sum.toString(),context!!.getString(R.string.obd_key_ReadinessStatus),"",true,isError))
                            }
                            launch(Dispatchers.Main) {
                                obdInfo.value = obdTempList
                                OBDResultData.obdInfo = obdTempList
                            }
                            //cmd(abs, 9)
                        }
                        9 ->{
                            println("取实时数据：${it.msg}")
                            launch(Dispatchers.Main) {
                                emissionInfo.value = it.getData()
                                OBDResultData.emissionInfo = it.getData()
                            }
                        }

                    }
                })
            }
        }

    }


    private fun isOk(code:OBDBean):Boolean{
        when(code.code){
            "0" -> {


                return true
            }
            "1" -> {
                //val errMsg = "cmd=${code.cmd},"+context!!.getString(R.string.obd_code_error)
                val errMsg = "cmd=${code.cmd},${code.msg} \n请重试"
                KLog.e(errMsg)
                //showToast(errMsg)
                RxBus.getDefault().post(BusEvent(EventType.CMD_ERR,code))
                return false
            }
            "5" -> {
                val errMsg = "cmd=${code.cmd},"+context!!.getString(R.string.obd_comm_error)+code.msg +"\n ${context!!.getString(R.string.obd_comm_error_5)}"
                KLog.e(errMsg)
                //showToast(errMsg)
                RxBus.getDefault().post(BusEvent(EventType.CMD_ERR,code))
                return false
            }
            else ->{
                val errMsg = "cmd=${code.cmd},"+context!!.getString(R.string.obd_comm_error)+"code=${code.code},msg="+code.msg
                KLog.e(errMsg)
                RxBus.getDefault().post(BusEvent(EventType.CMD_ERR,code))
                return false
            }
        }
    }




    public fun loadSoFile(context: Context) {
        //直接将so下载至 App的目录下
        val dir = context.getDir("jniLibs", Context.MODE_PRIVATE);
        if (!isLoadSoFile(dir)) {
            //copy(formPath, dir.absolutePath)
        }

    }
    public fun isLoadSoFile(dir :File):Boolean{
        val currentFiles = dir.listFiles()
        var hasJkffmpeg = false
        if (currentFiles == null) {
            return false
        }
        println("jniLibs文件个数="+currentFiles.size)

        for (f in currentFiles){
            if (f.isDirectory){
                println("jinLibs的目录下文件夹="+f.name)
                val fs = f.listFiles()
                for (f1 in fs){
                    println("${f.name}目录下文件="+f1.name)
                }
            }else{
                println("jinLibs目录下文件="+f.name)
            }
        }
        return hasJkffmpeg;
    }

    /**
     * 下载最新的诊断文件 然后记录
     */
    fun loadObdFile(loadObdFileProgress: Consumer<Int>){
        addDisposable(OBDInteractor.getMotorcycleType(getVCI()).subscribe({

            println(it[0].patch_url)
            val soCode = it[0].versions
            val loadCode = PreferencesUtils.getString(context, SharePreferencesConstant.OBD_SO_CODE,"")
            //比较已经下载的版本与当前的版本大小 V1.001
            if (toCode(soCode) <= toCode(loadCode)){
                RxBus.getDefault().post(BusEvent(EventType.UP_OBD_ERR)) //关闭进度条窗口
                showToast(context!!.getString(R.string.is_latest_version))
                return@subscribe
            }

            PreferencesUtils.putString(context, SharePreferencesConstant.OBD_SO_CODE,soCode)

            val filePath = FilePathManager.getDownloadOBD7zPath()
            println("filePath=$filePath")
            val unZipPath =  FilePathManager.getUnOBDZipPath()
            val unFile = File(unZipPath)
            if (unFile.exists()) {
                FileUtil.deleteFolderFile(unZipPath,true)
                println("删除历史文件")
            }

            if (unFile.mkdirs()){
                println("目录创建成功")
            }else{
                println("目录创建失败")
            }
            if (unFile.isDirectory){
                println("目录存在")
            }else{
                println("目录不存在")
            }

            addDisposable(OKHttpManager.getInstance().downloadFile(it[0].patch_url,filePath, Consumer {
                println("下载进度=$it")
                loadObdFileProgress.accept(it)
            }).subscribe({
                println("下载结束 进行解压")
                val file = File(filePath)
                unZip(file.absolutePath,unZipPath, Consumer {
                    if (it){
                        //解压结束 拷贝到jniLibs
                        copySoToJNILibs()
                        loadObdFileProgress.accept(120)
                    }else{
                        //解压失败
                        KLog.e("解压7z失败")
                        RxBus.getDefault().post(BusEvent(EventType.UP_OBD_ERR))
                        showToast("解压失败")
                    }
                })
            },{
                it.printStackTrace()
                RxBus.getDefault().post(BusEvent(EventType.UP_OBD_ERR))
            }))

        },{
            it.printStackTrace()
            RxBus.getDefault().post(BusEvent(EventType.UP_OBD_ERR))
        }))
    }

    /**
     * 转换版本号
     */
    private fun toCode(code:String):Int{
        if(TextUtils.isEmpty(code)){
            return 0
        }
        val temp = code.replace("V","").replace("v","").replace(".","")
        return temp.toInt()
    }

    /**
     * 拷贝so文件到应用的jnilibs文件中
     */
    private fun copySoToJNILibs(){
        val unZipPath =  FilePathManager.getUnOBDZipPath()
        val file = File(unZipPath)
        val obdFiles = file.listFiles()
        if (obdFiles.isEmpty()){
            KLog.e("$unZipPath   诊断文件为空")
            RxBus.getDefault().post(BusEvent(EventType.UP_OBD_ERR))
            showToast("请下载诊断文件后重试")
            return
        }

        ///data/user/0/com.zdyb.app/files/diagnostic/obd/obd/armeabi-v7a/libAndroidOBDDiagnoseBase.so
        ///data/user/0/com.zdyb.app/files/diagnostic/obd/obd/armeabi-v7a/libOBDDiagnosis.so

        val obdSo = File(FilePathManager.getOBDSoPath())
        if (!obdSo.exists()){
            KLog.e("so文件为空")
            RxBus.getDefault().post(BusEvent(EventType.UP_OBD_ERR))
            showToast("请重新下载诊断文件后重试")
            return
        }
        val dir = context!!.getDir("jniLibs", Context.MODE_PRIVATE)
        KLog.i("开始拷贝so文件到jinLibs")
        FileUtil.copy(obdSo.absolutePath,dir.absolutePath)
        KLog.i("拷贝结束")
    }

    /**
     * 删除文件下的全部内容
     */
    private fun deleteFile(dir: File){
        if (!dir.exists() || !dir.isDirectory)
            return;
        for (file in dir.listFiles()) {
            if (file.isFile)
                file.delete(); // 删除所有文件
            else if (file.isDirectory)
                deleteFile(file); // 递规的方式删除文件夹
        }
        //dir.delete();// 删除目录本身
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
     * 检测app升级
     */
    fun upApp(supportFragmentManager : FragmentManager,consumer: Consumer<Boolean>){
        val vci = getVCI()
        addDisposable(OBDInteractor.appUpdate(vci).subscribe({
            if (TextUtils.isEmpty(it.versions)){
                println("versions=null 没有最新版本")
                consumer.accept(false)
            }else{
                //V1.0.0
                if (it.versions.toInt() > AppUtils.getAppVersionCode()){
                    consumer.accept(true)
                    DialogUPApp().setBox(context!!.getString(R.string.app_up_hint),it.notes,false)
                        .setResult(
                            Consumer {
                                val intent = Intent()
                                intent.action = "android.intent.action.VIEW"
                                intent.data = Uri.parse("https://zdyban.zdeps.com/index.php/DownloadApk/inspectAnnually?vci=${vci}")
                                context?.startActivity(intent)
                            }).show(supportFragmentManager,"DialogUPApp")
                }
            }

        },{it.printStackTrace()}))
    }

}