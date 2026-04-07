package com.ailab.back.vo;

import lombok.Data;

@Data
//统计信息中的类型统计
//typeStats
public class NoteTypeStatVO {

    private String type;
    private String typeName;
    private Long count;


}
