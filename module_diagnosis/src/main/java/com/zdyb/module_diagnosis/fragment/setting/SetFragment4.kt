package com.zdyb.module_diagnosis.fragment.setting

import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.RegexUtils
import com.jakewharton.rxbinding3.view.clicks
import com.qmuiteam.qmui.kotlin.onClick
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.utils.PreferencesUtils
import com.zdyb.lib_common.utils.RxTimerUtil
import com.zdyb.lib_common.utils.SharePreferencesConstant
import com.zdyb.lib_common.utils.SharePreferencesDiagnosis
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.SettingsActivity
import com.zdyb.module_diagnosis.databinding.FragmentSetting4Binding
import com.zdyb.module_diagnosis.netservice.DiagInteractor
import io.reactivex.functions.Consumer
import java.util.concurrent.TimeUnit

class SetFragment4:BaseNavFragment<FragmentSetting4Binding,BaseViewModel>() {

    override fun initViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initViewObservable() {
        super.initViewObservable()

        val sn = PreferencesUtils.getString(BaseApplication.getInstance(), SharePreferencesDiagnosis.DEVICE_SN)

        //设备ID
        binding.edText2.setText(sn)

        //获取验证码
        binding.buttonCode.onClick {

            if (TextUtils.isEmpty(sn)){
                viewModel.showToast(getString(R.string.setting_login_no_sn_hint))
                return@onClick
            }

            val phone = binding.edText1.text.toString()
            if (!RegexUtils.isMobileSimple(phone)){
                viewModel.showToast(getString(R.string.setting_login_hint))
                return@onClick
            }

            addDisposable(DiagInteractor.sendSms(sn,phone).subscribe({
                println("获取验证码成功")

                RxTimerUtil.timer(60, Consumer{
                    binding.buttonCode.text = getString(R.string.setting_user_login_getVerCode)+"($it)"
                    binding.buttonCode.background = ContextCompat.getDrawable(context!!,R.drawable.bg_frame_radius_5_c_999999)
                    binding.buttonCode.isClickable = false
                    if (it <= 0){
                        binding.buttonCode.text = getString(R.string.setting_user_login_getVerCode)
                        binding.buttonCode.background = ContextCompat.getDrawable(context!!,R.drawable.bg_frame_radius_5_c_37bb4c)
                        binding.buttonCode.isClickable = true
                    }
                })

            },{it.printStackTrace()}))
        }

        //登录
        addDisposable(binding.buttonLogin.clicks().throttleFirst(1, TimeUnit.SECONDS).subscribe({
            val phone = binding.edText1.text.toString()
            val code = binding.edText3.text.toString()
            if (TextUtils.isEmpty(phone)){
                viewModel.showToast(getString(R.string.setting_login_no_phone_hint))
                return@subscribe
            }
            if (TextUtils.isEmpty(code)){
                viewModel.showToast(getString(R.string.setting_login_no_code_hint))
                return@subscribe
            }
            addDisposable(DiagInteractor.zdybValidate(sn,phone,code).subscribe({
                viewModel.showToast(getString(R.string.setting_login_success))
                  //存储登录后返回的信息
                val jsonString = GsonUtils.getGson().toJson(it)
                PreferencesUtils.putString(context,SharePreferencesDiagnosis.LOGIN_RESULT,jsonString)
                //存储登录的手机号
                PreferencesUtils.putString(context,SharePreferencesDiagnosis.LOGIN_PHONE,phone)
                //返回到首页
                (context as SettingsActivity).finish()
            },{it.printStackTrace()}))
        },{it.printStackTrace()}))

    }

}