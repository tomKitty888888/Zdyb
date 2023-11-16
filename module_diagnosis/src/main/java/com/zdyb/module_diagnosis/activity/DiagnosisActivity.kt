package com.zdyb.module_diagnosis.activity

import android.os.Bundle
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
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.utils.constant.RouteConstants
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.databinding.ActivityDiagnosisBinding
import com.zdyb.module_diagnosis.dialog.NavigationBarUtil
import com.zdyb.module_diagnosis.widget.BottomBarActionButton
import kotlin.system.exitProcess

@Route(path = RouteConstants.Diagnosis.DIAGNOSIS_ACTIVITY)
class DiagnosisActivity : AppCompatActivity() {

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
        println("DiagnosisActivity进来了")

        println("获取屏幕的宽度（单位：px）=${ScreenUtils.getScreenWidth()}")
        println("获取屏幕的高度（单位：px）=${ScreenUtils.getScreenHeight()}")
        println("获取应用屏幕的宽度（单位：px）=${ScreenUtils.getAppScreenWidth()}")
        println("获取应用屏幕的高度（单位：px）=${ScreenUtils.getAppScreenHeight()}")
        println("获取屏幕密度=${ScreenUtils.getScreenDensity()}")
        println("获取屏幕密度 DPI =${ScreenUtils.getScreenDensityDpi()}")


        initView()
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
}