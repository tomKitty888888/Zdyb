package com.zdyb.module_diagnosis.popup

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.qmuiteam.qmui.kotlin.onClick
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.bean.CDSGroupingEntity
import com.zdyb.module_diagnosis.bean.CDSGroupingListEntity
import com.zdyb.module_diagnosis.bean.CDSSelectEntity
import com.zdyb.module_diagnosis.bean.HelpMenuEntity
import io.reactivex.functions.Consumer


class MenuPopupWindow() : PopupWindow() {

    private var consumer: Consumer<MutableList<CDSSelectEntity>>? = null
    private var consumerDelete: Consumer<Int>? = null

    constructor(context: Context) : this() {

        //PopupWindow中显示的自定义xml布局
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mMenuView = inflater.inflate(R.layout.popup_menu, null)

        val recyclerView = mMenuView.findViewById<RecyclerView>(R.id.recyclerView)
        mAdapter.addChildClickViewIds(R.id.img_delete)
        mAdapter.setOnItemChildClickListener { adapter, view, position ->

        }
        recyclerView.adapter = mAdapter
        mAdapter.setOnItemClickListener(listener = { adapter, _, position ->
            val item = adapter.data[position] as CDSGroupingEntity
            consumer?.accept(item.list)
            dismiss()
        })

        // 设置外部可点击
        this.isOutsideTouchable = false

        // 设置弹出窗体的宽和高
        this.height = RelativeLayout.LayoutParams.WRAP_CONTENT
        this.width = context.resources.getDimensionPixelSize(com.zdyb.lib_common.R.dimen.dp_400)
        this.getMaxAvailableHeight(
            mMenuView,
            context.resources.getDimensionPixelSize(com.zdyb.lib_common.R.dimen.dp_320)
        )

        // 设置弹出窗体可点击
        this.isFocusable = true

        // 实例化一个ColorDrawable颜色为半透
        val dw = ColorDrawable(Color.TRANSPARENT)
        // 设置弹出窗体的背景
        setBackgroundDrawable(dw)
        // 设置视图
        this.contentView = mMenuView
        setData(context)
    }

    private fun setData(context: Context) {
        val data = mutableListOf<HelpMenuEntity>()
        data.add(HelpMenuEntity(context.getString(R.string.help_upData),R.mipmap.icon_d_update,R.color.color_theme))
        data.add(HelpMenuEntity(context.getString(R.string.help_dataBase),R.mipmap.icon_d_database,R.color.data_base))
        mAdapter.setList(data)
    }

    fun showGroupingListener(consumer: Consumer<MutableList<CDSSelectEntity>>) {
        this.consumer = consumer
    }
    fun deleteGroupingListener(consumerDelete: Consumer<Int>) {
        this.consumerDelete = consumerDelete
    }


    private val mAdapter: BaseQuickAdapter<HelpMenuEntity, BaseViewHolder> =
        object : BaseQuickAdapter<HelpMenuEntity, BaseViewHolder>(R.layout.item_help_menu) {
            override fun convert(holder: BaseViewHolder, item: HelpMenuEntity) {
                holder.setText(R.id.name, item.name)
                holder.setImageResource(R.id.image,item.image)
                holder.getView<CardView>(R.id.itemLayout).setCardBackgroundColor(ContextCompat.getColor(context,item.bgColor))
            }
        }
}