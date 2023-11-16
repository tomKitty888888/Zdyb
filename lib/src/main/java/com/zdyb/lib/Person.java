package com.zdyb.lib;

public class Person {


    String name;
    int age;
    public Person(String name, int age) {
        super();
        this.name = name;
        this.age = age;
    }
    public int hashCode() {
        return name.hashCode();
    }
    //重写equals方法中传入对象的重载方法
    public boolean equals(Object obj) {
        //地址相同表示是同一个对象
        if (this == obj) {
            return true;
        }
        //传入的对象为空不是同一个对象
        if (obj == null) {
            return false;
        }
        //判断传入的对象和该类是不是同一个对象
        if(!(obj instanceof Person)){
            return false;
        }
        //把object类型转换为person类型
        Person p = (Person) obj;
        //比较两个对象的属性值是否相等
        if(p.name.equals(this.name)){
            //如果要比较多个属性值可以使用if嵌套if
            return true;
        }
        //默认返回false
        return false;
    }
}
