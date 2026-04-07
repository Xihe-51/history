package com.ailab.back.service.impl;

import com.ailab.back.config.SiliconFlowConfig;
import com.ailab.back.DTO.*;
import com.ailab.back.service.AiService;
import com.ailab.back.vo.ResultVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AiServiceImpl implements AiService {

    @Autowired
    private SiliconFlowConfig siliconFlowConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ResultVO<AiChatResponse> chat(AiChatRequest request) {
        System.out.println("========== 收到 AI 问答请求 ==========");
        System.out.println("问题: " + request.getQuestion());

        try {
            String question = request.getQuestion();
            if (question == null || question.trim().isEmpty()) {
                System.out.println("问题为空，返回错误");
                return ResultVO.error("问题不能为空");
            }

            System.out.println("开始调用硅基流动 API...");
            String answer = callSiliconFlow(question, request.getModel(),
                    request.getMaxTokens(), request.getTemperature());
            System.out.println("硅基流动 API 返回成功，回答长度: " + answer.length());

            AiChatResponse response = new AiChatResponse();
            response.setAnswer(answer);
            response.setModel(request.getModel() != null ? request.getModel() : siliconFlowConfig.getDefaultModel());

            System.out.println("返回成功结果");
            return ResultVO.success(response);

        } catch (Exception e) {
            System.err.println("========== AI 问答失败 ==========");
            e.printStackTrace();
            System.err.println("错误信息: " + e.getMessage());
            return ResultVO.error("AI 问答失败: " + e.getMessage());
        }
    }

    @Override
    public ResultVO<Object> generateExtension() {
        System.out.println("========== 生成延伸解读 ==========");
        try {
            String prompt = "请生成一段关于中国历史的延伸解读内容，要求有标题、内容和相关标签。";
            System.out.println("调用硅基流动 API 生成延伸解读...");
            String result = callSiliconFlow(prompt, null, 300, 0.8);
            System.out.println("延伸解读生成成功，长度: " + result.length());

            Map<String, Object> extension = new HashMap<>();
            extension.put("title", "历史延伸解读");
            extension.put("content", "<p>" + result.replace("\n", "</p><p>") + "</p>");
            extension.put("tags", new String[]{"历史", "文化"});

            return ResultVO.success(extension);
        } catch (Exception e) {
            System.err.println("生成延伸解读失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> defaultExtension = new HashMap<>();
            defaultExtension.put("title", "中国历史的文化传承");
            defaultExtension.put("content", "<p>中国历史源远流长，五千年的文明积淀形成了独特的文化传统。</p>");
            defaultExtension.put("tags", new String[]{"文化", "历史"});
            return ResultVO.success(defaultExtension);
        }
    }

    @Override
    public ResultVO<Object> generateFunFact() {
        System.out.println("========== 生成趣味冷知识 ==========");
        try {
            String prompt = "请生成一个关于中国历史人物的趣味冷知识，要求简短有趣。";
            System.out.println("调用硅基流动 API 生成冷知识...");
            String result = callSiliconFlow(prompt, null, 200, 0.9);
            System.out.println("冷知识生成成功，长度: " + result.length());

            Map<String, Object> funFact = new HashMap<>();
            funFact.put("text", result);
            funFact.put("title", "趣味冷知识");
            funFact.put("image", null);

            return ResultVO.success(funFact);
        } catch (Exception e) {
            System.err.println("生成冷知识失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> defaultFact = new HashMap<>();
            defaultFact.put("text", "秦始皇统一六国后，曾五次巡游天下，最后一次在途中病逝。");
            defaultFact.put("title", "秦始皇的趣味冷知识");
            defaultFact.put("image", null);
            return ResultVO.success(defaultFact);
        }
    }

    @Override
    public ResultVO<Object> getRecommendations() {
        System.out.println("========== 获取推荐内容 ==========");
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

        System.out.println("返回 " + recommendations.size() + " 条推荐");
        return ResultVO.success(recommendations);
    }

    @Override
    public ResultVO<Void> recordInterest(Long userId, String question) {
        System.out.println("========== 记录用户兴趣 ==========");
        System.out.println("用户ID: " + userId);
        System.out.println("问题: " + question);
        return ResultVO.success();
    }

    private String callSiliconFlow(String prompt, String model, Integer maxTokens, Double temperature) throws Exception {
        System.out.println("========== 调用硅基流动 API ==========");
        System.out.println("模型: " + (model != null ? model : siliconFlowConfig.getDefaultModel()));
        System.out.println("MaxTokens: " + (maxTokens != null ? maxTokens : siliconFlowConfig.getMaxTokens()));
        System.out.println("Temperature: " + (temperature != null ? temperature : siliconFlowConfig.getTemperature()));
        System.out.println("Prompt: " + (prompt.length() > 100 ? prompt.substring(0, 100) + "..." : prompt));

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
        System.out.println("请求体长度: " + requestBody.length());

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(siliconFlowConfig.getApiUrl());
            httpPost.setHeader("Authorization", "Bearer " + siliconFlowConfig.getApiKey());
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            System.out.println("发送请求到: " + siliconFlowConfig.getApiUrl());
            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                System.out.println("响应状态码: " + statusCode);
                System.out.println("响应体长度: " + responseBody.length());

                if (statusCode != 200) {
                    System.err.println("API返回错误状态码: " + statusCode);
                    System.err.println("错误响应: " + (responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody));
                    throw new RuntimeException("API返回错误: " + statusCode);
                }

                System.out.println("解析响应JSON...");
                SiliconFlowResponse siliconResponse = objectMapper.readValue(responseBody, SiliconFlowResponse.class);
                if (siliconResponse.getChoices() == null || siliconResponse.getChoices().isEmpty()) {
                    System.err.println("API返回内容为空");
                    throw new RuntimeException("API返回内容为空");
                }

                String content = siliconResponse.getChoices().get(0).getMessage().getContent();
                System.out.println("成功获取回答，长度: " + content.length());
                System.out.println("回答内容: " + (content.length() > 200 ? content.substring(0, 200) + "..." : content));
                return content;
            });
        }
    }
}