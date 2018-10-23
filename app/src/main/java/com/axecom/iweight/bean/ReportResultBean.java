package com.axecom.iweight.bean;

import com.axecom.iweight.base.BaseEntity;

import java.util.List;

public class ReportResultBean<T> extends BaseEntity {
    public int total;
    public String total_amount;
    public String total_weight;
    public int all_number;
    public int total_num;
    public List<list> list;

    public class list {
        public String total_amount;
        public String total_weight;
        public int all_num;
        public int total_number;
        public String times;
    }
}
