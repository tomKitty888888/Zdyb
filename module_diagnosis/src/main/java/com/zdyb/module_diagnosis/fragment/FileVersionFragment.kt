package com.zdyb.module_diagnosis.fragment

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemLongClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.snackbar.Snackbar
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2.Fetch.Impl.getDefaultInstance
import com.tonyodev.fetch2core.FetchObserver
import com.tonyodev.fetch2core.Reason
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.bean.ItemVersionEntity
import com.zdyb.module_diagnosis.bean.ProductsEntity
import com.zdyb.module_diagnosis.databinding.FragmentVersionListBinding
import com.zdyb.module_diagnosis.model.FileListModel
import com.zdyb.module_diagnosis.widget.BottomBarActionButton
import java.io.File

class FileVersionFragment:BaseNavFragment<FragmentVersionListBinding,FileListModel>(),FetchObserver<Download> {


    lateinit var mProductsEntity : ProductsEntity

    var request: Request? = null
    lateinit var mFetch: Fetch

    override fun initViewModel(): FileListModel {
       return FileListModel()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun initParam() {
        super.initParam()
        mProductsEntity = arguments?.getSerializable(ProductsEntity.tag) as ProductsEntity
    }



    override fun initActionButton() {
        super.initActionButton()
        if (activity is DiagnosisActivity){
            val mActivity = (activity as DiagnosisActivity)
            mActivity.removeAllActionButton()
            mActivity.addLeftActionButton(
                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_rescan,getString(R.string.action_button_update))
                    .setClick {

                        if (binding.netLayout.visibility == View.GONE){
                            binding.netLayout.visibility = View.VISIBLE
                            viewModel.getVersionList(mProductsEntity)
                        }else if (binding.netLayout.visibility == View.VISIBLE){
                            binding.netLayout.visibility = View.GONE
                        }

                    }
            )
            mActivity.addRightActionButton(
                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_back,getString(R.string.action_button_back))
                    .setPartitionLineVisibility(ConstraintLayout.LayoutParams.LEFT)
                    .setClick {

                        findNavController().navigateUp()
                },

            )

            //mActivity.setTitle("it")

        }
    }

    override fun initViewObservable() {
        super.initViewObservable()
        mFetch = getDefaultInstance()
        //mFetch.deleteAll()

        viewModel.versionListLiveData.observe(this){
            mNetAdapter.setList(it)
        }

        //排序
        mProductsEntity.versionList.sortWith(Comparator { o1, o2 ->
            val a = o1.versions.replace("V", "").replace(".", "")
            val b = o2.versions.replace("V", "").replace(".", "")
            return@Comparator if (Integer.valueOf(a) > Integer.valueOf(b)) {-1} else 1
        })

        mAdapter.setList(mProductsEntity.versionList)
        mAdapter.setOnItemClickListener { _, _, position ->
            val version = mAdapter.getItem(position)
            mProductsEntity.menuFilePath = mProductsEntity.path +File.separator+ version.versions +File.separator+ "menu.txt"
            println("菜单文件路径=${mProductsEntity.menuFilePath}")
            val menuFile = File(mProductsEntity.menuFilePath)
            if (!menuFile.exists()){
                viewModel.showToast("menu文件为空，请下载后重试")
                return@setOnItemClickListener
            }
            val bundle = bundleOf(ProductsEntity.tag to mProductsEntity)
            findNavController().navigate(R.id.action_fileVersionFragment_to_localMenuListFragment,bundle)
        }
        mAdapter.setOnItemLongClickListener { adapter, view, position ->

            val item = mAdapter.getItem(position)
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.version_manager_hint1))
            builder.setMessage(getString(R.string.version_manager_hint2)+item.versions)
            builder.setPositiveButton(getString(R.string.confirm)) { dialog, which ->

                viewModel.deleteVersionFile(mProductsEntity.path + "/"+ item.versions)
                mAdapter.removeAt(position)
                mProductsEntity.versionList.removeAt(position)

                //刷新网络列表
                if (binding.netLayout.visibility == View.VISIBLE){
                    viewModel.getVersionList(mProductsEntity)
                }
            }
            builder.setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            builder.show()
            return@setOnItemLongClickListener true
        }

        binding.loadRecyclerView.isScrollbarFadingEnabled = false //常驻显示进度条
        binding.loadRecyclerView.scrollBarFadeDuration = 0
        binding.loadRecyclerView.adapter = mAdapter


        mNetAdapter.addChildClickViewIds(R.id.butDownload)
        mNetAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view.id == R.id.butDownload){

                for (a in mNetAdapter.data){
                    if (a.state == 1){
                        viewModel.showToast("有文件正在下载中")
                        return@setOnItemChildClickListener
                    }
                }
                val item = adapter.getItem(position) as ItemVersionEntity
                val url = getString(R.string.download_url)+item.patch_url
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(getString(R.string.load))
                builder.setMessage(getString(R.string.download_state_7)+item.versions)
                builder.setPositiveButton(getString(R.string.confirm)) { dialog, which ->

                    println("下载路径=$url")
                    //文件存储位置
                    item.savePath = PathManager.versionFilePath(requireContext(),url)
                    println("文件存储位置=${item.savePath}")
                    downloadNew(url,item.savePath,position)
                }
                builder.setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                    dialog.dismiss()
                }
                builder.show()
            }
        }
        binding.netRecyclerView.adapter = mNetAdapter

    }

    //motorcycle_type=AfterTreatment&type=01Bosch&vci=20221185052T
    //type=AfterTreatment&motorcycle_type=01Bosch&vci=20221185052T

    private val mAdapter: BaseQuickAdapter<ItemVersionEntity, BaseViewHolder> =
        object : BaseQuickAdapter<ItemVersionEntity, BaseViewHolder>(R.layout.item_load_version) {
            override fun convert(holder: BaseViewHolder, s: ItemVersionEntity) {
                holder.setText(R.id.tv_version,s.versions)
            }
        }

    private val mNetAdapter: BaseQuickAdapter<ItemVersionEntity, BaseViewHolder> =
        object : BaseQuickAdapter<ItemVersionEntity, BaseViewHolder>(R.layout.item_net_version) {
            override fun convert(holder: BaseViewHolder, s: ItemVersionEntity) {
                holder.setText(R.id.tv_name,s.versions)

                holder.getView<ProgressBar>(R.id.progressBar).progress = s.progress
                when(s.state){
                    0 ->{
                        holder.setText(R.id.butDownload,context.getString(R.string.load))
                    }
                    1 ->{
                        holder.setText(R.id.butDownload,context.getString(R.string.download_state_2))
                    }
                    2 ->{
                        holder.setText(R.id.butDownload,context.getString(R.string.download_state_3))
                    }
                    3 ->{
                        holder.setText(R.id.butDownload,context.getString(R.string.download_state_4))
                    }
                }
            }
        }


    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
        request?.let { mFetch.removeFetchObserversForDownload(it.id, this) }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mFetch.close()
    }

    private fun downloadNew(url:String, filePath:String, index :Int){
        request = Request(url, filePath)
        request!!.groupId = index
        mFetch.attachFetchObserversForDownload(request!!.id, this)
            .enqueue(request!!,
                { result -> request = result }
            ) { result ->
                println("SingleDownloadActivity Error: %1\$s$result")
            }
    }

    override fun onChanged(download: Download, reason: Reason) {
        if (request!!.id == download.id) {
            setProgressView(download.status, download.progress,download.group)
        }
        if (download.error != Error.NONE) {
            showDownloadErrorSnackBar(download.error)
        }
    }

    private fun setProgressView(status: Status, progress: Int, index: Int) {
        when (status) {
            Status.QUEUED -> {
                //progressTextView.setText(R.string.queued)
                println("加入下载队列")
                mNetAdapter.data[index].state = 1
                mNetAdapter.notifyItemChanged(index)
            }
            Status.ADDED -> {
                //progressTextView.setText(R.string.added)
                println("添加")
            }
            Status.DOWNLOADING-> {
                if (progress == -1) {
                    //progressTextView.setText(R.string.downloading)
                    println("下载中")
                } else {
                    val progressString = resources.getString(R.string.percent_progress, progress)
                    //progressTextView.setText(progressString)
                    println(progressString)
                    mNetAdapter.data[index].state = 1
                    mNetAdapter.data[index].progress = progress
                    mNetAdapter.notifyItemChanged(index)
                }
            }
            Status.COMPLETED -> {
                println("下载完毕")
                //1解压
                val item = mNetAdapter.data[index]
                item.state = 2
                mNetAdapter.notifyItemChanged(index)

                viewModel.unzipFile(mNetAdapter.data[index].savePath,mProductsEntity.path,object: FileListModel.ZipState{
                    override fun progress(progress: Int, isSuccess: Boolean) {
                        super.progress(progress, isSuccess)
                        if (isSuccess){
                            println("解压成功 执行完毕")
                            //2将下载好的版本 挪动到本地版本去 分别刷新两个列表
                            mNetAdapter.removeAt(index)
                            for (aa in mNetAdapter.data){
                                println("测试状态=${aa.state}")
                            }
                            mProductsEntity.versionList.add(item)
                            mProductsEntity.versionList.sortWith(Comparator { o1, o2 ->
                                val a = o1.versions.replace("V", "").replace(".", "")
                                val b = o2.versions.replace("V", "").replace(".", "")
                                return@Comparator if (Integer.valueOf(a) > Integer.valueOf(b)) {-1} else 1
                            })
                            mAdapter.setList(mProductsEntity.versionList)
                        }
                    }
                })
            }
            else -> {
                //progressTextView.setText(R.string.status_unknown)
                println("status_unknown")
            }
        }
    }

    private fun showDownloadErrorSnackBar(error: Error) {
        val snackbar: Snackbar = Snackbar.make(
            binding.mainView,
            "Download Failed: ErrorCode: $error", Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction("retry") { v ->
            mFetch.retry(request!!.id)
            snackbar.dismiss()
        }
        snackbar.show()
    }
}