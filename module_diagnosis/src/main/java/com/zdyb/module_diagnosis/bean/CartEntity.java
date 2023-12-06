package com.zdyb.module_diagnosis.bean;

import java.io.Serializable;
import java.util.List;

public class CartEntity implements Serializable {

    private String typeName; //类型名称
    private String typeImgPath; //类型图片

    private String instructionsPath; //维修说明书路径
    private List<ChildAction> childAction; //子类型功能

    public CartEntity() {
    }



    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeImgPath() {
        return typeImgPath;
    }

    public void setTypeImgPath(String typeImgPath) {
        this.typeImgPath = typeImgPath;
    }

    public List<ChildAction> getChildAction() {
        return childAction;
    }

    public void setChildAction(List<ChildAction> childAction) {
        this.childAction = childAction;
    }

    public String getInstructionsPath() {
        return instructionsPath;
    }

    public void setInstructionsPath(String instructionsPath) {
        this.instructionsPath = instructionsPath;
    }

    public static class ChildAction implements Serializable{

        private String name; //子类型名称
        private String menuPath; //路径
        private String versionName; //版本名称

        public ChildAction() {
        }

        public ChildAction(String name, String menuPath, String versionName) {
            this.name = name;
            this.menuPath = menuPath;
            this.versionName = versionName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMenuPath() {
            return menuPath;
        }

        public void setMenuPath(String menuPath) {
            this.menuPath = menuPath;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }
    }
}
