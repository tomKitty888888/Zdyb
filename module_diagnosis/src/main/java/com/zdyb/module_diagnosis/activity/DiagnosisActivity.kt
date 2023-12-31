package com.zdyb.module_diagnosis.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ScreenUtils
import com.google.android.material.snackbar.Snackbar
import com.qmuiteam.qmui.kotlin.onClick
import com.zdeps.diag.DiagJni
import com.zdeps.gui.ConnDevices
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.bus.BusEvent
import com.zdyb.lib_common.bus.EventTypeDiagnosis
import com.zdyb.lib_common.bus.RxBus
import com.zdyb.lib_common.bus.RxSubscriptions
import com.zdyb.lib_common.utils.PreferencesUtils
import com.zdyb.lib_common.utils.RxHelper
import com.zdyb.lib_common.utils.SharePreferencesDiagnosis
import com.zdyb.lib_common.utils.constant.RouteConstants
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.databinding.ActivityDiagnosisBinding
import com.zdyb.module_diagnosis.dialog.NavigationBarUtil
import com.zdyb.module_diagnosis.service.DiagnosisService
import com.zdyb.module_diagnosis.widget.BottomBarActionButton
import io.reactivex.disposables.Disposable
import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess

@Route(path = RouteConstants.Diagnosis.DIAGNOSIS_ACTIVITY)
class DiagnosisActivity : AppCompatActivity() {

    var mSubscription: Disposable? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDiagnosisBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        //WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityDiagnosisBinding.inflate(layoutInflater)

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

        println("获取屏幕的宽度（单位：px）=${ScreenUtils.getScreenWidth()}")
        println("获取屏幕的高度（单位：px）=${ScreenUtils.getScreenHeight()}")
        println("获取应用屏幕的宽度（单位：px）=${ScreenUtils.getAppScreenWidth()}")
        println("获取应用屏幕的高度（单位：px）=${ScreenUtils.getAppScreenHeight()}")
        println("获取屏幕密度=${ScreenUtils.getScreenDensity()}")
        println("获取屏幕密度 DPI =${ScreenUtils.getScreenDensityDpi()}")


        initView()

        registerListen() //电量监听
    }

    private fun initView(){

        initHomeActionButton()
        binding.includeBarTop.btnSet.onClick {

        }
        binding.includeBarTop.imgScreenshot.onClick {

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







    private val mLeftActionButtons =  mutableListOf<BottomBarActionButton>()
    private val mRightActionButtons =  mutableListOf<BottomBarActionButton>()

    /**
     * 初始home页面按键
     */
    private fun initHomeActionButton(){
        mLeftActionButtons.add(BottomBarActionButton(this).addValue(R.mipmap.icon_d_help,getString(R.string.action_button_help)).setClick {
            //帮助
        })
        mLeftActionButtons.add(BottomBarActionButton(this).addValue(R.mipmap.icon_d_voltage,getString(R.string.action_button_voltage)).setClick {
            //电压
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

    private fun eventComing(t: BusEvent?) {
        t?.let {
            runOnUiThread{
                when(it.what){
                    EventTypeDiagnosis.PORT_CONNECT ->{
                        BaseApplication.usbConnect = true
                        binding.includeBarTop.imgBleState.setImageResource(R.mipmap.icon_d_ble_state_connect)
                        Handler().post { getDeviceInfo() }

                    }
                    EventTypeDiagnosis.PORT_OUT ->{
                        BaseApplication.usbConnect = false
                        binding.includeBarTop.imgBleState.setImageResource(R.mipmap.icon_d_ble_state_disconnect)
                    }
                }
            }
        }
    }

    /**
     * 查询下位机的信息 包括sn与版本信息
     */
    private fun getDeviceInfo(){
        //让下位机停止发送数据
        ConnDevices.sendData(byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x01,0xd1.toByte(), 0x55))
        ConnDevices.outTimeReadData(200)
        ConnDevices.purge()
        //流控 开关，这里暂时不用

        //查询sn
        ConnDevices.sendData(byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x01, 0xf2.toByte(), 0x55))
        val snResult = ConnDevices.outTimeReadData(200)
        if (null != snResult && snResult.size >=17){
            val sn = String(snResult,5,12)
            KLog.d("sn=$sn")
            PreferencesUtils.putString(this, SharePreferencesDiagnosis.DEVICE_SN,sn)
        }
        ConnDevices.purge()
        //查询版本号
        ConnDevices.sendData(byteArrayOf(0xa5.toByte(), 0xa5.toByte(), 0x00, 0x02, 0xf0.toByte(), 0x00, 0x55))
        val versionResult = ConnDevices.outTimeReadData(200)
        val resultData = ByteArray(5)
        if (null != versionResult && versionResult.isNotEmpty()){
            for (i in versionResult.indices) {
                if (String(byteArrayOf(versionResult[i]), StandardCharsets.UTF_8) == "V") {
                    resultData[0] = versionResult[i]
                    resultData[1] = versionResult[i + 1]
                    resultData[2] = versionResult[i + 2]
                    resultData[3] = versionResult[i + 3]
                    resultData[4] = versionResult[i + 4]
                }
            }
            val versionString = String(resultData, StandardCharsets.UTF_8)
            KLog.d("version=$versionString")
            PreferencesUtils.putString(this, SharePreferencesDiagnosis.DEVICE_VERSION,versionString)
        }
    }
}