package com.ailab.back.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class NoteSearchItemVO {
    private Long id;
    private String title;
    private String content;
    private String type;
    private LocalDateTime createTime;
    private Map<String, List<String>>highlight;


}
