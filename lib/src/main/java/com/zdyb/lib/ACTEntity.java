package com.zdyb.lib;


public class ACTEntity {

    public String value1; //描述
    public String value2; //值
    public String value3; //单位


    public ACTEntity(String value1, String value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public ACTEntity(String value1, String value2, String value3) {
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
    }

    @Override
    public int hashCode() {
        return value1.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        try {
            //地址相同表示是同一个对象
            if (this == obj) {
                return true;
            }
            //传入的对象为空不是同一个对象
            if (obj == null) {
                return false;
            }
            //判断传入的对象和该类是不是同一个对象
            if(!(obj instanceof ACTEntity)){
                return false;
            }
            //比较两个对象的属性值是否相等
            ACTEntity se = (ACTEntity) obj;
            if (se.value1.equals(this.value1)){
                return true;
            }
        }catch (Exception e){

        }
        return false;
    }
}
