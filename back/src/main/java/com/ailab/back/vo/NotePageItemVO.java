package com.ailab.back.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NotePageItemVO {
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private List<String> tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;


}
