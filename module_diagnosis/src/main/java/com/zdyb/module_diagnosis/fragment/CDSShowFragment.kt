package com.zdyb.module_diagnosis.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdeps.gui.CMD
import com.zdeps.gui.ITaskCallbackAdapter
import com.zdyb.ITaskCallback
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.widget.click.ViewClickUtil
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.bean.CDSSelectEntity
import com.zdyb.module_diagnosis.bean.DeviceEntity
import com.zdyb.module_diagnosis.databinding.FragmentCdsShowBinding
import com.zdyb.module_diagnosis.dialog.DialogHintBox
import com.zdyb.module_diagnosis.help.BottomDeviceCmd
import com.zdyb.module_diagnosis.help.CDSHelp
import com.zdyb.module_diagnosis.model.LoadDiagnosisModel
import com.zdyb.module_diagnosis.widget.BottomBarActionButton
import java.util.concurrent.TimeUnit

class CDSShowFragment:BaseNavFragment<FragmentCdsShowBinding,LoadDiagnosisModel>() {

    lateinit var mDialogHintBox : DialogHintBox

    val cdsSelectLiveData = mutableSetOf<CDSSelectEntity>() //由于初始化一次 所以这里使用set装载数据
    val cdsSelectData = mutableSetOf<String>() //由于初始化一次 所以这里使用set装载数据

    var scrollViewState: Int = RecyclerView.SCROLL_STATE_IDLE
    var selectType = "" // 0查看全部  1查看部分

    override fun initViewModel(): LoadDiagnosisModel {
        val model: LoadDiagnosisModel by activityViewModels()
       return model
    }

    override fun initParam() {
        super.initParam()
        selectType = arguments?.getString("selectType").toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDialogHintBox = DialogHintBox()
        mDialogHintBox.setBackResult {
            val action = if (it) CMD.MB_YES else CMD.MB_NO
            viewModel.setCommonValue(CMD.ID_DIALOG_OFFSET, action)
        }
        mDialogHintBox.setHomeBackResult{
            println("HomeBackResult--执行到")
            BottomDeviceCmd.closeBottomDevice()
            BaseApplication.getInstance().outDiagnosisService = true
            findNavController().popBackStack(R.id.homeFragment,false)
        }
    }



    override fun initActionButton() {
        super.initActionButton()
        if (activity is DiagnosisActivity){
            val mActivity = (activity as DiagnosisActivity)
            mActivity.removeAllActionButton()
            mActivity.addLeftActionButton(
//                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_storage,getString(R.string.cds_start_save_data_to_file))
//                    .setClick {
//                        val dialog = AlertDialog.Builder(mActivity)
//                        dialog.setTitle(mActivity.getString(R.string.cds_save_data_to_file))
//                        dialog.setMessage(mActivity.getString(R.string.cds_save_data))
//                        dialog.setPositiveButton(mActivity.getString(R.string.confirm)) { dialog, _ ->
//
//                            val name = viewModel.deviceList.value?.get(0)?.name
//                            CDSHelp.createFile(name)
//                            viewModel.cdsSelectLiveData.value?.let { it1 -> CDSHelp.writeTitle(it1) }
//                            dialog.dismiss()
//                        }
//                        dialog.setNegativeButton(mActivity.getString(R.string.cancel)) { dialog, _ ->
//                            dialog.dismiss()
//                        }
//                        dialog.show()
//                    }
//            ,
//                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_cds_end,getString(R.string.cds_end_save_data_to_file))
//                    .setClick {
//                        CDSHelp.closeFile()
//                        if (CDSHelp.closeFile()){
//                            viewModel.showToast(mActivity.getString(R.string.cds_end_sampling))
//                        }
//                    }

//                ,
//                BottomBarActionButton(activity).addValue(0,getString(R.string.cds_lock_file))
//                    .setClick {
//                        //文件查看
//                        startActivity(Intent(requireContext(),CSVFileActivity::class.java))
//                    }
            )


            val button1 = BottomBarActionButton(activity).addValue(R.mipmap.icon_d_back,getString(R.string.action_button_back))
                .setPartitionLineVisibility(ConstraintLayout.LayoutParams.LEFT)
             val but1Click = View.OnClickListener {
                 mDialogHintBox.setActionType(CMD.MSG_MB_NOBUTTON).setInitMsg(getString(R.string.act_out_ing))
                 mDialogHintBox.show(childFragmentManager,"mDialogHintBox")
                 CDSHelp.closeFile()
                 KLog.e("测试被点击")
                 viewModel.setCommonValue(CMD.ID_CDS_BACK_OFFSET,CMD.ID_MENU_BACK.toByte())

             }
            button1.setOnClickListener(but1Click)
            ViewClickUtil.Builder()
                .setSkipDuration(6000)
                .setTimeUnit(TimeUnit.MILLISECONDS)
                .setType(ViewClickUtil.Type.VIEW)
                .build().clicks(but1Click, button1)

            //添加按钮
            mActivity.addRightActionButton(
                button1
            )
            mActivity.setTitle(getString(R.string.cds_lock_data))
//            viewModel.titleLiveData.observe(this){
//
//            }

        }
    }

