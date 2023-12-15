package com.zdyb.module_diagnosis.fragment

import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.bean.DeviceEntity
import com.zdyb.module_diagnosis.bean.DieseData
import com.zdyb.module_diagnosis.bean.ProductsEntity
import com.zdyb.module_diagnosis.bean.Transfer
import com.zdyb.module_diagnosis.databinding.FragmentLocalMenuListBinding
import com.zdyb.module_diagnosis.model.LoadDiagnosisModel
import com.zdyb.module_diagnosis.utils.FileUtils
import com.zdyb.module_diagnosis.widget.BottomBarActionButton
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileFilter

class LocalMenuListFragment:BaseNavFragment<FragmentLocalMenuListBinding, LoadDiagnosisModel>() {

    private var dis1: Disposable? = null
    private var dis2: Disposable? = null
    private val mMenuData = mutableListOf<DieseData>() //全部的数据

    val gradleList : HashMap<Int, List<DieseData>> = HashMap() //用于返回事件处理的数据存储

    lateinit var mProductsEntity : ProductsEntity
    private var title = MutableLiveData<String>()


    override fun initViewModel(): LoadDiagnosisModel {
        return ViewModelProvider(requireActivity())[LoadDiagnosisModel::class.java]
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

                        if (childIndex <= 0){
                            findNavController().navigateUp()
                            return@setClick
                        }
                        childIndex--
                        mAdapter.setList(gradleList[childIndex])
                },

            )


            //
            title.observe(this){
                mActivity.setTitle(it)
            }
        }
    }

    override fun initViewObservable() {
        super.initViewObservable()


        mAdapter.setOnItemClickListener { adapter, _, position ->
            val item = adapter.getItem(position) as DieseData
            pagination(item,position)
        }
        binding.recyclerView.isScrollbarFadingEnabled = false //常驻显示进度条
        binding.recyclerView.scrollBarFadeDuration = 0
        binding.recyclerView.adapter = mAdapter

        getAllData(mProductsEntity.menuFilePath)
    }



    private val mAdapter: BaseQuickAdapter<DieseData, BaseViewHolder> =
        object : BaseQuickAdapter<DieseData, BaseViewHolder>(R.layout.item_menu) {
            override fun convert(holder: BaseViewHolder, d: DieseData) {
                holder.setText(R.id.menuValue,d.name)

                if (holder.layoutPosition %2 != 0){
                    holder.getView<TextView>(R.id.menuValue).setBackgroundResource(R.color.item_bg)
                }else{
                    holder.getView<TextView>(R.id.menuValue).setBackgroundResource(0)
                }
            }
        }


    /**
     * 获取全部的数据
     */

    private fun getAllData(filePath :String) {
        dis1 = Observable.create { emitter -> emitter.onNext(FileUtils.fileRead(filePath)) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mMenuData.clear()
                mMenuData.addAll(it)
                mAdapter.setList(menuPaging(page,childIndex))
            },{it.printStackTrace()})
    }


    var page:Int = 0 //当前页面
    var childIndex:Int = 0 //当前第几个菜单


    /**
     * 获取页面数据
     */
    private fun menuPaging(page:Int,childIndex:Int):MutableList<DieseData>{
        val data = mutableListOf<DieseData>()
        for (item in mMenuData){
            if (item.id > page){
                if (childIndex == item.curGrade){
                    data.add(item)
                }else if (item.curGrade < childIndex){
                    break
                }
            }
        }
        //记录数据 返回的时候使用
        gradleList[childIndex] = data
        return data
    }

    /**
     * 处理 menu item点击逻辑
     */
    private fun pagination(item :DieseData,position :Int){
        try {
            if (!TextUtils.isEmpty(item.commend)){
                if (!TextUtils.isEmpty(item.path)){
                    //加载so
                    val path: String = PathManager.getBasePath() + item.path.replace("\\\\", "/")
                    KLog.i("localPath=$path")
                    val hint = getString(R.string.ecu_download_hint)+item.path
                    val file = File(path)
                    if (!file.exists()){
                        viewModel.showToast(hint)
                        return
                    }
                    val files : Array<File>  = file.listFiles(FileFilter { it -> return@FileFilter it.isDirectory}) as Array<File>
                    if (files.isEmpty()) {
                        viewModel.showToast(hint)
                        return
                    }

                    for (item in files){
                        println("取到得版本=${item.name}")
                    }

                    files.sortWith(Comparator { o1, o2 ->
                        val a = o1.name.replace("V", "").replace(".", "")
                        val b = o2.name.replace("V", "").replace(".", "")
                        return@Comparator if (Integer.valueOf(a) > Integer.valueOf(b)) {-1} else 1
                    })

                    KLog.i("查找具体的版本")
                    openDialog(item.commend,files[0].absolutePath,item.name,position)
                }else{
                    openDialog(item.commend,mProductsEntity.path,item.name,position)
                }
            }else{
                //翻页
                title.value = item.name
                childIndex++
                mAdapter.setList(menuPaging(item.id,childIndex))
                println("继续走这里？？")
            }
        }catch (e :Exception){
            e.printStackTrace()
            KLog.e(e.message)
        }
    }

    /**
     * 弹窗显示引脚图，刷写，或者是跳转到诊断去
     */
    private fun openDialog(commend: String, filepath: String, name: String, position: Int){
        KLog.i("commend=$commend name=$name path=$filepath")



        dis2 = Observable.create { emitter ->
            val dataList = mutableListOf<Transfer>()
            val file = File(PathManager.ecuReportPath())
            if (!file.exists()){
                emitter.onError(Throwable("${PathManager.ecuReportPath()}文件不存在"))
            }
            if (file.isDirectory){
                val files = file.listFiles()
                for (f in files!!){
                    val tempData = commend.replace("0x", "").lowercase()
                    if (f.isDirectory && f.name.lowercase() == tempData){
                        dataList.addAll(FileUtils.readTranfer(f.path,tempData))
                    }
                }
            }
            emitter.onNext(dataList)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                       if (it.size <= 0){
                           //直接跳转到诊断
                           println("直接跳转到诊断")
                           val tempStringId = commend.replace("0x","")
                           val id = tempStringId.toLong(16)

                           //处理filepath 路径格式
                           println("filepath=$filepath")
                           val tempPath = filepath.replace(PathManager.getBasePath(),"")
                           println("处理后filepath=$tempPath")
                           val deviceEntity = DeviceEntity(id,tempPath,name)
                           deviceEntity.versionPath = viewModel.getHighVersionSo(deviceEntity)
                           if (deviceEntity.versionPath.isEmpty()){
                               viewModel.showToast("诊断文件为空，请先下载")
                               return@subscribe
                           }

                           viewModel.anewDiagnosisService(Consumer {
                               if (it){
                                   viewModel.openDiagnosis(deviceEntity.versionPath, Consumer {
                                       val bundle = bundleOf(DeviceEntity.tag to deviceEntity)
                                       findNavController().navigate(R.id.action_localMenuListFragment_to_menuListFragment,bundle)
                                   })
                               }
                           })


                           return@subscribe
                       }

                //修改选择的背景颜色
                       println("弹窗查看引脚图等等 -- 待实现")
            },{it.printStackTrace()})



    }

    override fun onDestroyView() {

        super.onDestroyView()
    }
    override fun onDestroy() {
        dis1?.dispose()
        dis2?.dispose()
        super.onDestroy()
    }
}