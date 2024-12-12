package com.zdyb.module_diagnosis.dialog

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ToastUtils
import com.qmuiteam.qmui.kotlin.onClick
import com.zdyb.lib_common.base.BaseApplication
import com.zdyb.lib_common.base.BaseDialogFragment
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.databinding.DialogWebBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL
import java.net.URLDecoder

class DialogWebBox:BaseDialogFragment(){

    private var _binding: DialogWebBinding? = null
    private val binding get() = _binding!!

    private var title = "提示"
    private var hint = ""
    private var consumer: Consumer<Boolean>? = null //按钮
    private var isTouchOutside = false //触摸外部是否消失
    private var actionType :Byte = 0 //

    private var url = ""
    private var mDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL,R.style.FullSreenDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = DialogWebBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.setCanceledOnTouchOutside(isTouchOutside)

        binding.btnBack.onClick {
            dismiss()
        }
        initWeb()
        binding.webView.loadUrl(url)
    }


    private fun initWeb(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        binding.webView.settings.allowFileAccess = true // 设置允许访问文件数据
        binding.webView.settings.setSupportZoom(false) //支持放大网页功能
        binding.webView.settings.builtInZoomControls = false //支持缩小网页功能
        binding.webView.settings.javaScriptEnabled = true //支持JAVA
        binding.webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        binding.webView.settings.blockNetworkImage = false // 解决图片不显示


        binding.webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    view?.loadUrl(url)
                }
                return true
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                //super.onReceivedSslError(view, handler, error)
                handler?.proceed()
            }
        }
        binding.webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            ToastUtils.showShort(getString(R.string.dtc_start_load_file))
            val fileName = URLDecoder.decode(url.substring(url.lastIndexOf("/") + 1))
            val fileToute: String = PathManager.getDownloadFolderPath() + File.separator + fileName

            mDisposable?.dispose()
            mDisposable = Observable.create<String> {
                showNotification(getString(R.string.loading),fileName)
                val downloadFile = File(fileToute)
                FileUtils.copyURLToFile(URL(url), downloadFile)
                it.onNext(getString(R.string.load) + fileName +getString(R.string.succeed))
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    ToastUtils.showShort(it)
            },{
                    it.printStackTrace()
                    ToastUtils.showShort(getString(R.string.load) + getString(R.string.fail))
            })

        }
    }

    private val NOTIFICATION_TAG = "loadingHint"
    private val notificationId = 1018
    private fun showNotification(title:String, text:String){
        // 更为详细和判断全面的代码请参考文末源码链接
        val builder = Notification.Builder(context)
            .setContentTitle(title)
            .setContentText(text)
            // small icon一般是应用的图标
            .setSmallIcon(R.mipmap.app_logo)
            // largeIcon是显示在通知栏消息右侧的比smallIcon更大些的图，一般用来放和本消息强关联的图，没有large icon也可以
            //.setLargeIcon(BitmapFactory.decodeResource(BaseApplication.getInstance().resources, R.mipmap.person_icon))

        val manager = BaseApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_TAG, notificationId, builder.build())
    }

    fun setUrl(url:String){
        this.url = url
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        this.dialog?.window!!.decorView.setSystemUiVisibility(uiOptions)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            show = true
            super.show(manager, tag)
        }catch (e :Exception){
            e.printStackTrace()
        }
    }

    fun isShow():Boolean{
        return show
    }

    var show = false
    override fun dismiss() {
        mDisposable?.dispose()
        show = false
        super.dismiss()
    }
}