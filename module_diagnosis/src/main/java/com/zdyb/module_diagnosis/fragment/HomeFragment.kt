package com.zdyb.module_diagnosis.fragment

import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.qmuiteam.qmui.kotlin.onClick
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.module_diagnosis.databinding.FragmentHomeBinding
import androidx.navigation.fragment.findNavController
import com.zdeps.gui.CMD
import com.zdyb.lib_common.base.BaseDialogFragment
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.bean.DeviceEntity
import com.zdyb.module_diagnosis.databinding.DialogHintBoxBinding
import com.zdyb.module_diagnosis.dialog.*
import com.zdyb.module_diagnosis.widget.BottomBarActionButton

class HomeFragment: BaseNavFragment<FragmentHomeBinding, BaseViewModel>()  {



    override fun initViewModel(): BaseViewModel {
        return ViewModelProvider(requireActivity())[BaseViewModel::class.java]
    }

    val mDialogInputFileBox = DialogLockImgBox()

    override fun initViewObservable() {
        super.initViewObservable()

        binding.actionDiagnosis.onClick {
            println("6661")
            findNavController().navigate(R.id.action_homeFragment_to_AutoScanDeviceListFragment)

            //主要用于测试DNOx2.2系统计量喷射的准确性、检查系统是否泄漏、倒抽能力。\n在测试前,最好先读取故障码,排除潜在的后处理故障,以便更快地确定问题部件\n测试条件:钥匙打开,不启动发动机,催化剂上游温度不能高于200℃,尿素温度电压小于3V,尿素液位80%以上,气罐内压力为0

//            val mDialogHintBox = DialogHintBox()
//            mDialogHintBox.setMsg("进入系统失败\n" +
//                    "反馈编码：FDFD\n" +
//                    "通信波特率250000\n" +
//                    "ECU地址:00\n" +
//                    "模块ID:5858\n" +
//                    "处理方案：\\n1.直接使用车辆ECU线束旁边的2PIN诊断接头\\n2.拆下ECU跳线通讯，排除车辆CAN总线干扰")
//           mDialogHintBox.show(childFragmentManager,"mDialogHintBox")

//            var mDialogWebBox = DialogWebBox()
//            mDialogWebBox.setUrl("https://wx.ytobd.com/wx/errcode1018/list?content=${1117}&box=1&limit=15&page=1")
//            mDialogWebBox.show(childFragmentManager,"mDialogWebBox")
        }
        binding.actionEcuRw.onClick {
            println("6662")
            val bundle = bundleOf(FileListFragment.TAG to PathManager.getReflashFilePath())
            findNavController().navigate(R.id.action_homeFragment_to_fileListFragment,bundle)
        }
        binding.actionPpd.onClick {
            println("6663")

            mDialogInputFileBox.setBackResult{
                //mDialogInputBox.dismiss()
                visibleDialog(mDialogInputFileBox)
            }
            //mDialogInputFileBox.setActionType(CMD.FORM_INPUT).setTitle("注意：本次操作将改变ECU内部数据,请预先做好备份(使用读取数据功能进行备份)").setInitMsg("")
            //mDialogInputFileBox.setInitMsg("注意：本次操作将改变ECU内部数据,请预先做好备份(使用读取数据功能进行备份)")
            mDialogInputFileBox.show(childFragmentManager,"mDialogInputFileBox")
        }
        binding.actionCartDvs.onClick {
            println("6664")

        }
        binding.actionCartNevs.onClick {
            println("6665")
        }
        binding.actionCartNgvs.onClick {
            println("6666")
        }

        //addActionButton()
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