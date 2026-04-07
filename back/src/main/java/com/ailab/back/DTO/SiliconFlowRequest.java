package com.ailab.back.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SiliconFlowRequest {
    private String model;
    private List<Message> messages;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private Double temperature;
    private Boolean stream;

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}