package com.zdyb.module_diagnosis.fragment

import android.os.Bundle
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ConvertUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zdeps.gui.CMD
import com.zdeps.gui.ITaskCallbackAdapter
import com.zdyb.ITaskCallback
import com.zdyb.lib_common.base.BaseDialogFragment
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.base.KLog
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.bean.*
import com.zdyb.module_diagnosis.databinding.FragmentMenuListBinding
import com.zdyb.module_diagnosis.dialog.DialogHintBox
import com.zdyb.module_diagnosis.dialog.DialogInputBox
import com.zdyb.module_diagnosis.dialog.DialogInputFileBox
import com.zdyb.module_diagnosis.model.LoadDiagnosisModel
import com.zdyb.module_diagnosis.widget.BottomBarActionButton
import io.reactivex.disposables.Disposable

class MenuListFragment:BaseNavFragment<FragmentMenuListBinding,LoadDiagnosisModel>() {

    lateinit var mDialogHintBox : DialogHintBox
    lateinit var mDialogInputBox: DialogInputBox
    lateinit var mDialogInputFileBox: DialogInputFileBox
    lateinit var mDeviceEntity : DeviceEntity
    override fun initViewModel(): LoadDiagnosisModel {
        val model: LoadDiagnosisModel by activityViewModels()
       return model
    }

