package com.zdeps.bean;

public class AppUpdateBean {
    /**
     * vname : 2.9.3
     * versions : 293
     * notes : 优化特殊功能数据流滑动效果
     * is_upgrade : 2
     * md5_code : b017888e26a230faaf040f0817b756b7
     * apk_url : http://zdyban.zdeps.com/Diagnosis/APK/app-zdeps-v293.apk
     */

    private String vname;
    private String versions;
    private String notes;
    private String is_upgrade;
    private String md5_code;
    private String apk_url;

    public String getVname() {
        return vname;
    }

    public void setVname(String vname) {
        this.vname = vname;
    }

    public String getVersions() {
        return versions;
    }

    public void setVersions(String versions) {
        this.versions = versions;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getIs_upgrade() {
        return is_upgrade;
    }

    public void setIs_upgrade(String is_upgrade) {
        this.is_upgrade = is_upgrade;
    }

    public String getMd5_code() {
        return md5_code;
    }

    public void setMd5_code(String md5_code) {
        this.md5_code = md5_code;
    }

    public String getApk_url() {
        return apk_url;
    }

    public void setApk_url(String apk_url) {
        this.apk_url = apk_url;
    }
}
