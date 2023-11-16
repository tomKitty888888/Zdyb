package com.zdyb.module_diagnosis.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdeps.gui.CMD
import com.zdeps.gui.ITaskCallbackAdapter
import com.zdyb.ITaskCallback
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.widget.NewNestedScrollView
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.bean.CDSGroupingEntity
import com.zdyb.module_diagnosis.bean.CDSGroupingListEntity
import com.zdyb.module_diagnosis.bean.CDSSelectEntity
import com.zdyb.module_diagnosis.databinding.FragmentCdsShowBinding
import com.zdyb.module_diagnosis.dialog.DialogHintBox
import com.zdyb.module_diagnosis.help.CDSHelp
import com.zdyb.module_diagnosis.model.LoadDiagnosisModel
import com.zdyb.module_diagnosis.widget.BottomBarActionButton

class CDSShowFragment:BaseNavFragment<FragmentCdsShowBinding,LoadDiagnosisModel>() {

    lateinit var mDialogHintBox : DialogHintBox

    val cdsSelectLiveData = mutableSetOf<CDSSelectEntity>() //由于初始化一次 所以这里使用set装载数据
    var scrollViewState: Int = RecyclerView.SCROLL_STATE_IDLE


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
            mActivity.addLeftActionButton(
                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_storage,getString(R.string.cds_start_save_data_to_file))
                    .setClick {
                        val dialog = AlertDialog.Builder(mActivity)
                        dialog.setTitle(mActivity.getString(R.string.cds_save_data_to_file))
                        dialog.setMessage(mActivity.getString(R.string.cds_save_data))
                        dialog.setPositiveButton(mActivity.getString(R.string.confirm)) { dialog, _ ->

                            val name = viewModel.deviceList.value?.get(0)?.name
                            CDSHelp.createFile(name)
                            viewModel.cdsSelectLiveData.value?.let { it1 -> CDSHelp.writeTitle(it1) }
                            dialog.dismiss()
                        }
                        dialog.setNegativeButton(mActivity.getString(R.string.cancel)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        dialog.show()
                    }
            ,
                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_cds_end,getString(R.string.cds_end_save_data_to_file))
                    .setClick {
                        CDSHelp.closeFile()
                        if (CDSHelp.closeFile()){
                            viewModel.showToast(mActivity.getString(R.string.cds_end_sampling))
                        }
                    }
            )
            mActivity.addRightActionButton(
                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_back,getString(R.string.action_button_back))
                    .setPartitionLineVisibility(ConstraintLayout.LayoutParams.LEFT)
                    .setClick {
                        CDSHelp.closeFile()
                        viewModel.setCommonValue(CMD.ID_CDS_BACK_OFFSET,CMD.ID_MENU_BACK.toByte())
                        findNavController().navigateUp()
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

        viewModel.cdsSelectLiveData.observe(this){
            mAdapter.setList(it)
        }

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
                //RecyclerView.SCROLL_STATE_IDLE
                //RecyclerView.SCROLL_STATE_DRAGGING
                //RecyclerView.SCROLL_STATE_SETTLING
                scrollViewState = newState
            }
        })


        viewModel.registerCallback(iTaskCallback)
        viewModel.setDigValue(CMD.ID_CDS_VIEW.toByte())
    }



     private val mAdapter: BaseQuickAdapter<CDSSelectEntity, BaseViewHolder> =
        object : BaseQuickAdapter<CDSSelectEntity, BaseViewHolder>(R.layout.item_cds_three) {

            override fun convert(holder: BaseViewHolder, item: CDSSelectEntity) {

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



    private val iTaskCallback: ITaskCallback.Stub = object : ITaskCallbackAdapter(){

//        override fun dataInit(tag: Byte): Boolean {
//            listString.clear()
//            println("dataInit---->")
//            return super.dataInit(tag)
//        }

        override fun addDataStream(tag: Byte, index: Int, key: String, value: String?): Boolean {

            try {
                when(tag){
                    CMD.FORM_CDS ->{
                        println("添加数据---key=$key---value=$value")

                        if (viewModel.cdsSelectLiveData.value!!.size == viewModel.cdsSelectAllLiveData.value!!.size){
                            //如果是查看全部的数据流 避开循环提升效率
                            val item = viewModel.cdsSelectLiveData.value!![index]
                            item.value2 = key
                            cdsSelectLiveData.add(item)
                        }else{
                            viewModel.cdsSelectLiveData.value?.let {
                                for (item in it){
                                    if (item.index == index){
                                        item.value2 = key //此时的key是数据结果
                                        cdsSelectLiveData.add(item)
                                    }
                                }
                            }
                        }
                    }
                }
            }catch (e :Exception){
                e.printStackTrace()
            }
            return super.addDataStream(tag, index, key, value)
        }

        override fun addItemThree(
            tag: Byte,
            value1: String,
            value2: String,
            value3: String
        ): Boolean {
            when(tag){
                CMD.FORM_CDS ->{
                    cdsSelectLiveData.add(CDSSelectEntity(value1,value2,value3))
                }
            }
            return super.addItemThree(tag, value1, value2, value3)
        }
        override fun dataShow(tag: Byte): Boolean {
            println("dataShow---->")
            activity?.runOnUiThread {
                when(tag){
                    CMD.FORM_CDS ->{

                        if (scrollViewState == RecyclerView.SCROLL_STATE_IDLE){
                            mAdapter.setList(cdsSelectLiveData)
                        }
                        CDSHelp.writeValue(cdsSelectLiveData)

                    }
                }
            }
            return super.dataShow(tag)
        }

        override fun getByteData(tag: Byte): ByteArray {
            val temp = viewModel.cdsSelectLiveData.value
            val data = ByteArray(temp!!.size)
            for ((i, item) in temp.withIndex()){
                data[i] = item.index.toByte()
            }
            return data
        }

        override fun viewFinish() {
            super.viewFinish()
            println("viewFinish---cdsShow>")
            activity?.runOnUiThread {
                findNavController().popBackStack()
            }
        }

        override fun destroyDialog() :Long{

            activity?.runOnUiThread {
                println("destroyDialog---cdsShow>")
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
                println("showDialog---cdsShow>")
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
        viewModel.cdsSelectLiveData.value = null
        super.onDestroyView()
    }

}