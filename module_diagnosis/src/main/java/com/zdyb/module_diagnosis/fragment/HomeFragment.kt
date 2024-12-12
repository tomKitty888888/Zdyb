package com.zdyb.module_diagnosis.fragment

import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.qmuiteam.qmui.kotlin.onClick
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.module_diagnosis.databinding.FragmentHomeBinding
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ConvertUtils
import com.zdeps.gui.ConnDevices
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseDialogFragment
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.dialog.*
import com.zdyb.module_diagnosis.model.HomeModel
import com.zdyb.module_diagnosis.widget.BottomBarActionButton

class HomeFragment: BaseNavFragment<FragmentHomeBinding, HomeModel>()  {




    override fun initViewModel(): HomeModel {
        return ViewModelProvider(requireActivity())[HomeModel::class.java]
    }

    override fun initViewObservable() {
        super.initViewObservable()

        binding.actionDiagnosis.onClick {
            println("智能诊断")
            findNavController().navigate(R.id.action_homeFragment_to_AutoScanDeviceListFragment)
        }
        binding.actionEcuRw.onClick {
            println("ECU刷写")
            val bundle = bundleOf(FileListFragment.TAG to PathManager.getReflashFilePath())
            findNavController().navigate(R.id.action_homeFragment_to_fileListFragment,bundle)
        }
        binding.actionPpd.onClick {
            println("后处理诊断")
            val bundle = bundleOf(FileListFragment.TAG to PathManager.getATFilePath())
            findNavController().navigate(R.id.action_homeFragment_to_fileListFragment,bundle)
        }
        binding.actionCartDvs.onClick {
            println("柴油车系")
            findNavController().navigate(R.id.action_homeFragment_to_childCarSeriesFragment)
        }
        binding.actionCartNevs.onClick {
            println("新能源")
            val bundle = bundleOf(FileListFragment.TAG to PathManager.getEvFilePath())
            findNavController().navigate(R.id.action_homeFragment_to_fileListFragment,bundle)
        }
        binding.actionCartNgvs.onClick {
            println("天然气")
            val bundle = bundleOf(FileListFragment.TAG to PathManager.getCngFilePath())
            findNavController().navigate(R.id.action_homeFragment_to_fileListFragment,bundle)

        }

        //addActionButton()
        BaseApplication.getInstance().outDiagnosisService = false
        println("初始化首页")
    }

    fun visibleDialog(vararg dialogs : BaseDialogFragment){
        for (d in dialogs){
            if (d.isVisible)d.dismiss()
//            when(d){
//                is DialogHintBox ->{if (d.isVisible){d.dismiss()}}
//                is DialogInputBox ->{if (d.isVisible){d.dismiss()}}
//                is DialogInputFileBox ->{if (d.isVisible){d.dismiss()}}
//            }
        }
    }

    fun addActionButton(){
        if (activity is DiagnosisActivity){
            (activity as DiagnosisActivity).addLeftActionButton(
                BottomBarActionButton(activity).addValue(0,getString(R.string.action_button_help)),
                BottomBarActionButton(activity).addValue(0,getString(R.string.action_button_voltage))
            )
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as DiagnosisActivity).showHomeActionButton()
    }
}