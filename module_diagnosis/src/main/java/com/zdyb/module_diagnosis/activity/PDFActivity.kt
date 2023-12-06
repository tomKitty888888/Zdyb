package com.zdyb.module_diagnosis.activity

import android.app.Activity
import android.content.Intent
import com.qmuiteam.qmui.kotlin.onClick
import com.zdyb.lib_common.base.BaseActivity
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.module_diagnosis.databinding.ActivityPdfBinding
import java.io.File

class PDFActivity :BaseActivity<ActivityPdfBinding,BaseViewModel>(){

    companion object{

        fun startActivity(activity :Activity,pdfPath :String){
            activity.startActivity(Intent(activity,PDFActivity::class.java).putExtra("pdfPath",pdfPath))
        }
    }

    override fun initViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initViewObservable() {
        super.initViewObservable()
        val url = intent.getStringExtra("pdfPath").toString()

        binding.pdfView.fromFile(File(url))
            .onRender { nbPages, pageWidth, pageHeight ->   }
            .onPageScroll { page, positionOffset ->  }
            .onPageChange { page, pageCount ->  }
            .spacing(0) //间距
            .enableAntialiasing(true)
            .load()

        binding.tvBack.onClick { finish() }
    }


}