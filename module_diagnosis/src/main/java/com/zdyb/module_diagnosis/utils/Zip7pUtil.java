package com.zdyb.module_diagnosis.utils;


import android.os.Build;
import android.util.Log;


import com.blankj.utilcode.util.ConvertUtils;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import io.reactivex.functions.Consumer;

public class Zip7pUtil {

    /**
     * @param inCompressFile 需要解压的7zip文件
     * @param outputDir      解压后的文件的存储目录
     * @throws IOException
     */
    public static void unCompress(File inCompressFile, File outputDir, Consumer<Integer> consumer) throws Exception {
        if (inCompressFile == null || !inCompressFile.exists()) {
            throw new RuntimeException("Invalid outputDir:" + outputDir);
        }
        if (outputDir == null || !outputDir.exists() || !outputDir.isDirectory()) {
            outputDir.mkdirs();
        }

        SevenZFile sevenZFile = new SevenZFile(inCompressFile);
        Iterable<SevenZArchiveEntry> data = sevenZFile.getEntries();
        int sum = IterableUtils.size(data);
        int tempCurr = 0; //记录解压到的数量
        //System.out.println("总数量="+sum);
        SevenZArchiveEntry entry = null;
        while ((entry = sevenZFile.getNextEntry()) != null) {
            ++tempCurr;
            String entryName = entry.getName();
            if (entry.isDirectory()) {// //handle dir
                File file = new File(outputDir, entryName);
                file.mkdirs();

                int progress = (int) (tempCurr * 1.0f / sum * 100);
                //System.out.println("解压进度="+progress);
                consumer.accept(progress);
                continue;
            }
            int index = entryName.lastIndexOf(File.separator);
            String entryPath = index == -1 ? "" : entryName.substring(0, index);
            File file = new File(outputDir, entryPath);
            if (!file.exists() || !file.isDirectory()) file.mkdirs();
            File newFile = new File(outputDir, entryName);
            OutputStream outputStream = null;

            try {
                outputStream = new FileOutputStream(newFile);
                int len = -1;
                byte[] buffer = new byte[2048];
                while ((len = sevenZFile.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            }
            int progress = (int) (tempCurr * 1.0f / sum * 100);
            //System.out.println("解压进度="+progress);
            consumer.accept(progress);
        }
    }

    static String TAG = "Zip7putil";
    public static void unzipFile(String zipPtath, String outputDirectory, Consumer<Integer> consumer) throws Exception {
        /**
         * 解压assets的zip压缩文件到指定目录
         * @param context上下文对象
         * @param assetName压缩文件名
         * @param outputDirectory输出目录
         * @param isReWrite是否覆盖
         * @throws IOException
         */

        Log.i(TAG,"开始解压的文件： "  + zipPtath + "\n" + "解压的目标路径：" + outputDirectory );
        // 创建解压目标目录
        File file = new File(outputDirectory);
        // 如果目标目录不存在，则创建
        if (!file.exists()) {
            file.mkdirs();
        }
        //拿到压缩文件数量
        ZipFile zipFile = new ZipFile(zipPtath);
        int sum = zipFile.size();
        zipFile.close();
        int tempCurr = 0; //当前数量

        // 打开压缩文件
        InputStream inputStream = new FileInputStream(zipPtath); ;
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        // 读取一个进入点
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        // 使用1Mbuffer
        byte[] buffer = new byte[1024 * 1024];
        // 解压时字节计数
        int count = 0;
        // 如果进入点为空说明已经遍历完所有压缩包中文件和目录
        while (zipEntry != null) {
            ++tempCurr;
            //Log.i(TAG,"解压文件 入口 1： " +zipEntry );
            if (!zipEntry.isDirectory()) {  //如果是一个文件
                // 如果是文件
                String fileName = zipEntry.getName();
                Log.i(TAG,"解压文件 原来 文件的位置： " + fileName);
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);  //截取文件的名字 去掉原文件夹名字
                Log.i(TAG,"解压文件 的名字： " + fileName);
                file = new File(outputDirectory + File.separator + fileName);  //放到新的解压的文件路径

                file.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                while ((count = zipInputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, count);
                }
                fileOutputStream.close();

            }

            // 定位到下一个文件入口
            zipEntry = zipInputStream.getNextEntry();
            //Log.i(TAG,"解压文件 入口 2： " + zipEntry );

            int progress = (int) (tempCurr * 1.0f / sum * 100);
            System.out.println("解压进度="+progress);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                consumer.accept(progress);
            }
        }
        zipInputStream.close();
        Log.i(TAG,"解压完成");

    }


    /**
     * 检查文件真实类型
     *  返回值 1 7z文件类型，返回值2 zip文件类型
     */
    public static int fileType(File file) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            byte[] tag = new byte[6];
            inputStream.read(tag,0,6);
            String value = ConvertUtils.bytes2HexString(tag);
            if (value.equals("377ABCAF271C")){
                Log.v("UpdateModuleActivity","真实类型--7z文件 ");
                return 1;
            }else if (value.contains("504B0304")){
                Log.v("UpdateModuleActivity","真实类型--zip文件 ");
                return 2;
            }else {
                Log.e("Zip7pUtil","未知的压缩文件类型");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (inputStream != null){
                inputStream.close();
            }
        }
        return 0;
    }


}

