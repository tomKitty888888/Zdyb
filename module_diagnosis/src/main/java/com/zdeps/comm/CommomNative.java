package com.zdeps.comm;

/**
 *  下位机升级使用的，对应的so是libcomm.so
 */
public class CommomNative {


    static {
        System.loadLibrary("comm");
    }

    private static CallBack mCallBack;

    public static void registrationCallBack(CallBack cb){
        mCallBack = cb;
    }



    protected static boolean onSendData(byte[] data){
        if (mCallBack!=null)return mCallBack.onSendData(data);
        return false;
    }
    protected static void reconnect(){
        if (mCallBack!=null)mCallBack.reConnected();
    }
    //call from cpp
    protected static  byte[] onGetData(int len,int timeout){
        if (mCallBack!=null){
            byte[] bytes = mCallBack.onGetData(len, timeout);
            return   bytes;
        }
        return new byte[0];
    }
    //call from cpp
    protected static byte[] onReadOneFrame(int timeout){
        if (mCallBack!=null)return mCallBack.onReadOneFrame(timeout);
        return new byte[0];
    }
    //call from cpp
    public static  void onUpdateProgressing(String info){
        if (mCallBack!=null) mCallBack.onUpdateProgressing(info);
    }
    //call from cpp
    public static  void onUpdateFaild(String info){
        if (mCallBack!=null) mCallBack.onUpdateFaild(info);
    }

    public static  void onUpdateFaild(String title,String info){
        if (mCallBack!=null) mCallBack.onUpdateFaild(title,info);
    }

    //call from cpp
    public static  void onUpdateSuccess(){
        if (mCallBack!=null) mCallBack.onUpdateSuccess();
    }


    public interface CallBack{
        //发送数据给vci
        boolean onSendData(byte[] data);
        //从vci获取数据
        byte[] onGetData(int len,int timeout);

        byte[] onReadOneFrame(int timeout);

        //vci升级停止
        void onUpdateStop(String info);
        //vci升级失败
        void onUpdateFaild(String info);
        void onUpdateFaild(String title,String info);
        //升级中的过度信息
        void onUpdateProgressing(String info);
        //升级成功
        void onUpdateSuccess();

        void reConnected();
    }

    //开始升级下位机
    public static native boolean updateVci(String filePath,String vciVersion,int dwEachCount,boolean closedialog);

    //新升级下位机流程
    public static native boolean updateVciNew(String filePath,String vciVersion,byte brand,int dwEachCount,boolean closedialog);

    //1018下位机流程
    public static native boolean updateVci1018(String filePath,String vciVersion,byte brand,int dwEachCount,boolean closedialog);

    public static native boolean updateBluetooth(String filePath,int dwEachCount);

    public static native boolean updateVciIdentify(String filepath,int textPath);


    public static native String saveBlueTooth(String bl,String pt,String bt);//1018蓝牙救活


}
