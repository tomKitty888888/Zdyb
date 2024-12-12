package com.zdyb.module_diagnosis.fragment

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.view.View
import android.webkit.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ToastUtils
import com.zdeps.gui.CMD
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.lib_common.base.KLog
import com.zdyb.lib_common.utils.PathManager
import com.zdyb.module_diagnosis.R
import com.zdyb.module_diagnosis.activity.DiagnosisActivity
import com.zdyb.module_diagnosis.databinding.FragmentDataBaseBinding
import com.zdyb.module_diagnosis.dialog.DialogBox
import com.zdyb.module_diagnosis.utils.DisConfig
import com.zdyb.module_diagnosis.widget.BottomBarActionButton
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL
import java.net.URLDecoder

class DataBaseFragment:BaseNavFragment<FragmentDataBaseBinding,BaseViewModel>() {

    lateinit var mDialogBox : DialogBox

    override fun initViewModel(): BaseViewModel {
        return BaseViewModel()
    }
    override fun initActionButton() {
        super.initActionButton()
        if (activity is DiagnosisActivity){
            val mActivity = (activity as DiagnosisActivity)
            mActivity.removeAllActionButton()
//            mActivity.addLeftActionButton(
//                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_storage,getString(R.string.cds_start_save_data_to_file))
//                    .setClick {
//
//                    }
//
//            )
            mActivity.addRightActionButton(
                BottomBarActionButton(activity).addValue(R.mipmap.icon_d_back,getString(R.string.action_button_back))
                    .setPartitionLineVisibility(ConstraintLayout.LayoutParams.LEFT)
                    .setClick {
                              findNavController().popBackStack()
                    },

                )
        }
    }

    override fun initViewObservable() {
        super.initViewObservable()
        mDialogBox = DialogBox()
        mDialogBox.setResult(Consumer {

        })

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
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                if (url.contains(".txt")) {
                    mDialogBox.setBox(getString(R.string.help_dataBase_hint))
                    mDialogBox.show(childFragmentManager,"mDialogBox")
                } else {
                    view.loadUrl(url)
                    binding.progressBar.visibility = View.GONE
                }
                return true
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {

                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                binding.progressBar.visibility = View.GONE
            }
        }

        binding.webView.setDownloadListener(myWebViewDownLoadListener);

        binding.webView.loadUrl(DisConfig.HELPURL)
    }

    private var mDisposable: Disposable? = null
    private val myWebViewDownLoadListener = DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->

        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle(getString(R.string.hint))
        progressDialog.setMessage(getString(R.string.help_dataBase_load_hint))
        progressDialog.setCancelable(false)
        progressDialog.show()


        val fileName = URLDecoder.decode(url.substring(url.lastIndexOf("/") + 1))
        val fileToute: String = PathManager.getDownloadFolderPath() + File.separator + fileName

        mDisposable?.dispose()
        mDisposable = Observable.create<String> {
            val downloadFile = File(fileToute)
            FileUtils.copyURLToFile(URL(url), downloadFile)
            it.onNext(getString(R.string.load) + fileName +getString(R.string.succeed))

        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                progressDialog.dismiss()
                ToastUtils.showShort(it)
            },{
                it.printStackTrace()
                progressDialog.dismiss()
                ToastUtils.showShort(getString(R.string.load) + getString(R.string.fail))
            })
    }
}