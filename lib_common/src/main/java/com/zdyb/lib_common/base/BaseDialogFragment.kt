package com.zdyb.lib_common.base

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.zdyb.lib_common.bus.BusEvent
import com.zdyb.lib_common.bus.RxBus
import com.zdyb.lib_common.bus.RxSubscriptions
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

open class BaseDialogFragment : DialogFragment() {

    private var mSubscription: Disposable? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        rxBus()
    }

    private fun rxBus(){
        if (registerRxBus()) {
            mSubscription = RxBus.getDefault().toObservable(BusEvent::class.java)
                    .subscribe({ busEvent -> eventComing(busEvent) }, { err -> err.printStackTrace() })
            //.subscribe { busEvent -> eventComing(busEvent)}
            //将订阅者加入管理站
            RxSubscriptions.add(mSubscription)
        }
    }

    protected open fun registerRxBus(): Boolean {
        return false
    }

    protected open fun eventComing(t: BusEvent?) {

    }

    override fun dismiss() {

        super.dismiss()
        if (registerRxBus()) {
            RxSubscriptions.remove(mSubscription)
        }
    }


    var dismissConsumer : Consumer<Boolean>? = null
    protected open fun setDismissListener(consumer: Consumer<Boolean>){
        this.dismissConsumer = consumer;
    }

    override fun onDismiss(dialog: DialogInterface) {
        dismissConsumer?.apply { accept(true)  }
        super.onDismiss(dialog)
    }

    override fun show(manager: FragmentManager, tag: String?) {

        try {
            super.show(manager, tag)
        }catch (ignore: IllegalStateException) {
        }
    }





}