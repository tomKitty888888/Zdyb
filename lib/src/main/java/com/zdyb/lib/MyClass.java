package com.zdyb.lib;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class MyClass {

    public static void main(String[] args) {

//        HashSet<ACTEntity> set = new LinkedHashSet<ACTEntity>();
//
//        ACTEntity a1 = new ACTEntity("abc","","");
//        ACTEntity a2 = new ACTEntity("abc1","","");
//        ACTEntity a3 = new ACTEntity("abc2","","");
//        ACTEntity a4 = new ACTEntity("abc","8","9");
//
//        set.add(a1);
//        set.add(a2);
//        set.add(a3);
//        set.add(a4);
//
//        for (ACTEntity a:set) {
//            System.out.print(a.value1); System.out.print(a.value2); System.out.println(a.value3);
//        }

        HashSet hashSet = new HashSet();
        Person p1 = new Person("Jack",25);
        Person p2 = new Person("Rose",23);
        Person p3 = new Person("Jack",27);
        hashSet.add(p1);
        hashSet.add(p2);
        hashSet.add(p3);
        for(Object obj:hashSet) {
            Person p = (Person) obj;
            System.out.println(p.name + ":" + p.age);
        }

    }
}