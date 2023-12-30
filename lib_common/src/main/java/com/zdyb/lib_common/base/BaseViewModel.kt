package com.zdyb.lib_common.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afollestad.materialdialogs.MaterialDialog
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.qmuiteam.qmui.widget.textview.QMUISpanTouchFixTextView
import com.zdyb.lib_common.R
import com.zdyb.lib_common.utils.*
import com.zdyb.lib_common.widget.DeviceLoadingDialog
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel, IBaseViewModel {
    var context: Context? = null
    protected var fragment: Fragment? = null

    constructor() {}
    constructor(context: Context?) {
        this.context = context
        initDialog()
        initLoadingDialog()
        initQMUITipLoadingDialog()
    }

    constructor(fragment: Fragment) : this(fragment.context) {
        this.fragment = fragment
    }

    private fun initDialog() {
        try {
            val builder = MaterialDialogUtils.showIndeterminateProgressDialog(
                context,
                context!!.getString(R.string.dialog_please_wait),
                true
            )
            dialog = builder.build()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取用户id
     *
     * @return
     */
    fun userId(): String {
        return PreferencesUtils.getString(context, SharePreferencesConstant.USER_ID)
    }


    private fun initLoadingDialog() {
        loadingDialog = DeviceLoadingDialog.Builder(context as AppCompatActivity?).build()
    }

    private fun initQMUITipLoadingDialog() {
//        tipDialog = new QMUITipDialog.Builder(context)
//                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
//                //.setTipWord(hint)
//                .create();
    }

    @JvmOverloads
    fun afterCreate(autoRequest: Boolean = true) {
    }

    private var tipDialog: QMUITipDialog? = null
    private var tipTextView: QMUISpanTouchFixTextView? = null
    private var loadingDialog: DeviceLoadingDialog? = null
    private var dialog: MaterialDialog? = null
    fun showToast(msg: String?) {
        if (!TextUtils.isEmpty(msg)) {
            MyToastUtils.showDefaultToast(msg)
        }
    }


    val isShowing: Boolean
        get() = null != tipDialog && tipDialog!!.isShowing

    fun showQMUILoading(hint: String?) {
        if (null == tipDialog) {
            tipDialog = QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(hint)
                .create()
        }
        if (null != tipDialog) {
            (context as AppCompatActivity?)!!.runOnUiThread { tipDialog!!.show() }
            val rootView = tipDialog!!.window!!.decorView as ViewGroup
            findQMUITextView(rootView)
        }
    }

    fun updateQMUILoading(newHint: String?) {
        if (tipDialog == null) {
            return
        }
        if (tipTextView == null) {
            val rootView = tipDialog!!.window!!.decorView as ViewGroup
            findQMUITextView(rootView)
        }
        if (tipTextView != null && newHint != null) {
            tipTextView!!.text = newHint
        }
    }

    private fun findQMUITextView(parentView: ViewGroup) {
        val count = parentView.childCount
        for (i in 0 until count) {
            val child = parentView.getChildAt(i)
            if (child is ViewGroup) {
                findQMUITextView(child)
            } else if (child is QMUISpanTouchFixTextView) {
                tipTextView = child
                return
            }
        }
    }

    fun dismissQMUILoading() {
        if (null != tipDialog && tipDialog!!.isShowing) {
            (context as AppCompatActivity?)!!.runOnUiThread { tipDialog!!.dismiss() }
        }
    }

    fun showLoading() {
        if (null != loadingDialog) {
            (context as AppCompatActivity?)!!.runOnUiThread {
                loadingDialog!!.show(
                    (context as AppCompatActivity?)!!.supportFragmentManager,
                    ""
                )
            }
        }
    }

    fun dismissLoading() {
        if (null != loadingDialog && loadingDialog!!.isVisible) {
            (context as AppCompatActivity?)!!.runOnUiThread { loadingDialog!!.dismiss() }
        }
    }

    fun dismissNoVisibleLoading() {
        if (null != loadingDialog) {
            (context as AppCompatActivity?)!!.runOnUiThread {
                try {
                    loadingDialog!!.dismiss()
                } catch (e: Exception) {
                }
            }
        }
    }

    @JvmOverloads
    fun showDialog(title: String? = context!!.getString(R.string.dialog_please_wait)) {
        if (dialog != null) {
            (context as AppCompatActivity?)!!.runOnUiThread {
                dialog!!.titleView.text = title
                dialog!!.show()
            }
        }
    }

    fun dismissDialog() {
        if (dialog != null && dialog!!.isShowing) {
            (context as AppCompatActivity?)!!.runOnUiThread { dialog!!.dismiss() }
        }
    }

    /**
     * 跳转页面
     *
     * @param clz 所跳转的目的Activity类
     */
    fun startActivity(clz: Class<*>?) {
        context!!.startActivity(Intent(context, clz))
    }

    /**
     * 跳转页面
     *
     * @param clz    所跳转的目的Activity类
     * @param bundle 跳转所携带的信息
     */
    fun startActivity(clz: Class<*>?, bundle: Bundle?) {
        val intent = Intent(context, clz)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        context!!.startActivity(intent)
    }

    fun startActivityForResult(clz: Class<*>?, requestCode: Int) {
        val intent = Intent(context, clz)
        (context as Activity?)!!.startActivityForResult(intent, requestCode)
    }

    fun startActivityForResult(intent: Intent?, requestCode: Int) {
        (context as Activity?)!!.startActivityForResult(intent, requestCode)
    }

    fun finishSelf() {
        if (context != null) {
            if (context is Activity) {
                (context as Activity).finish()
            }
        }
    }

    fun mSetResult(code: Int, intent: Intent?) {
        if (context != null) {
            if (context is Activity) {
                (context as Activity).setResult(code, intent)
                finishSelf()
            }
        }
    }

    override fun onCreate() {
        initDialog()
        initLoadingDialog()
        initQMUITipLoadingDialog()
    }

    override fun onStart() {}
    override fun onResume() {}
    override fun onPause() {}
    override fun onStop() {}
    override fun onDestroy() {
        for (d in disposables){
            DisposeUtil.close(d)
        }
        onCleared()

    }


    override fun registerRxBus() {}
    override fun removeRxBus() {}



    var disposables = ArrayList<Disposable>()
    fun addDisposable(disposable: Disposable){
        disposables.add(disposable)
    }

    fun getVCI():String{
        return PreferencesUtils.getString(BaseApplication.getInstance(), SharePreferencesConstant.VCI_CODE,"20221185052T")//这里测试给的默认值
    }


}