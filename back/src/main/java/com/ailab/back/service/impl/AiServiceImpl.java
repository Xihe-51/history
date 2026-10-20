package com.ailab.back.service.impl;

import com.ailab.back.config.SiliconFlowConfig;
import com.ailab.back.DTO.*;
import com.ailab.back.service.AiService;
import com.ailab.back.vo.ResultVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Autowired
    private SiliconFlowConfig siliconFlowConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ResultVO<AiChatResponse> chat(AiChatRequest request) {
        try {
            String question = request.getQuestion();
            if (question == null || question.trim().isEmpty()) {
                return ResultVO.error("问题不能为空");
            }

            String answer = callSiliconFlow(question.trim(), request.getModel(),
                    request.getMaxTokens(), request.getTemperature());

            AiChatResponse response = new AiChatResponse();
            response.setAnswer(answer);
            response.setModel(request.getModel() != null ? request.getModel() : siliconFlowConfig.getDefaultModel());

            return ResultVO.<AiChatResponse>success("操作成功").setData(response);

        } catch (Exception e) {
            log.warn("AI 问答失败: {}", e.getMessage());
            return ResultVO.error("AI 问答失败：" + e.getMessage());
        }
    }

    @Override
    public ResultVO<Object> generateExtension() {
        try {
            String prompt = "请生成一段关于中国历史的延伸解读内容，要求有标题、内容和相关标签。";
            String result = callSiliconFlow(prompt, null, 300, 0.8);

            Map<String, Object> extension = new HashMap<>();
            extension.put("title", "历史延伸解读");
            extension.put("content", "<p>" + result.replace("\n", "</p><p>") + "</p>");
            extension.put("tags", new String[]{"历史", "文化"});

            return ResultVO.success("操作成功").setData(extension);
        } catch (Exception e) {
            // AI 不可用时降级为内置科普内容，保证页面仍可展示
            log.warn("生成延伸解读失败，使用默认内容: {}", e.getMessage());
            Map<String, Object> defaultExtension = new HashMap<>();
            defaultExtension.put("title", "中国历史的文化传承");
            defaultExtension.put("content", "<p>中国历史源远流长，五千年的文明积淀形成了独特的文化传统。</p>");
            defaultExtension.put("tags", new String[]{"文化", "历史"});
            return ResultVO.success("操作成功").setData(defaultExtension);
        }
    }

    @Override
    public ResultVO<Object> generateFunFact() {
        try {
            String prompt = "请生成一个关于中国历史人物的趣味冷知识，要求简短有趣。";
            String result = callSiliconFlow(prompt, null, 200, 0.9);

            Map<String, Object> funFact = new HashMap<>();
            funFact.put("text", result);
            funFact.put("title", "趣味冷知识");
            funFact.put("image", null);

            return ResultVO.success("操作成功").setData(funFact);
        } catch (Exception e) {
            log.warn("生成冷知识失败，使用默认内容: {}", e.getMessage());
            Map<String, Object> defaultFact = new HashMap<>();
            defaultFact.put("text", "秦始皇统一六国后，曾五次巡游天下，最后一次在途中病逝。");
            defaultFact.put("title", "秦始皇的趣味冷知识");
            defaultFact.put("image", null);
            return ResultVO.success("操作成功").setData(defaultFact);
        }
    }

    @Override
    public ResultVO<Object> getRecommendations() {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        Map<String, Object> rec1 = new HashMap<>();
        rec1.put("id", 1);
        rec1.put("type", "event");
        rec1.put("title", "贞观之治");
        rec1.put("desc", "唐太宗开创的盛世");
        rec1.put("category", "政治");
        recommendations.add(rec1);

        Map<String, Object> rec2 = new HashMap<>();
        rec2.put("id", 2);
        rec2.put("type", "event");
        rec2.put("title", "赤壁之战");
        rec2.put("desc", "奠定三国鼎立格局");
        rec2.put("category", "军事");
        recommendations.add(rec2);

        Map<String, Object> rec3 = new HashMap<>();
        rec3.put("id", 3);
        rec3.put("type", "person");
        rec3.put("title", "苏轼");
        rec3.put("desc", "宋代文学巨匠");
        rec3.put("category", "文学");
        recommendations.add(rec3);

        return ResultVO.success("操作成功").setData(recommendations);
    }

    @Override
    public ResultVO<Void> recordInterest(Long userId, String question) {
        // 当前仅记录日志，后续可落库到 AI 互动记录表
        log.info("记录用户兴趣 userId={} question={}", userId, question);
        return ResultVO.<Void>success("操作成功");
    }

    private String callSiliconFlow(String prompt, String model, Integer maxTokens, Double temperature) throws Exception {
        SiliconFlowRequest request = SiliconFlowRequest.builder()
                .model(model != null ? model : siliconFlowConfig.getDefaultModel())
                .maxTokens(maxTokens != null ? maxTokens : siliconFlowConfig.getMaxTokens())
                .temperature(temperature != null ? temperature : siliconFlowConfig.getTemperature())
                .stream(false)
                .messages(Arrays.asList(
                        SiliconFlowRequest.Message.builder()
                                .role("user")
                                .content(prompt)
                                .build()
                ))
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        // 修复：配置项 siliconflow.timeout 之前未生效，第三方接口无响应时会长时间占住请求线程
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(Math.max(1, siliconFlowConfig.getTimeout())))
                .setResponseTimeout(Timeout.ofSeconds(Math.max(1, siliconFlowConfig.getTimeout())))
                .build();

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            HttpPost httpPost = new HttpPost(siliconFlowConfig.getApiUrl());
            httpPost.setHeader("Authorization", "Bearer " + siliconFlowConfig.getApiKey());
            httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
            httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode != 200) {
                    log.warn("硅基流动 API 返回 {}：{}", statusCode,
                            responseBody.length() > 300 ? responseBody.substring(0, 300) + "..." : responseBody);
                    throw new RuntimeException("AI 服务返回错误（HTTP " + statusCode + "），请稍后重试");
                }

                SiliconFlowResponse siliconResponse = objectMapper.readValue(responseBody, SiliconFlowResponse.class);
                if (siliconResponse.getChoices() == null || siliconResponse.getChoices().isEmpty()
                        || siliconResponse.getChoices().get(0).getMessage() == null) {
                    throw new RuntimeException("AI 返回内容为空");
                }

                String content = siliconResponse.getChoices().get(0).getMessage().getContent();
                if (content == null || content.isBlank()) {
                    throw new RuntimeException("AI 返回内容为空");
                }
                // 只清理控制字符，保留换行，便于前端按段落展示
                content = content.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "").trim();
                return content;
            });
        }
    }
}