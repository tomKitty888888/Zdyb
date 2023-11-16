package com.zdyb.module_obd.adaptre

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.ContextParams
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdeps.bean.OBDBean.ObdData
import com.zdyb.module_obd.R

class ObdListAdapterApp(data :MutableList<ObdData>):BaseQuickAdapter<ObdData, BaseViewHolder>(R.layout.item_data_info_2,data) {

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

        if (item.isError){
            holder.getView<TextView>(R.id.name).setTextColor(context.getColor(R.color.red))
            holder.getView<TextView>(R.id.value).setTextColor(context.getColor(R.color.red))
        }else{
            holder.getView<TextView>(R.id.name).setTextColor(context.getColor(R.color.black))
            holder.getView<TextView>(R.id.value).setTextColor(context.getColor(R.color.black))
        }

        if (item.contextType == 1){
            val img = if (item.value.toString() == "0") R.mipmap.icon_faulty_light_off else R.mipmap.icon_faulty_light_on
            holder.getView<ImageView>(R.id.icon).setImageResource(img)

            if(img == R.mipmap.icon_faulty_light_on){
                //显示图片的屏蔽文字
                holder.getView<TextView>(R.id.name).setTextColor(context.getColor(R.color.red))
                holder.setText(R.id.value,"")
            }
            //去掉文字
            holder.setText(R.id.value,"")
        }

        if (holder.absoluteAdapterPosition == data.size-1){
            println("ObdListAdapterTow --- holder.layoutPosition="+holder.layoutPosition +"     "+data.size )
            val line = holder.getView<View>(R.id.lineBottom)
            line.setBackgroundResource(R.color.white)
            val params =  line.layoutParams as ConstraintLayout.LayoutParams
            params.marginEnd = context.resources.getDimension(com.zdyb.lib_common.R.dimen.dp_10).toInt()
        }

        if (item.chevron != null && item.chevron){
            val iconChevron = holder.getView<ImageView>(R.id.icon_chevron)
            iconChevron.visibility = View.VISIBLE


            if (holder.layoutPosition == index){
                //animation(iconChevron,0f,90f)
                iconChevron.rotation = 90F
            }else{
                iconChevron.rotation = 0F

            }
        }
    }

    fun setRotationIndex(isRotationIndex :Int){
        this.index = isRotationIndex
        notifyItemChanged(isRotationIndex)
    }


    private fun animation(view: View, start:Float, end:Float){
        val objectAnimation =
            ObjectAnimator.ofFloat(view, "rotation", start,end)
        objectAnimation.duration=1000
        objectAnimation.repeatMode = ValueAnimator.REVERSE
        objectAnimation.repeatCount = 0
        objectAnimation.start()


    }
}