package com.zdyb.module_obd.activity

import android.text.TextUtils
import com.blankj.utilcode.util.AppUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jakewharton.rxbinding3.view.clicks
import com.zdeps.bean.SetUpBean
import com.zdyb.lib_common.base.AppManager
import com.zdyb.lib_common.base.BaseActivity
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.utils.PreferencesUtils
import com.zdyb.lib_common.utils.RouterUtil
import com.zdyb.lib_common.utils.SharePreferencesConstant
import com.zdyb.lib_common.utils.constant.RouteConstants
import com.zdyb.module_obd.R
import com.zdyb.module_obd.databinding.ActivityObdSetBinding
import com.zdyb.module_obd.dialog.DialogBox
import io.reactivex.functions.Consumer
import java.util.concurrent.TimeUnit

class OBDSetActivity :BaseActivity<ActivityObdSetBinding,BaseViewModel>(){

    override fun initViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun getTitleText(): CharSequence {
        return getString(R.string.setUp)
    }

    override fun initViewObservable() {
        super.initViewObservable()

        val mAdapter = object :BaseQuickAdapter<SetUpBean, BaseViewHolder>(R.layout.item_obd_set) {
            override fun convert(holder: BaseViewHolder, item: SetUpBean) {
                holder.setText(R.id.title,item.title)
                holder.setText(R.id.value,item.value)
            }
        }


        val bottomCode = PreferencesUtils.getString(this, SharePreferencesConstant.BOTTOM_DEVICE_CODE,"")
        var obdSoCode = PreferencesUtils.getString(this, SharePreferencesConstant.OBD_SO_CODE,"")
        if (TextUtils.isEmpty(obdSoCode)){
            obdSoCode = "V1.0.0"
        }
        val data = mutableListOf<SetUpBean>()
        data.add(SetUpBean(getString(R.string.obd_set_vci),viewModel.getVCI()))
        data.add(SetUpBean(getString(R.string.obd_set_app_version),AppUtils.getAppVersionName()))
        data.add(SetUpBean(getString(R.string.obd_set_bottom_device_version),bottomCode))
        data.add(SetUpBean(getString(R.string.obd_set_obd_version),obdSoCode))
        mAdapter.setList(data)
        binding.recyclerView.adapter = mAdapter

        binding.butOutLogin.setChangeAlphaWhenPress(true)
        addDisposable(binding.butOutLogin.clicks().throttleFirst(1, TimeUnit.SECONDS).subscribe({
             DialogBox().setBox(getString(R.string.obd_set_obd_out_login_user)).setResult(Consumer {
                 if (it){
                     PreferencesUtils.putString(this,SharePreferencesConstant.USER_TOKEN,"")
                     PreferencesUtils.putString(this,SharePreferencesConstant.USER_ID,"")
                     PreferencesUtils.putString(this,SharePreferencesConstant.VCI_CODE,"")
                     PreferencesUtils.putString(this,SharePreferencesConstant.BOTTOM_DEVICE_CODE,"")
                     PreferencesUtils.putString(this,SharePreferencesConstant.OBD_SO_CODE,"")
                     PreferencesUtils.putString(this,SharePreferencesConstant.ADAPTED_BLE_DEVICES,"")
                     AppManager.getAppManager().finishAllActivity()
                     RouterUtil.build(RouteConstants.Obd.OBD_ACTIVITY_LOGIN).launch()
                 }
             }).show(supportFragmentManager,"outLoginDialog")
        },{it.printStackTrace()}))
    }

}