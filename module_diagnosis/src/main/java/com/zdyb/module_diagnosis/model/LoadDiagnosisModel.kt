package com.zdyb.module_diagnosis.model

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.SystemClock
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.zdeps.gui.ConnDevices
import com.zdyb.ITaskBinder
import com.zdyb.ITaskCallback
import com.zdyb.app.Transfer
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.bean.*
import com.zdyb.module_diagnosis.service.DiagnosisService
import io.reactivex.functions.Consumer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.nio.channels.FileChannel

/**
 * 加载诊断的model
 */
class LoadDiagnosisModel :BaseViewModel(){

    var isScan = true
    val deviceList = MutableLiveData<List<DeviceEntity>>() //识别到的设备
    var menuListLiveData = MutableLiveData<Set<String>>() //菜单数据
    val titleLiveData = MutableLiveData<String>() //记录菜单项 当作标题
    //ver
    val verData = mutableListOf<KVEntity>() //ver数据
    val verLiveData = MutableLiveData<List<KVEntity>>()
    //dtc
    val dtcData = mutableListOf<DtcEntity>() //dtc数据
    val dtcLiveData = MutableLiveData<List<DtcEntity>>() //dtc数据
    //cds-Select
    val cdsSelectData = mutableListOf<CDSSelectEntity>() //cdsSelect数据
    val cdsSelectAllLiveData = MutableLiveData<MutableList<CDSSelectEntity>>() //cdsSelect全部数据
    //cds-查询的数据流
    val cdsSelectLiveData = MutableLiveData<MutableList<CDSSelectEntity>>()
    //act

    val actLiveData = MutableLiveData<Set<ACTEntity>>() //act数据
    val actButtonData = mutableListOf<String>()
    val actButtonLiveData = MutableLiveData<MutableList<String>>()//act 按钮
    var actHint = StringBuffer()


    var mITaskBinder : ITaskBinder? = null
    val disServerConnect = MutableLiveData<Boolean>()//诊断服务是否连接

    var consumer : Consumer<Boolean>? = null

    //本地菜单的数据存储
    val mLocalMenuLiveData = MutableLiveData<MutableList<DieseData>>() //全部的数据
    val mLocalGradleList = MutableLiveData<HashMap<Int, List<DieseData>>>() //用于返回事件处理的数据存储
    var mLocalPage = 0 //当前页面
    var mLocalChildIndex = 0 //当前第几个菜单

    override fun onCreate() {
        super.onCreate()
        //bingDiagnosisService()
    }

    //重新绑定服务 先解绑之前的
    fun anewDiagnosisService(consumer : Consumer<Boolean>){
        this.consumer = consumer
        mITaskBinder?.let { android.os.Process.killProcess(it.processPid) }
        bingDiagnosisService()
    }

