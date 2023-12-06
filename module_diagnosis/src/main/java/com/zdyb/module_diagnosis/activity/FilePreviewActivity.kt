package com.zdyb.module_diagnosis.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.webkit.WebSettings
import com.zdyb.lib_common.base.BaseActivity
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.module_diagnosis.databinding.ActivityPreviewFileBinding

class FilePreviewActivity :BaseActivity<ActivityPreviewFileBinding,BaseViewModel>(){

    lateinit var filePath :String

    companion object{
        fun startActivity(activity : Activity, pdfPath :String){
            activity.startActivity(Intent(activity,FilePreviewActivity::class.java).putExtra("filePath",pdfPath))
        }
    }

    override fun initViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initViewObservable() {
        super.initViewObservable()

        filePath = intent.getStringExtra("filePath").toString()

        val webSettings = binding.webView.settings
        webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webSettings.loadsImagesAutomatically = true
        webSettings.loadWithOverviewMode = true
        webSettings.javaScriptEnabled = true
        webSettings.useWideViewPort = true
        webSettings.setSupportZoom(false)
        webSettings.builtInZoomControls = false
        webSettings.domStorageEnabled = true


        binding.webView.loadUrl(filePath)
    }

}