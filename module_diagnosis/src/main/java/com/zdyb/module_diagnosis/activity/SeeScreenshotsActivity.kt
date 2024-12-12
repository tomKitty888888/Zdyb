package com.zdyb.module_diagnosis.activity

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdyb.lib_common.base.BaseActivity
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.databinding.ActivitySeeScreenshotsBinding
import java.io.File
import java.io.FileFilter

class SeeScreenshotsActivity :BaseActivity<ActivitySeeScreenshotsBinding,BaseViewModel>(){

    override fun initViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    lateinit var mData:MutableList<String>
    val mViews = mutableListOf<View>()

    override fun initViewObservable() {
        super.initViewObservable()

        mData = initImgData()
        mAdapter.setOnItemClickListener { adapter, _, position ->
            //显示viewpage
            //定位到点击的图片位置
            binding.viewPager.setCurrentItem(position,false)
            binding.viewPager.visibility = View.VISIBLE
        }
        mAdapter.setList(mData)
        binding.recyclerView.adapter = mAdapter


        for (item in mData){
            val imageView = ImageView(this)
            imageView.setOnClickListener { binding.viewPager.visibility = View.GONE }
            Glide.with(this).load(item).into(imageView)
            mViews.add(imageView)
        }
        binding.viewPager.adapter = pageAdapter
    }

    /**
     * 取图片路径
     */
    private fun initImgData():MutableList<String>{

        val pathList = mutableListOf<String>()
        try {

            val file = File(PathManager.getScreenshots())
            if (!file.exists()){
                return pathList
            }
            val images = file.listFiles(FileFilter { file -> file.name.endsWith(".png") })

            for (image in images){
                pathList.add(image.absolutePath)
            }

            for (item in pathList){
                println("图片路径=$item")
            }
        }catch (e :Exception){
            e.printStackTrace()
        }



        return pathList
    }

    private val mAdapter: BaseQuickAdapter<String, BaseViewHolder> =
        object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_see_img) {

            override fun convert(holder: BaseViewHolder, item: String) {
                Glide.with(context).load(item).into(holder.getView(R.id.image))
            }
        }

    private val pageAdapter :PagerAdapter = object : PagerAdapter() {
        override fun getCount(): Int {
            return mViews.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(mViews.get(position))
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            container.addView(mViews[position])
            return mViews[position]
        }
    }
}