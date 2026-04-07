package com.ailab.back.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "siliconflow")
public class SiliconFlowConfig {
    private String apiKey;
    private String apiUrl = "https://api.siliconflow.cn/v1/chat/completions";
    private String defaultModel = "Qwen/Qwen2.5-7B-Instruct";
    private int maxTokens = 500;
    private double temperature = 0.7;
    private int timeout = 30;
}