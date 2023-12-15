package com.zdyb.module_diagnosis.fragment

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.zdyb.lib_common.base.BaseNavFragment
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdeps.gui.CMD
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.bus.BusEvent
import com.zdyb.lib_common.bus.EventTypeDiagnosis
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.activity.PDFActivity
import com.zdyb.module_diagnosis.activity.RepairInstActivity
import com.zdyb.module_diagnosis.bean.CartEntity
import com.zdyb.module_diagnosis.bean.CartEntity.ChildAction
import com.zdyb.module_diagnosis.bean.DeviceEntity
import com.zdyb.module_diagnosis.databinding.FragmentJcChildBinding
import com.zdyb.module_diagnosis.dialog.*
import com.zdyb.module_diagnosis.model.HomeModel
import com.zdyb.module_diagnosis.model.LoadDiagnosisModel
import com.zdyb.module_diagnosis.utils.FileUtils
import com.zdyb.module_diagnosis.widget.BottomBarActionButton
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class JCHomeChildFragment: BaseNavFragment<FragmentJcChildBinding, LoadDiagnosisModel>()  {

    lateinit var mCartEntity : CartEntity
    private var dis1: Disposable? = null

    override fun initParam() {
        super.initParam()
        mCartEntity = arguments?.getSerializable("CartEntity") as CartEntity
    }
    override fun initActionButton() {
        super.initActionButton()
        if (activity is DiagnosisActivity){
            val mActivity = (activity as DiagnosisActivity)
            mActivity.removeAllActionButton()

            mActivity.addRightActionButton(
                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_back,getString(R.string.action_button_back))
                    .setPartitionLineVisibility(ConstraintLayout.LayoutParams.LEFT)
                    .setClick {
                        findNavController().popBackStack()
                    },

                )
            mActivity.setTitle(mCartEntity.typeName)

        }
    }

    override fun initViewModel(): LoadDiagnosisModel {
        return ViewModelProvider(requireActivity())[LoadDiagnosisModel::class.java]
    }

    lateinit var mDialogLoadBox : DialogLoadBox
    override fun initViewObservable() {
        super.initViewObservable()
        mDialogLoadBox = DialogLoadBox()

        mAdapter.setOnItemClickListener{adapter,view,psotion ->
            if (psotion == adapter.data.size-1){
                //辅助维修帮助系统
                val item = adapter.getItem(psotion) as ChildAction
                val intent = Intent(requireContext(), RepairInstActivity::class.java)
                intent.putExtra("CartEntity",mCartEntity)
                startActivity(intent)
                return@setOnItemClickListener
            }
            if (psotion == adapter.data.size-2){
                if (TextUtils.isEmpty(mCartEntity.instructionsPath)){
                    viewModel.showToast("pdf说明文件缺失")
                    return@setOnItemClickListener
                }
                PDFActivity.startActivity(requireActivity(),mCartEntity.instructionsPath)
                return@setOnItemClickListener
            }

            if (!BaseApplication.getInstance().usbConnect){
                viewModel.showToast("请将诊断接头连接到平板电脑")
                return@setOnItemClickListener
            }

            mDialogLoadBox.show(childFragmentManager,"mDialogLoadBox")
            viewModel.anewDiagnosisService(Consumer {


                //加载menu文件
                //拿到路径 与执行id 还有名称
                val item = adapter.getItem(psotion) as ChildAction
                getAllData(item.menuPath)
            })

//            if (null == viewModel.mITaskBinder){
//                //诊断服务还未绑定 先屏蔽
//                return@setOnItemClickListener
//            }

        }
        mAdapter.setList(mCartEntity.childAction)
        binding.recyclerView.adapter = mAdapter

        viewModel.menuListLiveData.value = null //让诊断重新加载
        println("初始化首页--menuListLiveData-null")

    }



    private val mAdapter: BaseQuickAdapter<CartEntity.ChildAction, BaseViewHolder> =
        object : BaseQuickAdapter<CartEntity.ChildAction, BaseViewHolder>(R.layout.item_jc_child) {
            override fun convert(holder: BaseViewHolder, c: CartEntity.ChildAction) {
                holder.setText(R.id.name, getName(c.name))

            }

            fun getName(name :String):String{
                when(name){
                    "ABS" -> { return "防抱死系统\n(ABS)"}
                    "BCM" -> { return "车身系统\n(BCM)"}
                    "BCU" -> { return "车身系统\n(BCU)"}
                    "DCU" -> { return "后处理系统\n(DCU)"}
                    "Engine","ECM" -> { return "发动机系统\n(ECM)"}
                }
                return name
            }
        }


    /**
     * 获取全部的数据
     */

    private fun getAllData(filePath :String) {
        dis1 = Observable.create { emitter -> emitter.onNext(FileUtils.fileRead(filePath)) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                for (item in it){
                    println(item.name)
                    println(item.commend)
                    println(item.path)
                }
                if (it.size > 1){
                    val stringData = arrayListOf<String>()
                    for (item in it){
                        stringData.add(item.name)
                    }
                    //让用户选择
                    val listDialog = AlertDialog.Builder(requireActivity())
                    //listDialog.setCancelable(false)
                    listDialog.setTitle("车型选择")
                    listDialog.setItems(stringData.toTypedArray()) { dialog, which ->
                        val item = it[which]
                        // 加载so了
                        val tempStringId = item.commend.replace("0x","")
                        val id = tempStringId.toLong(16)
                        val deviceEntity = DeviceEntity(id,item.path,item.name)
                        deviceEntity.versionPath = viewModel.getHighVersionSo(deviceEntity)
                        if (deviceEntity.versionPath.isEmpty()){
                            mDialogLoadBox.dismiss()
                            viewModel.showToast("诊断文件为空，请检查文件")
                            return@setItems
                        }
                        KLog.i("versionPath="+deviceEntity.versionPath)
                        viewModel.openDiagnosis(deviceEntity.versionPath, Consumer {
                            mDialogLoadBox.dismiss()
                            val bundle = bundleOf(DeviceEntity.tag to deviceEntity)
                            findNavController().navigate(R.id.action_JCHomeChildFragment_to_menuListFragment,bundle)

                        })
                    }
                    listDialog.setOnDismissListener {
                        if (mDialogLoadBox.isVisible){
                            mDialogLoadBox.dismiss()
                        }
                    }
                    listDialog.show()
                }else if (it.size ==1){
                    val item = it[0]
                    // 加载so了
                    val tempStringId = item.commend.replace("0x","")
                    val id = tempStringId.toLong(16)
                    val deviceEntity = DeviceEntity(id,item.path,item.name)
                    deviceEntity.versionPath = viewModel.getHighVersionSo(deviceEntity)
                    if (deviceEntity.versionPath.isEmpty()){
                        mDialogLoadBox.dismiss()
                        viewModel.showToast("诊断文件为空，请检查文件")
                        return@subscribe
                    }
                    KLog.i("versionPath="+deviceEntity.versionPath)
                    viewModel.openDiagnosis(deviceEntity.versionPath, Consumer {

                        val bundle = bundleOf(DeviceEntity.tag to deviceEntity)
                        findNavController().navigate(R.id.action_JCHomeChildFragment_to_menuListFragment,bundle)
                        if (mDialogLoadBox.isVisible){
                            mDialogLoadBox.dismiss()
                        }
                    })
                }


            },{
                it.printStackTrace()
                if (mDialogLoadBox.isVisible){
                    mDialogLoadBox.dismiss()
                }
            })
    }


    override fun onPause() {
        super.onPause()

    }



}