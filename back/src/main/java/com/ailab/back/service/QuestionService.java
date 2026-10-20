package com.ailab.back.service;

import com.ailab.back.Entity.Question;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface QuestionService extends IService<Question> {
    ResultVO<Page<Question>> getQuestionPage(Integer pageNum, Integer pageSize, String topicType, String difficulty, String keyword);

    ResultVO<List<Question>> getRandomQuestions(Integer count, String topicType, String difficulty);

    ResultVO<Question> getQuestionDetail(Long id);

    ResultVO<?> addQuestion(Question question);

    ResultVO<?> updateQuestion(Question question);

    ResultVO<?> deleteQuestion(Long id);

    ResultVO<?> deleteQuestionBatch(List<Long> ids);
}
