package com.zdyb.module_diagnosis.bean;

public class TestingBean {

    public String name;
    public int state; //0 空白-- 1等待检测 2检测中 3检测结束
    public boolean result;

    public String resultString;


    public TestingBean() {
    }


    public TestingBean(String name, int state, boolean result, String resultString) {
        this.name = name;
        this.state = state;
        this.result = result;
        this.resultString = resultString;
    }
}
