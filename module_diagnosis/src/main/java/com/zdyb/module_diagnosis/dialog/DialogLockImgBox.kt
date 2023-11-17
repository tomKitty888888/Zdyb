package com.zdyb.module_diagnosis.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.FragmentManager
import com.qmuiteam.qmui.kotlin.onClick
import com.zdeps.gui.CMD
import com.zdyb.lib_common.base.BaseDialogFragment
import com.zdyb.lib_common.base.KLog
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.bean.Transfer
import com.zdyb.module_diagnosis.databinding.DialogHintBoxBinding
import com.zdyb.module_diagnosis.databinding.DialogLockImgBoxBinding
import io.reactivex.functions.Consumer

class DialogLockImgBox:BaseDialogFragment(){

    private var _binding: DialogLockImgBoxBinding? = null
    private val binding get() = _binding!!

    private var title = "提示"
    private var hint = ""
    private var consumer: Consumer<Boolean>? = null //按钮
    private var isTouchOutside = false //触摸外部是否消失
    private var actionType :Byte = 0 //

    private var data = mutableListOf<Transfer>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = DialogLockImgBoxBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.setCanceledOnTouchOutside(isTouchOutside)

        binding.actionButton.onClick {
            when(actionType){
                CMD.MSG_MB_NOBUTTON ->{
                    println("返回首页")
                    dismiss()
                }else ->{
                    println("截图")
                    dismiss()
                }

            }
        }
        binding.confirm.onClick {
            consumer?.accept(true)
        }

        binding.cancel.onClick {
            consumer?.accept(false)
        }
        println("---->onActivityCreated()")

        //初始数据
        binding.button1.isEnabled = false
        binding.button2.isEnabled = false
        binding.button3.isEnabled = false
        binding.button4.isEnabled = false
        binding.button5.isEnabled = false

        for (item in data){
            when(item.name){
                "01" -> { binding.button1.isEnabled = true}
                "02" -> { binding.button2.isEnabled = true}
                "03" -> { binding.button3.isEnabled = true}
                "04" -> { binding.button4.isEnabled = true}
                "05" -> { binding.button5.isEnabled = true}
            }
        }
    }


    fun setTouchOutside(isTouchOutside:Boolean):DialogLockImgBox{
        this.isTouchOutside = isTouchOutside
        return this
    }

    fun setTitle(title:String):DialogLockImgBox{
        this.title = title
        return this
    }
    fun setInitMsg(hint:String?){
        if (hint != null) {
            this.hint = hint
        }
    }
    fun setMsg(hint:String?){
        if (hint != null) {
            this.hint = hint
        }
    }
    fun setActionType(actionType:Byte):DialogLockImgBox{
        this.actionType = actionType
        return this
    }
    fun setBackResult(consumer: Consumer<Boolean>):DialogLockImgBox{
        this.consumer = consumer
        return this
    }

    fun setData(mutableList: MutableList<Transfer>){
        this.data = mutableList
    }

    /**
     * 在显示的情况下更新弹窗按钮情况
     */
    fun upActionType(actionType:Byte){
        this.actionType = actionType
        setShowStyle()
    }

    /**
     * 设置显示样式，一个确认按钮，一个取消按钮，确认与取消按钮
     */
    private fun setShowStyle(){
        when(actionType){
            CMD.MSG_MB_NOBUTTON ->{ //只有提示信息
                KLog.i("只有提示信息")
                binding.actionButton.setImageResource(R.mipmap.icon_d_action_home)
                binding.confirm.visibility = View.GONE
                binding.cancel.visibility = View.GONE
                binding.placeholder.visibility = View.GONE
                binding.placeholder2.visibility = View.GONE
            }
            CMD.MSG_MB_OK,CMD.MB_YES,CMD.MSG_MB_ERROR ->{ //一个确认按钮
                KLog.i("有提示信息--->一个确认按钮")
                binding.confirm.visibility = View.VISIBLE
                binding.cancel.visibility = View.GONE
                binding.placeholder.visibility = View.INVISIBLE
                binding.placeholder2.visibility = View.VISIBLE
            }
            CMD.MB_NO ->{ //一个取消按钮
                KLog.i("有提示信息--->一个取消按钮")
                binding.cancel.visibility = View.VISIBLE
                binding.confirm.visibility = View.GONE
                binding.placeholder.visibility = View.INVISIBLE
                binding.placeholder2.visibility = View.VISIBLE
            }
            CMD.MSG_MB_YESNO ->{ //确认和取消按钮
                KLog.i("有提示信息--->确认和取消按钮")
                binding.confirm.visibility = View.VISIBLE
                binding.cancel.visibility = View.VISIBLE
                binding.placeholder.visibility = View.VISIBLE
                binding.placeholder2.visibility = View.VISIBLE
            }
            else ->{
                binding.actionButton.setImageResource(R.mipmap.icon_d_screenshot)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        println("---->onResume()")
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        this.dialog?.window!!.decorView.setSystemUiVisibility(uiOptions)
    }

    override fun show(manager: FragmentManager, tag: String?) {

        try {
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
//                if (manager.isDestroyed)
//                    return
//            }
//            manager.beginTransaction().remove(this).commit()

            show = true
            super.show(manager, tag)
        }catch (e :Exception){
            e.printStackTrace()
        }
    }

    fun isShow():Boolean{
        return show
    }

    var show = false
    override fun dismiss() {
        show = false
        super.dismiss()
    }
}