package com.zdeps.obd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DiagStatus {

    /**
     * 获取诊断库当前下位机版本号
     * 版本号从mcu.txt文件里面获取如（2.52）
     */
    public static float getMcuVersion() {

        float mcuVer = 0;
        try {
            mcuVer = 0f;
            String[] filesName = ContextUtils.getContext().getAssets().list("");
            for (String file : filesName) {
                if (file.equals("mcu.txt")) {
                    InputStream inputStream = ContextUtils.getContext().getAssets().open(file);
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String versionStr = null;

                    while ((versionStr = bufferedReader.readLine()) != null) {

                        mcuVer = Float.parseFloat(versionStr);
                    }
                    bufferedReader.close();
                    return mcuVer;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mcuVer;
    }
}
