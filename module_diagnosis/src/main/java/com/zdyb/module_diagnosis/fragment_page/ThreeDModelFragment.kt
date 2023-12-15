package com.zdyb.module_diagnosis.fragment_page

import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.module_diagnosis.databinding.FragmentThreeDModelBinding

class ThreeDModelFragment:BaseNavFragment<FragmentThreeDModelBinding,BaseViewModel>() {

    lateinit var mPath :String
    companion object{

        fun instance(path:String):ThreeDModelFragment{
            val fragment = ThreeDModelFragment()
            fragment.mPath = path
            return fragment
        }
    }

    override fun initViewModel(): BaseViewModel {
        return BaseViewModel()
    }


}