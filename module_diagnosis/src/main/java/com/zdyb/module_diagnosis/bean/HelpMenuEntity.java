package com.zdyb.module_diagnosis.bean;

public class HelpMenuEntity {

    private String name;

    private int image;

    private int bgColor;

    public HelpMenuEntity(String name, int image, int bgColor) {
        this.name = name;
        this.image = image;
        this.bgColor = bgColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }
}
