package com.zdyb.lib_common.base

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.zdyb.lib_common.R
import com.zdyb.lib_common.bus.BusEvent
import com.zdyb.lib_common.bus.RxBus
import com.zdyb.lib_common.bus.RxSubscriptions
import com.zdyb.lib_common.utils.MyToastUtils
import com.zdyb.lib_common.utils.RxHelper
import com.zdyb.lib_common.utils.RouterUtil
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity
import com.zdyb.lib_common.utils.Helper
import com.zdyb.lib_common.widget.click.ViewClickUtil
import io.reactivex.disposables.Disposable
import java.lang.Exception
import java.lang.reflect.ParameterizedType
import java.util.ArrayList
import java.util.concurrent.TimeUnit

abstract class BaseActivity<V : ViewBinding, VM : BaseViewModel> :
    RxAppCompatActivity(), IBaseActivity, WeakHandlerCallback {
    protected var mContext: Context? = null
    protected lateinit var binding: V
    protected lateinit var viewModel: VM
    protected var viewModels: MutableList<BaseViewModel?>? = ArrayList()
    private var mSubscription: Disposable? = null
    protected val REQUEST_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        RouterUtil.inject(this)
        setAndroidNativeLightStatusBar(this, true)

        if(Helper.isPad(this)){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }else{
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        mContext = this
        initParam()

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating) {
            val result = fixOrientation() //处理透明页面和非全屏页设置横竖屏报错问题
            KLog.i("onCreate fixOrientation when Oreo, result = $result")
        }
        process(savedInstanceState)
        super.onCreate(savedInstanceState)
        initViewDataBinding(savedInstanceState)
        setStatusBarColor(this, R.color.white) //默认白色
        initViewObservable()
        initData()
        for (viewModel in viewModels!!) {
            viewModel!!.onCreate()
            viewModel.registerRxBus()
        }
        if (registerRxBus()) {
            mSubscription = RxBus.getDefault().toObservable(BusEvent::class.java)
                .compose(RxHelper.errorEmpty())
                .subscribe({ busEvent -> eventComing(busEvent) }
                ) { throwable -> throwable.printStackTrace() }
            //将订阅者加入管理站
            RxSubscriptions.add(mSubscription)
        }
        initEnd()
    }

    /**
     * 绑定
     * @param savedInstanceState
     */
    fun initViewDataBinding(savedInstanceState: Bundle?) {
        val type = javaClass.genericSuperclass as ParameterizedType
        val aClass = type.actualTypeArguments[0] as Class<*>
        val method = aClass.getDeclaredMethod("inflate", LayoutInflater::class.java)
        binding = method.invoke(null, layoutInflater) as V
        setContentView(binding.root)

        viewModel = initViewModel()
        viewModels = if (initViewModels() != null) {
            initViewModels()
        } else {
            ArrayList()
        }
        if (!viewModels!!.contains(viewModel)) {
            viewModels!!.add(viewModel)
        }
        for (viewModel in viewModels!!) {
            viewModel!!.context = this
        }


        //val appBarLayout: AppBarLayout = binding?.root?.findViewById(R.id.appbar)
        val appBarLayout: AppBarLayout? = binding.root.findViewById(R.id.appbar)
        //val toolbarLayout: CollapsingToolbarLayout? = binding.root.findViewById(R.id.toolbarLayout)
        //Toolbar toolbar = binding.getRoot().findViewById(R.id.toolbar);
        val btnBreak: ImageView? = binding.root.findViewById(R.id.btn_back)
        val title: TextView? = binding.root.findViewById(R.id.title)
        val subTitle:TextView? = binding.root.findViewById(R.id.subTitle)
        val tvRightMenu: TextView? = binding.root.findViewById(R.id.tv_right_menu)
        val btnRightMenu: ImageButton? = binding.root.findViewById(R.id.btn_right_menu)
        val btnRightMenu2: ImageButton? = binding.root.findViewById(R.id.btn_right_menu2)
        if (btnBreak != null) {
            btnBreak.setOnClickListener { onBackPressed() }
            btnBreak.visibility = leftBreakVisibility()
        }
        if (title != null) {
            title.text = getTitleText()
        }
        if (subTitle != null) {
            if (!TextUtils.isEmpty(getSubTitleText())){
                subTitle.visibility = View.VISIBLE
                subTitle.text = getSubTitleText()
            }
        }

        if (tvRightMenu != null) {
            tvRightMenu.text = rightMenu()
            val listener = rightMenuClick()
            tvRightMenu.setOnClickListener(listener)
            ViewClickUtil.Builder()
                .setSkipDuration(2000)
                .setTimeUnit(TimeUnit.MILLISECONDS)
                .setType(ViewClickUtil.Type.VIEW)
                .build().clicks(listener, tvRightMenu)
        }
        if (btnRightMenu != null) {
            btnRightMenu.visibility = rightMenuVisibility()
            btnRightMenu.setImageResource(rightMenuIcon())
            val listener = rightMenuClick()
            btnRightMenu.setOnClickListener(listener)
            ViewClickUtil.Builder()
                .setSkipDuration(2000)
                .setTimeUnit(TimeUnit.MILLISECONDS)
                .setType(ViewClickUtil.Type.VIEW)
                .build().clicks(listener, btnRightMenu)
        }

        if (btnRightMenu2 != null) {
            btnRightMenu2.visibility = rightMenuVisibility2()
            btnRightMenu2.setImageResource(rightMenuIcon2())
            val listener2 = rightMenuClick2()
            btnRightMenu2.setOnClickListener(listener2)
            ViewClickUtil.Builder()
                .setSkipDuration(2000)
                .setTimeUnit(TimeUnit.MILLISECONDS)
                .setType(ViewClickUtil.Type.VIEW)
                .build().clicks(listener2, btnRightMenu2)
        }

    }

    protected fun recyclerViewSetLayoutManager(recyclerView: RecyclerView) {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
    }

    override fun onResume() {
        super.onResume()
        for (viewModel in viewModels!!) {
            viewModel!!.onResume()
        }
    }

    override fun onStart() {
        super.onStart()
        for (viewModel in viewModels!!) {
            viewModel!!.onStart()
        }
    }

    override fun onStop() {
        super.onStop()
        for (viewModel in viewModels!!) {
            viewModel!!.onStop()
        }
    }

    override fun onPause() {
        super.onPause()
        for (viewModel in viewModels!!) {
            viewModel!!.onPause()
        }
        MyToastUtils.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (registerRxBus()) {
            RxSubscriptions.remove(mSubscription)
        }
        //Messenger.getDefault().unregister(this);
        for (viewModel in viewModels!!) {
            viewModel!!.removeRxBus()
            viewModel.onDestroy()
            //viewModel = null
        }
        viewModels!!.clear()

        for (d in disposables){
            d.dispose()
        }

    }


    var disposables = ArrayList<Disposable>()
    fun addDisposable(disposable:Disposable){
        disposables.add(disposable)
    }

    override fun initParam() {}

    /**
     * 初始化根布局
     *
     * @return 布局layout的id
     */
    open fun initContentView(savedInstanceState: Bundle?): Int {
        return 0
    }

    /**
     * 初始化ViewModel的id
     *
     * @return BR的id
     */
    fun initVariableId(): Int {
        return 0
    }

    /**
     * 初始化 viewModel
     * @return
     */
    abstract fun initViewModel(): VM

    /**
     * 初始化viewModels
     *
     * @return
     */
    protected fun initViewModels(): MutableList<BaseViewModel?>? {
        return ArrayList()
    }

    override fun initData() {}
    protected open fun initEnd() {}
    override fun initViewObservable() {}

    //RxBus
    protected open fun registerRxBus(): Boolean {
        return false
    }

    protected open fun eventComing(t: BusEvent?) {}

    /**
     * 消息队列回调监听
     */
    private val mCallback = Handler.Callback { msg -> handleMessage(msg) }
    /**
     * 弱引用handler
     * 防止内存泄露
     *
     * @return
     */
    /**
     * 弱引用handler
     */
    open val handler = Handler(mCallback)

    /**
     * Handler回调
     *
     * @param msg
     * @return
     */
    override fun handleMessage(msg: Message): Boolean {
        return false
    }

    override fun setRequestedOrientation(requestedOrientation: Int) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating) {
            KLog.i("avoid calling setRequestedOrientation when Oreo.")
            return
        }
        try {
            super.setRequestedOrientation(requestedOrientation)
        } catch (e: Exception) {
        }
    }

    private val isTranslucentOrFloating: Boolean
        private get() {
            var isTranslucentOrFloating = false
            try {
                val styleableRes = Class.forName("com.android.internal.R\$styleable")
                    .getField("Window")[null] as IntArray
                val ta = obtainStyledAttributes(styleableRes)
                val m =
                    ActivityInfo::class.java.getMethod(
                        "isTranslucentOrFloating",
                        TypedArray::class.java
                    )
                m.isAccessible = true
                isTranslucentOrFloating = m.invoke(null, ta) as Boolean
                m.isAccessible = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return isTranslucentOrFloating
        }

    private fun fixOrientation(): Boolean {
        try {
            val field = Activity::class.java.getDeclaredField("mActivityInfo")
            field.isAccessible = true
            val o = field[this] as ActivityInfo
            o.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            field.isAccessible = false
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 标题文字颜色
     *
     * @return
     */
    protected open fun getTitleTextColor(): Int{
        return 0
    }

    /**
     * 标题文字
     *
     * @return
     */
    protected open fun getTitleText(): CharSequence{
        return ""
    }

    /**
     * 子标题文字
     *
     * @return
     */
    protected open fun getSubTitleText(): CharSequence{
        return ""
    }


    /**
     * 右边menu文字
     *
     * @return
     */
    protected open fun rightMenu(): CharSequence{
        return ""
    }

    /**
     * 右边menu icon
     *
     * @return
     */
    protected open fun rightMenuVisibility(): Int{
        return View.GONE
    }

    /**
     * 左边的返回menu icon
     *
     * @return
     */
    protected open fun leftBreakVisibility(): Int{
        return View.VISIBLE
    }

    /**
     * 右边menu click事件
     * @return
     */
    protected open fun rightMenuClick(): View.OnClickListener?{
        return null
    }


    @DrawableRes
    protected open fun rightMenuIcon():  Int{
        return 0
    }

    /**
     * 右边menu icon
     *
     * @return
     */
    protected open fun rightMenuVisibility2(): Int{
        return View.GONE
    }

    /**
     * 右边menu click事件
     * @return
     */
    protected open fun rightMenuClick2(): View.OnClickListener?{
        return null
    }


    @DrawableRes
    protected open fun rightMenuIcon2():  Int{
        return 0
    }

    /**
     * 自定义左边返回按钮图标
     *
     * @return
     */
    protected fun navigationIcon(): Int{
        return 0
    }

    /**
     * 右边菜单
     *
     * @return
     */
    protected fun menuLayoutId(): Int{
        return 0
    }

    /**
     * 默认布局禁止被顶上去,默认不为全屏
     * @return
     */
    protected open val isUseFullScreenMode: Boolean
        protected get() = false

    /**
     * 修改状态栏字体颜色后，布局会被顶上去
     * @param savedInstanceState
     */
    private fun process(savedInstanceState: Bundle?) {
        // 华为,OPPO机型在StatusBarUtil.setLightStatusBar后布局被顶到状态栏上去了
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val content = (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
            if (content != null && !isUseFullScreenMode) {
                content.fitsSystemWindows = true
            }
        }
    }

    /**
     * 修改状态栏字体颜色
     * @param activity
     * @param dark
     */
    private fun setAndroidNativeLightStatusBar(activity: Activity, dark: Boolean) {
        val decor = activity.window.decorView
        if (dark) {
            decor.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decor.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    /**
     * 修改状态栏颜色，支持4.4以上版本
     * @param activity
     * @param colorId
     */
    protected open fun setStatusBarColor(activity: Activity, colorId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = activity.resources.getColor(colorId)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //5.0以下暂时不处理
            //使用SystemBarTint库使4.4版本状态栏变色，需要先将状态栏设置为透明
            //transparencyBar(activity);
            //SystemBarTintManager tintManager = new SystemBarTintManager(activity);
            //tintManager.setStatusBarTintEnabled(true);
            //tintManager.setStatusBarTintResource(colorId);
        }
    }



}