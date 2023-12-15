package com.zdyb.module_diagnosis.fragment_page

import com.github.barteksc.pdfviewer.util.FitPolicy
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.module_diagnosis.databinding.FragmentPdfBinding
import java.io.File

class PDFFragment:BaseNavFragment<FragmentPdfBinding,BaseViewModel>() {

    lateinit var mPath :String
    companion object{

        fun instance(path:String):PDFFragment{
            val fragment = PDFFragment()
            fragment.mPath = path
            return fragment
        }
    }

    override fun initViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initViewObservable() {
        super.initViewObservable()
        println("pdf=$mPath")


        binding.pdfView.fromFile(File(mPath))
            .onPageScroll { page, positionOffset ->  }
            .onPageChange { page, pageCount ->  }
            .spacing(0) //间距
            .enableAntialiasing(true)
            .pageFitPolicy(FitPolicy.WIDTH)
            .load()
    }

}