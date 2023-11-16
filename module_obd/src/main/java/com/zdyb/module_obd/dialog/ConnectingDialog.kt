package com.zdyb.module_obd.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.zdyb.lib_common.base.BaseDialogFragment
import com.zdyb.module_obd.databinding.DialogBleConnectBinding

class ConnectingDialog:BaseDialogFragment() {

    private var _binding: DialogBleConnectBinding? = null
    private val binding get() = _binding!!
    private var isTouchOutside = true //触摸外部是否可以消失

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogBleConnectBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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

    fun setBox(isTouchOutside:Boolean):ConnectingDialog{
        this.isTouchOutside = isTouchOutside
        return this
    }


    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}