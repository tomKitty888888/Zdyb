package com.zdyb.module_obd.fragment

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zdeps.bean.OBDBean.ObdData
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.bluetooth.BluetoothManager
import com.zdyb.lib_common.bluetooth.BluetoothManager.bleDevicesData
import com.zdyb.module_obd.R
import com.zdyb.module_obd.adaptre.ObdListAdapter
import com.zdyb.module_obd.adaptre.ObdListAdapterTow
import com.zdyb.module_obd.databinding.FragmentObdBinding
import com.zdyb.module_obd.model.ObdFragmentModel
import io.reactivex.functions.Consumer

class ObdFragment :BaseNavFragment<FragmentObdBinding, ObdFragmentModel>(){


    lateinit var rxPermissions: RxPermissions
    private var mCartAdapter = ObdListAdapter(arrayListOf())
    private var mObdAdapter = ObdListAdapterTow(arrayListOf())
    private var mEmissionAdapter = ObdListAdapter(arrayListOf())

    lateinit var mReadinessChildView : View
    lateinit var mFailureChildView : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rxPermissions = RxPermissions(requireActivity())
        mReadinessChildView = LayoutInflater.from(requireContext()).inflate(R.layout.include_failuer_info,null)
        mFailureChildView = LayoutInflater.from(requireContext()).inflate(R.layout.include_obd_info,null)
    }
    override fun initViewModel(): ObdFragmentModel {
        return ViewModelProvider(requireActivity()).get(ObdFragmentModel::class.java)
    }

    override fun initViewObservable() {
        super.initViewObservable()
        initAnimator()
        binding.cartInfoRecyclerView.adapter = mCartAdapter
        binding.obdInfoRecyclerView.adapter = mObdAdapter
        binding.emissionInfoRecyclerView.adapter = mEmissionAdapter
        mObdAdapter.addChildClickViewIds(R.id.value)
        mObdAdapter.setOnItemClickListener(OnItemClickListener { adapter, view, position ->
            val item = adapter.data[position] as ObdData
            if (item.chevron == null || !item.chevron){
                return@OnItemClickListener
            }
            //addView 子集模块
            if (item.key == viewModel.KEY_CURRENT){
                addOdbItemLayout(viewModel.failureData,getString(R.string.obd_fault_current_title),position)
            }else if (item.key == viewModel.KEY_READINESSSTATUS){
                addOdbItemLayout(viewModel.readinessStatus,getString(R.string.obd_readiness_title),position)
            }else{
                binding.obdInfoLayout.removeAllViews()
            }

        })

        binding.tvObdResult.setOnClickListener {
            //viewModel.obdInfoList(viewModel.abs)
            //binding.odbItemLayout.addView(mReadinessChildView)
        }
        bleDevicesData.observe(this){ devices ->
            val names = mutableListOf<String>()
            for (item in devices){
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    viewModel.showToast("缺少BLUETOOTH_CONNECT权限,请授权")
                    return@observe
                }
                item.name?.let { name-> names.add(name) }

            }

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("select device to connect")
            builder.setItems(names.toTypedArray(), DialogInterface.OnClickListener { dialog, which ->
                BluetoothManager.connect(devices[which].address)
            })
            viewModel.dismissLoading()
            builder.show()

        }
        binding.imgResult.setOnClickListener {

            addDisposable(rxPermissions.request(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ).subscribe {
                if (it){
                        viewModel.showLoading()
                        BluetoothManager.setListener(consumer = Consumer { state ->
                            if (state == BluetoothManager.BLEState.CONNECT){
                                //初始化 检测 //测试设备10290334014Y
                                viewModel.initOBD()
                            }
                        }).search(Consumer { devices ->

                        })
                    }
            })

        }




        viewModel.cartInfo.observe(this) {
            mCartAdapter.setList(it)
        }

        viewModel.obdInfo.observe(this) {
            mObdAdapter.setList(it)
        }
        viewModel.emissionInfo.observe(this) {
            mEmissionAdapter.setList(it)

            if (viewModel.failureSum > 0 && viewModel.readinessStatusSum >2){
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

    }

    private fun setObdState(context:String,isError:Boolean){
        val whiteSpan = ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.white))

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
    private fun addOdbItemLayout(data:MutableList<ObdData>,titleString:String,position:Int){

        if (titleString == getString(R.string.obd_readiness_title)){

            if(binding.odbItemLayout.childCount > 0){
                val view = binding.odbItemLayout.getChildAt(0)
                if (view.id != R.id.item_obd_fault){
                    println("就绪状态子view 已存在")
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
            val egr = mReadinessChildView.findViewById<CheckBox>(R.id.egrCheckBox)

            for (item in data){
                if (item.value.toString() == "2"){ // 只要有一个未完成就勾选上
                    yes.isChecked = true
                    not.isChecked = false
                }
                if (item.key == "EGR"){
                    egr.isChecked = item.value.toString() == "2"
                    egr.text = item.description+item.key
                }
                val checkBox = CheckBox(requireContext())
                checkBox.isClickable = false
                checkBox.isChecked = item.value.toString() == "2"
                val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.marginStart = 30
                checkBox.text = item.key
                checkBox.layoutParams = layoutParams
                checkBoxLayout.addView(checkBox)
            }

            binding.odbItemLayout.addView(mReadinessChildView)

        }else if (titleString == getString(R.string.obd_fault_current_title)){

            //清理掉Current 不需要显示这一行
            data.remove(data[0])
            if(binding.odbItemLayout.childCount > 0){
                val view = binding.odbItemLayout.getChildAt(0)
                if (view.id == R.id.item_obd_fault){
                    println("故障子view 已存在")
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
            val mAdapter = ObdListAdapter(arrayListOf())
            recyclerView.adapter = mAdapter
            mAdapter.setList(data)

            binding.odbItemLayout.addView(mFailureChildView)
        }


    }

    var rotationXAnimator : ObjectAnimator? = null
    private fun initAnimator(){
        rotationXAnimator = ObjectAnimator.ofFloat(binding.odbItemLayout, "alpha", 0f, 1f)
        rotationXAnimator?.repeatCount = ValueAnimator.INFINITE
        rotationXAnimator?.duration = 1000
        rotationXAnimator?.interpolator = LinearInterpolator()
        rotationXAnimator?.repeatCount = ValueAnimator.INFINITE
    }
}