package com.zdyb.lib_common.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;

public class PostFormBody {

    public static FormBody create(@NonNull HashMap<String, ?> params) {

        FormBody.Builder builder = new FormBody.Builder();
        if (params != null){
            for (String key :params.keySet()){
                builder.add(key,String.valueOf(params.get(key))).build();
            }
        }
        return builder.build();
    }
}