package com.zdyb.module_diagnosis.bean;

public class LoginResultBean {


    /**
     * auth_code : 7fa86d37dac8455c8054ce3779840253
     * key : 090f7006e8f4ed4225979943d903d40f
     * time : 1861804800
     * type : 2
     * subscribe : 0
     * activate_date : 已激活。（激活日期：2023-08-28）
     */

    private String auth_code;
    private String key;
    private String time;
    private String type;
    private String subscribe;
    private String activate_date;

    public String getAuth_code() {
        return auth_code;
    }

    public void setAuth_code(String auth_code) {
        this.auth_code = auth_code;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(String subscribe) {
        this.subscribe = subscribe;
    }

    public String getActivate_date() {
        return activate_date;
    }

    public void setActivate_date(String activate_date) {
        this.activate_date = activate_date;
    }
}
