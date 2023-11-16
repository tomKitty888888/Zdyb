package com.zdyb.lib_common.http;

import android.util.Log;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public abstract class MyObserver<T> implements Observer<T> {
    Disposable dd = null;
    @Override
    public void onSubscribe(@NonNull Disposable d) {
        dd = d;
    }

    @Override
    public void onNext(@NonNull T t) {
        onComplete();
    }

    @Override
    public void onError(@NonNull Throwable e) {

    }

    @Override
    public void onComplete() {
        if (dd != null){
            dd.dispose();
            dd = null;
            Log.i("","dispose");
        }
    }
}
