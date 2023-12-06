package com.zdyb.module_diagnosis.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.AppUtils
import com.qmuiteam.qmui.kotlin.onClick
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zdyb.lib_common.base.BaseActivity
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.bus.BusEvent
import com.zdyb.lib_common.bus.EventTypeDiagnosis
import com.zdyb.lib_common.utils.PreferencesUtils
import com.zdyb.lib_common.utils.SharePreferencesDiagnosis
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.databinding.ActivityPdfBinding
import com.zdyb.module_diagnosis.databinding.ActivitySetBinding
import java.io.File

class SetActivity :BaseActivity<ActivitySetBinding,BaseViewModel>(){

    lateinit var rxPermissions : RxPermissions
    override fun initViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initViewObservable() {
        super.initViewObservable()
        rxPermissions = RxPermissions(this)

        binding.tvAppVersion.text = "APP版本号：${AppUtils.getAppVersionName()}"
        upBottomDeviceInfo()

        binding.tvBack.onClick {
            finish()
        }
        binding.selectAppFile.onClick {

            if (TextUtils.isEmpty(BaseApplication.USBPath)){
                viewModel.showToast("请插入U盘")
                return@onClick
            }
            //寻找usb路径

            FileChooseActivity.startActivity(this,BaseApplication.USBPath,"")
        }
        binding.upApp.onClick {

            try {
                val path = binding.appUrl.text.toString()
                if (TextUtils.isEmpty(path)){
                    viewModel.showToast("请先选择安装文件")
                    return@onClick
                }
                if (!path.endsWith(".apk")){
                    viewModel.showToast("请选择apk文件")
                    return@onClick
                }

                val apk = File(binding.appUrl.text.toString())

                val intent = Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                val authority = applicationContext.packageName +".fileProvider"
                println("authority=$authority")
                val uri = FileProvider.getUriForFile(this, authority, apk)
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                startActivity(intent);
            }catch (e :Exception){
                e.printStackTrace()
            }

        }

        binding.openFileMsg.onClick {
            openFileManager()
        }
        getAllFilePermission()
    }

    //获取所有文件管理的权限 需要用户手动进行赋予
    fun getAllFilePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {

            }else{
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle(getString(R.string.all_file_permission_title_hint))
                dialog.setMessage(getString(R.string.all_file_permission_msg_hint))
                dialog.setPositiveButton(getString(R.string.confirm)) { dialog, _ ->

                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivityForResult(intent,2)
                    dialog.dismiss()
                }
                dialog.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                dialog.show()

            }
        }
    }

    fun upBottomDeviceInfo(){
        runOnUiThread {
            binding.tvVersion.text = "硬件版本号：${PreferencesUtils.getString(this, SharePreferencesDiagnosis.DEVICE_VERSION,"未识别")}"
            binding.tvSn.text = "序列号：${PreferencesUtils.getString(this, SharePreferencesDiagnosis.DEVICE_SN,"未识别")}"
        }

    }

    fun openFileManager(){
        val intent = packageManager.getLaunchIntentForPackage("com.android.documentsui")
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK )
        intent?.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED )
        startActivity(intent)
    }

    override fun registerRxBus(): Boolean {
        return true
    }

    override fun eventComing(t: BusEvent?) {
        super.eventComing(t)
        t?.let {
            when(it.what){
                EventTypeDiagnosis.USB_PATH ->{

                }
                EventTypeDiagnosis.USB_OUT ->{

                }
                EventTypeDiagnosis.CMD_SELECT_FILE ->{
                    binding.appUrl.text = it.data.toString()
                }

                EventTypeDiagnosis.PORT_CONNECT ->{
                    upBottomDeviceInfo()
                }
                EventTypeDiagnosis.PORT_OUT ->{

                }
            }
        }
    }



}