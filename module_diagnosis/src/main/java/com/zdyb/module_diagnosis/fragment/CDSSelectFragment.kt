package com.zdyb.module_diagnosis.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.PopupWindowCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.GsonUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.qmuiteam.qmui.kotlin.onClick
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zdeps.gui.CMD
import com.zdeps.gui.ITaskCallbackAdapter
import com.zdyb.ITaskCallback
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.utils.PreferencesUtils
import com.zdyb.lib_common.utils.SharePreferencesDiagnosis
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.bean.CDSGroupingEntity
import com.zdyb.module_diagnosis.bean.CDSGroupingListEntity
import com.zdyb.module_diagnosis.bean.CDSSelectEntity
import com.zdyb.module_diagnosis.databinding.FragmentCdsSelectBinding
import com.zdyb.module_diagnosis.dialog.DialogHintBox
import com.zdyb.module_diagnosis.dialog.DialogInputValueBox
import com.zdyb.module_diagnosis.model.LoadDiagnosisModel
import com.zdyb.module_diagnosis.popup.GroupingPopupWindow
import com.zdyb.module_diagnosis.widget.BottomBarActionButton
import io.reactivex.functions.Consumer

class CDSSelectFragment:BaseNavFragment<FragmentCdsSelectBinding,LoadDiagnosisModel>() {

    lateinit var mDialogHintBox : DialogHintBox
    lateinit var mPopup : GroupingPopupWindow
    var addIsShow = false
    lateinit var rxPermission : RxPermissions

    //val tempGroupingList = mutableListOf<CDSGroupingEntity>() //临时列表
    private var groupingListEntity = CDSGroupingListEntity() //分组

    override fun initViewModel(): LoadDiagnosisModel {
        val model: LoadDiagnosisModel by activityViewModels()
       return model
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rxPermission = RxPermissions(requireActivity())
        mDialogHintBox = DialogHintBox()
        mDialogHintBox.setBackResult {
            val action = if (it) CMD.MB_YES else CMD.MB_NO
            viewModel.setCommonValue(CMD.ID_DIALOG_OFFSET, action)
        }
        mPopup = GroupingPopupWindow(requireContext())

    }



