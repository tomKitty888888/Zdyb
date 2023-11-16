package com.zdeps.bean;

import java.util.List;

public class OBDBean {

    public String cmd;
    public String code;
    public String msg;
    public List<ObdData> data;


    public static class ObdData{

        public ObdData(String key, Object value, String description, String unit) {
            this.key = key;
            this.value = value;
            this.description = description;
            this.unit = unit;
        }

        public ObdData(String key, Object value, String description, String unit, Boolean chevron) {
            this.key = key;
            this.value = value;
            this.description = description;
            this.unit = unit;
            this.chevron = chevron;
        }

        public ObdData(String key, Object value, String description, String unit, Boolean chevron, Boolean isError) {
            this.key = key;
            this.value = value;
            this.description = description;
            this.unit = unit;
            this.chevron = chevron;
            this.isError = isError;
        }

        public String key;
        public Object value;
        public String description; //描述
        public String unit; //单位

        public Boolean chevron; //是否有下一级

        public Boolean isError = false; //是否故障

        public int contextType; //内容类型 0文字 1图片


        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Boolean getChevron() {
            return chevron;
        }

        public void setChevron(Boolean chevron) {
            this.chevron = chevron;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public Boolean getError() {
            return isError;
        }

        public void setError(Boolean error) {
            isError = error;
        }

        public int getContextType() {
            return contextType;
        }

        public void setContextType(int contextType) {
            this.contextType = contextType;
        }
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<ObdData> getData() {
        return data;
    }

    public void setData(List<ObdData> data) {
        this.data = data;
    }
}
