package com.zdyb.module_diagnosis.bean;

import java.io.Serializable;
import java.util.List;

public class ProductsEntity implements Serializable {

    public final static String tag = "ProductsEntity";

    public String path; //产品绝对路径--版本文件夹上一层的文件 /storage/emulated/0/zdeps/Diagnosis/Reflash/01Bosch
    public List<String> versionList; //版本列表
    public String imagePath; //图片路径

    public String menuFilePath; //菜单文件路径

    public ProductsEntity() {
    }

    public ProductsEntity(String path, List<String> versionList, String imagePath) {
        this.path = path;
        this.versionList = versionList;
        this.imagePath = imagePath;
    }
}