    override fun initActionButton() {
        super.initActionButton()
        if (activity is DiagnosisActivity){
            val mActivity = (activity as DiagnosisActivity)
            mActivity.removeAllActionButton()
            mActivity.addLeftActionButton(
                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_select,getString(R.string.action_button_select_look))
                    .setClick {
                        viewModel.cdsSelectLiveData.value = mAdapter.data
                        findNavController().navigate(R.id.action_CDSSelectFragment_to_CDSShowFragment)
                    }
            )
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
                groupingListEntity = getGroupIngListData()
            }

        }
    }

    override fun initViewObservable() {
        super.initViewObservable()

        viewModel.cdsSelectAllLiveData.observe(this){
            mAdapter.setList(it)
        }

        mAdapter.setOnItemClickListener { adapter, _, position ->
            if (addIsShow){
                val item = adapter.data[position] as CDSSelectEntity
                item.isAdd = !item.isAdd
                adapter.notifyItemChanged(position)
            }
        }

        binding.recyclerView.adapter = mAdapter
        binding.recyclerView.isScrollbarFadingEnabled = false //常驻显示进度条
        binding.recyclerView.scrollBarFadeDuration = 0
        binding.selectEdit.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (null != s){
                    val value =  s.toString().trim()
                    println("输入的内容=$value")
                    if (!TextUtils.isEmpty(value)){
                        mAdapter.setList(selectValue(value))
                    }else{
                        mAdapter.setList(viewModel.cdsSelectAllLiveData.value)
                    }
                }else{
                    mAdapter.setList(viewModel.cdsSelectAllLiveData.value)
                }
            }
        })
        //搜索按钮
        binding.butSelect.onClick {
            val value = binding.selectEdit.text.toString().trim()
            if (TextUtils.isEmpty(value)){
                mAdapter.setList(selectValue(value))
            }
        }
        //新建分组
        binding.butNewGrouping.onClick {
            if (binding.tvNew.text == getString(R.string.cds_grouping_add_success)){
                //如果没有一条数据被选中 则恢复数据，否则弹窗提示输入新建分组名称
                val newGroupingList = mutableListOf<CDSSelectEntity>()
                for (item in mAdapter.data){
                    if (item.isAdd){
                        newGroupingList.add(item)
                    }
                }

                if (newGroupingList.isEmpty()){
                    //恢复显示数据
                    addIsShow = false//屏蔽勾选框
                    binding.tvNew.text = getString(R.string.cds_new_grouping)
                    mAdapter.notifyDataSetChanged()
                }else{
                    //弹窗提示输入分组名称
                    val dialogInputValueBox = DialogInputValueBox()
                        .setTitle(getString(R.string.cds_grouping_add_title_hint))
                        .setInputHint(getString(R.string.cds_grouping_add_input_hint))
                        .setBackResult(Consumer {
                            if (it.result){
                                //存储格式 父menu名称+分组名称进行存储，不同类型的数据流下面有不同的分组
                                val groupingEntity = CDSGroupingEntity(it.value,newGroupingList)
                                groupingListEntity.list.add(groupingEntity)
                                if (install(groupingListEntity)){
                                    //切换当前分组数据列表
                                    addIsShow = false//屏蔽勾选框
                                    //tempGroupingList.add(CDSGroupingEntity(it.value,newGroupingList))//临时分组
                                    mAdapter.setList(newGroupingList)
                                    binding.tvNew.text = getString(R.string.cds_new_grouping)
                                }
                            }
                        }).show(childFragmentManager,"DialogInputValueBox")
                }
            }else if (binding.tvNew.text == getString(R.string.cds_new_grouping)){
                addIsShow = !addIsShow
                //初始化添加
                for (item in mAdapter.data){
                    item.isAdd = false
                }
                binding.tvNew.text = getString(R.string.cds_grouping_add_success)
                mAdapter.notifyDataSetChanged()
            }

        }
        //我的分组
        binding.butMyGrouping.onClick {
            if (mPopup.isShowing){
                return@onClick
            }
            mPopup.setData(groupingListEntity)
            PopupWindowCompat.showAsDropDown(mPopup, binding.butMyGrouping, 0, 20, Gravity.START)
        }


        mPopup.showGroupingListener(Consumer {
            if (null == it || it.isEmpty()){
                mAdapter.setList(viewModel.cdsSelectAllLiveData.value)
            }else{
                mAdapter.setList(it)
            }
        })
        mPopup.deleteGroupingListener(Consumer {
            deleteGroupingListItem(it)
            mAdapter.setList(viewModel.cdsSelectAllLiveData.value) //删除后显示全部数据
        })
        viewModel.registerCallback(iTaskCallback)
    }



     private val mAdapter: BaseQuickAdapter<CDSSelectEntity, BaseViewHolder> =
        object : BaseQuickAdapter<CDSSelectEntity, BaseViewHolder>(R.layout.item_cds_two) {

            override fun convert(holder: BaseViewHolder, item: CDSSelectEntity) {
                holder.setText(R.id.valueOne, item.value1)
                holder.setText(R.id.valueTwo, item.value3)

                if (holder.layoutPosition %2 != 0){
                    holder.getView<ConstraintLayout>(R.id.itemLayout).setBackgroundResource(R.color.item_bg)
                    holder.getView<View>(R.id.partition).setBackgroundResource(R.color.white)
                }else{
                    holder.getView<ConstraintLayout>(R.id.itemLayout).setBackgroundResource(R.color.white)
                    holder.getView<View>(R.id.partition).setBackgroundResource(R.color.item_bg)
                }

                val checkBox = holder.getView<CheckBox>(R.id.checkBox)
                if (addIsShow){
                    checkBox.visibility = View.VISIBLE
                }else{
                    checkBox.visibility = View.GONE
                }

                checkBox.isChecked = item.isAdd
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
            println("viewFinish---cdsSelect>")
            activity?.runOnUiThread {
                findNavController().popBackStack()
            }
        }

        override fun destroyDialog() :Long{

            activity?.runOnUiThread {
                println("destroyDialog---cdsSelect>")
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
                println("showDialog---cdsSelect>")
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


    /**
     * 关键字匹配列表内容
     */
    private fun selectValue(value:String):MutableList<CDSSelectEntity>{
        val tempList = mutableListOf<CDSSelectEntity>()
        viewModel.cdsSelectAllLiveData.value?.let {
            for (item in it){
                if (item.value1.contains(value)){
                    tempList.add(item)
                }
            }
        }
        return tempList
    }

    /**
     * 取存储中的数据
     */
    private fun getGroupIngListData():CDSGroupingListEntity{
        val key = SharePreferencesDiagnosis.CDS_GROUPING+viewModel.titleLiveData.value
        val jsonString = PreferencesUtils.getString(activity,key)
        if (TextUtils.isEmpty(jsonString))return CDSGroupingListEntity()
        return GsonUtils.getGson().fromJson(jsonString, CDSGroupingListEntity::class.java)
    }

    /**
     * 存储分组数据
     */
    private fun install(groupingListEntity :CDSGroupingListEntity):Boolean{
        val groupingString = GsonUtils.getGson().toJson(groupingListEntity)
        val key = SharePreferencesDiagnosis.CDS_GROUPING+viewModel.titleLiveData.value
        return PreferencesUtils.putString(activity,key,groupingString)
    }
    /**
     * 删除存储中的分组数据
     */
    private fun deleteGroupingListItem(index :Int){
        groupingListEntity.list.removeAt(index)
        install(groupingListEntity)
    }

//    override fun onDestroyView() {
//        viewModel.cdsSelectData.clear()
//        viewModel.cdsSelectAllLiveData.value = null
//        super.onDestroyView()
//    }
}