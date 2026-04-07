package com.ailab.back.DTO;

import lombok.Data;

@Data
public class AiChatResponse {
    private String answer;
    private String model;
    private Integer tokensUsed;
}