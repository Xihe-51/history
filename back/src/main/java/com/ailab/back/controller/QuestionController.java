package com.ailab.back.controller;

import com.ailab.back.Entity.Question;
import com.ailab.back.service.QuestionService;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 趣味互动题库接口。
 * GET 为公开接口（登录/匿名均可答题），新增/修改/删除由 JwtAuthFilter 限制为管理员。
 */
@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/page")
    public ResultVO<Page<Question>> getQuestionPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String topicType,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword
    ) {
        return questionService.getQuestionPage(pageNum, pageSize, topicType, difficulty, keyword);
    }

    /**
     * 随机取题：答题界面使用，例如 /question/list?count=8&topicType=event
     */
    @GetMapping("/list")
    public ResultVO<List<Question>> getRandomQuestions(
            @RequestParam(defaultValue = "8") Integer count,
            @RequestParam(required = false) String topicType,
            @RequestParam(required = false) String difficulty
    ) {
        return questionService.getRandomQuestions(count, topicType, difficulty);
    }

    @GetMapping("/{id}")
    public ResultVO<Question> getQuestionDetail(@PathVariable Long id) {
        return questionService.getQuestionDetail(id);
    }

    @PostMapping("/add")
    public ResultVO<?> addQuestion(@RequestBody Question question) {
        return questionService.addQuestion(question);
    }

    @PutMapping("/update")
    public ResultVO<?> updateQuestion(@RequestBody Question question) {
        return questionService.updateQuestion(question);
    }

    @DeleteMapping("/delete/{id}")
    public ResultVO<?> deleteQuestion(@PathVariable Long id) {
        return questionService.deleteQuestion(id);
    }

    @DeleteMapping("/delete/batch")
    public ResultVO<?> deleteQuestionBatch(@RequestBody List<Long> ids) {
        return questionService.deleteQuestionBatch(ids);
    }
}
