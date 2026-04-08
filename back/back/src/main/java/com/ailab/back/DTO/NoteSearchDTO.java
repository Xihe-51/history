package com.ailab.back.DTO;

import lombok.Data;

@Data
public class NoteSearchDTO  {
    private String keyword;
    private Integer pageNum=1;
    private Integer pageSize=10;
}
