package com.zdyb.module_diagnosis.adapter

import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Status
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.bean.MotorcycleTypeEntity
import com.zdyb.module_diagnosis.utils.FetchUtils

class DownloadAdapter: BaseQuickAdapter<MotorcycleTypeEntity, BaseViewHolder>(R.layout.item_download){

    override fun convert(holder: BaseViewHolder, item: MotorcycleTypeEntity) {
        holder.setText(R.id.name,item.brand_name_zh +" "+ item.versions)
        val imageView = holder.getView<ImageView>(R.id.image)
        Glide.with(context)
            .load(item.imgUrl)
            .override(200, 100)
            //.skipMemoryCache(true)
            //.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(imageView)

        //holder.getView<SeekBar>(R.id.seekBar).setOnTouchListener { v, event -> return@setOnTouchListener true }
        holder.getView<ProgressBar>(R.id.progressBar).progress = item.progress


        val checkBox = holder.getView<CheckBox>(R.id.checkBox)
        val state = holder.getView<TextView>(R.id.state)
        if (!item.isDownload){
            checkBox.isEnabled = false
            checkBox.isChecked = true
            state.setTextColor(ContextCompat.getColor(context, R.color.color_theme))
            state.text = context.getString(R.string.download_state_5)
        }else{
            checkBox.isEnabled = true
            checkBox.isChecked = item.isSelect
            when (item.state){ //1 正在下载 2下载完毕在解压 3已是最新版本
                1 -> {
                    state.text = context.getString(R.string.download_state_1)
                }
                2 -> {
                    state.text = context.getString(R.string.download_state_2)
                }
                3 -> {
                    state.text = context.getString(R.string.download_state_3)
                }
                4 -> {
                    state.text = context.getString(R.string.download_state_4)
                }
                5 -> {
                    state.text = context.getString(R.string.download_state_5)
                    state.setTextColor(ContextCompat.getColor(context, R.color.color_theme))
                }
                else ->{
                    state.setTextColor(ContextCompat.getColor(context, R.color.red))
                    state.text = context.getString(R.string.download_state_6)
                }
            }

        }
    }



    fun update(download: Download,eta :Long,downloadedBytesPerSecond : Long){

        val pres =  FetchUtils.getDownloadSpeedString(context,downloadedBytesPerSecond)
        println(pres)

        data[download.group].progress = download.progress
        println(getStatusString(download.status))
        when (download.status){
            Status.QUEUED ->{
                data[download.group].state = 1
            }
            Status.DOWNLOADING ->{
                data[download.group].state = 2
            }
            Status.COMPLETED ->{
                data[download.group].state = 3
            }
            Status.FAILED ->{
                data[download.group].state = 4
            }

            else -> {}
        }
        notifyItemChanged(download.group)
    }


    private fun getStatusString(status: Status): String {
        return when (status) {
            Status.COMPLETED -> "Done - 已完成"
            Status.DOWNLOADING -> "Downloading - 下载中"
            Status.FAILED -> "Error"
            Status.PAUSED -> "Paused - 暂停"
            Status.QUEUED -> "Waiting in Queue - 队列中"
            Status.REMOVED -> "Removed - 已删除"
            Status.NONE -> "Not Queued - 无"
            else -> "Unknown"
        }
    }


}