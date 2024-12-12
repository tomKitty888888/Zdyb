package com.zdyb.module_diagnosis.bean;

public class VciUpdateInfoBean {

    private int code;
    private String message;
    private String version;
    private String lower_machine_url; //下位机升级文件

    private String bluetooth_url; //ble升级文件

    private String patch_url; //自动识别文件


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLower_machine_url() {
        return lower_machine_url;
    }

    public void setLower_machine_url(String lower_machine_url) {
        this.lower_machine_url = lower_machine_url;
    }

    public String getBluetooth_url() {
        return bluetooth_url;
    }

    public void setBluetooth_url(String bluetooth_url) {
        this.bluetooth_url = bluetooth_url;
    }

    public String getPatch_url() {
        return patch_url;
    }

    public void setPatch_url(String patch_url) {
        this.patch_url = patch_url;
    }
}
