package com.zdyb.module_diagnosis.fragment

import android.os.Bundle
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ConvertUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zdeps.gui.CMD
import com.zdeps.gui.ITaskCallbackAdapter
import com.zdyb.ITaskCallback
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseDialogFragment
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.base.KLog
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.bean.*
import com.zdyb.module_diagnosis.databinding.FragmentMenuListBinding
import com.zdyb.module_diagnosis.dialog.DialogChooseFileBox
import com.zdyb.module_diagnosis.dialog.DialogHintBox
import com.zdyb.module_diagnosis.dialog.DialogInputBox
import com.zdyb.module_diagnosis.dialog.DialogInputFileBox
import com.zdyb.module_diagnosis.help.BottomDeviceCmd
import com.zdyb.module_diagnosis.model.LoadDiagnosisModel
import com.zdyb.module_diagnosis.widget.BottomBarActionButton
import io.reactivex.disposables.Disposable
import java.util.concurrent.locks.ReentrantLock

class MenuListFragment:BaseNavFragment<FragmentMenuListBinding,LoadDiagnosisModel>() {

    lateinit var mDialogHintBox : DialogHintBox //文字消息提示
    lateinit var mDialogInputBox: DialogInputBox //内容输入
    lateinit var mDialogInputFileBox: DialogInputFileBox //输入文件名称
    lateinit var mDialogChooseFileBox: DialogChooseFileBox //选择文件

    lateinit var mDeviceEntity : DeviceEntity
    override fun initViewModel(): LoadDiagnosisModel {
        val model: LoadDiagnosisModel by activityViewModels()
       return model
    }

    lateinit var rxPermission : RxPermissions
    private var dis1: Disposable? = null
    private var listString = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rxPermission = RxPermissions(requireActivity())


