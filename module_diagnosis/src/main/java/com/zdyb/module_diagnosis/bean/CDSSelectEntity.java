package com.zdyb.module_diagnosis.bean;

import androidx.annotation.Nullable;

public class CDSSelectEntity {

    public String value1; //描述
    public String value2; //值
    public String value3; //单位

    public boolean isAdd;


    public boolean select; //选中

    public int index;

    public CDSSelectEntity(String value1, String value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public CDSSelectEntity(String value1, String value2, String value3) {
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        try {
            CDSSelectEntity se = (CDSSelectEntity) obj;
            if (null != se && se.value1.equals(value1)){
                return true;
            }
        }catch (Exception e){

        }
        return super.equals(obj);
    }
}
