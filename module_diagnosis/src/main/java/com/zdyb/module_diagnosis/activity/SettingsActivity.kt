package com.zdyb.module_diagnosis.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdyb.lib_common.base.BaseActivity
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.utils.PreferencesUtils
import com.zdyb.lib_common.utils.SharePreferencesDiagnosis
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.adapter.BaseFragmentAdapter
import com.zdyb.module_diagnosis.bean.ACTEntity
import com.zdyb.module_diagnosis.databinding.ActivitySettingsBinding
import com.zdyb.module_diagnosis.dialog.NavigationBarUtil
import com.zdyb.module_diagnosis.fragment.setting.SetFragment1
import com.zdyb.module_diagnosis.fragment.setting.SetFragment2
import com.zdyb.module_diagnosis.fragment.setting.SetFragment3
import com.zdyb.module_diagnosis.fragment.setting.SetFragment4
import com.zdyb.module_diagnosis.model.SettingModel

class SettingsActivity:BaseActivity<ActivitySettingsBinding, SettingModel>() {

    override fun initViewModel(): SettingModel {
        return SettingModel()
    }

    override fun initContentView(savedInstanceState: Bundle?): Int {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        NavigationBarUtil.hideNavigationBar(window)
        return super.initContentView(savedInstanceState)
    }

    override fun initViewObservable() {
        super.initViewObservable()

        val menuNames = arrayOf(
            getString(R.string.setting_company_information),
            getString(R.string.setting_app_information),
            getString(R.string.setting_user_information),
            getString(R.string.setting_user_login),
            getString(R.string.setting_user_out_login),
            getString(R.string.setting_lock_screenshot_img))

        mAdapter.setOnItemClickListener { adapter, _, position ->
            menuListener(position)
        }
        mAdapter.setList(menuNames.toList())
        binding.recyclerView.adapter = mAdapter

        //
        val pages = mutableListOf<Fragment>()
        pages.add(SetFragment1())
        pages.add(SetFragment2())
        pages.add(SetFragment3())
        pages.add(SetFragment4())
        binding.viewPager2.adapter = BaseFragmentAdapter(this,pages)
        binding.viewPager2.isUserInputEnabled = false

        //隐藏一些控件
        binding.includeBarTop.btnSet.visibility = View.INVISIBLE
        binding.includeBarTop.imgBleState.visibility = View.INVISIBLE
        //添加返回按钮
        binding.includeBarBottom
    }


    private fun menuListener(position:Int){
        when(position){
            0 -> {
                binding.viewPager2.setCurrentItem(0,false)
            }
            1 -> {
                binding.viewPager2.setCurrentItem(1,false)
            }
            2 -> {
                binding.viewPager2.setCurrentItem(2,false)
            }
            3 -> {
                binding.viewPager2.setCurrentItem(3,false)
            }
            4 -> {
                val normalDialog = AlertDialog.Builder(this)
                normalDialog.setMessage(getString(R.string.setting_sign_back_in))
                normalDialog.setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                    //清除登录后存储的数据
                    PreferencesUtils.putString(this, SharePreferencesDiagnosis.LOGIN_RESULT,"")
                    dialog.dismiss()
                }
                normalDialog.setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                    dialog.dismiss()
                }
                normalDialog.show()
            }
            5 -> {
                startActivity(Intent(this,SeeScreenshotsActivity::class.java))
            }

        }
    }

    private val mAdapter: BaseQuickAdapter<String, BaseViewHolder> =
        object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_settings) {

            override fun convert(holder: BaseViewHolder, item: String) {

                holder.setText(R.id.name, item)

            }
        }

}