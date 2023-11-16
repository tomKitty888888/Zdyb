package com.zdyb.module_obd.fragment

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.qmuiteam.qmui.kotlin.onClick
import com.zdeps.bean.OBDBean
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.module_obd.R
import com.zdyb.module_obd.adaptre.ObdFaultListAdapter
import com.zdyb.module_obd.adaptre.ObdListAdapterApp
import com.zdyb.module_obd.databinding.ActivityObdResultBinding
import com.zdyb.module_obd.databinding.FragmentObdBinding
import com.zdyb.module_obd.databinding.FragmentObdLoginBinding
import com.zdyb.module_obd.model.OBDResultData
import com.zdyb.module_obd.model.ObdFragmentModel

class OBDResultFragment : BaseNavFragment<ActivityObdResultBinding, ObdFragmentModel>(){

    private var mCartAdapter = ObdListAdapterApp(arrayListOf())
    private var mObdAdapter = ObdListAdapterApp(arrayListOf())
    private var mEmissionAdapter = ObdListAdapterApp(arrayListOf())

    lateinit var mReadinessChildView : View
    lateinit var mFailureChildView : View

    override fun initViewModel(): ObdFragmentModel {
        val model: ObdFragmentModel by activityViewModels()
        return model
    }



    override fun initParam() {
        mReadinessChildView = LayoutInflater.from(requireContext()).inflate(R.layout.include_failuer_info_2,null)
        mFailureChildView = LayoutInflater.from(requireContext()).inflate(R.layout.include_obd_info_2,null)
    }

//    override fun getTitleText(): CharSequence {
//        return getString(R.string.obd_cart_scan_result)
//    }
    override fun initViewObservable() {
        super.initViewObservable()

    binding.tvObdResult.onClick{
        Navigation.findNavController(it).navigateUp()
    }
    //btn_back
    //
        if (OBDResultData.emissionInfo.isNotEmpty()){
            println("有数据")
        }else{
            println("无数据")
        }

        binding.cartInfoRecyclerView.adapter = mCartAdapter
        binding.obdInfoRecyclerView.adapter = mObdAdapter
        binding.emissionInfoRecyclerView.adapter = mEmissionAdapter
        mCartAdapter.setList(OBDResultData.cartInfo)
        mObdAdapter.setList(OBDResultData.obdInfo)
        mEmissionAdapter.setList(OBDResultData.emissionInfo)

        mObdAdapter.addChildClickViewIds(R.id.value)
        mObdAdapter.setOnItemClickListener(OnItemClickListener { adapter, view, position ->
            val item = adapter.data[position] as OBDBean.ObdData
            if (item.chevron == null || !item.chevron){
                return@OnItemClickListener
            }
            //addView 子集模块
            if (item.key == OBDResultData.KEY_CURRENT){
                addOdbItemLayout(OBDResultData.failureData,getString(R.string.obd_fault_current_title_2),position)
            }else if (item.key == OBDResultData.KEY_READINESSSTATUS){
                addOdbItemLayout(OBDResultData.readinessStatus,getString(R.string.obd_readiness_title_2),position)
            }else{
                binding.obdInfoLayout.removeAllViews()
            }

        })

        if (OBDResultData.failureSum > 0 && OBDResultData.readinessStatusSum >2){
            //异常
            setObdState(getString(R.string.obd_err_hint),true)
            binding.opinion.visibility = View.VISIBLE
            binding.imgResult.setImageResource(R.mipmap.icon_obd_err)
        }else{
            //正常
            setObdState(getString(R.string.obd_ok_hint),false)
            binding.opinion.visibility = View.INVISIBLE
            binding.imgResult.setImageResource(R.mipmap.icon_obd_ok)
        }
    }


    private fun setObdState(context:String,isError:Boolean){
        val whiteSpan = ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.black))

        val stateSpan = if (isError){
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red))
        }else{
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.green))
        }


        val spanBuilder = SpannableStringBuilder(context)
        spanBuilder.setSpan(whiteSpan,0,context.lastIndexOf("，")+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanBuilder.setSpan(stateSpan,context.lastIndexOf("，")+1,context.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvObdResult.text = spanBuilder
    }

    /**
     * 添加子项
     */
    private fun addOdbItemLayout(data:MutableList<OBDBean.ObdData>, titleString:String, position:Int){

        if (titleString == getString(R.string.obd_readiness_title_2)){

            if(binding.odbItemLayout.childCount > 0){
                val view = binding.odbItemLayout.getChildAt(0)
                if (view.id != R.id.item_obd_fault){
                    println("就绪状态子view 已存在")
                    //清理掉
                    binding.odbItemLayout.removeAllViews()
                    return
                }else{
                    binding.odbItemLayout.removeAllViews()
                }
            }
            mObdAdapter.setRotationIndex(position)
            mObdAdapter.notifyItemChanged(position-1)
            val title = mReadinessChildView.findViewById<TextView>(R.id.title)
            val not = mReadinessChildView.findViewById<CheckBox>(R.id.notCheckBox)
            val yes = mReadinessChildView.findViewById<CheckBox>(R.id.yesCheckBox)
            val checkBoxLayout = mReadinessChildView.findViewById<LinearLayout>(R.id.checkBoxLayout)
            checkBoxLayout.removeAllViews()

            title.text = titleString

            for (item in data){
                if (item.value.toString() == "2"){ // 只要有一个未完成就勾选上
                    yes.isChecked = true
                    not.isChecked = false
                }

                val checkBox = CheckBox(requireContext())
                checkBox.isClickable = false
                checkBox.isChecked = item.value.toString() == "2"
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.marginStart = 30
                checkBox.text = item.description+item.key
                checkBox.layoutParams = layoutParams
                checkBoxLayout.addView(checkBox)
            }
            not.isChecked = !yes.isChecked

            binding.odbItemLayout.addView(mReadinessChildView)

        }else if (titleString == getString(R.string.obd_fault_current_title_2)){

            //清理掉Current 不需要显示这一行
            data.remove(data[0])
            if(binding.odbItemLayout.childCount > 0){
                val view = binding.odbItemLayout.getChildAt(0)
                if (view.id == R.id.item_obd_fault){
                    println("故障子view 已存在")
                    binding.odbItemLayout.removeAllViews()
                    return
                }else{
                    binding.odbItemLayout.removeAllViews()
                }
            }
            mObdAdapter.setRotationIndex(position)
            mObdAdapter.notifyItemChanged(position+1)
            val title = mFailureChildView.findViewById<TextView>(R.id.title)
            val recyclerView = mFailureChildView.findViewById<RecyclerView>(R.id.recyclerView)
            title.text = titleString
            val mAdapter = ObdFaultListAdapter(arrayListOf())
            recyclerView.adapter = mAdapter
            mAdapter.setList(data)

            binding.odbItemLayout.addView(mFailureChildView)
        }

    }


}