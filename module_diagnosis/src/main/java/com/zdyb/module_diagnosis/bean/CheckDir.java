package com.zdyb.module_diagnosis.bean;

import java.io.Serializable;

public class CheckDir implements Serializable {

    private String name;
    private String path;
    private int verName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getVerName() {
        return verName;
    }

    public void setVerName(int verName) {
        this.verName = verName;
    }
}
