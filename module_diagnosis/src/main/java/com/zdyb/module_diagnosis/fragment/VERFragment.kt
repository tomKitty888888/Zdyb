package com.zdyb.module_diagnosis.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zdeps.gui.CMD
import com.zdeps.gui.ITaskCallbackAdapter
import com.zdyb.ITaskCallback
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.bean.DeviceEntity
import com.zdyb.module_diagnosis.bean.KVEntity
import com.zdyb.module_diagnosis.databinding.FragmentVerListBinding
import com.zdyb.module_diagnosis.dialog.DialogHintBox
import com.zdyb.module_diagnosis.model.LoadDiagnosisModel
import com.zdyb.module_diagnosis.widget.BottomBarActionButton
import io.reactivex.disposables.Disposable

class VERFragment:BaseNavFragment<FragmentVerListBinding,LoadDiagnosisModel>() {

    lateinit var mDialogHintBox : DialogHintBox
    override fun initViewModel(): LoadDiagnosisModel {
        val model: LoadDiagnosisModel by activityViewModels()
       return model
    }

    lateinit var rxPermission : RxPermissions
    private var dis1: Disposable? = null
    //private var listString = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rxPermission = RxPermissions(requireActivity())
        mDialogHintBox = DialogHintBox()
    }



    override fun initActionButton() {
        super.initActionButton()
        if (activity is DiagnosisActivity){
            val mActivity = (activity as DiagnosisActivity)
            mActivity.removeAllActionButton()
//            mActivity.addLeftActionButton(
//                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_rescan,getString(R.string.action_button_rescan))
//                    .setClick {
//                        viewModel.startScan()
//                    }
//            )
            mActivity.addRightActionButton(
                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_back,getString(R.string.action_button_back))
                    .setPartitionLineVisibility(ConstraintLayout.LayoutParams.LEFT)
                    .setClick {
                        viewModel.setDigValue(CMD.ID_MENU_BACK.toByte())
                        //mDialogHintBox.show(childFragmentManager,"mDialogHintBox")
                        findNavController().navigateUp()
                },

            )
            viewModel.titleLiveData.observe(this){
                mActivity.setTitle(it)
            }

        }
    }

    override fun initViewObservable() {
        super.initViewObservable()

        viewModel.verLiveData.observe(this){
            mAdapter.setList(it)
        }

        mAdapter.setOnItemClickListener { _, _, position ->
            viewModel.setDigValue(position.toByte())
        }
        binding.recyclerView.isScrollbarFadingEnabled = false //常驻显示进度条
        binding.recyclerView.scrollBarFadeDuration = 0
        binding.recyclerView.adapter = mAdapter
        viewModel.registerCallback(iTaskCallback)
    }



    private val mAdapter: BaseQuickAdapter<KVEntity, BaseViewHolder> =
        object : BaseQuickAdapter<KVEntity, BaseViewHolder>(R.layout.item_ver) {
            override fun convert(holder: BaseViewHolder, e: KVEntity) {
                holder.setText(R.id.menuKey, e.key)
                holder.setText(R.id.menuValue, e.value)
                if (holder.layoutPosition %2 != 0){
                    holder.getView<ConstraintLayout>(R.id.itemLayout).setBackgroundResource(R.color.item_bg)
                    holder.getView<View>(R.id.partition).setBackgroundResource(R.color.white)
                }else{
                    holder.getView<ConstraintLayout>(R.id.itemLayout).setBackgroundResource(R.color.white)
                    holder.getView<View>(R.id.partition).setBackgroundResource(R.color.item_bg)
                }
            }
        }

    private val iTaskCallback: ITaskCallback.Stub = object : ITaskCallbackAdapter(){

//        override fun dataInit(tag: Byte): Boolean {
//            listString.clear()
//            println("dataInit---->")
//            return super.dataInit(tag)
//        }
//
//        override fun addItem(tag: Byte, pszMenu: String): Boolean {
//            println("addItem----ver>$pszMenu")
//            listString.add(pszMenu)
//            return super.addItem(tag, pszMenu)
//        }
//
//        override fun dataShow(tag: Byte): Boolean {
//            println("dataShow---->")
//            return super.dataShow(tag)
//        }

        override fun viewFinish() {
            super.viewFinish()
            println("viewFinish---ver>")
            activity?.runOnUiThread {
                findNavController().popBackStack()
            }
        }

        override fun destroyDialog() :Long{

            activity?.runOnUiThread {
                println("destroyDialog---ver>")
                if (mDialogHintBox.isVisible)mDialogHintBox.dismiss()
            }
            return super.destroyDialog()
        }

        override fun showDialog(
            tag: Byte,
            type: Byte,
            title: String?,
            msg: String?,
            imgPath: String?,
            color: Long
        ) :Long{

            activity?.runOnUiThread {
                println("destroyDialog---ver>")
                when (tag){
                    CMD.MSG_MB_NOBUTTON,CMD.MSG_MB_OK,CMD.MB_NO,CMD.MSG_MB_YESNO -> {
                        if (!mDialogHintBox.isVisible && !mDialogHintBox.isShow()){
                            mDialogHintBox.setActionType(tag).setInitMsg(msg)
                            mDialogHintBox.show(childFragmentManager,"mDialogHintBox")
                        }else if(mDialogHintBox.isVisible){
                            mDialogHintBox.setMsg(msg)
                        }
                    }
                }
            }
            return super.showDialog(tag, type, title, msg, imgPath, color)
        }
    }

}