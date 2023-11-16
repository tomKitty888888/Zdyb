/*
 * Copyright (c) 18-8-13 上午9:48. create by User,email:godmarvin@163.com.
 */

package com.zdyb.module_diagnosis.bean;
import java.io.Serializable;


public class Transfer implements Serializable{

    private String name;
    private String foler;

    public String getFoler() {
        return foler;
    }

    public void setFoler(String foler) {
        this.foler = foler;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
