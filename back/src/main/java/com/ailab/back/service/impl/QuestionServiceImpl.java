package com.ailab.back.service.impl;

import com.ailab.back.Entity.Question;
import com.ailab.back.mapper.QuestionMapper;
import com.ailab.back.service.QuestionService;
import com.ailab.back.vo.ResultVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public ResultVO<Page<Question>> getQuestionPage(Integer pageNum, Integer pageSize, String topicType, String difficulty, String keyword) {
        Page<Question> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(topicType != null && !topicType.isEmpty(), Question::getTopicType, topicType);
        wrapper.eq(difficulty != null && !difficulty.isEmpty(), Question::getDifficulty, difficulty);
        wrapper.like(keyword != null && !keyword.isEmpty(), Question::getContent, keyword);
        wrapper.orderByDesc(Question::getId);
        Page<Question> result = questionMapper.selectPage(page, wrapper);
        return ResultVO.<Page<Question>>success("操作成功").setData(result);
    }

    @Override
    public ResultVO<List<Question>> getRandomQuestions(Integer count, String topicType, String difficulty) {
        int size = (count == null || count <= 0) ? 8 : Math.min(count, 50);
        List<Question> list = questionMapper.selectRandom(size, topicType, difficulty);
        return ResultVO.<List<Question>>success("操作成功").setData(list);
    }

    @Override
    public ResultVO<Question> getQuestionDetail(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            return ResultVO.<Question>error("该题目不存在");
        }
        return ResultVO.<Question>success("操作成功").setData(question);
    }

    @Override
    public ResultVO<?> addQuestion(Question question) {
        questionMapper.insert(question);
        return ResultVO.success("添加成功");
    }

    @Override
    public ResultVO<?> updateQuestion(Question question) {
        questionMapper.updateById(question);
        return ResultVO.success("更新成功");
    }

    @Override
    public ResultVO<?> deleteQuestion(Long id) {
        questionMapper.deleteById(id);
        return ResultVO.success("删除成功");
    }

    @Override
    public ResultVO<?> deleteQuestionBatch(List<Long> ids) {
        questionMapper.deleteBatchIds(ids);
        return ResultVO.success("批量删除成功");
    }
}
