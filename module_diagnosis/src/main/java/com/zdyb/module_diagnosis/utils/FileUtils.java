package com.zdyb.module_diagnosis.utils;

import android.util.Log;

import com.zdyb.lib_common.utils.PathManager;
import com.zdyb.module_diagnosis.bean.DieseData;
import com.zdyb.module_diagnosis.bean.Transfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {


    public static List<DieseData> fileRead(String absolutePath) {

        int curline = 0;
        DieseData dieseData;
        List<DieseData> dieseDataList = new ArrayList<>();
        try {
            File file = new File(absolutePath);
            if (file.exists()) {

                FileInputStream inputStream = new FileInputStream(file);
                InputStreamReader read = new InputStreamReader(inputStream, "GBK");//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String line;
                while ((line = bufferedReader.readLine()) != null) {


                    if (!line.startsWith("//")) {
                        curline++;
                        int curGradle = 0;
                        char[] lines = line.toCharArray();
                        for (int i = 0; i < lines.length; i++) {
                            if ((int) lines[i] != (int) '\t') {
                                curGradle = i;
                                break;
                            }
                        }
                        dieseData = new DieseData();
                        dieseData.setCurGrade(curGradle);
                        dieseData.setId(curline);
                        int pos = line.lastIndexOf("ID");
                        int pathpos = line.lastIndexOf("PATH");

                        if (pos != -1) {
                            dieseData.setName(line.substring(0, pos).trim());
                            if (pathpos!=-1){
                                dieseData.setPath(line.substring(pathpos+5,line.length()));
                                dieseData.setCommend(line.substring(pos + 3, pathpos).trim());
                            }else {
                                dieseData.setCommend(line.substring(pos + 3, line.length()).trim());
                            }

                            Log.v("menutxt","fileRead Commend="+dieseData.getCommend()+" path="+dieseData.getPath());
                        } else {
                            dieseData.setName(line.trim());
                        }
                        dieseDataList.add(dieseData);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (DieseData d:dieseDataList){
            Log.v("FileUtils","fileRead "+d.getName());
        }
        return dieseDataList;
    }


    public static List<Transfer> readTranfer(String path, String folderName) {

        List<Transfer> transferList = new ArrayList<>();

        try {
            File file = new File(path);


            for (File fileItem : file.listFiles()) {

                if (fileItem.getName().endsWith(".txt")) {
                    Log.v("FileUtils","read text ="+fileItem.getName());
                    String line = "";
                    FileInputStream in = new FileInputStream(fileItem);
                    InputStreamReader reader = new InputStreamReader(in, PathManager.INSTANCE.FILE_ECODING);
                    BufferedReader bufferedReader = new BufferedReader(reader);


                    while ((line = bufferedReader.readLine()) != null) {

                        String[] names = line.split("=");
                        if (names.length == 2) {

                            Transfer transfer = new Transfer();
                            transfer.setFoler(names[1]);
                            transfer.setName(names[0]);
                            transferList.add(transfer);
                        }
                    }
                } else if (fileItem.getName().endsWith(".pdf")) {
                    Log.v("FileUtils","read pdf ="+fileItem.getAbsolutePath());
                    String[] names = fileItem.getName().toString().split("\\.");
                    if (names.length == 2) {

                        Transfer transfer = new Transfer();
                        transfer.setFoler(folderName);
                        transfer.setName(names[0]);
                        transferList.add(transfer);
                    }

                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return transferList;
    }



}
