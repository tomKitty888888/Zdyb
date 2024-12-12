package com.zdyb.lib_common.bluetooth


import android.bluetooth.BluetoothDevice
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ConvertUtils
import com.ble.Bluetooth
import com.ble.DeviceCallback
import com.ble.DiscoveryCallback
import com.inuker.bluetooth.library.BluetoothClient
import com.inuker.bluetooth.library.search.SearchRequest
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.KLog
import io.reactivex.functions.Consumer
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue


object BluetoothManagerNew {

    enum class BLEState{
        OPEN,CLOSE,CONNECT,CONNECT_ERROR,DISCONNECT,INIT
    }
    enum class BLESearchState{
        //开始，取消，暂停，发现
        START,CANCELED,STOP,FOUNDED
    }

    /**
     * 是否连接到设备
     */
    public var isConnectDevice = false

    private var mClient = BluetoothClient(BaseApplication.getInstance())

    private lateinit var bluetooth: Bluetooth
    //private var bluetooth: Bluetooth = Bluetooth.instance(BaseApplication.getInstance())

    private var loopDatas: Queue<ByteArray> = ConcurrentLinkedQueue()

    fun init():BluetoothManagerNew{
        bluetooth = Bluetooth.instance(BaseApplication.getInstance())
        bluetooth.onStart()
        println("蓝牙启动完毕")
        return this
    }

    fun setListener(consumer: Consumer<BLEState>) :BluetoothManagerNew{
        //bluetooth = Bluetooth.instance(BaseApplication.getInstance())
        bluetooth.setDiscoveryCallback(object : DiscoveryCallback {
            override fun onDiscoveryStarted() {
                println("开始扫描")
                //开始扫描
            }

            override fun onDiscoveryFinished() {
                println("结束扫描")
            }

            override fun onDeviceFound(device: BluetoothDevice?) {
//                if (ActivityCompat.checkSelfPermission(
//                        BaseApplication.getInstance(),
//                        Manifest.permission.BLUETOOTH_CONNECT
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    return
//                }
                println("onDeviceFound:" + device!!.name)
            }

            override fun onDevicePaired(device: BluetoothDevice?) {

//                if (ActivityCompat.checkSelfPermission(
//                        BaseApplication.getInstance(),
//                        Manifest.permission.BLUETOOTH_CONNECT
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    return
//                }
                println("onDevicePaired="+device!!.name)
            }

            override fun onDeviceUnpaired(device: BluetoothDevice?) {
            }

            override fun onError(message: String?) {
                println("onError=$message")
            }
        })
        bluetooth.setDeviceCallback(object : DeviceCallback {
            override fun onDeviceConnected(device: BluetoothDevice?) {
                println("设备已连接")
                consumer.accept(BLEState.CONNECT)
                isConnectDevice = true
            }

            override fun onDeviceDisconnected(device: BluetoothDevice?) {
                println("onDeviceDisconnected=")
                consumer.accept(BLEState.DISCONNECT)
                isConnectDevice = false
            }

            override fun onMessage(message: String?) {
                println("onMessage=$message")
            }

            override fun onConnecting() {
                println("连接中...")
            }

        })
        //bluetooth.onStart()

        consumer.accept(BLEState.INIT)
        return this
    }


    val bleDevicesData = MutableLiveData<MutableList<BluetoothDevice>>()
    val srBleDevicesData = MutableLiveData<MutableList<SearchResult>>()
    var searchResults = mutableListOf<SearchResult>()
    fun search(consumer: Consumer<MutableList<BluetoothDevice>>){

        val devices = bluetooth.pairedDevices
        consumer.accept(devices)
        bleDevicesData.value = devices
    }

    fun search2(consumer: Consumer<MutableList<SearchResult>>,searchProcess:Consumer<BLESearchState>){
        val request = SearchRequest.Builder()
            .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
            .build()
        searchResults.clear()
        mClient.search(request,object : SearchResponse {
            override fun onSearchStarted() {
                searchProcess.accept(BLESearchState.START)
            }

            override fun onDeviceFounded(device: SearchResult?) {
                device?.let {
                    if (it.name.length == 12){
                        val tag = it.name.substring(it.name.length-1,it.name.length)
                        //if (tag == "T" || tag == "9"){ //去掉样机与客机分别，只判断设备类型 79与86，时间：2023/9/15 // 增加69设备类型这种是1018的用的，时间：2023/10/20
                            val rules = it.name.substring(6,8)
                            if (rules == "79" || rules == "86"){
                                searchResults.add(device)
                                searchProcess.accept(BLESearchState.FOUNDED)
                            }
                        //}
                    }
                }
            }
            override fun onSearchStopped() {
                val temp = searchResults.distinct()
                consumer.accept(temp.toMutableList())
                srBleDevicesData.value = searchResults
                searchProcess.accept(BLESearchState.STOP)
            }
            override fun onSearchCanceled() {
                searchProcess.accept(BLESearchState.CANCELED)
            }
        })
    }

    fun connect(address:String){
        bluetooth.connectToAddress(address)
        //mController.connect(address)
    }

    fun disConnect(){
        println("主动断开连接")
    }

    fun send(data :ByteArray):Int{
        //purgeData()
        //mController.sendData(data)
        KLog.d("sendData:"+ ConvertUtils.bytes2HexString(data))
       return bluetooth.send(data,data.size)

    }

    fun stopScan(){

    }

    fun onDestroy() {

    }

    fun readData(retlen: Int): ByteArray? {

        return bluetooth.recvDataQuere(retlen)
    }

    fun timedReadsData(outTime:Long): ByteArray{
        val startTime = System.currentTimeMillis() //开始时间
        //做空耗时操作 结束后读取数据
        println("等待读取数据")
        //假如开始时间2023年 循环时的时间变成了 1970年那么陷入死循环
        while (System.currentTimeMillis() - startTime < outTime) {
            //println(Thread.currentThread().name)
        }
        println("开始读取数据")

        val data = bluetooth.recvData()
        if (data != null){
            KLog.d("recvData:${ConvertUtils.bytes2HexString(data)}")
            return data
        }

        return ByteArray(0)
    }

    fun purgeData(){
        //KLog.d("purgeData--清理数据队列")

        bluetooth.purgeQuere()
    }


    ///////////

    //判断蓝牙是否打开
    fun isBluetoothOpened():Boolean{
        return mClient.isBluetoothOpened
    }
    //打开蓝牙
    fun openBluetooth(){
        mClient.openBluetooth()
    }

    /**
     * 设备是否连接
     */
    fun isConnected():Boolean{
        return bluetooth.isConnected
    }


}