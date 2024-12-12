package com.zdyb.module_diagnosis.fragment.setting

import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.module_diagnosis.databinding.FragmentSetting1Binding

class SetFragment1:BaseNavFragment<FragmentSetting1Binding,BaseViewModel>() {

    override fun initViewModel(): BaseViewModel {
        return BaseViewModel()
    }


}