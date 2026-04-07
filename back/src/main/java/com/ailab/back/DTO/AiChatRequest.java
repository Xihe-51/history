package com.ailab.back.DTO;

import lombok.Data;

@Data
public class AiChatRequest {
    private String question;
    private String model;
    private Integer maxTokens;
    private Double temperature;
}