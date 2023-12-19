// ITaskBinder.aidl
package com.zdyb;

import com.zdyb.app.Transfer;
import com.zdyb.ITaskCallback;

interface ITaskBinder {

//    boolean isTaskRunning();
//    void stopRunningTask();
    void registerCallback(ITaskCallback cb);

    void unregisterCallback(); //注销
    void run(String soLibsAbsolutePath,String versionPath);

    void registerTransferCallback(Transfer cb);
    void unregisterTransferCallback(Transfer cb);

    void setTaskID(long taskID);
    void setMenuValue(byte value);
    void setCommonValue(int offset,byte value);
    void setCommonValueToArray(int offset,String value); //string转byte[] inputDialog 输入使用到
//    void run(String path);
//    void shutDownThread(boolean flag);
//
//    void setMsgBox(byte button);
//    void setInputBox(byte button,String value);
//    void setFileBox(byte button,String path);
//void setFileHandleFinish(byte value);
//void setCreateFileByPath(byte value);
//  void setReflashDataDownload(String filename);
//
//    void setMenuValue(byte value);
//void setMenuExValue(byte value,in byte[] vecIndex);
//void setMenuCtrlValue(byte value,in String[] vecStrDefText);
//    void setDtcValue(byte value);
//void setMenuDtcMulitValue(byte value);
//    void setVerValue(byte value);
//    void setCdsSelectValue(byte value);//数据流，查看时要放的值。
//    void setCdsUpdate(byte value);
//    void setCdsValue(byte value);
//    void setActValue(byte value);
//void setMsgTitleText(String strMenuText);
//
//    void setCurrentPath(String name);//v2.000
//
    int getProcessPid(); //进程ID
//    String getSN();
//
//    String getVersions();
//
//    String getLogPath();
//
//    void connectDevices();
//
//         boolean setBaudRate(int baudRate);
//
//         byte[] read(int len);
//         int write(in byte[] data);
}
