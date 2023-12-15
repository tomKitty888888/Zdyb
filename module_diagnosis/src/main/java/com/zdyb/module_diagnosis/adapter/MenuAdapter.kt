package com.zdyb.module_diagnosis.adapter

import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.bean.RepairInstBean

class MenuAdapter: BaseQuickAdapter<RepairInstBean, BaseViewHolder>(R.layout.item_sensor) {

    override fun convert(holder: BaseViewHolder, item: RepairInstBean) {
        holder.setText(R.id.name, item.menu)


        if (holder.layoutPosition == index){
            holder.setBackgroundColor(R.id.itemLayout,
                ContextCompat.getColor(context,R.color.dialog_bar_bg_end))
        }else{
            holder.setBackgroundColor(R.id.itemLayout,
                ContextCompat.getColor(context,R.color.white))
        }
    }

    var index :Int = 0
    infix fun setSelectIndex(index :Int){
        this.index = index
    }
}