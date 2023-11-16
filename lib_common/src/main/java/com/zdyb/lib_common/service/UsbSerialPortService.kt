package com.zdyb.lib_common.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.*
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.zdyb.lib_common.bus.BusEvent
import com.zdyb.lib_common.receiver.USBReceiver
import com.zdyb.lib_common.receiver.USBReceiver.CallBack
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.LinkedBlockingQueue

class UsbSerialPortService : BaseService(){

    var manager: UsbManager? = null
    var serialPort: UsbSerialPort? = null
    var driver: UsbSerialDriver? = null
    var ACTION_DEVICE_PERMISSION = "ACTION_DEVICE_PERMISSION"
    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

    private var context: Context? = null
    private var mUsbReceiver: USBReceiver? = null
    //private var loopDatas: Queue<Byte> = ConcurrentLinkedQueue()
    private var loopDatas: BlockingQueue<Byte> = LinkedBlockingQueue<Byte>()

    private fun registerReceiver() {
        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        mUsbReceiver = USBReceiver()
        mUsbReceiver!!.setListener(object : CallBack {
            override fun usbState(b: Boolean) {
                if (b) {
                    try {
                        initSerial()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
        context!!.registerReceiver(mUsbReceiver, filter)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return MyBinder()
    }

    inner class MyBinder : Binder(), IMyBinder {

        val service: UsbSerialPortService
            get() = this@UsbSerialPortService

        override fun invokeMethodInMyService() {}
        override fun send() {}
        override fun read() {}
    }


    interface IMyBinder {
        fun invokeMethodInMyService()
        fun send()
        fun read()
    }


    var wakeLock :PowerManager.WakeLock? = null
    override fun onCreate() {
        super.onCreate()
        context = this
        try {
            //usb监听
            registerReceiver()
            //锁屏后保活
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.TAG)
            wakeLock?.acquire(10*60*1000L /*10 minutes*/)
            onStartForeground()
            initSerial()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val SERVICE_ID = 136
    val CHANNEL_ID_STRING = "1"
    val TAG = UsbSerialPortService::class.java.simpleName


//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//        log("UsbServiec----onStartCommand");
//        onStartForeground();
//        return START_STICKY;
//    }


    //    @Override
    //    public int onStartCommand(Intent intent, int flags, int startId) {
    //
    //        log("UsbServiec----onStartCommand");
    //        onStartForeground();
    //        return START_STICKY;
    //    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        onStartForeground()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        if (networkConnectChangedReceiver != null) {
            unregisterReceiver(networkConnectChangedReceiver)
        }
        if (mUsbReceiver != null){
            unregisterReceiver(mUsbReceiver)
        }

        //val localIntent = Intent()
        //localIntent.setClass(this, UsbSerialPortService::class.java)
        //startService(localIntent)
        log("UsbServiec----onDestroy")
        super.onDestroy()
    }


    override fun registerRxBus(): Boolean {
        return true
    }

    override fun eventComing(t: BusEvent?) {}


    /**
     * 初始化usb串口
     * @throws IOException
     */
    @Throws(IOException::class)
    fun initSerial() {
        manager = getSystemService(USB_SERVICE) as UsbManager
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            return
        }
        for (serialDriver in availableDrivers) {
            val usbDevice = serialDriver.device
            LogUtils.i(usbDevice.toString())
        }
        driver = availableDrivers[0]
        if (!manager!!.hasPermission(driver!!.device)) {
            log("没有权限")
            //usbPermissionReceiver = new UsbPermissionReceiver();
            //申请权限
            val intent = Intent(ACTION_DEVICE_PERMISSION)
            var mPermissionIntent: PendingIntent? = null //PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), 0)
            mPermissionIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            }
            val permissionFilter = IntentFilter(ACTION_DEVICE_PERMISSION)
//            permissionFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
//            permissionFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
//            permissionFilter.addAction(ACTION_USB_PERMISSION)
            registerReceiver(usbPermissionReceiver, permissionFilter)
            manager!!.requestPermission(driver!!.device, mPermissionIntent)
            return
        } else {
            log("有权限")
            openUsb(driver!!)
        }
    }

    private var usbPermissionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_DEVICE_PERMISSION == action) {
                synchronized(this) {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    //if (device!!.deviceName == driver!!.device.deviceName) {
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false)) {
                            //授权成功,在这里进行打开设备操作
                            log("授权成功")
                            try {
                                openUsb(driver!!)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        } else {
                            //授权失败
                            log("授权失败")
                        }
                    //}
                }
            }
        }
    }

    private val queue: BlockingQueue<*> = LinkedBlockingQueue<Any?>()

    @Throws(IOException::class)
    fun openUsb(driver: UsbSerialDriver) {
        val connection = manager!!.openDevice(driver.device)
            ?: // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            return
        serialPort = driver.ports[0]
        serialPort?.apply {

            open(connection)
            log("usb:已连接")
            setParameters(
                115200,
                8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
            )
            val usbIoManager = SerialInputOutputManager(
                this,
                object : SerialInputOutputManager.Listener {
                    override fun onNewData(data: ByteArray) {
                        result(data)
                    }

                    override fun onRunError(e: Exception) {

                        ThreadUtils.runOnUiThread { log("usb连接:未连接") }
                        LogUtils.e(e)
                        //Queueing USB request failed
                        //usb被删除断开了,原因是线断了-。-
                        //重启设备
                        //DevUtils.normalReboot();
                    }
                })
            ThreadUtils.getIoPool().execute(usbIoManager)
        }

    }

    private fun result(data: ByteArray?) {
        try {
            val string = ConvertUtils.bytes2HexString(data)
            log("<--串口接收到数据$string")
            if (data == null) {
                return
            }
            loopDatas.addAll(data.toTypedArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    fun sendData(bytes: ByteArray):Int {
        try {
            //loopDatas.clear()
            if (serialPort != null && bytes.isNotEmpty()) {
                serialPort!!.write(bytes, 0)
                log("发送完毕--${ConvertUtils.bytes2HexString(bytes)}")
                return bytes.size
            }else{
                println("serialPort==null")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }


    /**
     * 指定读取多长的数据段
     */
    fun readData(rdLength: Int): ByteArray? {
        var data: ByteArray? = null
        synchronized (loopDatas){

            if (loopDatas.isEmpty()){
                return null
            }
            val loopSize = loopDatas.size
            val tempSize = if (rdLength > loopSize){
                loopSize
            }else{
                rdLength
            }
            data = ByteArray(tempSize)
            data?.let {
                for (i in it.indices){
                    it[i] = loopDatas.poll()
                }
            }

        }

        return data
    }

    /**
     * 间隔多长时间后 进行读取 不指定长度 。！！！还有一种 读取多长时间并且指定了长度 这个暂时未实现 先记录有这个
     */
    fun readData(outTime: Long): ByteArray {
        try {
            val startTime = System.currentTimeMillis() //开始时间
            //做空耗时操作 结束后读取数据
            println("等待读取数据")
            //假如开始时间2023年 循环时的时间变成了 1970年那么陷入死循环
            while (System.currentTimeMillis() - startTime < outTime) {
                //println(Thread.currentThread().name)
            }
            println("开始读取数据")
            if (loopDatas.isEmpty()) {
                log("未读取到数据")
            }
            //循环读取这段时间内的数据 读空队列中的数据
            val loopSize = loopDatas.size //不能使用loopDatas.isEmpty()做为条件进行while循环读取 不安全，此处的size也必须先赋值
            println("loopDatas.size=$loopSize")
            var bytes = ByteArray(loopSize)
            for (i in bytes.indices){
                bytes[i] = loopDatas.poll()
            }
            return bytes

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ByteArray(0)
    }

    fun purge(){
        loopDatas.clear()
    }

}