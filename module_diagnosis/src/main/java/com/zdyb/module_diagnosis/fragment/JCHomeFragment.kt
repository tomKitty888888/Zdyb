package com.zdyb.module_diagnosis.fragment

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.qmuiteam.qmui.kotlin.onClick
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.module_diagnosis.databinding.FragmentHomeBinding
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseDialogFragment
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.bus.BusEvent
import com.zdyb.lib_common.bus.EventTypeDiagnosis
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.CSVFileActivity
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.bean.CartEntity
import com.zdyb.module_diagnosis.bean.DeviceEntity
import com.zdyb.module_diagnosis.bean.DtcEntity
import com.zdyb.module_diagnosis.databinding.FragmentJcHomeBinding
import com.zdyb.module_diagnosis.dialog.*
import com.zdyb.module_diagnosis.model.HomeModel
import com.zdyb.module_diagnosis.widget.BottomBarActionButton

class JCHomeFragment: BaseNavFragment<FragmentJcHomeBinding, HomeModel>()  {



    override fun initViewModel(): HomeModel {
        return ViewModelProvider(requireActivity())[HomeModel::class.java]
    }


    override fun initViewObservable() {
        super.initViewObservable()

        viewModel.cartLiveList.observe(this){
            mAdapter.setList(it)
        }

        mAdapter.setOnItemClickListener { adapter,view,position ->

//            if (!BaseApplication.getInstance().usbConnect){
//                viewModel.showToast("请将诊断接头连接到平板电脑")
//                return@setOnItemClickListener
//            }
            val item = adapter.getItem(position) as CartEntity

            val childItem = getChild(item.typeName)
            val listDialog = AlertDialog.Builder(requireActivity())
            //listDialog.setCancelable(false)
            listDialog.setTitle("车型选择")
            listDialog.setItems(childItem) { dialog, which ->
                item.currChildName = childItem[which]


                if (item.currChildName == "ZZ1167M"){ //只有abs
                    val tempData = mutableListOf<CartEntity.ChildAction>()
                    for (action in item.childAction){
                        println(action.name)
                        if (action.name == "BCU" || action.name == "DCU" || action.name == "Engine"){
                            tempData.add(action)
                        }
                    }
                    for (d in tempData){
                        item.childAction.remove(d)
                    }
                }

                val bundle = bundleOf("CartEntity" to item)
                findNavController().navigate(R.id.action_JCHomeFragment_to_JCHomeChildFragment,bundle)
            }
            listDialog.show()


        }
        binding.recyclerView.adapter = mAdapter

        BaseApplication.getInstance().outDiagnosisService = false
        println("初始化首页")

        getAllFilePermission()
    }


    private val mAdapter: BaseQuickAdapter<CartEntity, BaseViewHolder> =
        object : BaseQuickAdapter<CartEntity, BaseViewHolder>(R.layout.item_jc) {
            override fun convert(holder: BaseViewHolder, c: CartEntity) {
                holder.setText(R.id.name, c.typeName)
                val img = holder.getView<ImageView>(R.id.image)
                Glide.with(context).load(c.typeImgPath).into(img)
            }
        }


    override fun onResume() {
        super.onResume()
        (activity as DiagnosisActivity).showHomeActionButton()
        //viewModel.getCartData()

    }

    fun getAllFilePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {

            }else{
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setTitle(getString(R.string.all_file_permission_title_hint))
                dialog.setMessage(getString(R.string.all_file_permission_msg_hint))
                dialog.setPositiveButton(getString(R.string.confirm)) { dialog, _ ->

                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivityForResult(intent,2)
                    dialog.dismiss()
                }
                dialog.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                dialog.show()

            }
        }
    }


    /**
     * 根据大类型获取子类型
     */
    fun getChild(type:String):Array<String>{
        val array = arrayListOf<String>()

        when(type){
            "CA1125系列" -> {
                array.add("CA1125")
                array.add("CA1125JA3E5")
            }
            "EQ1120系列" -> {
                array.add("EQ1120GA")
                array.add("EQ1120GA2")
            }
            "BJ1161系列" -> {
                array.add("BJ1161VJPJG-2/3")
            }
            "ZZ1167系列" -> {
                array.add("ZZ1167M")
                array.add("ZZ1167N")
            }
            "ZZ2197系列" -> {
                array.add("ZZ2197")
                array.add("ZZ2197M4357A")
            }
            "CTL161系列" -> {
                array.add("CTL161")
                array.add("CTL162")
                array.add("CTL163")
            }
        }

        return array.toTypedArray()
    }

}