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

        String temp = "/storage/emulated/0/zdeps/Download/webViewDownload/P949V791_潍柴WP12.375E50_订货号DHP12Q1848_北奔_缸内制动_有力省油.bin";  //A5A500010055
        String a = temp.substring(temp.lastIndexOf("/")+1,temp.length());

        System.out.println(a);


        String temp2 = "潍柴WP12";  //A5A500010055
        System.out.println(temp2);
    }
}