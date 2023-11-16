package com.zdyb.lib_common.utils.file;

import static com.zdyb.lib_common.utils.file.FileUtil.deleteFolderFile;

import android.os.Build;
import android.text.TextUtils;

import com.zdyb.lib_common.base.KLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileUtil {



    public static int copy(String fromFile, String toFile) {
        //要复制的文件目录
        File[] currentFiles;
        File root = new File(fromFile);
        //如同判断SD卡是否存在或者文件是否存在
        //如果不存在则 return出去
        if (!root.exists()) {
            return -1;
        }
        //如果存在则获取当前目录下的全部文件 填充数组
        currentFiles = root.listFiles();

        //目标目录
        File targetDir = new File(toFile);
        //创建目录
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        //删除目标目录下的文件
        for (File f : targetDir.listFiles()){
            if (!f.isDirectory()){
                f.delete();
            }
        }

        //遍历要复制该目录下的全部文件
        for (int i = 0; i < currentFiles.length; i++) {
            if (currentFiles[i].isDirectory()) {
                //如果当前项为子目录 进行递归
                copy(currentFiles[i].getPath() + "/", toFile + currentFiles[i].getName() + "/");

            } else {
                //如果当前项为文件则进行文件拷贝
                KLog.d("path:" + currentFiles[i].getPath());
                KLog.d("name:" + currentFiles[i].getName());
                if (currentFiles[i].getName().contains(".so")) {
                    int id = copySdcardFile(currentFiles[i].getPath(), toFile + File.separator + currentFiles[i].getName());
                    KLog.d( "id:" + id);
                    if (id == 0){
                        KLog.d( "拷贝成功："+currentFiles[i].getName());
                    }
                }
            }
        }
        return 0;
    }


    //文件拷贝
    //要复制的目录下的所有非子目录(文件夹)文件拷贝
    public static int copySdcardFile(String fromFile, String toFile) {

        try {
            FileInputStream fosfrom = new FileInputStream(fromFile);
            FileOutputStream fosto = new FileOutputStream(toFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = fosfrom.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            // 从内存到写入到具体文件
            fosto.write(baos.toByteArray());
            // 关闭文件流
            baos.close();
            fosto.close();
            fosfrom.close();
            return 0;

        } catch (Exception ex) {
            return -1;
        }
    }


    public static void deleteFolderFile(String filePath, boolean deleteThisPath){
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFolderFile(files[i].getAbsolutePath(), true);
                }
            }
            if (deleteThisPath) {
                if (!file.isDirectory()) {
                    file.delete();
                }else {
                    if (file.listFiles().length == 0) {
                        file.delete();
                    }
                }
            }
        }
    }




    public static void isARMv7Compatible() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                for (String abi:Build.SUPPORTED_32_BIT_ABIS){
                    KLog.d("cpu-1-支持的序列:"+abi);
                    if (abi.equals("armeabi-v7a")){
                        KLog.d("cpu-1-支持 armeabi-v7a");
                    }
                }

                for (String abi:Build.SUPPORTED_64_BIT_ABIS){
                    KLog.d("cpu-2-支持的序列:"+abi);
                }

                for (String abi:Build.SUPPORTED_ABIS){
                    KLog.d("cpu-3-支持的序列:"+abi);

                }
            }else {

                KLog.d("cpu-4-支持的序列:"+Build.CPU_ABI);
                KLog.d("cpu-5-支持的序列:"+Build.CPU_ABI2);
                if (Build.CPU_ABI.equals("armeabi-v7a") || Build.CPU_ABI.equals("areabi-v8a")){
                    KLog.d("cpu-6-支持 armeabi-v7a");
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }
}
