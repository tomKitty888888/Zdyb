package com.module_obd.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.module_obd.databinding.TestLayoutBinding
import com.zdyb.lib_common.base.BaseActivity
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.utils.constant.RouteConstants

@Route(path = RouteConstants.Base.TEST_A)
class TestActivity:BaseActivity<TestLayoutBinding,BaseViewModel>() {

    override fun initViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initViewObservable() {
        super.initViewObservable()

    }

    override fun initData() {
        super.initData()
        println("123eeee")
    }
}