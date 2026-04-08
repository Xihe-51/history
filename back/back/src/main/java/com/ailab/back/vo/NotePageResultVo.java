package com.ailab.back.vo;

import com.ailab.back.Entity.Note;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

@Data
public class NotePageResultVo<T> {
    private List<T> records;
    private long total;
    private long size;
    private long current;
    private long pages;
}
