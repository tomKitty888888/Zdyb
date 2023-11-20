package com.zdyb.module_diagnosis.dialog

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.*
import android.text.method.DigitsKeyListener
import android.view.*
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ToastUtils
import com.qmuiteam.qmui.kotlin.onClick
import com.zdeps.gui.CMD
import com.zdyb.lib_common.base.BaseDialogFragment
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.bus.BusEvent
import com.zdyb.lib_common.bus.EventTypeDiagnosis
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.FileChooseActivity
import com.zdyb.module_diagnosis.bean.BoxActionResult
import com.zdyb.module_diagnosis.databinding.DialogChooseFileBoxBinding
import com.zdyb.module_diagnosis.databinding.DialogInputFileBoxBinding
import io.reactivex.functions.Consumer
import java.io.File

class DialogChooseFileBox:BaseDialogFragment(){

    private val tag = "DialogChooseFileBox"
    private var _binding: DialogChooseFileBoxBinding? = null
    private val binding get() = _binding!!

    private var title = "提示"
    private var path = ""
    private var fileType = ""
    private var consumer: Consumer<BoxActionResult>? = null //按钮
    private var isTouchOutside = false //触摸外部是否消失
    private var actionType :Byte = 0 //
    private var inputSumUnit : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inputSumUnit = getString(R.string.bit)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = DialogChooseFileBoxBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.setCanceledOnTouchOutside(isTouchOutside)

        //带输入框的dialog 点击后的回调事件处理，接口中也需要进行定义 思路从这里续接

        //binding.tvFilePath.text = hint //.replace("\\n","\n")
        binding.title.text = title
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

        binding.edText.onClick {
            FileChooseActivity.startActivity(activity!!, path,fileType)
        }

        binding.confirm.onClick {
            val value = binding.tvHint.text.toString().trim()
            if (TextUtils.isEmpty(value)){
                ToastUtils.showShort(getString(R.string.please_select_a_file))
                return@onClick
            }

            //选择的文件路径
            consumer?.accept(BoxActionResult(true, value))
        }

        binding.cancel.onClick {
            consumer?.accept(BoxActionResult(false,""))
        }
        println("$tag---->onActivityCreated()")
        setShowStyle()
    }


    fun setTouchOutside(isTouchOutside:Boolean):DialogChooseFileBox{
        this.isTouchOutside = isTouchOutside
        return this
    }

    fun setTitle(title:String?):DialogChooseFileBox{
        if (title != null) {
            this.title = title
        }
        return this
    }
    fun setInitPath(path:String?,fileType:String?){
        if (path != null) {
            this.path = path
        }
        if (fileType != null) {
            this.fileType = fileType
        }
    }


    fun setActionType(actionType:Byte):DialogChooseFileBox{
        this.actionType = actionType
        return this
    }
    fun setBackResult(consumer: Consumer<BoxActionResult>):DialogChooseFileBox{
        this.consumer = consumer
        return this
    }

    /**
     * 设置输入框的 输入限制
     */
    private fun setShowStyle(){
        when(actionType){
            CMD.INPUT_MODE_DEC ->{
                KLog.i("限制只输入十进制")
                binding.edText.keyListener = DigitsKeyListener.getInstance("0123456789")
            }
            CMD.INPUT_MODE_HEX ->{
                KLog.i("限制只输入十六进制字符")
                binding.edText.keyListener = DigitsKeyListener.getInstance("0123456789ABCDEF")
            }
            CMD.INPUT_MODE_VIN,CMD.INPUT_MODE_ALL ->{
                KLog.i("限制只输入VIN码")
                binding.edText.keyListener = DigitsKeyListener.getInstance("0123456789ABCDEFGHJKLMNPRSTUVWXYZ")
            }
            else ->{
                println("actionType=$actionType")

            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        println("$tag---->onResume()")
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
        //this.dialog?.window!!.decorView.setSystemUiVisibility(uiOptions)
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


    override fun eventComing(t: BusEvent?) {
        super.eventComing(t)
        t?.let {
            when(it.what){
                EventTypeDiagnosis.CMD_SELECT_FILE ->{
                    val filePath = it.data.toString()
                    binding.tvHint.text = filePath
                }
            }
        }
    }

    override fun registerRxBus(): Boolean {
        return true
    }
}