package com.zdeps.obd;

public interface DiagInface {


    int sendData(byte[] dat);

    byte[] recvData(int retlen);

    int commInit();

//    void oBDDiagnosisInit();

    void purgeData();


}
