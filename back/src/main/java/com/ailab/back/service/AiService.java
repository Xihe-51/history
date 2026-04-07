package com.ailab.back.service;

import com.ailab.back.DTO.AiChatRequest;
import com.ailab.back.DTO.AiChatResponse;
import com.ailab.back.vo.ResultVO;

public interface AiService {
    ResultVO<AiChatResponse> chat(AiChatRequest request);
    ResultVO<Object> generateExtension();
    ResultVO<Object> generateFunFact();
    ResultVO<Object> getRecommendations();
    ResultVO<Void> recordInterest(Long userId, String question);
}