package com.zdyb.module_diagnosis.adapter

import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.bean.HelpMenuEntity

class PopupMenuAdapter: BaseQuickAdapter<HelpMenuEntity, BaseViewHolder> (R.layout.item_help_menu){


    override fun convert(holder: BaseViewHolder, item: HelpMenuEntity) {
        holder.setText(R.id.name, item.name)
        holder.setImageResource(R.id.image,item.image)
        holder.getView<CardView>(R.id.itemLayout).setCardBackgroundColor(ContextCompat.getColor(context,item.bgColor))
    }


}