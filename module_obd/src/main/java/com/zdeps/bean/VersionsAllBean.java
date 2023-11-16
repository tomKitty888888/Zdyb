package com.zdeps.bean;

public class VersionsAllBean {
    /**
     * versions : V4.010
     * patch_url : http://zdyban.zdeps.com/NewDiagnosis/Electronic/01Bosch/V4.010.7z
     * upnotes : 优化 自识别进度条显示
     */

    private String versions;
    private String patch_url;
    private String upnotes;

    public String getVersions() {
        return versions;
    }

    public void setVersions(String versions) {
        this.versions = versions;
    }

    public String getPatch_url() {
        return patch_url;
    }

    public void setPatch_url(String patch_url) {
        this.patch_url = patch_url;
    }

    public String getUpnotes() {
        return upnotes;
    }

    public void setUpnotes(String upnotes) {
        this.upnotes = upnotes;
    }
}
