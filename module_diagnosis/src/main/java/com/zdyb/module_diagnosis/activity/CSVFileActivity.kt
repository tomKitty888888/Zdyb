package com.zdyb.module_diagnosis.activity


import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdyb.lib_common.base.BaseActivity
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.databinding.ActivityCsvListBinding
import java.io.File

class CSVFileActivity :BaseActivity<ActivityCsvListBinding,BaseViewModel>(){


    override fun initViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initViewObservable() {
        super.initViewObservable()

        mAdapter.setOnItemClickListener { adapter, _, position ->

            val file = adapter.data[position] as File
            //FilePreviewActivity.startActivity(this,file.absolutePath)

            startActivity(getTextFileIntent(file.absolutePath))
        }
        binding.recyclerView.adapter = mAdapter
        mAdapter.setList(getData())
    }

    fun getExcelFileIntent(Path: String?): Intent? {
        val file = File(Path)
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //val uri: Uri = Uri.fromFile(file)
        val authority = applicationContext.packageName +".fileProvider"
        val uri: Uri = FileProvider.getUriForFile(this,authority,file)
        intent.setDataAndType(uri, "application/vnd.ms-excel")
        return intent
    }

    fun getTextFileIntent(Path: String?): Intent? {
        val file = File(Path)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //val uri = Uri.fromFile(file)
        val authority = applicationContext.packageName +".fileProvider"
        val uri: Uri = FileProvider.getUriForFile(this,authority,file)
        intent.setDataAndType(uri, "text/plain")
        return intent
    }


    private fun getData(): MutableList<File> {
        val list = mutableListOf<File>()

        val file = File(PathManager.getBasePath() + "/save")
        if (!file.exists()){
            return list
        }
        val files = file.listFiles()
        if (null != files){
            for (f in files){
                list.add(f)
            }
        }
        return list
    }

    private val mAdapter: BaseQuickAdapter<File, BaseViewHolder> =
        object : BaseQuickAdapter<File, BaseViewHolder>(R.layout.item_csv) {

            override fun convert(holder: BaseViewHolder, item: File) {
                holder.setText(R.id.name,item.name)
            }
        }

}