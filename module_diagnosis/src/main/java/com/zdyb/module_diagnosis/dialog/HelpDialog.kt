package com.zdyb.module_diagnosis.dialog

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdyb.lib_common.base.BaseDialogFragment
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.bean.HelpMenuEntity
import com.zdyb.module_diagnosis.databinding.PopupMenuBinding

class HelpDialog:BaseDialogFragment() {

    private var _binding: PopupMenuBinding? = null
    private val binding get() = _binding!!

    private var isTouchOutside = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PopupMenuBinding.inflate(layoutInflater, container, false)
        return binding.root
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialog?.setCanceledOnTouchOutside(isTouchOutside)

        binding.recyclerView.adapter = mAdapter

        setData(requireContext())
        mAdapter.setOnItemClickListener{ adapter, view, position ->

        }
    }


    private fun setData(context: Context) {
        val data = mutableListOf<HelpMenuEntity>()
        data.add(
            HelpMenuEntity(context.getString(R.string.help_upData),
                R.mipmap.icon_d_update,
                R.color.color_theme)
        )
        data.add(
            HelpMenuEntity(context.getString(R.string.help_dataBase),
                R.mipmap.icon_d_database,
                R.color.data_base)
        )
        mAdapter.setList(data)
    }
    private val mAdapter: BaseQuickAdapter<HelpMenuEntity, BaseViewHolder> =
        object : BaseQuickAdapter<HelpMenuEntity, BaseViewHolder>(R.layout.item_help_menu) {
            override fun convert(holder: BaseViewHolder, item: HelpMenuEntity) {
                holder.setText(R.id.name, item.name)
                holder.setImageResource(R.id.image,item.image)
                holder.getView<CardView>(R.id.itemLayout).setCardBackgroundColor(ContextCompat.getColor(requireContext(),item.bgColor))
            }
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


    override fun onStart() {
        super.onStart()
//
//        val window = dialog!!.window
//        val params = window!!.attributes
//        params.y = 100
//        window.attributes = params
//        window.setGravity(Gravity.BOTTOM)

        val attributes: WindowManager.LayoutParams = dialog!!.window!!.attributes
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT
        attributes.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog!!.window!!.attributes = attributes


        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        dialog!!.window!!.decorView.setSystemUiVisibility(uiOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}