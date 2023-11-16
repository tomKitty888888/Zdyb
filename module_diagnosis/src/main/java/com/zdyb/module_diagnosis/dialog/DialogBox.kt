package com.zdyb.module_diagnosis.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qmuiteam.qmui.kotlin.onClick
import com.zdyb.lib_common.base.BaseDialogFragment
import com.zdyb.module_diagnosis.databinding.DialogBoxBinding
import io.reactivex.functions.Consumer

class DialogBox:BaseDialogFragment() {

    private var _binding: DialogBoxBinding? = null
    private val binding get() = _binding!!

    private var title = "提示"
    private var hint = ""
    private var consumer: Consumer<Boolean>? = null
    private var isTouchOutside = false //触摸外部是否消失

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

        dialog?.setCanceledOnTouchOutside(isTouchOutside)
        binding.tvTitle.text = title
        binding.tvHint.text = hint

        binding.confirm.onClick {
            this.consumer?.accept(true)
            dismiss()
        }
        binding.cancel.onClick {
            this.consumer?.accept(false)
            dismiss()
        }
    }

    fun setBox(title:String,hint:String,isTouchOutside:Boolean):DialogBox{
        this.title = title
        this.hint = hint
        this.isTouchOutside = isTouchOutside
        return this
    }
    fun setBox(hint:String,isTouchOutside:Boolean):DialogBox{
        this.hint = hint
        this.isTouchOutside = isTouchOutside
        return this
    }

    fun setBox(title:String,hint:String):DialogBox{
        this.title = title
        this.hint = hint
        return this
    }

    fun setBox(hint:String):DialogBox{
        this.hint = hint
        return this
    }

    fun setResult(consumer: Consumer<Boolean>):DialogBox{
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