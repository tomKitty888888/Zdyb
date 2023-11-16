// ITaskCallback.aidl
package com.zdyb;

// Declare any non-default types here with import statements

interface ITaskCallback {

	boolean guiOpen();
	boolean guiClose();

    //菜单
    boolean dataInit(byte tag);
	boolean addItemOne(byte tag,String value);
	boolean addItemTwo(byte tag,String key,String value);
	boolean addItemThree(byte tag,String value1,String value2,String value3);
    boolean addItemChild(byte tag,String value);

    //实时显示数据流
    boolean addDataStream(byte tag,int index,String key,String value);

    //动作测试-添加按钮-添加提示
    boolean addButton(byte tag,String name);
    boolean addHint(byte tag,String hint);
	boolean dataShow(byte tag);

	//拿取数据 数据流查询时会获取需要查询的数据id,凡是以byte[]为返回的数据都可以通过此方法回调，每个功能拿取的数据都不一样
    byte[] getByteData(byte tag);

    //显示对话框
    long showDialog(byte tag,byte type,String title,String msg,String imgPath,long color);
    long destroyDialog();
    //关闭页面
    void viewFinish();
}
