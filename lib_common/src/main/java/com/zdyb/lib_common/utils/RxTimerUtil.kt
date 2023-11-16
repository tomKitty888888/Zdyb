package com.zdyb.lib_common.utils

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import java.util.concurrent.TimeUnit

object RxTimerUtil {
    private var timing: Long = 0
    private var mDisposable: Disposable? = null;

    /**
     * timing执行次数一秒一次 手动关闭
     */
    fun timerTow(timing: Long, consumer: Consumer<RxTimerUtil>) {
        mDisposable =
                Observable.interval(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            if (it >= timing) cancel()
                            consumer.accept(this)
                                   },{it.printStackTrace()})
    }

    /**
     * timing执行次数一秒一次
     */
    fun timer(timing: Long, consumer: Consumer<Long>) {
        cancel()
        mDisposable =
                Observable.interval(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            consumer.accept(timing - it)
                            if (it >= timing) cancel()
                        },{it.printStackTrace()})
    }

    /**
     * timing执行次数20 毫秒一次
     */
    fun millisecondsTimer(timing: Long, consumer: Consumer<Long>) {
        cancel()
        mDisposable =
            Observable.interval(2, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    consumer.accept(it)
                    if (it >= timing) cancel()
                },{it.printStackTrace()})
    }

    /**
     * timing执行次数20 毫秒一次
     */
    fun millisecondsTimer2(timing: Long, consumer: Consumer<Long>) {
        cancel()
        mDisposable =
            Observable.interval(1, TimeUnit.MILLISECONDS).repeat(2)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    consumer.accept(it)
                    if (it >= timing) cancel()
                },{it.printStackTrace()})
    }

    /**
     * 定时多少秒后执行
     */
    fun timing(timing: Long, consumer: Consumer<Long>) {
        mDisposable =
                Observable.interval(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( {
                            if (it >= timing){
                                consumer.accept(timing - it)
                                cancel()
                            }
                        },{it.printStackTrace()})
    }

    /**
     * 定时多少秒后执行
     */
    fun timingBoolean(timing: Long, consumer: Consumer<Boolean>) {
        mDisposable =
            Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( {
                    if (it >= timing){
                        consumer.accept(true)
                        cancel()
                    }
                },{it.printStackTrace()})
    }


    fun justData(time: Long): Observable<Long> {
        return Observable.just(time)
    }

    public fun cancel() {
        mDisposable?.dispose()
    }
}