package com.zdyb.lib_common.bus;


import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class RxSubscriptionsProvide {
    private static CompositeDisposable mSubscriptions = new CompositeDisposable ();

    public static boolean isDisposed() {
        return mSubscriptions.isDisposed();
    }

    public static void add(Disposable s) {
        if (s != null) {
            mSubscriptions.add(s);
        }
    }

    public static void remove(Disposable s) {
        if (s != null) {
            mSubscriptions.remove(s);
        }
    }

    public static void clear() {
        mSubscriptions.clear();
    }

    public static void dispose() {
        mSubscriptions.dispose();
    }
}
