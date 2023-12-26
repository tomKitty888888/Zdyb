package com.zdyb.module_diagnosis.fragment

import android.view.View
import android.widget.AdapterView.OnItemClickListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdeps.gui.CMD
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.bean.ACTEntity
import com.zdyb.module_diagnosis.bean.ChildCarBean
import com.zdyb.module_diagnosis.databinding.FragmentChildCarSeriesBinding
import com.zdyb.module_diagnosis.widget.BottomBarActionButton

class ChildCarSeriesFragment :BaseNavFragment<FragmentChildCarSeriesBinding,BaseViewModel>(){



    override fun initViewModel(): BaseViewModel {
        return BaseViewModel()
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
                              findNavController().popBackStack()
                    },

                )

        }
    }


    override fun initViewObservable() {
        super.initViewObservable()

        mAdapter.setOnItemClickListener { adapter, _, position ->

            val item = adapter.getItem(position) as ChildCarBean

            when(item.name){
                getString(R.string.dvs_ec) ->{
                    val bundle = bundleOf(FileListFragment.TAG to PathManager.getElectronicFilePath())
                    findNavController().navigate(R.id.action_childCarSeriesFragment_to_fileListFragment,bundle)
                }
                getString(R.string.dvs_abs) ->{
                    val bundle = bundleOf(FileListFragment.TAG to PathManager.getEngineFilePath())
                    findNavController().navigate(R.id.action_childCarSeriesFragment_to_fileListFragment,bundle)
                }
                getString(R.string.dvs_ct) ->{
                    val bundle = bundleOf(FileListFragment.TAG to PathManager.getVehicleFilePath())
                    findNavController().navigate(R.id.action_childCarSeriesFragment_to_fileListFragment,bundle)
                }
                getString(R.string.dvs_cm) ->{
                    val bundle = bundleOf(FileListFragment.TAG to PathManager.getMechanicalFilePath())
                    findNavController().navigate(R.id.action_childCarSeriesFragment_to_fileListFragment,bundle)
                }
                getString(R.string.dvs_tool) ->{
                    val bundle = bundleOf(FileListFragment.TAG to PathManager.getObdFilePath())
                    findNavController().navigate(R.id.action_childCarSeriesFragment_to_fileListFragment,bundle)
                }
            }
        }
        binding.recyclerView.adapter = mAdapter

        val childData = mutableListOf<ChildCarBean>()
        childData.add(ChildCarBean(getString(R.string.dvs_ec),R.mipmap.icon_d_diagnosis))
        childData.add(ChildCarBean(getString(R.string.dvs_abs),R.mipmap.icon_d_diagnosis))
        childData.add(ChildCarBean(getString(R.string.dvs_ct),R.mipmap.icon_d_diagnosis))
        childData.add(ChildCarBean(getString(R.string.dvs_cm),R.mipmap.icon_d_diagnosis))
        childData.add(ChildCarBean(getString(R.string.dvs_tool),R.mipmap.icon_d_diagnosis))
        mAdapter.setList(childData)
    }


    private val mAdapter: BaseQuickAdapter<ChildCarBean, BaseViewHolder> =
        object : BaseQuickAdapter<ChildCarBean, BaseViewHolder>(R.layout.item_child_car) {

            override fun convert(holder: BaseViewHolder, item: ChildCarBean) {

                holder.setImageResource(R.id.img,item.image)
                holder.setText(R.id.name,item.name)

            }
        }
}