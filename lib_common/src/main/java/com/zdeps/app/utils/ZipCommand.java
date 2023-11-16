/*
 * Copyright (c) 18-8-13 上午9:48. create by User,email:godmarvin@163.com.
 */

package com.zdeps.app.utils;

/**
 * Created by huzongyao on 8/1/17.
 */

public class ZipCommand {

    public static String getExtractCmd(String archivePath, String outPath) {
        return String.format("7z -y x '%s' '-o%s' -aoa", archivePath, outPath);
    }

    public static String getCompressCmd(String filePath, String outPath, String type) {
        return String.format("7z a -t%s '%s' '%s'", type, outPath, filePath);
    }
    public static String getCompressPwdCmd(String filePath, String outPath, String type,String pwd) {
        return String.format("7z a -t%s -p%s '%s' '%s'", type,pwd, outPath, filePath);
    }
}
