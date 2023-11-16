package com.zdyb.module_diagnosis.fragment

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.bean.ProductsEntity
import com.zdyb.module_diagnosis.databinding.FragmentVersionListBinding
import com.zdyb.module_diagnosis.model.FileListModel
import com.zdyb.module_diagnosis.widget.BottomBarActionButton
import java.io.File
import java.util.*

class FileVersionFragment:BaseNavFragment<FragmentVersionListBinding,FileListModel>() {


    lateinit var mProductsEntity : ProductsEntity
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

        //排序
        mProductsEntity.versionList.sortWith(Comparator { o1, o2 ->
            val a = o1.replace("V", "").replace(".", "")
            val b = o2.replace("V", "").replace(".", "")
            return@Comparator if (Integer.valueOf(a) > Integer.valueOf(b)) {-1} else 1
        })

        mAdapter.setList(mProductsEntity.versionList)
        mAdapter.setOnItemClickListener { _, _, position ->
            val version = mAdapter.getItem(position)
            mProductsEntity.menuFilePath = mProductsEntity.path +File.separator+ version +File.separator+ "menu.txt"
            println("菜单文件路径=${mProductsEntity.menuFilePath}")
            val menuFile = File(mProductsEntity.menuFilePath)
            if (!menuFile.exists()){
                viewModel.showToast("menu文件为空，请下载后重试")
                return@setOnItemClickListener
            }
            val bundle = bundleOf(ProductsEntity.tag to mProductsEntity)
            findNavController().navigate(R.id.action_fileVersionFragment_to_localMenuListFragment,bundle)
        }
        binding.loadRecyclerView.isScrollbarFadingEnabled = false //常驻显示进度条
        binding.loadRecyclerView.scrollBarFadeDuration = 0
        binding.loadRecyclerView.adapter = mAdapter

    }



    private val mAdapter: BaseQuickAdapter<String, BaseViewHolder> =
        object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_load_version) {
            override fun convert(holder: BaseViewHolder, s: String) {
                holder.setText(R.id.tv_version,s)
            }
        }



}