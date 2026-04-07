package com.ailab.back.utils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtils {
    public static List<Map<Integer,String>>readExcel(Workbook workbook){
        //存一张表
        List<Map<Integer,String>>list=new ArrayList<>();
        //获得第一张工作表
        Sheet sheet=workbook.getSheetAt(0);
        //跳过表头,读取每一行
        for(int i=1;i<=sheet.getLastRowNum();i++){
            Row row=sheet.getRow(i);
            //判断空行
            if(row==null){
                continue;
            }
            //存当前行中的所有列
            Map<Integer,String> map=new HashMap<>();
            for(int j=0;j<row.getLastCellNum();j++){
                Cell cell=row.getCell(j);
                //转成字符，处理空情况
                map.put(j,cell==null?"":cell.toString().trim());
            }
            list.add(map);
        }
        return list;
    }
}
