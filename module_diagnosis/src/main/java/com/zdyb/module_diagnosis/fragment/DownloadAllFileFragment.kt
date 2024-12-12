package com.zdyb.module_diagnosis.fragment

import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.Func
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.adapter.DownloadAdapter
import com.zdyb.module_diagnosis.adapter.TabMenuAdapter
import com.zdyb.module_diagnosis.bean.DataTabBean
import com.zdyb.module_diagnosis.bean.MotorcycleTypeEntity
import com.zdyb.module_diagnosis.databinding.FragmentDownloadAllFileBinding
import com.zdyb.module_diagnosis.model.DownloadAllModel
import com.zdyb.module_diagnosis.utils.FetchData
import com.zdyb.module_diagnosis.widget.BottomBarActionButton
import io.reactivex.functions.Consumer


class DownloadAllFileFragment : BaseNavFragment<FragmentDownloadAllFileBinding, DownloadAllModel>() {

    private val STORAGE_PERMISSION_CODE = 200
    private val UNKNOWN_REMAINING_TIME: Long = -1
    private val UNKNOWN_DOWNLOADED_BYTES_PER_SECOND: Long = 0
    private val GROUP_ID = "listGroup".hashCode()
    private lateinit var mTabAdapter: TabMenuAdapter
    private lateinit var mAdapter: DownloadAdapter

    override fun initViewModel(): DownloadAllModel {
        return DownloadAllModel()
    }

