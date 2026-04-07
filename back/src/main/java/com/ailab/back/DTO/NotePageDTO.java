package com.ailab.back.DTO;

import lombok.Data;

@Data
public class NotePageDTO {
    private Integer pageNum=1;
    private Integer pageSize=10;
    private String type;
    private String keyword;
    private String tag;
    private String startTime;
    private String endTime;
}
