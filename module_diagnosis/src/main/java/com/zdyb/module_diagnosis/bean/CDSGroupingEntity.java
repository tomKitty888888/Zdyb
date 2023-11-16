package com.zdyb.module_diagnosis.bean;

import java.util.List;

public class CDSGroupingEntity {

    public String title;
    public List<CDSSelectEntity> list;

    public CDSGroupingEntity(String title, List<CDSSelectEntity> list) {
        this.title = title;
        this.list = list;
    }
}
