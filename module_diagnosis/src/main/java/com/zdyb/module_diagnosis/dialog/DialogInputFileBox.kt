package com.zdyb.module_diagnosis.dialog

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
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.bean.BoxActionResult
import com.zdyb.module_diagnosis.databinding.DialogInputFileBoxBinding
import io.reactivex.functions.Consumer
import java.io.File

class DialogInputFileBox:BaseDialogFragment(){

    private val tag = "DialogInputBox"
    private var _binding: DialogInputFileBoxBinding? = null
    private val binding get() = _binding!!

    private var title = "提示"
    private var hint = ""
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
        _binding = DialogInputFileBoxBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.setCanceledOnTouchOutside(isTouchOutside)

        //带输入框的dialog 点击后的回调事件处理，接口中也需要进行定义 思路从这里续接

        binding.tvFilePath.text = hint //.replace("\\n","\n")
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
        binding.confirm.onClick {
            val value = binding.edText.text.toString().trim()
            if (TextUtils.isEmpty(value)){
                ToastUtils.showShort(getString(R.string.please_enter_the_content))
                return@onClick
            }

            //创建父路径
            val path = binding.tvFilePath.text.toString()
            val file = File(path)
            file.mkdirs()
            consumer?.accept(BoxActionResult(true, "$path/$value"))
        }

        binding.cancel.onClick {
            consumer?.accept(BoxActionResult(false,""))
        }
        println("$tag---->onActivityCreated()")
        setShowStyle()
    }


    fun setTouchOutside(isTouchOutside:Boolean):DialogInputFileBox{
        this.isTouchOutside = isTouchOutside
        return this
    }

    fun setTitle(title:String?):DialogInputFileBox{
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
    fun setMsg(hint:String?){
        if (hint != null) {
            this.hint = hint
            //binding.tvHint.text = hint
        }
    }
    fun setActionType(actionType:Byte):DialogInputFileBox{
        this.actionType = actionType
        return this
    }
    fun setBackResult(consumer: Consumer<BoxActionResult>):DialogInputFileBox{
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
        binding.edText.addTextChangedListener(object :TextWatcher{

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.apply {
                    binding.tvInputSum.text = "$length$inputSumUnit"
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        binding.edText.setOnEditorActionListener { v, actionId, event -> return@setOnEditorActionListener false }
        binding.edText.filters = arrayOf(object:InputFilter{
            override fun filter(
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
            ): CharSequence {
                if (!TextUtils.isEmpty(source)) {
                    if (source.toString().indexOf("*") >= 0) {
                        return ""
                    }
                    if (source.toString().indexOf("/") >= 0) {
                        return ""
                    }
                }
                return source.toString()
            }

        })
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
}