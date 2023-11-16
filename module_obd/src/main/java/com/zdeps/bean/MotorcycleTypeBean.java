package com.zdeps.bean;

public class MotorcycleTypeBean {


    /**
     * brand_name : 01Bosch
     * brand_name_zh : 博世
     * brand_logo : Electronic/01Bosch/logo_1018.png
     * brand_type : Electronic
     * versions : V4.022
     * patch_url : http://zdyban.zdeps.com/NewDiagnosis/Electronic/01Bosch/V4.003.7z
     */

    private String brand_name;
    private String brand_name_zh;
    private String brand_logo;
    private String brand_type;
    private String versions;
    private String patch_url;

    public String getBrand_name() {
        return brand_name;
    }

    public void setBrand_name(String brand_name) {
        this.brand_name = brand_name;
    }

    public String getBrand_name_zh() {
        return brand_name_zh;
    }

    public void setBrand_name_zh(String brand_name_zh) {
        this.brand_name_zh = brand_name_zh;
    }

    public String getBrand_logo() {
        return brand_logo;
    }

    public void setBrand_logo(String brand_logo) {
        this.brand_logo = brand_logo;
    }

    public String getBrand_type() {
        return brand_type;
    }

    public void setBrand_type(String brand_type) {
        this.brand_type = brand_type;
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
}
