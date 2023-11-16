// Transfer.aidl
package com.zdyb.app;

// Declare any non-default types here with import statements

interface Transfer {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
/*    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);*/

       int sendData(in byte[] dat,in int len);

       byte[] recvData(in int retlen);

       void purge();

       boolean setBaudRate(int baudRate);
       int IsWiredOrBluetooth();


       byte[] readFrame(int TimeOut);

}
