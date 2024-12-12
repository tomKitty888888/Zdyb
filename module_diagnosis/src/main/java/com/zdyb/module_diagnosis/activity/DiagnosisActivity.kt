package com.zdyb.module_diagnosis.activity

import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.jakewharton.rxbinding3.view.clicks
import com.qmuiteam.qmui.kotlin.onClick
import com.zdeps.comm.CommomNative
import com.zdeps.gui.CMD
import com.zdeps.gui.ConnDevices
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.bus.BusEvent
import com.zdyb.lib_common.bus.EventTypeDiagnosis
import com.zdyb.lib_common.bus.RxBus
import com.zdyb.lib_common.bus.RxSubscriptions
import com.zdyb.lib_common.service.UsbSerialPortService
import com.zdyb.lib_common.utils.RxHelper
import com.zdyb.lib_common.utils.constant.RouteConstants
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.adapter.PopupMenuAdapter
import com.zdyb.module_diagnosis.bean.HelpMenuEntity
import com.zdyb.module_diagnosis.databinding.ActivityDiagnosisBinding
import com.zdyb.module_diagnosis.dialog.DialogHintBox
import com.zdyb.module_diagnosis.dialog.DialogProgressBox
import com.zdyb.module_diagnosis.dialog.HelpDialog
import com.zdyb.module_diagnosis.dialog.NavigationBarUtil
import com.zdyb.module_diagnosis.help.CaptureHelp
import com.zdyb.module_diagnosis.help.InitDeviceInfo
import com.zdyb.module_diagnosis.model.DiagnosisActivityModel
import com.zdyb.module_diagnosis.popup.MenuPopupWindow
import com.zdyb.module_diagnosis.widget.BottomBarActionButton
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

@Route(path = RouteConstants.Diagnosis.DIAGNOSIS_ACTIVITY)
class DiagnosisActivity : AppCompatActivity() {
    var h = Handler(Looper.getMainLooper())
    var mSubscription: Disposable? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDiagnosisBinding
    lateinit var mMenuPopupWindow: MenuPopupWindow
    lateinit var mHelpDialog: HelpDialog
    lateinit var viewModel : DiagnosisActivityModel

    override fun onCreate(savedInstanceState: Bundle?) {
        //WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityDiagnosisBinding.inflate(layoutInflater)
        viewModel = DiagnosisActivityModel().init(this)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        NavigationBarUtil.hideNavigationBar(window)
        setContentView(binding.root)
        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }


