package com.zdyb.module_diagnosis.activity

import android.content.ContextParams
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.qmuiteam.qmui.kotlin.onClick
import com.zdyb.lib_common.base.BaseActivity
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.adapter.BaseFragmentAdapter
import com.zdyb.module_diagnosis.adapter.MenuAdapter
import com.zdyb.module_diagnosis.bean.CartEntity
import com.zdyb.module_diagnosis.bean.RepairInstBean
import com.zdyb.module_diagnosis.bean.SensorBean
import com.zdyb.module_diagnosis.databinding.ActivityRepairInstructionsBinding
import com.zdyb.module_diagnosis.fragment_page.PDFFragment
import com.zdyb.module_diagnosis.fragment_page.VideoFragment
import com.zdyb.module_diagnosis.model.RepairInstModel
import java.io.File

class RepairInstActivity:BaseActivity<ActivityRepairInstructionsBinding, RepairInstModel>() {

    lateinit var mFragmentAdapter : BaseFragmentAdapter
    val fragmentPage = arrayListOf<Fragment>()
    val sensorBeanList = mutableListOf<SensorBean>()
    lateinit var mCartEntity : CartEntity

    override fun initViewModel(): RepairInstModel {
        return RepairInstModel()
    }

    override fun initParam() {
        mCartEntity = intent.getSerializableExtra("CartEntity") as CartEntity
    }

    override fun initViewObservable() {
        super.initViewObservable()
        mAdapter = MenuAdapter()
        binding.tvTitle.text = mCartEntity.typeName+">辅助维修帮助系统"
        binding.tvBack.onClick { finish() }
        mFragmentAdapter = BaseFragmentAdapter(this,fragmentPage)

        viewModel.dataList.observe(this){
            mAdapter.setList(it)

            initViewPage(it[0])
        }
        mAdapter.setOnItemClickListener { adapter, view, position ->
            // viewpage 清空数据,或者修改显示路径
            val item = adapter.getItem(position) as RepairInstBean
            initViewPage(item)
            binding.radioPdf.isChecked = true
            mAdapter.setSelectIndex(position)
            mAdapter.notifyDataSetChanged()
        }

        binding.recyclerView.adapter = mAdapter

        initReadButton()
        getTypeData()
    }

    //测试数据
    fun getTypeData(){
        viewModel.getMenuList(mCartEntity.typeName)
        //sensorBeanList.add(SensorBean("设备"))
    }

    private lateinit var mAdapter : MenuAdapter

//    private val mAdapter: BaseQuickAdapter<RepairInstBean, BaseViewHolder> =
//        object : BaseQuickAdapter<RepairInstBean, BaseViewHolder>(R.layout.item_sensor) {
//
//
//            override fun convert(holder: BaseViewHolder, item: RepairInstBean) {
//                holder.setText(R.id.name, item.menu)
//
//
//                if (holder.layoutPosition == index){
//                    holder.setBackgroundColor(R.id.itemLayout,ContextCompat.getColor(this@RepairInstActivity,R.color.color_theme))
//                }else{
//                    holder.setBackgroundColor(R.id.itemLayout,ContextCompat.getColor(this@RepairInstActivity,R.color.white))
//                }
//            }
//
//            var index :Int = 0
//            infix fun setSelectIndex(index :Int){
//                this.index = index
//            }
//        }


    fun initViewPage(r:RepairInstBean){
        fragmentPage.clear()
        if (fragmentPage.isNotEmpty()){
            for (f in fragmentPage){
                f.onDestroyView()
            }
        }

        fragmentPage.add(PDFFragment.instance(r.helpPath))
        fragmentPage.add(VideoFragment.instance(r.videoPath))
        fragmentPage.add(PDFFragment.instance(r.errorCodePath))
        binding.viewPager.adapter = mFragmentAdapter
        binding.viewPager.offscreenPageLimit = fragmentPage.size
        binding.viewPager.isUserInputEnabled = false //禁止滑动
    }

    fun initReadButton(){
        binding.radioPdf.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked){
                println("pdf")
                binding.viewPager.setCurrentItem(0,false)
            }
        }
        binding.radioVideo.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                println("Video")
                binding.viewPager.setCurrentItem(1,false)
            }
        }
        binding.radioErrorCode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                println("errorCode")
                binding.viewPager.setCurrentItem(2,false)
            }
        }
    }
}