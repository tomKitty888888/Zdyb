package com.zdyb.module_diagnosis.popup

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.qmuiteam.qmui.kotlin.onClick
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.bean.CDSGroupingEntity
import com.zdyb.module_diagnosis.bean.CDSGroupingListEntity
import com.zdyb.module_diagnosis.bean.CDSSelectEntity
import io.reactivex.functions.Consumer


class GroupingPopupWindow() : PopupWindow() {

    private var consumer: Consumer<MutableList<CDSSelectEntity>>? = null
    private var consumerDelete: Consumer<Int>? = null

    constructor(context: Context) : this() {

        //PopupWindow中显示的自定义xml布局
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mMenuView = inflater.inflate(R.layout.popup_grouping, null)

        val tvAllData = mMenuView.findViewById<TextView>(R.id.tv_allData)
        tvAllData.onClick {
            consumer?.accept(null)
            dismiss()
        }
        val recyclerView = mMenuView.findViewById<RecyclerView>(R.id.recyclerView)
        mAdapter.addChildClickViewIds(R.id.img_delete)
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view.id == R.id.img_delete) {

                val dialog = AlertDialog.Builder(context)
                dialog.setTitle(context.getString(R.string.cds_grouping_delete_hint))
                dialog.setPositiveButton(context.getString(R.string.confirm)) { dialog, _ ->
                    consumerDelete?.accept(position)
                    mAdapter.data.removeAt(position)
                    dialog.dismiss()
                    dismiss()
                }
                dialog.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                dialog.show()
            }
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
        this.width = context.resources.getDimensionPixelSize(com.zdyb.lib_common.R.dimen.dp_200)
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
    }

    fun setData(groupingListEntity: CDSGroupingListEntity) {
        mAdapter.setList(groupingListEntity.list)
    }

    fun showGroupingListener(consumer: Consumer<MutableList<CDSSelectEntity>>) {
        this.consumer = consumer
    }
    fun deleteGroupingListener(consumerDelete: Consumer<Int>) {
        this.consumerDelete = consumerDelete
    }


    private val mAdapter: BaseQuickAdapter<CDSGroupingEntity, BaseViewHolder> =
        object : BaseQuickAdapter<CDSGroupingEntity, BaseViewHolder>(R.layout.item_grouping) {
            override fun convert(holder: BaseViewHolder, item: CDSGroupingEntity) {
                holder.setText(R.id.title, item.title)
            }
        }
}