package com.zdyb.module_diagnosis.fragment

import android.os.Bundle
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.bean.ProductsEntity
import com.zdyb.module_diagnosis.databinding.FragmentFileListBinding
import com.zdyb.module_diagnosis.model.FileListModel
import com.zdyb.module_diagnosis.widget.BottomBarActionButton

class FileListFragment:BaseNavFragment<FragmentFileListBinding,FileListModel>() {


    companion object{
        var TAG : String ="filePath"
    }

    private lateinit var mFilePath : String
    override fun initViewModel(): FileListModel {
       return FileListModel()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun initParam() {
        super.initParam()
        mFilePath = arguments?.getString(TAG).toString()
    }



    override fun initActionButton() {
        super.initActionButton()
        if (activity is DiagnosisActivity){
            val mActivity = (activity as DiagnosisActivity)
            mActivity.removeAllActionButton()
//            mActivity.addLeftActionButton(
//                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_rescan,getString(R.string.action_button_rescan))
//                    .setClick {
//                        viewModel.startScan()
//                    }
//            )
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

        viewModel.fileListLiveData.observe(this){
            for (item in it){
                println(item.path)
                println(item.imagePath)
            }
            mAdapter.setList(it)
        }
        mAdapter.setOnItemClickListener { _, _, position ->
            val item = mAdapter.getItem(position)
            val bundle = bundleOf(ProductsEntity.tag to item)
            findNavController().navigate(R.id.action_fileListFragment_to_fileVersionFragment,bundle)
        }
        binding.recyclerView.isScrollbarFadingEnabled = false //常驻显示进度条
        binding.recyclerView.scrollBarFadeDuration = 0
        binding.recyclerView.adapter = mAdapter

        viewModel.getData(mFilePath)
    }



    private val mAdapter: BaseQuickAdapter<ProductsEntity, BaseViewHolder> =
        object : BaseQuickAdapter<ProductsEntity, BaseViewHolder>(R.layout.item_file_img) {
            override fun convert(holder: BaseViewHolder, p: ProductsEntity) {

                Glide.with(requireContext()).load(p.imagePath).into(holder.getView<ImageView>(R.id.image))
            }
        }



}