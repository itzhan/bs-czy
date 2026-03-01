package com.campus.zhihu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.zhihu.common.PageResult;
import com.campus.zhihu.entity.*;
import com.campus.zhihu.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @RequiredArgsConstructor
public class TagService {
    private final TagMapper tagMapper;
    private final QuestionTagMapper questionTagMapper;
    private final QuestionMapper questionMapper;

    public List<Tag> list() {
        return tagMapper.selectList(new LambdaQueryWrapper<Tag>().orderByDesc(Tag::getSortOrder));
    }

    public List<Tag> hot(int limit) {
        return tagMapper.selectList(new LambdaQueryWrapper<Tag>().orderByDesc(Tag::getQuestionCount).last("LIMIT " + limit));
    }

    public PageResult<Question> getQuestionsByTag(Long tagId, int page, int size) {
        List<QuestionTag> qts = questionTagMapper.selectList(new LambdaQueryWrapper<QuestionTag>().eq(QuestionTag::getTagId, tagId));
        List<Long> qids = qts.stream().map(QuestionTag::getQuestionId).toList();
        if (qids.isEmpty()) return PageResult.of(List.of(), 0, page, size);
        Page<Question> p = questionMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Question>().in(Question::getId, qids).eq(Question::getStatus, 1).orderByDesc(Question::getCreatedAt));
        return PageResult.of(p);
    }

    public Tag create(Tag tag) { tagMapper.insert(tag); return tag; }
    public Tag update(Tag tag) { tagMapper.updateById(tag); return tag; }
    public void delete(Long id) { tagMapper.deleteById(id); }
}
