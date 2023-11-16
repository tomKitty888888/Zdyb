package com.zdyb.module_diagnosis.fragment

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdeps.gui.CMD
import com.zdeps.gui.ITaskCallbackAdapter
import com.zdyb.ITaskCallback
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.base.KLog
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.bean.ACTEntity
import com.zdyb.module_diagnosis.databinding.FragmentActionTestBinding
import com.zdyb.module_diagnosis.dialog.DialogHintBox
import com.zdyb.module_diagnosis.model.LoadDiagnosisModel
import com.zdyb.module_diagnosis.widget.BottomBarActionButton

class ACTFragment:BaseNavFragment<FragmentActionTestBinding,LoadDiagnosisModel>() {

    lateinit var mDialogHintBox : DialogHintBox

    //val buttonList = mutableSetOf<String>()
    val mActData = mutableListOf<ACTEntity>()

    val hintStringBuffer :StringBuffer = StringBuffer()
    var scrollViewState: Int = RecyclerView.SCROLL_STATE_IDLE
    var butScrollViewState: Int = RecyclerView.SCROLL_STATE_IDLE
    var maxActSize = 0
    var isBack = false

    override fun initViewModel(): LoadDiagnosisModel {
        val model: LoadDiagnosisModel by activityViewModels()
       return model
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDialogHintBox = DialogHintBox()
        mDialogHintBox.setBackResult {
            val action = if (it) CMD.MB_YES else CMD.MB_NO
            viewModel.setCommonValue(CMD.ID_DIALOG_OFFSET, action)
        }
    }



