package com.zdeps.bean;

import java.io.Serializable;


public class LoginBean implements Serializable {


    private String offdate;
    private String token;

    public String getOffdate() {
        return offdate;
    }

    public void setOffdate(String offdate) {
        this.offdate = offdate;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