        //消息窗口
        mDialogHintBox = DialogHintBox()
        mDialogHintBox.setHomeBackResult{
            println("HomeBackResult--执行到")
            BottomDeviceCmd.closeBottomDevice()
            BaseApplication.getInstance().outDiagnosisService = true
            findNavController().popBackStack(R.id.JCHomeFragment,false)
        }
        mDialogHintBox.setBackResult {
            val action = if (it) CMD.MB_YES else CMD.MB_NO
            viewModel.setCommonValue(CMD.ID_DIALOG_OFFSET, action)
        }
        //输入窗口
        mDialogInputBox = DialogInputBox()
        mDialogInputBox.setBackResult{
            val action = if (it.result) CMD.MB_YES else CMD.MB_NO
            val value = it.value + CMD.INPUT_VALUE_END
            viewModel.setCommonValueToArray(CMD.ID_DIALOG_VALUE_OFFSET, value)
            viewModel.setCommonValue(CMD.ID_DIALOG_OFFSET, action)
        }
        //文件名称输入窗口
        mDialogInputFileBox = DialogInputFileBox()
        mDialogInputFileBox.setBackResult{
            val action = if (it.result) CMD.MB_YES else CMD.MB_NO
            val value = it.value + CMD.INPUT_VALUE_END
            viewModel.setCommonValueToArray(CMD.ID_DIALOG_VALUE_FILE_NAME, value)
            viewModel.setCommonValue(CMD.ID_DIALOG_OFFSET, action)
        }
        //文件选择窗口
        mDialogChooseFileBox = DialogChooseFileBox()
        mDialogChooseFileBox.setBackResult{
            val action = if (it.result) CMD.MB_YES else CMD.MB_NO
            val value = it.value + CMD.INPUT_VALUE_END
            viewModel.setCommonValueToArray(CMD.ID_DIALOG_VALUE_FILE_NAME, value)
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
                        mActivity.setTitle(mDeviceEntity.name)
                        viewModel.setDigValue(CMD.ID_MENU_BACK.toByte())
                        //mDialogHintBox.show(childFragmentManager,"mDialogHintBox")
                        //findNavController().navigateUp()
                },

            )

            viewModel.titleLiveData.observe(this){
                //mActivity.setTitle(it)//多层menu菜单标题
            }
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

        if (viewModel.menuListLiveData.value == null || viewModel.menuListLiveData.value!!.isEmpty()){
            KLog.e("启动诊断")
            viewModel.startDiagnosis(mDeviceEntity.id,iTaskCallback,mDeviceEntity.versionPath)
        }else{
            KLog.e("恢复菜单页的监听")
            viewModel.registerCallback(iTaskCallback)
        }

        //显示版本号
        val path = mDeviceEntity.versionPath
        val versionTagIndex = path.indexOf("V")
        val soVersionString = path.substring(versionTagIndex,path.length-1)
        binding.soVersion.text = soVersionString

        //动作测试初始化 必须要有
        viewModel.setCommonValue(CMD.ID_ACT_BACK_OFFSET,CMD.ID_ACT_INIT.toByte())
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

    //var actLock = false
    private val iTaskCallback: ITaskCallback.Stub = object : ITaskCallbackAdapter(){


        override fun dataInit(tag: Byte): Boolean {
            KLog.i("这里的进程id=${android.os.Process.myPid()}")
            when(tag){
                CMD.FORM_VER -> {
                    //if (actLock)return super.dataInit(tag)
                    viewModel.verData.clear()
                }
                CMD.FORM_DTC -> {viewModel.dtcData.clear()}
                CMD.FORM_CDS_SELECT -> {viewModel.cdsSelectData.clear()}
                CMD.FORM_ACT -> {
                    //
                    //if (actLock)return super.dataInit(tag)
                    //viewModel.actData.clear()
                    viewModel.actButtonData.clear()

                    KLog.e("提示信息清空了")
                }
                CMD.FORM_MENU -> {

                    listString.clear()
                    KLog.d("清除菜单了")
                }
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
                CMD.FORM_VER -> {
                    //if (actLock)return super.dataInit(tag)
                    viewModel.verData.add(KVEntity(key,value))
                }
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
                    //if (actLock)return super.addItemThree(tag, value1, value2, value3)
                    //viewModel.actData.add(ACTEntity(value1,value2,value3))
                    KLog.e("添加数据----${value1}----${value2}-----${value3}--------------")
                }
            }
            return super.addItemThree(tag, value1, value2, value3)
        }

        override fun addButton(tag: Byte, name: String): Boolean {
            when(tag){
                CMD.FORM_ACT -> {
                    println("m-addButton--->$name")
                    //if (actLock)return super.addButton(tag, name)
                    viewModel.actButtonData.add(name)
                }
            }
            return super.addButton(tag, name)
        }

        override fun addHint(tag: Byte, hint: String): Boolean {
            when(tag){
                CMD.FORM_ACT -> {
                    println("m-提示信息=$hint")
                    val temp = hint.replace("\\n", "\r\n")
                    viewModel.actHint.append(temp)
                }
            }
            return super.addHint(tag, hint)
        }

        override fun dataShow(tag: Byte): Boolean {
            println("dataShow---->")
            val lock = ReentrantLock()
            requireActivity().runOnUiThread {
                try {
                    lock.lock()
                    when(tag){
                        CMD.FORM_VER -> {
                            viewModel.removeCallback()
                            viewModel.verLiveData.value = viewModel.verData
                            val bundle = bundleOf(DeviceEntity.tag to mDeviceEntity)
                            findNavController().navigate(R.id.action_menuListFragment_to_VERFragment,bundle)
                        }
                        CMD.FORM_DTC ->{
                            viewModel.removeCallback()
                            viewModel.dtcLiveData.value = viewModel.dtcData
                            val bundle = bundleOf(DeviceEntity.tag to mDeviceEntity)
                            findNavController().navigate(R.id.action_menuListFragment_to_DTCFragment,bundle)
                        }
                        CMD.FORM_CDS_SELECT -> {
                            viewModel.removeCallback()
                            for ((i, item) in viewModel.cdsSelectData.withIndex()){
                                item.index = i //记录下标
                            }
                            viewModel.cdsSelectAllLiveData.value = viewModel.cdsSelectData
                            val bundle = bundleOf(DeviceEntity.tag to mDeviceEntity)
                            findNavController().navigate(R.id.action_menuListFragment_to_CDSSelectFragment,bundle)
                            val size = viewModel.menuListLiveData.value!!.size
                            KLog.d("数量$size")
                        }
                        CMD.FORM_ACT -> {
                            viewModel.removeCallback()
                            synchronized(viewModel.actButtonData){

                                //viewModel.actLiveData.value = viewModel.actData //这里没有数据 act的模式并没有遵守init add show的流程
                                viewModel.actButtonLiveData.value = viewModel.actButtonData
                                viewModel.actButtonData.toTypedArray().forEach { println(it) }
                                KLog.e("传过去的button=${viewModel.actButtonData.size}")
                                findNavController().navigate(R.id.action_menuListFragment_to_ACTFragment)
                            }

                        }
                        CMD.FORM_MENU -> {
                            if (mDialogHintBox.isVisible){
                                mDialogHintBox.dismiss()
                            }

                            val size = listString.size
                            viewModel.menuListLiveData.value = listString
                            println("检查这里！！$size")
                            mAdapter.setList(listString)
                        }
                    }
                    destroyDialog()
                    lock.unlock()
                }catch (e :Exception){
                    e.printStackTrace()
                }
            }
            return super.dataShow(tag)
        }

        override fun viewFinish() {
            super.viewFinish()
            println("viewFinish--->")
            requireActivity().runOnUiThread {
                findNavController().popBackStack()
            }
        }

        override fun destroyDialog() :Long{

            requireActivity().runOnUiThread {
                println("destroyDialog--->")
                if (mDialogHintBox.isVisible || mDialogHintBox.isShow()){
                    mDialogHintBox.dismiss()
                    return@runOnUiThread
                }
                if (mDialogInputBox.isVisible){
                    mDialogInputBox.dismiss()
                }
                if (mDialogChooseFileBox.isVisible){
                    mDialogChooseFileBox.dismiss()
                }
                if (mDialogInputFileBox.isVisible){
                    mDialogInputFileBox.dismiss()
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
            requireActivity().runOnUiThread {
                println("showDialog--->")
                when(tag){
                    CMD.MSG_MB_NOBUTTON,CMD.MSG_MB_OK,CMD.MB_NO,CMD.MSG_MB_YESNO,CMD.MSG_MB_ERROR -> {
                        if (!mDialogHintBox.isVisible && !mDialogHintBox.isShow()){
                            mDialogHintBox.setActionType(tag).setInitMsg(msg)
                            mDialogHintBox.show(childFragmentManager,"mDialogHintBox")
                            visibleDialog(mDialogInputBox,mDialogInputFileBox,mDialogChooseFileBox)
                        }else if(mDialogHintBox.isVisible){
                            mDialogHintBox.upActionType(tag)
                            mDialogHintBox.setMsg(msg)
                        }

                    }
                    CMD.FORM_INPUT ->{ //输入框
                        if (!mDialogInputBox.isVisible && !mDialogInputBox.isShow()){
                            mDialogInputBox.setActionType(type).setTitle(title).setInitMsg(msg)
                            mDialogInputBox.show(childFragmentManager,"mDialogInputBox")
                            visibleDialog(mDialogHintBox,mDialogInputFileBox,mDialogChooseFileBox)
                        }
                    }
                    CMD.FORM_FILEDIALOG ->{ //文件选择 或是 输入名称
                        //此处的title与msg 分别对应 文件存储路径与文件类型
                        val dialogType = type > 0
                        if (dialogType){
                            //文件选择
                            if (!mDialogChooseFileBox.isVisible && !mDialogChooseFileBox.isShow()){
                                mDialogChooseFileBox.setInitPath(title,msg)
                                mDialogChooseFileBox.show(childFragmentManager,"mDialogChooseFileBox")
                                visibleDialog(mDialogHintBox,mDialogInputBox)
                            }

                        }else{
                            //名称输入
                            if (!mDialogInputFileBox.isVisible && !mDialogInputFileBox.isShow()){
                                mDialogInputFileBox.setInitMsg(title)
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