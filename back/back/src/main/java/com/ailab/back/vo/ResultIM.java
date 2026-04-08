package com.ailab.back.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResultIM {

    private int successCount;
    private int failCount;
    private List<FailDetail> failDetailList=new ArrayList<>();
    @Data
    //汇总失败返回信息
    public static class FailDetail{
        private int row;
        private String reason;
    }
    //基本返回信息
    public String builMsg(){
        if(failCount==0){
            return "批量导入成功，共导入"+successCount+"条数据";
        }
        return "批量导入完成，成功"+successCount+"条，失败"+failCount+"条";
    }

}
