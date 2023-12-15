package com.zdyb.module_diagnosis.bean;

/**
 * 传感器设备
 */
public class SensorBean {

    public String name;

    public SensorBean(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
