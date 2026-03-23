package com.campus.zhihu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.zhihu.common.BusinessException;
import com.campus.zhihu.common.PageResult;
import com.campus.zhihu.dto.AnswerDTO;
import com.campus.zhihu.entity.*;
import com.campus.zhihu.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerMapper answerMapper;
    private final QuestionMapper questionMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Transactional
    public Answer create(Long userId, Long questionId, AnswerDTO dto) {
        Question q = questionMapper.selectById(questionId);
        if (q == null) throw new BusinessException("问题不存在");
        Answer a = new Answer();
        a.setContent(dto.getContent()); a.setQuestionId(questionId);
        a.setUserId(userId); a.setLikeCount(0); a.setCommentCount(0);
        a.setIsAccepted(0); a.setStatus(1);
        answerMapper.insert(a);
        questionMapper.update(null, new LambdaUpdateWrapper<Question>().eq(Question::getId, questionId).setSql("answer_count = answer_count + 1"));
        userMapper.update(null, new LambdaUpdateWrapper<User>().eq(User::getId, userId).setSql("answer_count = answer_count + 1"));
        return a;
    }

    public PageResult<Map<String, Object>> listByQuestion(Long questionId, int page, int size) {
        Page<Answer> p = answerMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Answer>().eq(Answer::getQuestionId, questionId).eq(Answer::getStatus, 1)
                        .orderByDesc(Answer::getIsAccepted).orderByDesc(Answer::getLikeCount));
        List<Map<String, Object>> records = p.getRecords().stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("answer", a);
            User author = userMapper.selectById(a.getUserId());
            if (author != null) author.setPassword(null);
            map.put("author", author);
            return map;
        }).toList();
        return PageResult.of(records, p.getTotal(), page, size);
    }

    public Answer update(Long userId, Long id, AnswerDTO dto) {
        Answer a = answerMapper.selectById(id);
        if (a == null) throw new BusinessException("回答不存在");
        if (!a.getUserId().equals(userId)) throw new BusinessException(403, "只能编辑自己的回答");
        a.setContent(dto.getContent());
        answerMapper.updateById(a);
        return a;
    }

    @Transactional
    public void delete(Long userId, Long id, boolean isAdmin) {
        Answer a = answerMapper.selectById(id);
        if (a == null) throw new BusinessException("回答不存在");
        if (!isAdmin && !a.getUserId().equals(userId)) throw new BusinessException(403, "只能删除自己的回答");
        answerMapper.deleteById(id);
        questionMapper.update(null, new LambdaUpdateWrapper<Question>().eq(Question::getId, a.getQuestionId()).setSql("answer_count = answer_count - 1"));
    }

    @Transactional
    public void acceptAnswer(Long userId, Long answerId) {
        Answer a = answerMapper.selectById(answerId);
        if (a == null) throw new BusinessException("回答不存在");
        Question q = questionMapper.selectById(a.getQuestionId());
        if (!q.getUserId().equals(userId)) throw new BusinessException(403, "只有提问者才能采纳回答");
        if (q.getAcceptedAnswerId() != null) {
            answerMapper.update(null, new LambdaUpdateWrapper<Answer>().eq(Answer::getId, q.getAcceptedAnswerId()).set(Answer::getIsAccepted, 0));
        }
        a.setIsAccepted(1); answerMapper.updateById(a);
        q.setAcceptedAnswerId(answerId); questionMapper.updateById(q);
        // 通知回答作者
        if (!a.getUserId().equals(userId)) {
            String preview = a.getContent().length() > 20 ? a.getContent().substring(0, 20) + "..." : a.getContent();
            notificationService.send(a.getUserId(), userId, 3, "回答被采纳",
                    "您在问题「" + q.getTitle() + "」下的回答「" + preview + "」已被提问者采纳！",
                    2, answerId);
        }
    }
}
