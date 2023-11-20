package com.zdyb.lib_common.base

import android.R
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.viewbinding.ViewBinding
import com.trello.rxlifecycle3.components.support.RxFragment
import com.zdyb.lib_common.bus.BusEvent
import com.zdyb.lib_common.bus.RxBus
import com.zdyb.lib_common.bus.RxSubscriptions
import com.zdyb.lib_common.utils.RxHelper
import io.reactivex.disposables.Disposable
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType


/**
 * Created by goldze on 2017/6/15.
 */
abstract class BaseNavFragment<V : ViewBinding, VM : BaseViewModel> : RxFragment(),
    IBaseActivity {
    //protected lateinit var binding: V
    private var _binding: V? = null
    protected val binding get() = _binding!!

    protected lateinit var viewModel: VM
    protected var viewModels: MutableList<BaseViewModel?>? = ArrayList()
    private var isPrepareView = false //用来确定View是否创建完成
    private var isInitData = false //数据是否加载完成
    private var isVisibleToUser = false //Fragment是否可见
    private var mSubscription: Disposable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //RouterUtil.inject(this)
        initParam()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        binding = DataBindingUtil.inflate(inflater, initContentView(inflater, container, savedInstanceState), container, false);
//        binding.setVariable(initVariableId(), viewModel = initViewModel());
        viewModel = initViewModel()
        try {

            val type = javaClass.genericSuperclass as ParameterizedType
            val cls = type.actualTypeArguments[0] as Class<*>
            val inflate = cls.getDeclaredMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.javaPrimitiveType
            )
            _binding = inflate.invoke(null, inflater, container, false) as V
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        viewModels = if (initViewModels() != null) {
            initViewModels()
        } else {
            ArrayList()
        }
        if (!viewModels!!.contains(viewModel)) {
            viewModels!!.add(viewModel)
        }
        for (viewModel in viewModels!!) {
            //viewModel!!.fragment = this
            viewModel!!.context = context
        }
        if (registerRxBus()) {
            mSubscription = RxBus.getDefault().toObservable(BusEvent::class.java)
                .compose(RxHelper.errorEmpty())
                .subscribe({ t -> eventComing(t) }
                ) { throwable -> throwable.printStackTrace() }
            //将订阅者加入管理站
            RxSubscriptions.add(mSubscription)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewObservable()
        initActionButton()
        for (viewModel in viewModels!!) {
            viewModel!!.onCreate()
            viewModel.registerRxBus()
        }
        isPrepareView = true
    }


    /**
     * 初始化viewModels
     *
     * @return
     */
    protected fun initViewModels(): MutableList<BaseViewModel?>? {
        return ArrayList()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lazyInitData()
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

    fun setViewMode():BaseViewModel{
        return BaseViewModel()
    }

    override fun onPause() {
        super.onPause()
        for (viewModel in viewModels!!) {
            viewModel!!.onPause()
        }
    }

    override fun onStop() {
        super.onStop()
        for (viewModel in viewModels!!) {
            viewModel!!.onStop()
        }
    }

    override fun onDestroyView() {
        isInitData = false
        isPrepareView = false

        for (d in disposables){
            DisposeUtil.close(d)
        }
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (registerRxBus()) {
            RxSubscriptions.remove(mSubscription)
        }
        //Messenger.getDefault().unregister(this.getContext());
        for (viewModel in viewModels!!) {
            if (viewModel != null) {
                KLog.e("测试名称=${viewModel.javaClass.simpleName}")
                if (viewModel.javaClass.simpleName != "LoadDiagnosisModel"){
                    viewModel.removeRxBus()
                    viewModel.onDestroy()
                }else{
                    if (BaseApplication.getInstance().outDiagnosisService){
                        viewModel.removeRxBus()
                        viewModel.onDestroy()
                        KLog.e("退出诊断服务=${viewModel.javaClass.simpleName}")
                    }
                }
//                if (viewModel.fragment != null) {
//                    println("fragment fragment fragment")
//                    val viewModelStore: ViewModelStore = viewModel.fragment.getViewModelStore()
//                    viewModelStore.clear()
//                }
            }
        }
        //viewModel = null
        isInitData = false
        isPrepareView = false
        //binding = null
    }

    override fun initParam() {}
    protected open fun initActionButton() {}

    /**
     * Fragment可见状态变化时该方法被调用
     *
     * @param isVisibleToUser
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        this.isVisibleToUser = isVisibleToUser
        lazyInitData()
    }

    protected open fun registerRxBus(): Boolean {
        return false
    }

    protected open fun eventComing(t: BusEvent?) {}

    /**
     * 刷新布局
     */
    fun refreshLayout() {
        if (viewModel != null) {
            //binding.setVariable(initVariableId(), viewModel);
        }
    }

    /**
     * 懒加载
     */
    private fun lazyInitData() {
        //全部符合条件才进行加载
        if (!isInitData && isPrepareView && isVisibleToUser) {
            isInitData = true
            initData()
        }
    }

    var disposables = ArrayList<Disposable>()
    fun addDisposable(disposable:Disposable){
        disposables.add(disposable)
    }


    /**
     * 初始化根布局
     *
     * @return 布局layout的id
     */
    //public abstract int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);
    /**
     * 初始化ViewModel的id
     *
     * @return BR的id
     */
    //public abstract int initVariableId();
    /**
     * 初始化ViewModel
     *
     * @return 继承BaseViewModel的ViewModel
     */
    abstract fun initViewModel(): VM

    /**
     * 懒加载数据
     */
    override fun initData() {}
    override fun initViewObservable() {}
    fun onBackPressed(): Boolean {
        return false
    }

    interface FragmentCallback {
        fun callback(`object`: Any?)
    }

    /**
     * 默认布局禁止被顶上去,默认不为全屏
     * @return
     */
    protected val isUseFullScreenMode: Boolean
        protected get() = false

    /**
     * 修改状态栏字体颜色后，布局会被顶上去
     * @param savedInstanceState
     */
    private fun process(savedInstanceState: Bundle) {
        // 华为,OPPO机型在StatusBarUtil.setLightStatusBar后布局被顶到状态栏上去了
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val content = (requireActivity().findViewById<View>(R.id.content) as ViewGroup).getChildAt(0)
            if (content != null && !isUseFullScreenMode) {
                content.fitsSystemWindows = true
            }
        }
    }

    companion object {
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
        fun setStatusBarColor(activity: Activity, colorId: Int) {
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
}