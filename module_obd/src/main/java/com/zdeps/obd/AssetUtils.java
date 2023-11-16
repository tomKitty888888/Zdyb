package com.zdeps.obd;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AssetUtils {


    /**
     * 从assets目录中复制整个文件夹内容,考贝到 /data/data/包名/files/目录中
     *
     * @param filePath String  文件路径,如：/assets/aa
     */
    public static void copyAssetsDir2Phone(String filePath) {
        Context context = ContextUtils.getContext();
        try {
            String[] fileList = context.getAssets().list(filePath);
            if (fileList.length > 0) {//如果是目录
                File file = new File(context.getFilesDir().getAbsolutePath() + File.separator + filePath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileList) {
                    filePath = filePath + File.separator + fileName;

                    copyAssetsDir2Phone(filePath);

                    filePath = filePath.substring(0, filePath.lastIndexOf(File.separator));
                    Log.e("oldPath", filePath);
                }
            } else {//如果是文件
                InputStream inputStream = context.getAssets().open(filePath);
                File file = new File(context.getFilesDir().getAbsolutePath() + File.separator + filePath);
                Log.i("copyAssets2Phone", "file:" + file);
                if (!file.exists() || file.length() == 0) {
                    FileOutputStream fos = new FileOutputStream(file);
                    int len = -1;
                    byte[] buffer = new byte[1024];
                    while ((len = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();
                    inputStream.close();
                    fos.close();
                    // showToast(activity,"模型文件复制完毕");
                } else {
                    //showToast(activity,"模型文件已存在，无需复制");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将文件从assets目录，考贝到 /data/data/包名/files/ 目录中。assets 目录中的文件，会不经压缩打包至APK包中，使用时还应从apk包中导出来
     *
     * @param fileName 文件名,如aaa.txt
     */
    public static void copyAssetsFile2Phone(String fileName,String toPaht) {
        Context context = ContextUtils.getContext();
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            //getFilesDir() 获得当前APP的安装路径 /data/data/包名/files 目录

            String filePath = toPaht; //context.getFilesDir().getAbsolutePath() + File.separator + fileName;
            System.out.println("拷贝文件的路径="+filePath);
            File file = new File(filePath);

            file.mkdirs();

            if (file.exists()) {
                file.delete();
            }
            if (!file.exists() || file.length() == 0) {
                FileOutputStream fos = new FileOutputStream(file);//如果文件不存在，FileOutputStream会自动创建文件
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();//刷新缓存区
                inputStream.close();
                fos.close();
                Log.i(context.getPackageName(), "文件" + fileName + "复制完毕");
            } else {
                Log.i(context.getPackageName(), "文件" + fileName + "已存在");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyAssetsFile3Phone(String fileName) {
        Context context = ContextUtils.getContext();
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            //getFilesDir() 获得当前APP的安装路径 /data/data/包名/files 目录
            File file = new File(context.getFilesDir().getAbsolutePath() + File.separator + fileName + File.separator);
            if (file.exists()) {
                file.delete();
            }
            if (!file.exists() || file.length() == 0) {
                FileOutputStream fos = new FileOutputStream(file);//如果文件不存在，FileOutputStream会自动创建文件
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();//刷新缓存区
                inputStream.close();
                fos.close();
                Log.i(context.getPackageName(), "文件" + fileName + "复制完毕");
            } else {
                Log.i(context.getPackageName(), "文件" + fileName + "已存在");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