    lateinit var mAllCheckBox :CheckBox
    override fun initActionButton() {
        super.initActionButton()
        if (activity is DiagnosisActivity){
            val mActivity = (activity as DiagnosisActivity)
            mActivity.removeAllActionButton()
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.action_button_checkbox,null)
            mAllCheckBox = view.findViewById<CheckBox>(R.id.checkBox)
            mAllCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked){
                    println("全部选择")
                    for (item in mAdapter.data){
                        if (!item.isSelect){
                            item.isSelect = true
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                }else{
                    println("取消全部选择")
                    for (item in mAdapter.data){
                        if (item.isSelect){
                            item.isSelect = false
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                }
            }
            mActivity.addLeftActionButton(
                view,
                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_update,getString(R.string.action_button_update))
                    //.setPartitionLineVisibility(ConstraintLayout.LayoutParams.LEFT)
                    .setClick {

                        for (item in mAdapter.data){
                            if (item.isDownload && item.isSelect){
                                println("升级==${item.downloadUrl}")
                                println("   ==${item.downloadSavePath}")
                            }
                        }

                        //这里需要传递 下载链接与下载结束后存放的地址
                        val requests = FetchData.getFetchRequestWithGroupId(requireContext(),mAdapter.data)
                        mFetch.enqueue(requests) { updatedRequests ->
                            println("kk")
                        }

                        //下载
                        for ((i, item) in mAdapter.data.withIndex()){
                            if (item.isDownload && item.isSelect){

                                println("需要下载的=${item.brand_name}")
                                mFetch.resume(i)
                            }
                        }
                    }

            )
            mActivity.addRightActionButton(
                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_back,getString(R.string.action_button_back))
                    .setPartitionLineVisibility(ConstraintLayout.LayoutParams.LEFT)
                    .setClick {
                        findNavController().popBackStack()
                    },
                )
        }
    }

    override fun initViewObservable() {
        super.initViewObservable()
        mAdapter = DownloadAdapter()
        mTabAdapter = TabMenuAdapter()

        viewModel.childData.observe(this){
            mAdapter.setList(it)
        }


        mTabAdapter.setOnItemClickListener { adapter, _, position ->
            val item = adapter.getItem(position) as DataTabBean
            mTabAdapter.selectIndex = position
            mTabAdapter.notifyDataSetChanged()
            //加载子项
            viewModel.getChildData(item, Consumer {
                if (it){
                    mAdapter.data.clear()
                    mAdapter.notifyDataSetChanged()
                }else{
                    println("重置")
                    mFetch.cancelAll()
                }
            })
            //重置全选按钮
            mAllCheckBox.isChecked = false
        }

        binding.tabRecyclerView.adapter = mTabAdapter
        binding.tabRecyclerView.isScrollbarFadingEnabled = false //常驻显示进度条
        binding.tabRecyclerView.scrollBarFadeDuration = 0
        mTabAdapter.setList(initTabData())

        //

        mAdapter.addChildClickViewIds(R.id.checkBox)
        mAdapter.setOnItemChildClickListener{ adapter, _, position ->
            val item = adapter.getItem(position) as MotorcycleTypeEntity
            if (item.isDownload){
                item.isSelect = !item.isSelect
                adapter.notifyItemChanged(position)
            }
        }
        mAdapter.setOnItemClickListener { adapter, _, position ->
            val item = adapter.getItem(position) as MotorcycleTypeEntity
            if (item.isDownload){
                item.isSelect = !item.isSelect
                adapter.notifyItemChanged(position)
            }

        }

        binding.recyclerView.adapter = mAdapter
        binding.recyclerView.isScrollbarFadingEnabled = false //常驻显示进度条
        binding.recyclerView.scrollBarFadeDuration = 0

        initRxFetch()
    }

    lateinit var mFetch : Fetch

    /**
     * 初始化下载
     */
    fun initRxFetch(){
        mFetch = Fetch.Impl.getDefaultInstance()
//        mFetch.getDownloadsInGroup(GROUP_ID, Func {
//
//        }).addListener(mFetchListener)

        mFetch.addListener(mFetchListener)
    }
    /**
     * tab 数据
     */
    private fun initTabData():MutableList<DataTabBean>{
        val list = mutableListOf<DataTabBean>()
        list.add(DataTabBean(getString(R.string.tab_1),"Zdeps"))
        list.add(DataTabBean(getString(R.string.tab_2),"Electronic"))
        list.add(DataTabBean(getString(R.string.tab_3),"AfterTreatment"))
        list.add(DataTabBean(getString(R.string.tab_4),"Reflash"))
        list.add(DataTabBean(getString(R.string.tab_5),"Vehicle"))
        list.add(DataTabBean(getString(R.string.tab_6),"Natural"))
        list.add(DataTabBean(getString(R.string.tab_7),"Obd"))
        list.add(DataTabBean(getString(R.string.tab_8),"Engine"))
        list.add(DataTabBean(getString(R.string.tab_9),"NER"))
        list.add(DataTabBean(getString(R.string.tab_10),"Mechanical"))
        return list
    }




    private val mFetchListener = object : AbstractFetchListener() {

        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
            super.onQueued(download, waitingOnNetwork)
            println("队列中=${download.id}")
            mAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
        }

        override fun onCompleted(download: Download) {
            super.onCompleted(download)
            mAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
            if (download.status == Status.COMPLETED){
                //解压文件
                viewModel.unzipFile(mAdapter.data[download.group],object :DownloadAllModel.ZipState{
                    override fun progress(progress: Int, isSuccess: Boolean) {
                        println("进度aa=$progress")
                        if (isSuccess){
                            //刷新状态 已是最新版本
                            mAdapter.data[download.group].state = 5
                            mAdapter.data[download.group].isDownload = false
                            mAdapter.notifyItemChanged(download.group)
                        }else{
                            //解压失败
                        }
                    }
                })
            }
        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            super.onError(download, error, throwable)
            println("下载错误=${download.id}")
            mAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
        }

        override fun onProgress(download: Download,etaInMilliSeconds: Long,downloadedBytesPerSecond: Long) {
            super.onProgress(download, etaInMilliSeconds, downloadedBytesPerSecond)
            println("进度=$downloadedBytesPerSecond")
            mAdapter.update(download, etaInMilliSeconds, downloadedBytesPerSecond)
        }

        override fun onResumed(download: Download) {
            super.onResumed(download)
            mAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
        }

        override fun onCancelled(download: Download) {
            super.onCancelled(download)
            mAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
        }

        override fun onRemoved(download: Download) {
            super.onRemoved(download)
            mAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
        }

        override fun onDeleted(download: Download) {
            super.onDeleted(download)
            mAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
        }
    }

    override fun onPause() {
        super.onPause()
        mFetch.removeListener(mFetchListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mFetch.close()
    }
}