    fun bingDiagnosisService(){
        val intent = Intent(context, DiagnosisService::class.java)
        intent.action = "android.intent.action.zd.DiagnosisService"
        BaseApplication.getInstance().bindService(intent,mConnection,Context.BIND_AUTO_CREATE)
    }
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mITaskBinder = ITaskBinder.Stub.asInterface(service)
            mITaskBinder?.registerTransferCallback(mTransfer)
            disServerConnect.value = true
            LogUtils.i("诊断服务绑定成功")
            consumer?.accept(true)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            LogUtils.i("诊断服务断开--重新绑定")
            disServerConnect.value = false
            if (BaseApplication.getInstance().outDiagnosisService){
                return
            }
            bingDiagnosisService()
        }
    }

    var mTransfer = object :Transfer.Stub() {
        override fun sendData(dat: ByteArray?, len: Int): Int {
            if (dat != null) {
                //println("----mTransfer----->sendData=${android.os.Process.myPid()}")
                return ConnDevices.sendData(dat)
            }
            return 0
        }

        override fun recvData(retlen: Int): ByteArray {
            val data = ConnDevices.readData(retlen)
            if (data != null){
                return data
            }
            return ByteArray(0)
        }

        override fun purge() {
            KLog.i("回到App进程id=${android.os.Process.myPid()}")
            ConnDevices.purge()
        }

        override fun setBaudRate(baudRate: Int): Boolean {

            return true
        }

        override fun IsWiredOrBluetooth(): Int {

            return 0
        }

        override fun readFrame(TimeOut: Int): ByteArray {
            return ByteArray(0)
        }

    }

    fun test(){
        if (!ConnDevices.isConnect()){
            return
        }
        //A5A50008FC01000216BC337F55
        ConnDevices.sendData(byteArrayOf(
            0xa5.toByte(), 0xa5.toByte(),
            0x00, 0x08,
            0xFC.toByte(), 0x01,
            0x00,0x02,
            0x16,0xBC.toByte(),
            0x33,0x7F,
            0x55))

    }

    //开始扫描
    fun startScan(){

        if (!ConnDevices.isConnect()){
            return
        }
        //ConnDevices.sendData(byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x01, 0xD1.toByte(), 0x55))//暂停扫描
        val closeFlow =
            byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x02, 0x64.toByte(), 0x00, 0x55) //关闭流控
        if (ConnDevices.sendData(closeFlow) > 0){
            val result = ConnDevices.outTimeReadData(50)//读取掉接收缓存区
            ConnDevices.purge()
            isScan = true
            var scanSum = 0
            while (isScan){
                aiScan()
                scanSum++
                if (scanSum ==5){

                }else if (scanSum >=10){
                    KLog.e("扫描10次未能成功 停止扫描")
                    break
                }
            }
            println("扫描任务结束")
        }

    }

    private fun aiScan(){
        val setVciBaudRate = byteArrayOf(0xA5.toByte(), 0xA5.toByte(), 0x00, 0x02, 0x70.toByte(), 0x00, 0x55) //扫描指令
        ConnDevices.sendData(setVciBaudRate)
        val result = ConnDevices.outTimeReadData(50)

        if (result == null){
            println("未读取到数据")
            showToast("未读取到数据")
            return
        }
        println("读取数据长度:${result.size}")
        if (result.isNotEmpty() &&result.size < 7){
            showToast("读取到的数据长度错误：${ConvertUtils.bytes2HexString(result)}")
            return
        }
        if (result.isEmpty()){
            return
        }
        result.apply {

            val data = ConvertUtils.bytes2HexString(result)
            println("data=$data")
            val head = data.substring(0,4)
            val state = data.substring(8,12)

            if (head != "A5A5"){
                showToast("命令头效验错误")
                return
            }
            when(state){
                "7000" -> {
                    println("识别中...")
                }
                "7001" -> {
                    println("识别失败,继续识别...")
                    KLog.e("识别失败,继续识别... 发送扫描复位指令")
                    val setVciBaudRate = byteArrayOf(0xA5.toByte(), 0xA5.toByte(), 0x00, 0x02, 0x72.toByte(), 0x00, 0x55) //重新扫描指令
                    ConnDevices.sendData(setVciBaudRate)
                    val result = ConnDevices.outTimeReadData(3000)//读取掉接收缓存区
                }
                "7002" -> {
                    println("识别成功")
                    if (data.length>=24){
                        isScan = false
                        parseID(data)
                    }

                }
                else ->{
                    println("识别失败${state}")
                    isScan = false

                }
            }
            println("解析结束--->扫描状态=$isScan")
        }

    }

    private fun parseID(data: String){
        //解析ID A5A5 00时间 08类型 70 020101 00 00 00 6355
        //var parseIndex = 2
        //  A5A500 08 7002 01010000006355
        //val sum = data.substring(4,6) //总条数
        //val carType = data.substring(6,8) //汽车类型
        val cardId = data.substring(16,24)
        val cartIds = arrayListOf(cardId)

        //文件夹匹配车辆的信息
        matchingCar(cartIds)
    }

    /**
     * 设备id匹配文件路径
     */
    private fun matchingCar(cartIds:ArrayList<String>){

        //读取文件流
        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            println("${coroutineContext[CoroutineName].toString()} 处理异常：$throwable")
        }) {

            val devices = arrayListOf<DeviceEntity>()

            launch(Dispatchers.IO) {
                var buff: BufferedReader? = null
                ///data/user/0/com.zdyb.app/cache/config/TaskID.ini
                val file = File(context!!.cacheDir.absolutePath + "/config/TaskID.ini")
                if (file.exists()) {
                    val open = FileInputStream(file)
                    buff = BufferedReader(InputStreamReader(FileInputStream(file)))
                    buff.mark(open.available() - 1)
                } else {
                    val open = context!!.resources.assets.open("TaskID.ini")
                    buff = BufferedReader(InputStreamReader(open))
                    buff.mark(open.available() - 1)
                }

                for (id in cartIds){
                    val format = "0x$id"
                    while (buff.ready()){
                        val lineValue = buff.readLine()
                        if (lineValue.contains(format)){
                            val linedata: Array<String> =
                                lineValue.split("=".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            val split = linedata[1]
                            val index = split.lastIndexOf("\\")
                            val id = linedata[0]
                            val path = split.substring(0,index)
                            val name = split.substring(index+1)

                            println("id=${id}")  //1282
                            val tempStringId = id.replace("0x","")
                            val tempId: Long = tempStringId.toLong(16)
                            println("tempId=${tempId}")
                            println("path=${path}")
                            println("name=${name}")
                            //val soSdPath = String.format("%s/diag/%s/libdiag-lib.so", path, "armeabi-v7a")

                            devices.add(DeviceEntity(tempId,path, name))
                        }
                    }
                }
                launch(Dispatchers.Main) {
                    deviceList.value = devices
                }
            }

        }
    }

    /**
     * 获取最高版本的文件地址
     */
    fun getHighVersionSo(dev: DeviceEntity): String {
        if (TextUtils.isEmpty(dev.path)) {
            showToast("配置文件未匹配到此项路径：id=${dev.id},${dev.name},${dev.path}")
            KLog.e("配置文件未匹配到此项路径：id=${dev.id},${dev.name},${dev.path}")
            return ""
        }
        val path = PathManager.getBasePath()+dev.path.replace("\\", "/")
        //  /storage/emulated/0/zdeps/Diagnosis\Electronic\01Bosch
        KLog.i("文件路径=${path}")
        //获取所有目录
        val folder = File(path)
        if (!folder.exists()) {
            //后续改为自动下载 自动进行后续操作
            KLog.e("文件为空=${folder}")
            showToast("诊断文件为空，请先下载文件,检查路径")
            return ""
        }
        val files = folder.listFiles { pathname -> pathname.isDirectory }
        if (files == null || files.isEmpty()) {
            showToast("文件为空，请先下载文件")
            return ""
        }

        //获取版本最高的
        var toplevel = files[0]

        for (f in files) {
            val nowVersion = toplevel.name.replace("V|v|\\.".toRegex(), "").toInt()
            val nextVersion = f.name.replace("V|v|\\.".toRegex(), "").toInt()
            if (nextVersion > nowVersion) {
                toplevel = f
            }
        }
        //注意末尾加上斜杠
        return toplevel.absolutePath + File.separator
    }

    lateinit var mLoadAbsolutePath :String
    /**
     * 查找文件 动态加载so启动诊断
     */
    fun openDiagnosis(path:String,context :Consumer<Boolean>){
        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            println("${coroutineContext[CoroutineName].toString()} 处理异常：$throwable")
        }) {
            launch(Dispatchers.IO) {

                //val path0 = String.format("%s/libdiag-lib.so", path)
                //val path = String.format("%s/diag/%s/libdiag-lib.so", path, "armeabi-v7a")
                val allPath = path +"diag/armeabi-v7a/libdiag-lib.so"
                KLog.i("需要加载的path=$allPath")
                //将sd卡的so拷贝到程序私有空间，并存储该路径
                mLoadAbsolutePath = getLoadName(allPath)
                if (TextUtils.isEmpty(mLoadAbsolutePath)){
                    showToast("诊断文件拷贝失败")
                    KLog.e("loadName -- isEmpty")
                    return@launch
                }
                KLog.i("loadName=$mLoadAbsolutePath")
                val file = File(mLoadAbsolutePath)
                if (file.exists()){
                    KLog.i("SO文件存在")
                }
                launch(Dispatchers.Main) {
                    context.accept(true)
                }
            }

        }


    }

    /**
     * 创建私有空间的so文件，进行拷贝
     */
    private fun getLoadName(path: String): String {
        val srcFile = File(path)
        if (!srcFile.exists()) return ""
        val dir: File = context!!.getDir("jniLibs", Context.MODE_PRIVATE)
        val desFile = File(dir.absolutePath + File.separator + "libdiag-lib.so")



        try {
            if (desFile.exists()) {
                desFile.delete()
                KLog.i("删除之前的文件")
            }
            copyFileUsingFileChannels(srcFile, desFile)
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
        return desFile.absolutePath
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

    /**
     * 启动诊断
     */
    fun startDiagnosis(id:Long,iTaskCallback: ITaskCallback,versionPath: String){

        try {
            mITaskBinder?.let {
                println("原本id=${id}")
                println("versionPath=$versionPath")
                mITaskBinder?.registerCallback(iTaskCallback)
                mITaskBinder?.run(mLoadAbsolutePath,versionPath)
                SystemClock.sleep(1000)
                mITaskBinder?.setTaskID(id)
            }

        }catch (e :Exception){
            e.printStackTrace()
        }
    }

    /**
     * 删除掉监听防止串数据
     */
    fun removeCallback(){
        mITaskBinder?.unregisterCallback()

    }

    fun registerCallback(iTaskCallback: ITaskCallback){
        mITaskBinder?.registerCallback(iTaskCallback)
    }


    /**
     * 偏移量固定为6
     */
    fun setDigValue(data: Byte){
        mITaskBinder?.setMenuValue(data)
    }
    fun setCommonValue(offset: Int,data: Byte){
        mITaskBinder?.setCommonValue(offset,data)
    }
    fun setCommonValueToArray(offset: Int,data: String){
        mITaskBinder?.setCommonValueToArray(offset,data)
    }

    override fun onDestroy() {
        KLog.e("杀掉进程？------------->android.os.Process.killProcess(mITaskBinder.processPid)")

        try {
            closeData()
            mITaskBinder?.let { android.os.Process.killProcess(it.processPid) }
            mITaskBinder = null
            BaseApplication.getInstance().unbindService(mConnection)
        }catch (e :Exception){

        }
        super.onDestroy()
    }



    private fun closeData(){
        deviceList.value = mutableListOf()
        menuListLiveData.value = mutableSetOf()
        titleLiveData.value = ""
        verData.clear()
        verLiveData.value = mutableListOf()
        dtcData.clear()
        dtcLiveData.value = mutableListOf()
        cdsSelectData.clear()
        cdsSelectAllLiveData.value = mutableListOf()
        cdsSelectLiveData.value = mutableListOf()

        actLiveData.value = mutableSetOf()
        actButtonData.clear()
        actButtonLiveData.value = mutableListOf()
        actHint = StringBuffer()

    }
}