package com.zdyb.module_diagnosis.fragment.setting

import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.GsonUtils
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.utils.PreferencesUtils
import com.zdyb.lib_common.utils.SharePreferencesDiagnosis
import com.zdyb.module_diagnosis.bean.LoginResultBean
import com.zdyb.module_diagnosis.databinding.FragmentSetting2Binding
import com.zdyb.module_diagnosis.help.InitDeviceInfo

class SetFragment2:BaseNavFragment<FragmentSetting2Binding,BaseViewModel>() {

    override fun initViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initViewObservable() {
        super.initViewObservable()

        val sn = PreferencesUtils.getString(BaseApplication.getInstance(), SharePreferencesDiagnosis.DEVICE_SN)
        val version = PreferencesUtils.getString(BaseApplication.getInstance(), SharePreferencesDiagnosis.DEVICE_VERSION)
        val loginResultJsonString = PreferencesUtils.getString(BaseApplication.getInstance(), SharePreferencesDiagnosis.LOGIN_RESULT)
        val loginResultBean = GsonUtils.getGson().fromJson(loginResultJsonString,LoginResultBean::class.java)
        val phone = PreferencesUtils.getString(BaseApplication.getInstance(),SharePreferencesDiagnosis.LOGIN_PHONE)
        //设备ID
        binding.edText1.setText(sn)

        //设备状态 -登录后才有
        binding.edText2.setText(phone)

        //设备状态 -登录后才有
        binding.edText3.setText(loginResultBean?.activate_date)

        //硬件版本号
        binding.edText4.setText(version)
        //软件版本号
        binding.edText5.setText(AppUtils.getAppVersionCode().toString())
    }

}