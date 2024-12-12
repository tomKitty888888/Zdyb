package com.zdyb.module_diagnosis.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.qmuiteam.qmui.kotlin.onClick
import com.zdeps.gui.CMD
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseDialogFragment
import com.zdyb.lib_common.base.KLog
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.bean.BoxActionResult
import com.zdyb.module_diagnosis.databinding.DialogHintBoxBinding
import com.zdyb.module_diagnosis.databinding.DialogImgBoxBinding
import io.reactivex.functions.Consumer

class DialogImgBox:BaseDialogFragment(){

    private var _binding: DialogImgBoxBinding? = null
    private val binding get() = _binding!!

    private var title = "提示"
    private var hint = ""
    private var imagePath = ""
    private var consumer: Consumer<Boolean>? = null //按钮
    private var homeConsumer: Consumer<Boolean>? = null //home按钮

    private var isTouchOutside = false //触摸外部是否消失
    private var actionType :Byte = 0 //


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = DialogImgBoxBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.setCanceledOnTouchOutside(isTouchOutside)

        binding.title.text = title
        context?.let { Glide.with(it).load(imagePath).into(binding.imageHint) }
        binding.actionButton.onClick {
            when(actionType){
                CMD.MSG_MB_NOBUTTON ->{
                    println("返回首页")
                    homeConsumer?.accept(true)
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
        setShowStyle()

        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT)
    }


    fun setTouchOutside(isTouchOutside:Boolean):DialogImgBox{
        this.isTouchOutside = isTouchOutside
        return this
    }

    fun setTitle(title:String?):DialogImgBox{
        if (title != null) {
            this.title = title
        }
        return this
    }
    fun setInitMsg(hint:String?){
        if (hint != null) {
            this.hint = hint
        }
    }
    fun setImagePath(path:String?){
        if (path != null) {
            this.imagePath = path
        }
    }
    fun setActionType(actionType:Byte):DialogImgBox{
        this.actionType = actionType
        return this
    }
    fun setBackResult(consumer: Consumer<Boolean>):DialogImgBox{
        this.consumer = consumer
        return this
    }

    fun setHomeBackResult(consumer: Consumer<Boolean>):DialogImgBox{
        this.homeConsumer = consumer
        return this
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