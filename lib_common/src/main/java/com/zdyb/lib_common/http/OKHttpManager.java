package com.zdyb.lib_common.http;

import android.text.TextUtils;

import com.blankj.utilcode.util.ConvertUtils;
import com.zdyb.lib_common.http.ssl.SSLSocketClient;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class OKHttpManager {

    //请求超时时间
    private static final int REQUEST_TIME = 10;
    //读取超时时间
    private static final int READ_TIME = 30;
    //写入超时时间
    private static final int WRITE_TIME = 30;

    private static OKHttpManager okHttpManager;
    private static OkHttpClient client;

    public static OKHttpManager getInstance(){

        if (okHttpManager == null){
            synchronized (OKHttpManager.class){
                if (okHttpManager == null){
                    okHttpManager = new OKHttpManager();
                }
            }
        }
        return okHttpManager;
    }

    public OkHttpClient getClient(){
        if (client == null){
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            client = new OkHttpClient.Builder()
                    .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())//配置
                    .hostnameVerifier(SSLSocketClient.getHostnameVerifier())//配置
            .connectTimeout(REQUEST_TIME, TimeUnit.SECONDS)
                    .readTimeout(READ_TIME, TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIME, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .build();
        }
        return client;
    }



    public Observable<HashMap<String, Object>> getNetData(String url,Map<String,String>params) {
        return Observable.create(new ObservableOnSubscribe<HashMap<String, Object>>(){

            @Override
            public void subscribe(@NonNull ObservableEmitter<HashMap<String, Object>> emitter) throws Exception {

                    HttpUrl.Builder httpBuilder = HttpUrl.parse(NetUrl.BASE_URL+url).newBuilder();
                    if (params != null) {
                        for(Map.Entry<String, String> param : params.entrySet()) {
                            httpBuilder.addQueryParameter(param.getKey(),param.getValue());
                        }
                    }
                    OkHttpClient client = getClient();
                    Request request = new Request.Builder()
                            .method("GET", null)
                            .header("Content-type", "application/json")
                            .url(httpBuilder.build()).build();
                    //同步
                    //Response response = client.newCall(request).execute();
//                    String context = response.body().string();
//                    HashMap<String, Object> mDatas = new Gson().fromJson(context,HashMap.class);
//                    emitter.onNext(mDatas);

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            emitter.onError(e);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String context = response.body().string();
                            HashMap<String, Object> mDatas = new Gson().fromJson(context,HashMap.class);
                            emitter.onNext(mDatas);
                        }
                    });

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    /**
     *  下载文件
     * @param url
     * @return
     */
    public Observable<String> downloadFile(String url, String filePath, Consumer<Integer> consumer) {
        return Observable.create(new ObservableOnSubscribe<String>(){

            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
                HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
                OkHttpClient client = getClient();
                Request request = new Request.Builder()
                        .get()
                        .url(httpBuilder.build()).build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        emitter.onError(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        InputStream inputStream = null;
                        FileOutputStream fops = null;
                        //文件处理完毕
                        try {
                            assert response.body() != null;
                            ResponseBody responseBody = response.body();
                            inputStream = responseBody.byteStream();
                            File file = new File(filePath);

                            if (!file.exists()){
                                file.getParentFile().mkdirs();
                                file.createNewFile();
                            }else {
                                file.delete();
                                file.createNewFile();
                            }

                            long sum = 0;
                            long total = responseBody.contentLength();
                            fops = new FileOutputStream(file);
                            byte[] buf = new byte[1024];
                            int read = inputStream.read(buf);
                            while (read>0){
                                fops.write(buf,0,read);
                                read= inputStream.read(buf);

                                sum += read;
                                int progress = (int) (sum * 1.0f / total * 100);
                                //下载进度
                                consumer.accept(progress);
                            }
                            fops.close();
                            inputStream.close();
                            responseBody.close();
                            emitter.onNext(filePath);
                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            if (inputStream != null){
                                inputStream.close();
                            }
                            if (fops != null){
                                fops.close();
                            }
                        }


                    }
                });

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     *  下载文件
     * @param url
     * @return
     */
    public Observable<byte[]> downloadFile(String url) {
        return Observable.create(new ObservableOnSubscribe<byte[]>(){
            @Override
            public void subscribe(@NonNull ObservableEmitter<byte[]> emitter) throws Exception {
                HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
                OkHttpClient client = getClient();
                Request request = new Request.Builder()
                        .get()
                        .url(httpBuilder.build()).build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        emitter.onError(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String value = response.body().string();
                        byte[] bytes  = ConvertUtils.hexString2Bytes(value);
                        emitter.onNext(bytes);
                    }
                });

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<String> uploadFileObservable(String url, File file,String mobile) {
        return Observable.create(new ObservableOnSubscribe<String>(){

            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {

                uploadFile(url, file,mobile, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        emitter.onError(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String context = response.body().string();
                            emitter.onNext(context);
                        }catch (Exception e){

                        }
                    }
                });

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 上传文件
     * @param url
     * @param file
     * @param callback
     */
    public void uploadFile(String url, File file,String mobile,Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("platform","android")
                .addFormDataPart("mobile",mobile)
                .addFormDataPart(
                        "iconImage",
                        file.getName(),
                        RequestBody.create(MediaType.parse(guessMimeType(file.getAbsolutePath())), file)
                );
        Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    private String guessMimeType(String absolutePath) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(absolutePath);
        if(TextUtils.isEmpty(contentTypeFor)){
            return "application/octet-stream";
        }
        return contentTypeFor;
    }
}