        //setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment_content_diagnosis)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAnchorView(R.id.fab)
//                .setAction("Action", null).show()


        }
        binding.includeBarBottom.menuHomeButton.onClick {
            BaseApplication.getInstance().outDiagnosisService = true
            navController.popBackStack(R.id.homeFragment,false)
        }
        mSubscription = RxBus.getDefault().toObservable(BusEvent::class.java)
            .compose(RxHelper.errorEmpty())
            .subscribe({ busEvent -> eventComing(busEvent) }
            ) { throwable -> throwable.printStackTrace() }
        //将订阅者加入管理站
        RxSubscriptions.add(mSubscription)
        println("DiagnosisActivity进来了")
        //初始化一些view
        //mMenuPopupWindow = MenuPopupWindow(this)
        //mHelpDialog = HelpDialog()
        //

        println("获取屏幕的宽度（单位：px）=${ScreenUtils.getScreenWidth()}")
        println("获取屏幕的高度（单位：px）=${ScreenUtils.getScreenHeight()}")
        println("获取应用屏幕的宽度（单位：px）=${ScreenUtils.getAppScreenWidth()}")
        println("获取应用屏幕的高度（单位：px）=${ScreenUtils.getAppScreenHeight()}")
        println("获取屏幕密度=${ScreenUtils.getScreenDensity()}")
        println("获取屏幕密度 DPI =${ScreenUtils.getScreenDensityDpi()}")


        initView()

        registerListen() //电量监听

        if (UsbSerialPortService.isNotDevice){
            KLog.i("首次进入连接蓝牙")
            viewModel.oneRunConnect()
        }
    }

    private fun initView(){
        initDialog()
        initHelpView()
        initHomeActionButton()
        binding.includeBarTop.btnSet.onClick {
            startActivity(Intent(this,SettingsActivity::class.java))
        }
        viewModel.addDisposable(binding.includeBarTop.imgScreenshot.clicks().throttleFirst(2, TimeUnit.SECONDS).subscribe({
            CaptureHelp.capture(this@DiagnosisActivity)
        },{it.printStackTrace()}))

        binding.includeBarTop.imgBleState.onClick {
            //检查usb是否连接
            if (!ConnDevices.isUSBConnect()){
                //打开定位，打开蓝牙权限，查询已经配对的蓝牙 显示列表
                viewModel.isShowList = true
                viewModel.showBleList()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menuInflater.inflate(R.menu.menu_diagnosis, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_diagnosis)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }


    private fun initHelpView(){
        val popupMenuAdapter = PopupMenuAdapter()
        val data = mutableListOf<HelpMenuEntity>()
        data.add(HelpMenuEntity(getString(R.string.help_upData),R.mipmap.icon_d_update,R.color.color_theme))
        data.add(HelpMenuEntity(getString(R.string.help_dataBase),R.mipmap.icon_d_database,R.color.data_base))
        popupMenuAdapter.setList(data)
        popupMenuAdapter.setOnItemClickListener{ adapter, view, position ->
            println(position)
            val navController = findNavController(R.id.nav_host_fragment_content_diagnosis)
            when(position){
                0 ->{navController.navigate(R.id.action_homeFragment_to_downloadAllFileFragment)}
                1 ->{navController.navigate(R.id.dataBaseFragment)}
            }
            binding.helpRecyclerView.visibility = View.GONE
        }
        binding.helpRecyclerView.adapter = popupMenuAdapter
    }

    private fun initDialog(){
        mDialogHintBox = DialogHintBox()
        mDialogHintBox.setHomeBackResult{
            println("HomeBackResult--执行到")
        }
        mDialogHintBox.setBackResult {
            mDialogHintBox.dismiss()
        }
        mDialogProgressBox = DialogProgressBox()
        KLog.d("qq进程id=${android.os.Process.myPid()}")

        demoProgress = ProgressDialog(this@DiagnosisActivity)
    }
    lateinit var demoProgress : ProgressDialog

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (binding.helpRecyclerView.visibility == View.VISIBLE){
            binding.helpRecyclerView.visibility = View.GONE
        }
        return super.onTouchEvent(event)
    }


    private val mLeftActionButtons =  mutableListOf<BottomBarActionButton>()
    private val mRightActionButtons =  mutableListOf<BottomBarActionButton>()

    /**
     * 初始home页面按键
     */
    private fun initHomeActionButton(){
        mLeftActionButtons.add(BottomBarActionButton(this).addValue(R.mipmap.icon_d_help,getString(R.string.action_button_help)).setClick {
            //帮助
//            if (mMenuPopupWindow.isShowing){
//                return@setClick
//            }
//            val offsetX = Math.abs(mMenuPopupWindow.getContentView().getMeasuredWidth()-it.getWidth()) / 2
//            val offsetY = -(mMenuPopupWindow.getContentView().getMeasuredHeight()+it.getHeight());
//            PopupWindowCompat.showAsDropDown(mMenuPopupWindow, it, offsetX, offsetY, Gravity.START)


            if (binding.helpRecyclerView.visibility == View.VISIBLE){
                binding.helpRecyclerView.visibility = View.GONE
            }else{
                binding.helpRecyclerView.visibility = View.VISIBLE
            }

//            if (!mHelpDialog.isVisible){
//                mHelpDialog.show(supportFragmentManager,"mHelpDialog")
//            }


        })
        mLeftActionButtons.add(BottomBarActionButton(this).addValue(R.mipmap.icon_d_voltage,getString(R.string.action_button_voltage)).setClick {
            //电压
            val navController = findNavController(R.id.nav_host_fragment_content_diagnosis)
            navController.navigate(R.id.action_homeFragment_to_voltageFragment)

        })

        mRightActionButtons.add(BottomBarActionButton(this).addValue(R.mipmap.icon_d_out,getString(R.string.action_button_out))
            .setPartitionLineVisibility(ConstraintLayout.LayoutParams.LEFT)
            .setClick {
                BaseApplication.getInstance().unBingUsbService()
                //stopService(Intent(this, DiagnosisService::class.java))
                //退出
                finish()
                android.os.Process.killProcess(android.os.Process.myPid());
                exitProcess(0);
            })
    }

    /**
     * 初始显示home页面功能按键，标题
     */
    fun showHomeActionButton(){
        binding.includeBarBottom.leftMenuLayout.removeAllViews()
        binding.includeBarBottom.rightMenuLayout.removeAllViews()
        for (v in mLeftActionButtons){
            addLeftActionButton(v)
        }
        for (v in mRightActionButtons){
            addRightActionButton(v)
        }
        binding.includeBarTop.title.text = ""
        binding.includeBarTop.logo.visibility = View.VISIBLE
    }

    /**
     * 左侧添加按钮
     */
    fun addLeftActionButton(vararg views: View){
        for (v in views){
            binding.includeBarBottom.leftMenuLayout.addView(v)
        }
    }

    /**
     * 右侧添加按钮
     */
    fun addRightActionButton(vararg views: View){
        for (v in views){
            binding.includeBarBottom.rightMenuLayout.addView(v)
        }
    }

    /**
     * 清空掉左侧按钮
     */
    public fun removeLeftActionButton(){
        binding.includeBarBottom.leftMenuLayout.removeAllViews()
    }
    /**
     * 清空掉所有侧按钮
     */
    public fun removeAllActionButton(){
        binding.includeBarBottom.leftMenuLayout.removeAllViews()
        binding.includeBarBottom.rightMenuLayout.removeAllViews()

        if (binding.helpRecyclerView.visibility == View.VISIBLE){
            binding.helpRecyclerView.visibility = View.GONE
        }
    }

    /**
     * 设置标题
     */
    fun setTitle(title:String){
        binding.includeBarTop.title.text = title
        binding.includeBarTop.logo.visibility = View.GONE
    }


    /**
     * 注册电量广播
     */
    private fun registerListen() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED) //电量显示监听
        registerReceiver(batteryReceiver, intentFilter)
    }

    private val batteryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
                when((level.toFloat() / scale * 100).toInt()){ //电量百分比
                    in 1..20 ->{
                        binding.includeBarTop.imgElectricity.setImageResource(R.mipmap.icon_battery_0)
                    }
                    in 21..40 ->{
                        binding.includeBarTop.imgElectricity.setImageResource(R.mipmap.icon_battery_1)
                    }
                    in 41..60 ->{
                        binding.includeBarTop.imgElectricity.setImageResource(R.mipmap.icon_battery_2)
                    }
                    in 61..80 ->{
                        binding.includeBarTop.imgElectricity.setImageResource(R.mipmap.icon_battery_3)
                    }
                    in 81..100 ->{
                        binding.includeBarTop.imgElectricity.setImageResource(R.mipmap.icon_battery_4)
                    }
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
        upConnectState()
    }
    lateinit var mDialogHintBox : DialogHintBox //文字消息提示
    lateinit var mDialogProgressBox: DialogProgressBox //刷写时的进度动画

    private val mUpDeviceCallBack = object :CommomNative.CallBack{
        override fun onSendData(data: ByteArray?): Boolean {
            //发送数据
            if (data != null) {
                val maxsend = 16384
                val times = data.size / maxsend
                val leftsize = data.size % maxsend
                for (i in 0 until times) {
                    val tempData = ByteArray(maxsend)
                    System.arraycopy(data, times * i, tempData, 0, maxsend)
                    ConnDevices.sendData(tempData)
                }
                if (leftsize > 0) {
                    val tempData = ByteArray(leftsize)
                    System.arraycopy(data, times * maxsend, tempData, 0, leftsize)
                    ConnDevices.sendData(tempData)
                }
                return true
            }
            return false
        }

        override fun onGetData(len: Int, timeout: Int): ByteArray {

            val data = ConnDevices.timedReadsDataLength(timeout.toLong(),len)
            if (data != null) {
                if (data.isNotEmpty()){
                    println("一段时间读取指定长度数据=${ConvertUtils.bytes2HexString(data)}")
                    return data
                }
            }
            return ByteArray(0)
        }

        override fun onReadOneFrame(timeout: Int): ByteArray {
            //读取一帧数据
            val data = ConnDevices.readFrameData(timeout.toLong())

            if (data != null) {
                if (data.isNotEmpty()){
                    println("读取一帧数据=${ConvertUtils.bytes2HexString(data)}")
                    return data
                }else{
                    println("读取一帧数据 为空")
                }
            }
            return ByteArray(0)
        }

        override fun onUpdateStop(info: String?) {
            runOnUiThread {
                if(mDialogProgressBox.isVisible){
                    mDialogProgressBox.dismiss()
                }
            }
        }

        override fun onUpdateFaild(info: String?) {
            //弹框提示-有一个确定按钮的
            runOnUiThread {
                if (!mDialogHintBox.isVisible && !mDialogHintBox.isShow()){
                    mDialogHintBox.setActionType(CMD.MSG_MB_OK).setInitMsg(info)
                    mDialogHintBox.show(supportFragmentManager,"mDialogHintBox")
                    if(mDialogProgressBox.isVisible){
                        mDialogProgressBox.dismiss()
                    }
                }else if(mDialogHintBox.isVisible){
                    mDialogHintBox.setMsg(info)
                }
            }
        }

        override fun onUpdateFaild(title: String?, info: String?) {
            //弹框提示-有一个确定按钮的
            runOnUiThread {
                if (!mDialogHintBox.isVisible && !mDialogHintBox.isShow()){
                    mDialogHintBox.setActionType(CMD.MSG_MB_OK).setTitle(title).setInitMsg(info)
                    mDialogHintBox.show(supportFragmentManager,"mDialogHintBox")
                    if(mDialogProgressBox.isVisible){
                        mDialogProgressBox.dismiss()
                    }
                }else if(mDialogHintBox.isVisible){
                    mDialogHintBox.setMsg(info)
                }
            }
        }

        override fun onUpdateProgressing(info: String?) {
            //显示进度
            runOnUiThread {
                    if (!mDialogProgressBox.isVisible && !mDialogProgressBox.isShow()){
                        mDialogProgressBox.setActionType(CMD.FORM_DIALOG_PROGRESS).setInitMsg(info)
                        mDialogProgressBox.show(supportFragmentManager,"mDialogProgressBox")
                    }else if(mDialogProgressBox.isVisible){
                        mDialogProgressBox.setMsg(info)
                    }

            }
        }

        override fun onUpdateSuccess() {
            //升级成功 关闭进度框
            println("onUpdateSuccess")
            runOnUiThread {
                if(mDialogProgressBox.isVisible){
                    mDialogProgressBox.dismiss()
                }
            }
        }

        override fun reConnected() {
            //关闭串口之后 在重新打开，不明白有何作用-。-
            try {
                println("reConnected---关闭串口之后 在重新打开")
                ConnDevices.closeSerialPort()
                ConnDevices.openSerialPort()
            }catch (e :Exception){
                e.printStackTrace()
            }
        }

    }

    /**
     * 更新连接状态
     */
    private fun upConnectState(){
        if (BaseApplication.connectType == BaseApplication.ConnectType.no){
            binding.includeBarTop.imgBleState.setImageResource(R.mipmap.icon_d_ble_state_disconnect)
        }else{
            binding.includeBarTop.imgBleState.setImageResource(R.mipmap.icon_d_ble_state_connect)
        }
    }

    private fun eventComing(t: BusEvent?) {
        t?.let {
            runOnUiThread{
                when(it.what){
                    EventTypeDiagnosis.PORT_CONNECT ->{
                        upConnectState()
                        InitDeviceInfo.registrationCallBack(mUpDeviceCallBack)
                        var mDisposable =
                        Observable.create<String> {
                            if (!InitDeviceInfo.isCheckIng){
                                InitDeviceInfo.getDeviceInfo()
                            }
                        }.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({

                            },{
                                it.printStackTrace()
                                InitDeviceInfo.isCheckIng = false
                            })

                    }
                    EventTypeDiagnosis.PORT_OUT ->{
                        upConnectState()
                    }

                    EventTypeDiagnosis.CONNECT_BLE ->{
                        viewModel.oneRunConnect()
                    }
                    EventTypeDiagnosis.BLE_CONNECT ->{
                        val data = it.data as Int
                        binding.includeBarTop.imgBleState.setImageResource(data)
                    }
                    EventTypeDiagnosis.BlE_OUT ->{
                        val data = it.data as Int
                        binding.includeBarTop.imgBleState.setImageResource(data)
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        viewModel.onDestroy()
        super.onDestroy()
    }

}