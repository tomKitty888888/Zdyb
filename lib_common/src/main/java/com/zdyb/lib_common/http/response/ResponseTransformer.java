package com.zdyb.lib_common.http.response;

import com.google.gson.reflect.TypeToken;
import com.zdyb.lib_common.base.AppManager;
import com.zdyb.lib_common.base.BaseApplication;
import com.zdyb.lib_common.bean.NoValueBean;
import com.zdyb.lib_common.http.exception.CustomException;
import com.zdyb.lib_common.utils.PreferencesUtils;
import com.zdyb.lib_common.utils.RouterUtil;
import com.zdyb.lib_common.utils.SharePreferencesConstant;
import com.zdyb.lib_common.utils.constant.RouteConstants;
import com.zdyb.lib_common.utils.constant.RouteParam;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ResponseTransformer {

    public static <T> ObservableTransformer<Response<T>, T> handleResult() {
        return upstream -> upstream
                .onErrorResumeNext(new ErrorResumeFunction<>())
                .flatMap(new ResponseFunction<>())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> ObservableTransformer<Response<T>, Response<T>> handleResult1() {
        return upstream -> upstream
                .onErrorResumeNext(new ErrorResumeFunction<>())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 非服务器产生的异常，比如本地无无网络请求，Json数据解析错误等等。
     *
     * @param <T>
     */
    private static class ErrorResumeFunction<T> implements Function<Throwable, ObservableSource<? extends Response<T>>> {

        @Override
        public ObservableSource<? extends Response<T>> apply(Throwable throwable) throws Exception {
            return Observable.error(CustomException.handleException(throwable));
        }
    }

    /**
     * 服务其返回的数据解析
     * 正常服务器返回数据和服务器可能返回的exception
     *
     * @param <T>
     */
    private static class ResponseFunction<T> implements Function<Response<T>, ObservableSource<T>> {


        @Override
        public ObservableSource<T> apply(Response<T> tResponse) throws Exception {

            String message = tResponse.getMessage();
            if (tResponse.getCode() ==  CustomException.SUCCESS) {
                if (tResponse.getData() == null){
                    //兼容后台，接口成功的情况下 data 还是给的null 这种情况统一使用NoValueBean对象接收
                    tResponse.setData((T) new NoValueBean());
                }
                return createData(tResponse.getData());
            }else if (tResponse.getCode() ==  CustomException.NO_LOGIN ||
                    tResponse.getCode() ==  CustomException.NO_Permissions ||
                    tResponse.getCode() ==  CustomException.NO_USER_INFO
            ){
                PreferencesUtils.putString(BaseApplication.getInstance(), SharePreferencesConstant.USER_TOKEN,"");
                PreferencesUtils.putString(BaseApplication.getInstance(), SharePreferencesConstant.VCI_CODE,"");
                RouterUtil.build(RouteConstants.Obd.OBD_ACTIVITY_LOGIN).launch();
                AppManager.getAppManager().finishAllOBDLoginActivity();
                return Observable.error(new Exception(message));
            }else {
                if (message == null) {
                    message = "msg=null";
                }
                return Observable.error(new Exception(message));
            }
        }

    }


    /**
     * 将数据存入subscriber
     *
     * @param data
     * @param <T>
     * @return
     */
    private static <T> Observable<T> createData(final T data) {
        return Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> emitter) throws Exception {
                try {
                    if (data == null){
                        emitter.onNext((T) new TypeToken<T>(){}.getType());
                    }else {
                        emitter.onNext(data);
                    }
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        });
    }



}
