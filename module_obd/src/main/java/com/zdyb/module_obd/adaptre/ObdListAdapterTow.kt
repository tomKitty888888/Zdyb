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

class ObdListAdapterTow(data :MutableList<ObdData>):BaseQuickAdapter<ObdData, BaseViewHolder>(R.layout.item_data_info,data) {

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

        println("holder.absoluteAdapterPosition="+holder.absoluteAdapterPosition )
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
                animation(iconChevron,0f,90f)
            }else{
                //iconChevron.rotation = 0F
                //animation(iconChevron,90f,0f)
//                if(iconChevron.rotation > 0){
//                    animation(iconChevron,0f,90f)
//                }

                if (iconChevron.animation != null){
                    iconChevron.animation.cancel()
                    println("动画有进行关闭")
                }
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