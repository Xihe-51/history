package com.ailab.back.controller;

import com.ailab.back.DTO.AiChatRequest;
import com.ailab.back.DTO.AiChatResponse;
import com.ailab.back.service.AiService;
import com.ailab.back.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
// @CrossOrigin(origins = "*")  // 注释掉，让 CorsConfig 和 SecurityConfig 统一处理
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/chat")
    public ResultVO<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        return aiService.chat(request);
    }

    @GetMapping("/extension")
    public ResultVO<Object> generateExtension() {
        return aiService.generateExtension();
    }

    @GetMapping("/fun-fact")
    public ResultVO<Object> generateFunFact() {
        return aiService.generateFunFact();
    }

    @GetMapping("/recommendations")
    public ResultVO<Object> getRecommendations() {
        return aiService.getRecommendations();
    }

    @PostMapping("/record-interest")
    public ResultVO<Void> recordInterest(@RequestBody Map<String, String> request) {
        return aiService.recordInterest(1L, request.get("question"));
    }
}