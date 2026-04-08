package com.ailab.back.DTO;

import lombok.Data;

import java.util.List;

@Data
public class NoteBatchDeleteDTO {
    private List<Long>ids;
}
