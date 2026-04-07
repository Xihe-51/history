package com.ailab.back.DTO;

import lombok.Data;
import java.util.List;

@Data
public class NoteAddDTO {
    private Long userId;
    private String type;
    private String title;
    private String content;
    private List<String> tags;
}