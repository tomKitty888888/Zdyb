package com.zdyb.lib;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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

        List<String> a = new ArrayList<String>();
        a.add("1");
        a.add("2");
        a.add("3");
        a.add("4");


        List<String> b = new ArrayList<String>();
        b.add("2");
        b.add("3");

        a.removeAll(b);

        for (String s:a) {
            System.out.println(s);
        }

    }
}