/*
 * Copyright (c) 18-8-13 上午9:48. create by User,email:godmarvin@163.com.
 */

package com.zdyb.module_diagnosis.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/1/4.
 */

public class DieseData implements Serializable {

    private String name;
    //private T Data;
    private int id;
    private String commend;
    private Integer curGrade;
    private String Path;

    public String getPath() {
        return Path;
    }

    public void setPath(String path) {
        Path = path;
    }

    private boolean bgFlag;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

//    public T getData() {
//        return Data;
//    }
//
//    public void setData(T data) {
//        Data = data;
//    }

    public String getCommend() {
        return commend;
    }

    public void setCommend(String commend) {
        this.commend = commend;
    }

    public Integer getCurGrade() {
        return curGrade;
    }

    public void setCurGrade(Integer curGrade) {
        this.curGrade = curGrade;
    }

    public boolean isBgFlag() {
        return bgFlag;
    }

    public void setBgFlag(boolean bgFlag) {
        this.bgFlag = bgFlag;
    }
}
