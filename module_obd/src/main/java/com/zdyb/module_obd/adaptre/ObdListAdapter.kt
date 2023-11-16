package com.zdyb.module_obd.adaptre

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.ContextParams
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdeps.bean.OBDBean.ObdData
import com.zdyb.module_obd.R

class ObdListAdapter(data :MutableList<ObdData>):BaseQuickAdapter<ObdData, BaseViewHolder>(R.layout.item_data_info,data) {

    var index = 999

    override fun convert(holder: BaseViewHolder, item: ObdData) {

        holder.setText(R.id.name,item.description)
        if (item.value != null){
            if (item.value.toString() != context.getString(R.string.obd_not_supported)){
                holder.setText(R.id.value,item.value.toString()+item.unit)
            }else {
                holder.setText(R.id.value,item.value.toString())
            }

        }

        if (holder.absoluteAdapterPosition == data.size-1){
            println("ObdListAdapter --- holder.absoluteAdapterPosition="+holder.absoluteAdapterPosition +"     "+data.size )
            val line = holder.getView<View>(R.id.lineBottom)
            line.setBackgroundResource(R.color.white)
            val params =  line.layoutParams as ConstraintLayout.LayoutParams
            params.marginEnd = context.resources.getDimension(com.zdyb.lib_common.R.dimen.dp_10).toInt()

        }

        if (item.chevron != null && item.chevron){
            val iconChevron = holder.getView<ImageView>(R.id.icon_chevron)
            iconChevron.visibility = View.VISIBLE
        }
    }


}