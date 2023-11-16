package com.zdyb.lib_common.base;

public interface IBaseViewModel {
    /**
     * View的界面创建时回调
     */
    void onCreate();

    /**
     * view创建完后回调
     */
    void onStart();

    /**
     * view即将显示时回调
     */
    void onResume();


    void onPause();

    void onStop();

    /**
     * View的界面销毁时回调
     */
    void onDestroy();


    /**
     * 注册RxBus
     */
    void registerRxBus();

    /**
     * 移除RxBus
     */
    void removeRxBus();

    //void getModes();
}