    lateinit var rxPermission : RxPermissions
    private var dis1: Disposable? = null
    private var listString = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rxPermission = RxPermissions(requireActivity())
        //消息窗口
        mDialogHintBox = DialogHintBox()
        mDialogHintBox.setBackResult {
            val action = if (it) CMD.MB_YES else CMD.MB_NO
            viewModel.setCommonValue(CMD.ID_DIALOG_OFFSET, action)
        }
        //输入窗口
        mDialogInputBox = DialogInputBox()
        mDialogInputBox.setBackResult{
            val action = if (it.result) CMD.MB_YES else CMD.MB_NO
            val value = it.value + CMD.INPUT_VALUE_END
            viewModel.setCommonValue(CMD.ID_DIALOG_VALUE_OFFSET, value.toByteArray()[0])
            viewModel.setCommonValue(CMD.ID_DIALOG_OFFSET, action)
        }
        //文件名称输入窗口
        mDialogInputFileBox = DialogInputFileBox()
        mDialogInputFileBox.setBackResult{
            val action = if (it.result) CMD.MB_YES else CMD.MB_NO
            val value = it.value + CMD.INPUT_VALUE_END
            viewModel.setCommonValue(CMD.ID_DIALOG_VALUE_FILE_NAME, value.toByteArray()[0])
            viewModel.setCommonValue(CMD.ID_DIALOG_OFFSET, action)
        }
    }

    override fun initParam() {
        super.initParam()
        mDeviceEntity = arguments?.getSerializable(DeviceEntity.tag) as DeviceEntity
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
                        //findNavController().navigateUp()
                },

            )
            mActivity.setTitle(mDeviceEntity.name)
        }
    }

    override fun initViewObservable() {
        super.initViewObservable()

        viewModel.menuListLiveData.observe(this){
            mAdapter.setList(it)
        }

        mAdapter.setOnItemClickListener { adapter, view, position ->
            viewModel.titleLiveData.value = adapter.data[position] as String
            viewModel.setDigValue(position.toByte())
        }

        binding.recyclerView.adapter = mAdapter
        //
        viewModel.actHint = StringBuffer()

        if (viewModel.menuListLiveData.value == null){
            KLog.e("启动诊断")
            viewModel.startDiagnosis(mDeviceEntity.id,iTaskCallback,mDeviceEntity.versionPath)
        }else{
            KLog.e("恢复菜单页的监听")
            viewModel.registerCallback(iTaskCallback)
        }

    }



    private val mAdapter: BaseQuickAdapter<String, BaseViewHolder> =
        object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_menu) {
            override fun convert(holder: BaseViewHolder, s: String) {
                holder.setText(R.id.menuValue, s)
                if (holder.layoutPosition %2 != 0){
                    holder.getView<TextView>(R.id.menuValue).setBackgroundResource(R.color.item_bg)
                }else{
                    holder.getView<TextView>(R.id.menuValue).setBackgroundResource(0)
                }
            }
        }

    private val iTaskCallback: ITaskCallback.Stub = object : ITaskCallbackAdapter(){


        override fun dataInit(tag: Byte): Boolean {
            KLog.i("这里的进程id=${android.os.Process.myPid()}")
            when(tag){
                CMD.FORM_VER -> {viewModel.verData.clear()}
                CMD.FORM_DTC -> {viewModel.dtcData.clear()}
                CMD.FORM_CDS_SELECT -> {viewModel.cdsSelectData.clear()}
                CMD.FORM_ACT -> {
                    //
                    viewModel.actData.clear()
                }
                CMD.FORM_MENU -> {listString.clear()}
            }
            println("dataInit---->")
            return super.dataInit(tag)
        }

        override fun addItemOne(tag: Byte, pszMenu: String): Boolean {
            println("addItem---->$pszMenu")
            when(tag){

                CMD.FORM_DTC ->{

                }
                CMD.FORM_MENU -> {listString.add(pszMenu)}
            }
            return super.addItemOne(tag, pszMenu)
        }

        override fun addItemTwo(tag: Byte, key: String, value: String): Boolean {
            when(tag){
                CMD.FORM_VER -> {viewModel.verData.add(KVEntity(key,value))}
                CMD.FORM_CDS_SELECT -> {viewModel.cdsSelectData.add(CDSSelectEntity(key,"",value))}
            }
            return super.addItemTwo(tag, key, value)
        }

        override fun addItemThree(
            tag: Byte,
            value1: String,
            value2: String,
            value3: String
        ): Boolean {
            when(tag){
                CMD.FORM_DTC -> {viewModel.dtcData.add(DtcEntity(value1,value2,value3))}
                CMD.FORM_ACT -> {
                    viewModel.actData.add(ACTEntity(value1,value2,value3))
                    KLog.e("添加数据----${value1}----${value2}-----${value3}--------------")
                }
            }
            return super.addItemThree(tag, value1, value2, value3)
        }

        override fun addButton(tag: Byte, name: String): Boolean {
            when(tag){
                CMD.FORM_ACT -> {viewModel.actButtonData.add(name)}
            }
            return super.addButton(tag, name)
        }

        override fun addHint(tag: Byte, hint: String): Boolean {
            when(tag){
                CMD.FORM_ACT -> {
                    val temp = hint.replace("\\n", "\r\n")
                    viewModel.actHint.append(temp)
                }
            }
            return super.addHint(tag, hint)
        }

        override fun dataShow(tag: Byte): Boolean {
            println("dataShow---->")
            activity?.runOnUiThread {
                when(tag){
                    CMD.FORM_VER -> {
                        viewModel.verLiveData.value = viewModel.verData
                        findNavController().navigate(R.id.action_menuListFragment_to_VERFragment)
                    }
                    CMD.FORM_DTC ->{
                        viewModel.dtcLiveData.value = viewModel.dtcData
                        findNavController().navigate(R.id.action_menuListFragment_to_DTCFragment)
                    }
                    CMD.FORM_CDS_SELECT -> {
                        for ((i, item) in viewModel.cdsSelectData.withIndex()){
                            item.index = i //记录下标
                        }
                        viewModel.cdsSelectAllLiveData.value = viewModel.cdsSelectData
                        findNavController().navigate(R.id.action_menuListFragment_to_CDSSelectFragment)
                    }
                    CMD.FORM_ACT -> {
                        viewModel.actLiveData.value = viewModel.actData //这里没有数据 act的模式并没有遵守init add show的流程
                        viewModel.actButtonLiveData.value = viewModel.actButtonData
                        findNavController().navigate(R.id.action_menuListFragment_to_ACTFragment)
                    }
                    CMD.FORM_MENU -> {
                        if (mDialogHintBox.isVisible){
                            mDialogHintBox.dismiss()
                        }
                        viewModel.menuListLiveData.value = listString
                        mAdapter.setList(listString)
                    }
                }
                destroyDialog()
            }
            return super.dataShow(tag)
        }

        override fun viewFinish() {
            super.viewFinish()
            println("viewFinish--->")
            activity?.runOnUiThread {
                findNavController().popBackStack()
            }
        }

        override fun destroyDialog() :Long{

            activity?.runOnUiThread {
                println("destroyDialog--->")
                if (mDialogHintBox.isVisible){
                    mDialogHintBox.dismiss()
                }
                if (mDialogInputBox.isVisible){
                    mDialogInputBox.dismiss()
                }
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
                println("showDialog--->")
                when(tag){
                    CMD.MSG_MB_NOBUTTON,CMD.MSG_MB_OK,CMD.MB_NO,CMD.MSG_MB_YESNO,CMD.MSG_MB_ERROR -> {
                        if (!mDialogHintBox.isVisible && !mDialogHintBox.isShow()){
                            mDialogHintBox.setActionType(tag).setInitMsg(msg)
                            mDialogHintBox.show(childFragmentManager,"mDialogHintBox")
                            visibleDialog(mDialogInputBox,mDialogInputFileBox)
                        }else if(mDialogHintBox.isVisible){
                            mDialogHintBox.upActionType(tag)
                            mDialogHintBox.setMsg(msg)
                        }

                    }
                    CMD.FORM_INPUT ->{ //输入框
                        if (!mDialogInputBox.isVisible && !mDialogInputBox.isShow()){
                            mDialogInputBox.setActionType(type).setTitle(title).setInitMsg(msg)
                            mDialogInputBox.show(childFragmentManager,"mDialogInputBox")
                            visibleDialog(mDialogHintBox,mDialogInputFileBox)
                        }
                    }
                    CMD.FORM_FILEDIALOG ->{ //文件选择 或是 输入名称
                        //此处的title与msg 分别对应 文件存储路径与文件类型
                        val dialogType = type > 0
                        if (dialogType){
                            //文件选择

                        }else{
                            //名称输入
                            if (!mDialogInputFileBox.isVisible && !mDialogInputFileBox.isShow()){
                                mDialogInputFileBox.setInitMsg(msg)
                                mDialogInputFileBox.show(childFragmentManager,"mDialogInputFileBox")
                                visibleDialog(mDialogHintBox,mDialogInputBox)
                            }
                        }
                    }
                    else ->{
                        val temp = ByteArray(1){tag}
                        println("漏网之鱼=${ConvertUtils.bytes2HexString(temp)}")
                    }
                }
            }
            return super.showDialog(tag, type, title, msg, imgPath, color)
        }

    }


    fun visibleDialog(vararg dialogs : BaseDialogFragment){
        for (d in dialogs){
            if (d.isVisible)d.dismiss()
        }
    }
    override fun onPause() {
        super.onPause()
    }

}