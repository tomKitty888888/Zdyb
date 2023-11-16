package com.zdyb.module_diagnosis.bean;

import java.util.ArrayList;
import java.util.List;

public class CDSGroupingListEntity {

    public List<CDSGroupingEntity> list;

    public CDSGroupingListEntity() {
        list = new ArrayList<>();
    }
    public CDSGroupingListEntity(List<CDSGroupingEntity> list) {
        this.list = list;
    }


}
