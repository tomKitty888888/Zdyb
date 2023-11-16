package com.zdyb.lib_common.http;

import android.content.Context;
import android.text.TextUtils;

import com.blankj.utilcode.util.NetworkUtils;
import com.zdyb.lib_common.R;
import com.zdyb.lib_common.base.BaseApplication;
import com.zdyb.lib_common.http.gson.IntTypeAdapter;
import com.zdyb.lib_common.http.gson.LongTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetWorkManager {
    //请求超时时间
    private static final int REQUEST_TIME = 10;
    //读取超时时间
    private static final int READ_TIME = 30;
    //写入超时时间
    private static final int WRITE_TIME = 30;

    //红外读写入超时时间
    private static final int INFRARED_WRITE_READ_TIME = 180;
    //语音设置超时时间
    private static final int VOICE_WRITE_READ_TIME = 300;

    private static NetWorkManager mInstance;
    private static Retrofit retrofit;
    private static Gson gson;
    public final static String HEADER_KEY = "urlTag";


    //private static volatile Request request = null;

    private static List<Retrofit> mRetrofitList = new ArrayList<>();
    private static List<NetWorkManager> mApiRetrofitList = new ArrayList<>();

    private File file;
    private long cacheMaxSize = 10*1024*1024; //缓存最大值10MB


    public static NetWorkManager getInstance() {
        if (mInstance == null) {
            synchronized (NetWorkManager.class) {
                if (mInstance == null) {
                    mInstance = new NetWorkManager();
                }
            }
        }
        return mInstance;
    }


    /**
     * 初始化必要对象和参数
     */
    public void init(Context context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        /*loggingInterceptor.logger(new Logger() {
            @Override
            public void log(int level, @Nullable String tag, @Nullable String msg) {
                MLog.d(tag, msg);
            }
        });*/

        // 初始化okhttp
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(REQUEST_TIME, TimeUnit.SECONDS)
                .readTimeout(READ_TIME, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIME, TimeUnit.SECONDS)
                .addInterceptor(new AppenHeadParamsInterceptor())
                .addInterceptor(new notifUrlInterceptor())
                //.addInterceptor(new ChangeUrlIntercept())
                //.addInterceptor(new LogInterceptor())
                //.addInterceptor(new MyHttpLogInterceptor())
                .addInterceptor(logging)
                //.addInterceptor(new NoNetPostCacheInterceptor())
                //.addNetworkInterceptor(new HasNetPostCacheInterceptor())
                .cache(new Cache(getCacheFile(context), cacheMaxSize))
                .build();

        gson = new GsonBuilder()
                .registerTypeAdapter(Integer.class, new IntTypeAdapter())
                .registerTypeAdapter(int.class, new IntTypeAdapter())
                .registerTypeAdapter(Long.class, new LongTypeAdapter())
                .registerTypeAdapter(long.class, new LongTypeAdapter())
                .create();

        // 初始化Retrofit
        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(NetUrl.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public <T> T getService(Class<T> service) {
        return retrofit.create(service);
    }


    private class notifUrlInterceptor implements Interceptor{

        @Override
        public Response intercept(Chain chain) throws IOException {
            //获取request
            Request request = chain.request();
            //获取原baseUrl
            HttpUrl oldHttpUrl = request.url();
            Request.Builder builder = request.newBuilder().addHeader("Content-Type", "application/json");

            List<String> headerValues = chain.request().headers(HEADER_KEY);
            if (headerValues != null && headerValues.size() > 0){
                //如果存在这个header先删除，此header只作用于app与okhttp之间使用
                builder.removeHeader(HEADER_KEY);
                String handerValue = headerValues.get(0);
                HttpUrl newBaseUrl = null;

                if (isNewUrl(handerValue)){
                    newBaseUrl = HttpUrl.parse(newUrl(handerValue));
                }else {
                    newBaseUrl = oldHttpUrl;
                }

                //红外的接口读写超时时间延长至1分钟
//                if (handerValue.equals(HeadersValue.HEAD_INFRARED)){
//                    HttpUrl newFullUrl = oldHttpUrl
//                            .newBuilder()
//                            .scheme(newBaseUrl.scheme())
//                            .host(newBaseUrl.host())
//                            .port(newBaseUrl.port())
//                            .build();
//                    return chain.withConnectTimeout(INFRARED_WRITE_READ_TIME,TimeUnit.SECONDS)
//                            .withWriteTimeout(INFRARED_WRITE_READ_TIME,TimeUnit.SECONDS)
//                            .withReadTimeout(INFRARED_WRITE_READ_TIME,TimeUnit.SECONDS)
//                            .proceed(builder.url(newFullUrl).build());
//                }


                HttpUrl newFullUrl = oldHttpUrl
                        .newBuilder()
                        .scheme(newBaseUrl.scheme())
                        .host(newBaseUrl.host())
                        .port(newBaseUrl.port())
                        .build();
                return chain.proceed(builder.url(newFullUrl).build());
            }

            return chain.proceed(request);
        }
    }

    private String newUrl(String value){
        String url = NetUrl.BASE_URL;
        switch (value){
//            case HeadersValue.HEAD_HOPE:
//                return NetUrl.HOPE_URL;
        }
        return url;
    }

    private boolean isNewUrl(String value){
        boolean bo = false;
//        switch (value){
//            case HeadersValue.HEAD_HOPE:
//            return true;
//        }
        return bo;
    }

    /**
     * 请求头添加参数
     */
    private class AppenHeadParamsInterceptor implements Interceptor{

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request.Builder builder = chain.request().newBuilder();

            builder.method(chain.request().method(),chain.request().body());
            //String language = BaseApplication.getInstance().getString(R.string.language);
            builder.addHeader("language","chinese");
            if (!TextUtils.isEmpty(NetUrl.TOKEN)){
                builder.addHeader("token",NetUrl.TOKEN);
            }
//            if (!TextUtils.isEmpty(NetUrl.USERID)){
//                builder.addHeader("creator",NetUrl.USERID);
//                builder.addHeader("userId",NetUrl.USERID);
//            }
//
//            String headsString = builder.build().headers().toString();
//            builder.addHeader("Authorization",MD5Utils.parseStrToMd5L32(headsString));

            return chain.proceed(builder.build());
        }
    }




    private File getCacheFile(Context context) {
        file = new File(context.getCacheDir(), "okhttpCache");
        return file;
    }

    private void saveRequestBoby(Request.Builder builder, RequestBody body) {
        try {
            Field bodyField = builder.getClass().getDeclaredField("body");
            bodyField.setAccessible(true);
            bodyField.set(builder, body);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
