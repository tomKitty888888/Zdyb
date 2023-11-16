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
import com.zdyb.module_obd.databinding.DialogScanningBinding
import kotlinx.coroutines.*

class ScanningDialog:BaseDialogFragment() {

    private var _binding: DialogScanningBinding? = null
    private val binding get() = _binding!!
    private var isTouchOutside = true //触摸外部是否可以消失

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogScanningBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var temp = 40
        binding.tvHint.onClick {

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

    fun setBox(isTouchOutside:Boolean):ScanningDialog{
        this.isTouchOutside = isTouchOutside
        return this
    }

    private fun animation(){
        if (_binding == null)return
        val width = binding.scanningView.width
        val temp = width.toFloat()

        val animatorX = PropertyValuesHolder.ofFloat("toX", 40f, temp)
        val objectAnimation = ObjectAnimator.ofPropertyValuesHolder(binding.scanningView, animatorX)

        //val objectAnimation = ObjectAnimator.ofFloat(binding.scanningView, "toX", 40f, temp)
        objectAnimation.duration= 1000
        objectAnimation.repeatMode = ValueAnimator.RESTART
        objectAnimation.repeatCount = ValueAnimator.INFINITE
        objectAnimation.start()
    }

    private val mailScope = MainScope()

    override fun onResume() {
        super.onResume()

        mailScope.launch {
            delay(1000)
            animation()
        }

//        Handler().postDelayed(Runnable {
//            animation()
//             },1000)

    }

    override fun show(manager: FragmentManager, tag: String?) {

        try {
            super.show(manager, tag)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mailScope.cancel()
        binding.scanningView.onDestroy()
    }
}