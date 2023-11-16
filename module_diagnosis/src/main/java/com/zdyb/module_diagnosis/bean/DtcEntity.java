package com.zdyb.module_diagnosis.bean;

public class DtcEntity {

    public String value1;
    public String value2;
    public String value3;


    public DtcEntity(String value1, String value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public DtcEntity(String value1, String value2, String value3) {
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
    }
}
