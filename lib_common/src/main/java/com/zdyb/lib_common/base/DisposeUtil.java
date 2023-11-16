package com.zdyb.lib_common.base;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class DisposeUtil {
    public static void close(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

}
