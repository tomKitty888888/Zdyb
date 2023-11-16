package com.zdyb.lib_common.http;

import android.text.TextUtils;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.HashMap;
import androidx.annotation.NonNull;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class PostFileBody {


    /**
     *
     * @param params 其他参数
     * @param file 需要上传的文件
     * @param fileKey 文件在包头的key名称
     * @return
     */
    public static MultipartBody create(@NonNull HashMap<String,Object> params, File file,String fileKey) {

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        if (params != null){
            for (String key :params.keySet()){
                builder.addFormDataPart(key,String.valueOf(params.get(key))).build();
            }
            builder.addFormDataPart(
                    fileKey,
                    file.getName(),
                    RequestBody.create(MediaType.parse(guessMimeType(file.getAbsolutePath())), file)
            );
        }

        return builder.build();
    }


    private static String guessMimeType(String absolutePath) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(absolutePath);
        if(TextUtils.isEmpty(contentTypeFor)){
            return "application/octet-stream";
        }
        return contentTypeFor;
    }

}