    var mFirstVisiblePosition = 0
    var mLastVisiblePosition = 0

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
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                scrollViewState = newState

                if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // DES: 找出当前可视Item位置
                    val layoutManager = binding.recyclerView.layoutManager
                    if (layoutManager is LinearLayoutManager) {
                        mFirstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                        mLastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                    }
                }

                if (newState == RecyclerView.SCROLL_STATE_IDLE){

                    println("滑动停止了==")
                    //重新设置查看
                    viewModel.setCommonValue(CMD.ID_CDS_UP_DATA,CMD.ID_CDS_UP_DATA_VALUE.toByte())

                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        viewModel.registerCallback(iTaskCallback)
        viewModel.setDigValue(CMD.ID_CDS_VIEW.toByte())
    }

    fun s(){


        try {
            println("当前可视化位置 start=$mFirstVisiblePosition     end=$mLastVisiblePosition")
            if (mFirstVisiblePosition == 0 && mLastVisiblePosition == 0){
                if (mAdapter.data.size > 9){
                    mLastVisiblePosition = 9
                }else{
                    mLastVisiblePosition = mAdapter.data.size
                }

            }
            val tempData = mAdapter.data.subList(mFirstVisiblePosition,mLastVisiblePosition)


        }catch (e :Exception){
            e.printStackTrace()
        }
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
//            println("dataInit---- 会过来？？>")
//            return super.dataInit(tag)
//        }

        override fun addDataStream(tag: Byte, index: Int, key: String, value: String?): Boolean {

            try {
                when(tag){
                    CMD.FORM_CDS ->{
                        println("添加数据---key=$key---value=$value")

//                        if (viewModel.cdsSelectLiveData.value!!.size == viewModel.cdsSelectAllLiveData.value!!.size){
//                            //如果是查看全部的数据流 避开循环提升效率
//                            val item = viewModel.cdsSelectLiveData.value!![index]
//                            item.value2 = key
//                            cdsSelectLiveData.add(item)
//                        }else{
//                            viewModel.cdsSelectLiveData.value?.let {
//                                for (item in it){
//                                    if (item.index == index){
//                                        item.value2 = key //此时的key是数据结果
//                                        cdsSelectLiveData.add(item)
//                                    }
//                                }
//                            }
//                        }

                        try {
                            if (selectType == "0"){
                                mAdapter.data[index].value2 = key
                            }else{
                                for (item in mAdapter.data){
                                    if (item.index == index){
                                        item.value2 = key //此时的key是数据结果
                                    }
                                }
                            }

                        }catch (e :Exception){
                            e.printStackTrace()
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
                    //cdsSelectLiveData.add(CDSSelectEntity(value1,value2,value3))
                    //cdsSelectData.add(value1)
                }
            }
            return super.addItemThree(tag, value1, value2, value3)
        }
        override fun dataShow(tag: Byte): Boolean {
            println("dataShow---->")
            requireActivity().runOnUiThread {
                when(tag){
                    CMD.FORM_CDS ->{

                        if (scrollViewState == RecyclerView.SCROLL_STATE_IDLE){
                            //mAdapter.setList(cdsSelectLiveData)
                            //mAdapter.notifyItemChanged(index)
                            mAdapter.notifyDataSetChanged()
                        }
                        CDSHelp.writeValue(cdsSelectLiveData)

                    }
                    CMD.FORM_CDS_SELECT ->{
                        //这个时候才退出
                        findNavController().navigateUp()
                    }
                    CMD.FORM_MENU ->{ //普通菜单
                        //这里是返回两层
                        findNavController().popBackStack(R.id.menuListFragment,false)
                    }
                }
            }
            return super.dataShow(tag)
        }

        override fun getByteData(tag: Byte): ByteArray {

            try {
                if (mAdapter.data.size == 0){
                    return ByteArray(0)
                }
                if (mFirstVisiblePosition == 0 && mLastVisiblePosition == 0){
                    if (mAdapter.data.size > 12){
                        mLastVisiblePosition = 12
                    }else{
                        mLastVisiblePosition = mAdapter.data.size
                    }

                }else{
                    if ((mLastVisiblePosition +2)<= mAdapter.data.size){
                        mLastVisiblePosition += 2
                    }else{
                        mLastVisiblePosition = mAdapter.data.size
                    }
                }
                val tempData = mAdapter.data.subList(mFirstVisiblePosition,mLastVisiblePosition)

                //val temp = viewModel.cdsSelectLiveData.value
                val temp = tempData
                val data = ByteArray(temp!!.size)
                for ((i, item) in temp.withIndex()){
                    data[i] = item.index.toByte()
                }
                return data
            }catch (e : Exception){
                e.printStackTrace()
            }
            return ByteArray(0)
        }

        override fun viewFinish() {
            super.viewFinish()
            println("viewFinish---cdsShow>")
            requireActivity().runOnUiThread {
                findNavController().popBackStack()
            }
        }

        override fun destroyDialog() :Long{

            requireActivity().runOnUiThread {
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

            requireActivity().runOnUiThread {
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