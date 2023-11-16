package com.zdyb.module_diagnosis.fragment

import android.Manifest
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.bean.DeviceEntity
import com.zdyb.module_diagnosis.databinding.FragmentAutoScanDeviceListBinding
import com.zdyb.module_diagnosis.model.LoadDiagnosisModel
import com.zdyb.module_diagnosis.widget.BottomBarActionButton
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

class AutoScanDeviceListFragment:BaseNavFragment<FragmentAutoScanDeviceListBinding,LoadDiagnosisModel>() {


    override fun initViewModel(): LoadDiagnosisModel {
       return ViewModelProvider(requireActivity())[LoadDiagnosisModel::class.java]
    }

    lateinit var rxPermission : RxPermissions
    private var dis1: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rxPermission = RxPermissions(requireActivity())
    }

    override fun initActionButton() {
        super.initActionButton()
        if (activity is DiagnosisActivity){
            val mActivity = (activity as DiagnosisActivity)
            mActivity.removeAllActionButton()
            mActivity.addLeftActionButton(
                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_rescan,getString(R.string.action_button_rescan))
                    .setClick {
                        viewModel.startScan()
                        //viewModel.test()
                    }
            )
            mActivity.addRightActionButton(
                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_back,getString(R.string.action_button_back))
                    .setPartitionLineVisibility(ConstraintLayout.LayoutParams.LEFT)
                    .setClick {
                        //findNavController().navigateUp()
                        findNavController().popBackStack()
                },

            )
        }
    }

    override fun initViewObservable() {
        super.initViewObservable()

        viewModel.deviceList.observe(this){
            mAdapter.setList(it)
        }

        mAdapter.setOnItemClickListener { adapter, view, position ->

            addDisposable(rxPermission.request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).subscribe {
                if (it){
                    val item = adapter.data[position]as DeviceEntity
                    println(item.path)
                    item.versionPath = viewModel.getHighVersionSo(item)
                    if (item.versionPath.isEmpty()){
                        viewModel.showToast("诊断文件为空，请先下载")
                        return@subscribe
                    }
                    viewModel.openDiagnosis(item.versionPath, Consumer {
                        val bundle = bundleOf(DeviceEntity.tag to item)
                        findNavController().navigate(R.id.action_AutoScanDeviceListFragment_to_menuListFragment,bundle)
                    })
                }else{
                    viewModel.showToast(getString(R.string.allow_permission_is_required))
                }
            })
        }
        binding.recyclerView.adapter = mAdapter

        if (viewModel.deviceList.value == null){
            Handler().postDelayed({
                viewModel.startScan()
            },200)

        }

    }


    private val mAdapter: BaseQuickAdapter<DeviceEntity, BaseViewHolder> =
        object : BaseQuickAdapter<DeviceEntity, BaseViewHolder>(R.layout.item_device_name) {
            override fun convert(baseViewHolder: BaseViewHolder, d: DeviceEntity) {
                baseViewHolder.setText(R.id.tv_name, d.name)

            }
        }

}