    override fun initActionButton() {
        super.initActionButton()
        if (activity is DiagnosisActivity){
            val mActivity = (activity as DiagnosisActivity)
            mActivity.removeAllActionButton()
//            mActivity.addLeftActionButton(
//                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_storage,getString(R.string.cds_start_save_data_to_file))
//                    .setClick {
//
//                    }
//
//            )
            mActivity.addRightActionButton(
                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_back,getString(R.string.action_button_back))
                    .setPartitionLineVisibility(ConstraintLayout.LayoutParams.LEFT)
                    .setClick {
                        val backIndex = mButtonAdapter.data.size-1
                        viewModel.setCommonValue(CMD.ID_ACT_BACK_OFFSET,backIndex.toByte())
                        KLog.e("退出动作测试")
                        isBack = true
                        mDialogHintBox.setActionType(CMD.MSG_MB_NOBUTTON).setInitMsg(getString(R.string.act_out_ing))
                        mDialogHintBox.show(childFragmentManager,"mDialogHintBox")
                },

            )
            mActivity.setTitle(getString(R.string.cds_lock_data))
//            viewModel.titleLiveData.observe(this){
//
//            }

        }
    }

    override fun initViewObservable() {
        super.initViewObservable()


        viewModel.actLiveData.observe(this){
            mAdapter.setList(it)
        }
        viewModel.actButtonLiveData.observe(this){
            mButtonAdapter.setList(it)
        }
        binding.message.text = viewModel.actHint.toString()

        mAdapter.setOnItemClickListener { adapter, _, position ->

        }

        binding.recyclerView.adapter = mAdapter
        binding.recyclerView.isScrollbarFadingEnabled = false //常驻显示进度条
        binding.recyclerView.scrollBarFadeDuration = 0
        binding.recyclerView.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                scrollViewState = newState
            }
        })



        mButtonAdapter.addChildClickViewIds(R.id.button)
        mButtonAdapter.setOnItemChildClickListener { adapter, view, position ->
            viewModel.setCommonValue(CMD.ID_ACT_BACK_OFFSET,position.toByte())
            if (position == adapter.data.size-1){
                isBack = true
                mDialogHintBox.setActionType(CMD.MSG_MB_NOBUTTON).setInitMsg(getString(R.string.act_out_ing))
                mDialogHintBox.show(childFragmentManager,"mDialogHintBox")
            }
        }
        binding.buttonRecyclerView.adapter = mButtonAdapter
        binding.buttonRecyclerView.isScrollbarFadingEnabled = false //常驻显示进度条
        binding.buttonRecyclerView.scrollBarFadeDuration = 0
        binding.buttonRecyclerView.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                butScrollViewState = newState
            }
        })

        viewModel.registerCallback(iTaskCallback)
        //viewModel.setDigValue(CMD.ID_CDS_VIEW.toByte())
    }



     private val mAdapter: BaseQuickAdapter<ACTEntity, BaseViewHolder> =
        object : BaseQuickAdapter<ACTEntity, BaseViewHolder>(R.layout.item_act_three) {

            override fun convert(holder: BaseViewHolder, item: ACTEntity) {

                holder.setText(R.id.valueOne, item.value1)
                holder.setText(R.id.valueTwo, item.value2)
                holder.setText(R.id.valueThree, item.value3)

                if (holder.layoutPosition %2 != 0){
                    holder.getView<ConstraintLayout>(R.id.itemLayout).setBackgroundResource(R.color.item_bg)
                    holder.getView<View>(R.id.partition).setBackgroundResource(R.color.white)
                    holder.getView<View>(R.id.partitionTwo).setBackgroundResource(R.color.white)
                }else{
                    holder.getView<ConstraintLayout>(R.id.itemLayout).setBackgroundResource(R.color.white)
                    holder.getView<View>(R.id.partition).setBackgroundResource(R.color.item_bg)
                    holder.getView<View>(R.id.partitionTwo).setBackgroundResource(R.color.item_bg)
                }

            }
        }

    private val mButtonAdapter: BaseQuickAdapter<String, BaseViewHolder> =
        object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_action_test_button) {

            override fun convert(holder: BaseViewHolder, item: String) {
                holder.setText(R.id.button, item)
            }
        }

    private val iTaskCallback: ITaskCallback.Stub = object : ITaskCallbackAdapter(){

        override fun dataInit(tag: Byte): Boolean {
            when(tag){
                CMD.FORM_ACT ->{
                    viewModel.actButtonData.clear()
                    println("dataInit--->")
                }
            }
            return super.dataInit(tag)
        }
        override fun addButton(tag: Byte, name: String): Boolean {
            when(tag){
                CMD.FORM_ACT ->{
                    println("addButton--->$name")
                    viewModel.actButtonData.add(name)
                }
            }
            return super.addButton(tag, name)
        }


        override fun addHint(tag: Byte, hint: String): Boolean {

            activity?.runOnUiThread {
                println("提示信息=$hint")
                val temp = hint.replace("\\n", "\r\n")
                viewModel.actHint.append(temp).append("\n")
                binding.message.text = viewModel.actHint.toString()
            }
            return super.addHint(tag, hint)
        }
        override fun addItemThree(
            tag: Byte,
            value1: String,
            value2: String,
            value3: String
        ): Boolean {
            println("addItemThree----act----${value1}----${value2}----${value3}---->")
            when(tag){
                //act的模式并没有遵守init add show的流程，经过反复测试结论 这个流程只有第一次的时候会将列表数据传递过来，后续只会传递发生变动的数据
                CMD.FORM_ACT ->{
                    if (maxActSize == 0){
                        viewModel.actData.add(ACTEntity(value1,value2,value3))
                    }else{
                        for (item in viewModel.actData){
                            if (item.value1 == value1){
                                item.value2 = value2
                                item.value3 = value3
                            }
                        }
                    }
                }
            }
            return super.addItemThree(tag, value1, value2, value3)
        }
        override fun dataShow(tag: Byte): Boolean {
            println("dataShow----act>")
            activity?.runOnUiThread {
                when(tag){
                    CMD.FORM_ACT ->{
                        if (scrollViewState == RecyclerView.SCROLL_STATE_IDLE){
                            maxActSize = viewModel.actData.size
                            mAdapter.setList(viewModel.actData)
                        }
                        if (butScrollViewState == RecyclerView.SCROLL_STATE_IDLE){
                            mButtonAdapter.setList(viewModel.actButtonData)
                        }
                    }
                    CMD.FORM_MENU ->{
                        //此处退出 诊断的流程不完整,退出过快会 但是act未能及时关闭 导致数据还会传过来一轮init,add,show的数据，形成重复进入页面的问题；但是结束后它会走一遍showMenu
                        if (isBack){
                            findNavController().navigateUp()
                        }

                    }
                }
            }
            return super.dataShow(tag)
        }


        override fun viewFinish() {
            super.viewFinish()
            println("viewFinish---act>")
            activity?.runOnUiThread {
                findNavController().popBackStack()
            }
        }

        override fun destroyDialog() :Long{

            activity?.runOnUiThread {
                println("destroyDialog---act>")
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
                println("showDialog---act>")
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

    override fun onDestroyView() {
        if (mDialogHintBox.isVisible){
            mDialogHintBox.dismiss()
        }
        viewModel.actData.clear()
        viewModel.actLiveData.value = null
        viewModel.actButtonLiveData.value = null
        super.onDestroyView()
    }

}