package com.zdyb.module_diagnosis.bean;

import java.util.List;

public class MotorcycleTypeEntity {

    private String brand_name;
    private String en;
    private String brand_logo;
    private String brand_name_zh;
    private String logo_md5;
    private String patch_url;
    private String versions;


    private int progress; //下载 解压进度
    private int state; //状态： 1 正在下载 2下载完毕在解压 3已是最新版本
    private boolean isDownload = false;  //是否需要下载
    private boolean isSelect = false;  //是否选择
    private String imgUrl; //图片链接
    private String downloadUrl; //下载链接
    private String downloadSavePath; //下载后存放的路径
    private String basePath; //存放文件的父目录

    public String getBrand_name() {
        return brand_name;
    }

    public void setBrand_name(String brand_name) {
        this.brand_name = brand_name;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getBrand_logo() {
        return brand_logo;
    }

    public void setBrand_logo(String brand_logo) {
        this.brand_logo = brand_logo;
    }

    public String getBrand_name_zh() {
        return brand_name_zh;
    }

    public void setBrand_name_zh(String brand_name_zh) {
        this.brand_name_zh = brand_name_zh;
    }

    public String getLogo_md5() {
        return logo_md5;
    }

    public void setLogo_md5(String logo_md5) {
        this.logo_md5 = logo_md5;
    }

    public String getPatch_url() {
        return patch_url;
    }

    public void setPatch_url(String patch_url) {
        this.patch_url = patch_url;
    }

    public String getVersions() {
        return versions;
    }

    public void setVersions(String versions) {
        this.versions = versions;
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

    public boolean isDownload() {
        return isDownload;
    }

    public void setDownload(boolean download) {
        isDownload = download;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDownloadSavePath() {
        return downloadSavePath;
    }

    public void setDownloadSavePath(String downloadSavePath) {
        this.downloadSavePath = downloadSavePath;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
