package com.zdyb.module_diagnosis.activity

import android.app.Activity
import android.content.Intent
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.qmuiteam.qmui.kotlin.onClick
import com.zdyb.lib_common.base.BaseActivity
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.bus.BusEvent
import com.zdyb.lib_common.bus.EventType
import com.zdyb.lib_common.bus.EventTypeDiagnosis
import com.zdyb.lib_common.bus.RxBus
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.databinding.ActivityFileChooseBinding
import com.zdyb.module_diagnosis.model.FileChooseModel
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class FileChooseActivity :BaseActivity<ActivityFileChooseBinding, FileChooseModel>(){


    lateinit var mPath :String
    lateinit var fileType :String
    var mTempPath :String = ""


    companion object{
        const val REQUESTCODE = 102

        fun startActivity(activity: Activity,path:String,fileType:String){
            activity.startActivityForResult(Intent(activity,FileChooseActivity::class.java)
                .putExtra("path",path)
                .putExtra("fileType",fileType),REQUESTCODE)
        }

    }

    override fun initViewModel(): FileChooseModel {
        return FileChooseModel()
    }


    override fun initViewObservable() {
        super.initViewObservable()
        mPath = intent.getStringExtra("path").toString()
        fileType = intent.getStringExtra("path").toString()
        mTempPath = PathManager.getBasePathNoSeparator()


        binding.layoutBack.fileImg.setImageResource(R.mipmap.icon_file_folder)
        binding.layoutBack.tvFileName.text = getString(R.string.go_back_to_the_previous_file)
        binding.cancel.onClick {
            finish()
        }
        binding.layoutBack.item.onClick {
            //返回上一级文件

            viewModel.getFileArray(topPath(mPath))
        }

        viewModel.fileListLiveData.observe(this){
            mAdapter.setList(it)
        }
        viewModel.currentFilePath.observe(this){
            //当前目录
            mPath = it
            binding.path.text = mPath
        }

        mAdapter.setOnItemClickListener { adapter, view, position ->
            val file = adapter.data[position] as File
            if (file.isDirectory){
                viewModel.getFileArray(nextPath(file.name))
            }else{
                //选择了文件
                val intent = Intent()
                intent.putExtra("filePath",file.absoluteFile)
                setResult(REQUEST_CODE,intent)
                RxBus.getDefault().post(BusEvent(EventTypeDiagnosis.CMD_SELECT_FILE,file.absoluteFile))
                finish()
            }
        }
        binding.path.text = mPath
        binding.recyclerView.adapter = mAdapter
        viewModel.getFileArray(mPath)
    }

    fun topPath(string: String):String{
        val index = string.lastIndexOf("/")
        val temp = string.substring(0,index)
        println("上一层路径=$temp")
        mPath = temp
        binding.path.text = mPath
        return temp
    }

    fun nextPath(string: String):String{
        val temp = mPath + File.separator +string

        mPath = temp
        binding.path.text = mPath
        return mPath
    }


    private val mAdapter: BaseQuickAdapter<File, BaseViewHolder> =
        object : BaseQuickAdapter<File, BaseViewHolder>(R.layout.item_file_info) {
            override fun convert(holder: BaseViewHolder, file: File) {

                try {
                    holder.setText(R.id.tv_fileName, file.name)
                    holder.setText(R.id.tv_fileTime, getFileInfoString(file))
                    holder.getView<ImageView>(R.id.fileImg).setImageResource(getFileIconId(file))
                }catch (e : Exception){
                    e.printStackTrace()
                }
            }

            private fun getFileInfoString(file: File): String {
                val fileSize = file.length()
                var ret = SimpleDateFormat("yyyy-MM-dd HH:mm  ").format(file.lastModified())
                if (file.isDirectory) {
                    val sub = file.listFiles()
                    var subCount = 0
                    if (sub != null) {
                        subCount = sub.size
                    }
                    ret += "$subCount items"
                } else {
                    var size = 0.0f
                    if (fileSize > 1024 * 1024 * 1024) {
                        size = fileSize / (1024f * 1024f * 1024f)
                        ret += DecimalFormat("#.00").format(size.toDouble()) + "GB"
                    } else if (fileSize > 1024 * 1024) {
                        size = fileSize / (1024f * 1024f)
                        ret += DecimalFormat("#.00").format(size.toDouble()) + "MB"
                    } else if (fileSize >= 1024) {
                        size = (fileSize / 1024).toFloat()
                        ret += DecimalFormat("#.00").format(size.toDouble()) + "KB"
                    } else {
                        ret += fileSize.toString() + "B"
                    }
                }
                return ret
            }


            private fun getFileIconId(file: File): Int {
                val id: Int = R.mipmap.icon_file_unknown
                val fileName = file.name
                when(fileName.substring(fileName.lastIndexOf(".") + 1)){
                    "7z" ->{ return R.mipmap.icon_file_7z}
                    "cab" ->{ return R.mipmap.icon_file_cab}
                    "iso" ->{ return R.mipmap.icon_file_iso}
                    "rar" ->{ return R.mipmap.icon_file_rar}
                    "tar" ->{ return R.mipmap.icon_file_tar}
                    "zip" ->{ return R.mipmap.icon_file_zip}
                    else ->{
                        if (file.isDirectory){
                            return R.mipmap.icon_file_folder
                        }
                        return R.mipmap.icon_file_unknown
                    }
                }
            }

        }
}