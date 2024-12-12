package com.zdyb.module_diagnosis.bean;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class ItemVersionEntity implements Serializable {

    private String versions;
    private String patch_url;
    private String upnotes;

    private int progress; //进度
    private int state; //下载状态 1正在下载 2 下载完毕解压 3 下载失败

    private String savePath; //下载后存储的路径

    public ItemVersionEntity(String versions) {
        this.versions = versions;
    }

    public ItemVersionEntity(String versions, String patch_url, String upnotes) {
        this.versions = versions;
        this.patch_url = patch_url;
        this.upnotes = upnotes;
    }

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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        try {
            ItemVersionEntity se = (ItemVersionEntity) obj;
            if (null != se && se.versions.equals(versions)){
                return true;
            }
        }catch (Exception e){

        }
        return super.equals(obj);
    }
}
