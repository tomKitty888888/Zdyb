package com.zdyb.module_diagnosis.fragment

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.base.KLog
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.bean.VoltageBean
import com.zdyb.module_diagnosis.databinding.FragmentVoltageBinding
import com.zdyb.module_diagnosis.model.VoltageModel
import com.zdyb.module_diagnosis.widget.BottomBarActionButton
import io.reactivex.functions.Consumer

class VoltageFragment:BaseNavFragment<FragmentVoltageBinding,VoltageModel>() {



    override fun initViewModel(): VoltageModel {
        return VoltageModel()
    }

    override fun initActionButton() {
        super.initActionButton()
        if (activity is DiagnosisActivity){
            val mActivity = (activity as DiagnosisActivity)
            mActivity.removeAllActionButton()
            mActivity.addLeftActionButton(
                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_select,getString(R.string.action_button_select_look))
                    .setClick {
                        KLog.d("查看电压")
                        viewModel.scan(Consumer {
                            if (it.index <=8){
                                mAdapter.data[it.index-1].value = it.value
                                mAdapter.notifyItemChanged(it.index-1)
                            }else {
                                mAdapter2.data[it.index-9].value = it.value
                                mAdapter2.notifyItemChanged(it.index-9)
                            }
                        })
                    }

            )
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

        val data1 = mutableListOf<VoltageBean>()
        data1.add(VoltageBean(1,""))
        data1.add(VoltageBean(2,""))
        data1.add(VoltageBean(3,""))
        data1.add(VoltageBean(4,"车身地"))
        data1.add(VoltageBean(5,"信号地"))
        data1.add(VoltageBean(6,""))
        data1.add(VoltageBean(7,""))
        data1.add(VoltageBean(8,""))
        mAdapter.setList(data1)
        binding.recyclerView.adapter = mAdapter

        val data2 = mutableListOf<VoltageBean>()
        data2.add(VoltageBean(9,""))
        data2.add(VoltageBean(10,""))
        data2.add(VoltageBean(11,""))
        data2.add(VoltageBean(12,""))
        data2.add(VoltageBean(13,""))
        data2.add(VoltageBean(14,""))
        data2.add(VoltageBean(15,""))
        data2.add(VoltageBean(16,""))
        mAdapter2.setList(data2)
        binding.recyclerView2.adapter = mAdapter2


        viewModel.scan(Consumer {

            if (it.index <=8){
                mAdapter.data[it.index-1].value = it.value
                mAdapter.notifyItemChanged(it.index-1)
            }else {
                mAdapter2.data[it.index-9].value = it.value
                mAdapter2.notifyItemChanged(it.index-9)
            }
        })

    }


    private val mAdapter: BaseQuickAdapter<VoltageBean, BaseViewHolder> =
        object : BaseQuickAdapter<VoltageBean, BaseViewHolder>(R.layout.item_widget_voltage_1) {

            override fun convert(holder: BaseViewHolder, item: VoltageBean) {

                holder.setText(R.id.index, item.index.toString())
                holder.setText(R.id.value, item.value)

                if (item.index == 6){
                    //绿色背景
                    holder.getView<TextView>(R.id.index).background = ContextCompat.getDrawable(context,R.drawable.bg_frame_radius_10_c_00ff00)
                    holder.getView<TextView>(R.id.value).setTextColor(ContextCompat.getColor(context,R.color.green))
                }else if (item.index == 4 || item.index == 5){
                    //不做处理
                }else{
                    holder.getView<TextView>(R.id.value).setTextColor(ContextCompat.getColor(context,R.color.blue))
                    holder.getView<TextView>(R.id.index).background = ContextCompat.getDrawable(context,R.drawable.bg_frame_radius_10_c_ffffff)
                }
            }
        }

    private val mAdapter2: BaseQuickAdapter<VoltageBean, BaseViewHolder> =
        object : BaseQuickAdapter<VoltageBean, BaseViewHolder>(R.layout.item_widget_voltage_2) {

            override fun convert(holder: BaseViewHolder, item: VoltageBean) {

                holder.setText(R.id.index, item.index.toString())
                holder.setText(R.id.value, item.value)
                val hintView = holder.getView<TextView>(R.id.hint)

                if (item.index == 14){
                    hintView.visibility = View.GONE
                    holder.getView<TextView>(R.id.value).setTextColor(ContextCompat.getColor(context,R.color.yellow))
                    holder.getView<TextView>(R.id.index).background = ContextCompat.getDrawable(context,R.drawable.bg_frame_radius_10_c_ffff00)
                }else if (item.index == 16){

                    hintView.text = "电源"
                    hintView.setTextColor(ContextCompat.getColor(context,R.color.red))
                    hintView.visibility = View.VISIBLE
                    holder.getView<TextView>(R.id.value).setTextColor(ContextCompat.getColor(context,R.color.red))
                    holder.getView<TextView>(R.id.index).background = ContextCompat.getDrawable(context,R.drawable.bg_frame_radius_10_c_ff0000)
                }else{
                    holder.getView<TextView>(R.id.value).setTextColor(ContextCompat.getColor(context,R.color.blue))
                    hintView.visibility = View.GONE
                    holder.getView<TextView>(R.id.index).background = ContextCompat.getDrawable(context,R.drawable.bg_frame_radius_10_c_ffffff)
                }

            }
        }
}