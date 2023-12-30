package com.zdyb.module_diagnosis.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.zdyb.lib_common.base.BaseApplication;
import com.zdyb.module_diagnosis.bean.MotorcycleTypeEntity;

import java.util.ArrayList;
import java.util.List;


public final class FetchData {

    public static final String[] sampleUrls = new String[]{
            "http://speedtest.ftp.otenet.gr/files/test100Mb.db",
            "https://download.blender.org/peach/bigbuckbunny_movies/big_buck_bunny_720p_stereo.avi",
            "http://media.mongodb.org/zips.json",
            "http://www.exampletonyotest/some/unknown/123/Errorlink.txt",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/8/82/Android_logo_2019.svg/687px-Android_logo_2019.svg.png",
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"};

    private FetchData() {

    }

    @NonNull
    private static List<Request> getFetchRequests(Context context,List<MotorcycleTypeEntity> data) {
        final List<Request> requests = new ArrayList<>();
//        for (String sampleUrl : sampleUrls) {
//            final Request request = new Request(sampleUrl, getFilePath(sampleUrl, context));
//            requests.add(request);
//        }

        int i = 0;
        for (MotorcycleTypeEntity mt :data){
            // /storage/emulated/0/zdeps/Diagnosis/Electronic/75TPMS/V4.000.7z
            //参数 下载链接，本地存放路径
            if (mt.isDownload() && mt.isSelect()){  //
                final Request request = new Request(mt.getDownloadUrl(), mt.getDownloadSavePath());
                request.setGroupId(i); //以data的下标位置 为id 对应列表的位置
                requests.add(request);

            }
            i++;
        }
        return requests;
    }

    @NonNull
    public static List<Request> getFetchRequestWithGroupId( Context context,List<MotorcycleTypeEntity> data) {
        final List<Request> requests = getFetchRequests(context,data);


        return requests;
    }

    @NonNull
    private static String getFilePath(@NonNull final String url, Context context) {
        final Uri uri = Uri.parse(url);
        final String fileName = uri.getLastPathSegment();
        final String dir = getSaveDir(context);
        return (dir + "/DownloadList/" + fileName);
    }

    @NonNull
    static String getNameFromUrl(final String url) {
        return Uri.parse(url).getLastPathSegment();
    }

    @NonNull
    public static List<Request> getGameUpdates(Context context) {
        final List<Request> requests = new ArrayList<>();
        final String url = "http://speedtest.ftp.otenet.gr/files/test100k.db";
        for (int i = 0; i < 10; i++) {
            final String filePath = getSaveDir(context) + "/gameAssets/" + "asset_" + i + ".asset";
            final Request request = new Request(url, filePath);
            request.setPriority(Priority.HIGH);
            requests.add(request);
        }
        return requests;
    }

    @NonNull
    public static String getSaveDir(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/fetch";
    }

}
