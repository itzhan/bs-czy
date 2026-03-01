package com.campus.zhihu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.zhihu.common.BusinessException;
import com.campus.zhihu.common.PageResult;
import com.campus.zhihu.dto.QuestionDTO;
import com.campus.zhihu.entity.*;
import com.campus.zhihu.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionMapper questionMapper;
    private final QuestionTagMapper questionTagMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;
    private final AnswerMapper answerMapper;
    private final UserFollowMapper userFollowMapper;

    @Transactional
    public Question create(Long userId, QuestionDTO dto) {
        Question q = new Question();
        q.setTitle(dto.getTitle());
        q.setContent(dto.getContent());
        q.setUserId(userId);
        q.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        q.setViewCount(0); q.setAnswerCount(0); q.setLikeCount(0);
        q.setFavoriteCount(0); q.setCommentCount(0); q.setIsTop(0);
        questionMapper.insert(q);
        if (dto.getTagIds() != null) {
            for (Long tagId : dto.getTagIds()) {
                QuestionTag qt = new QuestionTag();
                qt.setQuestionId(q.getId()); qt.setTagId(tagId);
                questionTagMapper.insert(qt);
                tagMapper.update(null, new LambdaUpdateWrapper<Tag>().eq(Tag::getId, tagId).setSql("question_count = question_count + 1"));
            }
        }
        userMapper.update(null, new LambdaUpdateWrapper<User>().eq(User::getId, userId).setSql("question_count = question_count + 1"));
        return q;
    }

    public Map<String, Object> getById(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null) throw new BusinessException("问题不存在");
        questionMapper.update(null, new LambdaUpdateWrapper<Question>().eq(Question::getId, id).setSql("view_count = view_count + 1"));
        q.setViewCount(q.getViewCount() + 1);
        Map<String, Object> result = new HashMap<>();
        result.put("question", q);
        User author = userMapper.selectById(q.getUserId());
        if (author != null) author.setPassword(null);
        result.put("author", author);
        List<QuestionTag> qts = questionTagMapper.selectList(new LambdaQueryWrapper<QuestionTag>().eq(QuestionTag::getQuestionId, id));
        List<Tag> tags = qts.stream().map(qt -> tagMapper.selectById(qt.getTagId())).filter(Objects::nonNull).toList();
        result.put("tags", tags);
        return result;
    }

    public PageResult<Map<String, Object>> list(int page, int size, String keyword, Long tagId, String sort) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>().eq(Question::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Question::getTitle, keyword).or().like(Question::getContent, keyword));
        }
        if (tagId != null) {
            List<QuestionTag> qts = questionTagMapper.selectList(new LambdaQueryWrapper<QuestionTag>().eq(QuestionTag::getTagId, tagId));
            List<Long> qids = qts.stream().map(QuestionTag::getQuestionId).toList();
            if (qids.isEmpty()) return PageResult.of(Collections.emptyList(), 0, page, size);
            wrapper.in(Question::getId, qids);
        }
        if ("hot".equals(sort)) wrapper.orderByDesc(Question::getLikeCount);
        else wrapper.orderByDesc(Question::getCreatedAt);
        Page<Question> p = questionMapper.selectPage(new Page<>(page, size), wrapper);
        List<Map<String, Object>> records = p.getRecords().stream().map(q -> {
            Map<String, Object> map = new HashMap<>();
            map.put("question", q);
            User author = userMapper.selectById(q.getUserId());
            if (author != null) author.setPassword(null);
            map.put("author", author);
            List<QuestionTag> qts = questionTagMapper.selectList(new LambdaQueryWrapper<QuestionTag>().eq(QuestionTag::getQuestionId, q.getId()));
            map.put("tags", qts.stream().map(qt -> tagMapper.selectById(qt.getTagId())).filter(Objects::nonNull).toList());
            return map;
        }).toList();
        return PageResult.of(records, p.getTotal(), page, size);
    }

    public PageResult<Map<String, Object>> getRecommend(Long userId, int page, int size) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                .eq(Question::getStatus, 1).orderByDesc(Question::getLikeCount).orderByDesc(Question::getCreatedAt);
        Page<Question> p = questionMapper.selectPage(new Page<>(page, size), wrapper);
        List<Map<String, Object>> records = p.getRecords().stream().map(q -> {
            Map<String, Object> map = new HashMap<>();
            map.put("question", q);
            User author = userMapper.selectById(q.getUserId());
            if (author != null) author.setPassword(null);
            map.put("author", author);
            return map;
        }).toList();
        return PageResult.of(records, p.getTotal(), page, size);
    }

    public PageResult<Map<String, Object>> getFollowingQuestions(Long userId, int page, int size) {
        List<UserFollow> follows = userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>().eq(UserFollow::getUserId, userId));
        List<Long> followIds = follows.stream().map(UserFollow::getFollowUserId).toList();
        if (followIds.isEmpty()) return PageResult.of(Collections.emptyList(), 0, page, size);
        Page<Question> p = questionMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Question>().eq(Question::getStatus, 1).in(Question::getUserId, followIds).orderByDesc(Question::getCreatedAt));
        List<Map<String, Object>> records = p.getRecords().stream().map(q -> {
            Map<String, Object> map = new HashMap<>();
            map.put("question", q);
            User author = userMapper.selectById(q.getUserId());
            if (author != null) author.setPassword(null);
            map.put("author", author);
            return map;
        }).toList();
        return PageResult.of(records, p.getTotal(), page, size);
    }

    @Transactional
    public Question update(Long userId, Long id, QuestionDTO dto) {
        Question q = questionMapper.selectById(id);
        if (q == null) throw new BusinessException("问题不存在");
        if (!q.getUserId().equals(userId)) throw new BusinessException(403, "只能编辑自己的问题");
        q.setTitle(dto.getTitle()); q.setContent(dto.getContent());
        if (dto.getStatus() != null) q.setStatus(dto.getStatus());
        questionMapper.updateById(q);
        if (dto.getTagIds() != null) {
            questionTagMapper.delete(new LambdaQueryWrapper<QuestionTag>().eq(QuestionTag::getQuestionId, id));
            for (Long tagId : dto.getTagIds()) {
                QuestionTag qt = new QuestionTag();
                qt.setQuestionId(id); qt.setTagId(tagId);
                questionTagMapper.insert(qt);
            }
        }
        return q;
    }

    @Transactional
    public void delete(Long userId, Long id, boolean isAdmin) {
        Question q = questionMapper.selectById(id);
        if (q == null) throw new BusinessException("问题不存在");
        if (!isAdmin && !q.getUserId().equals(userId)) throw new BusinessException(403, "只能删除自己的问题");
        questionMapper.deleteById(id);
    }
}
