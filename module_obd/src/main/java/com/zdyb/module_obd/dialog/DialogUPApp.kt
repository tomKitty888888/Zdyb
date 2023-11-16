package com.zdyb.module_obd.dialog

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.qmuiteam.qmui.kotlin.onClick
import com.zdyb.lib_common.base.BaseDialogFragment
import com.zdyb.module_obd.databinding.DialogBoxBinding
import com.zdyb.module_obd.databinding.DialogScanningBinding
import io.reactivex.functions.Consumer
import kotlinx.coroutines.*

class DialogUPApp:BaseDialogFragment() {

    private var _binding: DialogBoxBinding? = null
    private val binding get() = _binding!!

    private var title = "提示"
    private var hint = ""
    private var consumer: Consumer<Boolean>? = null
    private var isTouchOutside = true //触摸外部是否可以消失

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogBoxBinding.inflate(layoutInflater, container, false)
        return binding.root
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        binding.tvTitle.text = title
        binding.tvHint.text = hint

        binding.cancel.visibility = View.GONE

        binding.confirm.onClick {
            this.consumer?.accept(true)
        }
        binding.cancel.onClick {
            this.consumer?.accept(false)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(isTouchOutside)
        dialog?.setOnKeyListener(object :DialogInterface.OnKeyListener {
            override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true
                }
                return false
            }
        })
    }

    fun setBox(title:String,hint:String,isTouchOutside:Boolean):DialogUPApp{
        this.title = title
        this.hint = hint
        this.isTouchOutside = isTouchOutside
        return this
    }

    fun setBox(title:String,hint:String):DialogUPApp{
        this.title = title
        this.hint = hint
        return this
    }

    fun setBox(hint:String):DialogUPApp{
        this.hint = hint
        return this
    }

    fun setResult(consumer: Consumer<Boolean>):DialogUPApp{
        this.consumer = consumer
        return this
    }

//
//    private fun animation(){
//        val width = binding.scanningView.width
//        val temp = width.toFloat()
//
//        val animatorX = PropertyValuesHolder.ofFloat("toX", 40f, temp)
//        val objectAnimation = ObjectAnimator.ofPropertyValuesHolder(binding.scanningView, animatorX)
//
//        //val objectAnimation = ObjectAnimator.ofFloat(binding.scanningView, "toX", 40f, temp)
//        objectAnimation.duration= 1000
//        objectAnimation.repeatMode = ValueAnimator.RESTART
//        objectAnimation.repeatCount = ValueAnimator.INFINITE
//        objectAnimation.start()
//    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}