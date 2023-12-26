package com.zdyb.module_diagnosis.adapter

import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.bean.DataTabBean

class TabMenuAdapter: BaseQuickAdapter<DataTabBean, BaseViewHolder> (R.layout.item_type_text){

    var selectIndex = 9999

    override fun convert(holder: BaseViewHolder, item: DataTabBean) {
        holder.setText(R.id.name,item.name)
        val textView = holder.getView<TextView>(R.id.name)
        if (holder.layoutPosition == selectIndex){
            holder.getView<ConstraintLayout>(R.id.itemLayout).setBackgroundResource(R.color.color_theme)
            textView.setTextColor(ContextCompat.getColor(context,R.color.white))
        }else{
            holder.getView<ConstraintLayout>(R.id.itemLayout).setBackgroundResource(R.color.white)
            textView.setTextColor(ContextCompat.getColor(context,R.color.black))
        }
    }


}