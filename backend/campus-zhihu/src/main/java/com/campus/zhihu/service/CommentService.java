package com.campus.zhihu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.zhihu.common.BusinessException;
import com.campus.zhihu.common.PageResult;
import com.campus.zhihu.dto.CommentDTO;
import com.campus.zhihu.entity.*;
import com.campus.zhihu.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor
public class CommentService {
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;

    @Transactional
    public Comment create(Long userId, CommentDTO dto) {
        Comment c = new Comment();
        c.setContent(dto.getContent()); c.setUserId(userId);
        c.setTargetType(dto.getTargetType()); c.setTargetId(dto.getTargetId());
        c.setParentId(dto.getParentId()); c.setReplyUserId(dto.getReplyUserId());
        c.setLikeCount(0); c.setStatus(1);
        commentMapper.insert(c);
        if (dto.getTargetType() == 1) questionMapper.update(null, new LambdaUpdateWrapper<Question>().eq(Question::getId, dto.getTargetId()).setSql("comment_count = comment_count + 1"));
        else answerMapper.update(null, new LambdaUpdateWrapper<Answer>().eq(Answer::getId, dto.getTargetId()).setSql("comment_count = comment_count + 1"));
        return c;
    }

    public PageResult<Map<String, Object>> list(Integer targetType, Long targetId, int page, int size) {
        Page<Comment> p = commentMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Comment>().eq(Comment::getTargetType, targetType).eq(Comment::getTargetId, targetId)
                        .eq(Comment::getStatus, 1).isNull(Comment::getParentId).orderByAsc(Comment::getCreatedAt));
        List<Map<String, Object>> records = p.getRecords().stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("comment", c);
            User author = userMapper.selectById(c.getUserId());
            if (author != null) author.setPassword(null);
            map.put("author", author);
            List<Comment> replies = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getParentId, c.getId()).eq(Comment::getStatus, 1).orderByAsc(Comment::getCreatedAt));
            List<Map<String, Object>> replyList = replies.stream().map(r -> {
                Map<String, Object> rm = new HashMap<>(); rm.put("comment", r);
                User ra = userMapper.selectById(r.getUserId()); if (ra != null) ra.setPassword(null); rm.put("author", ra);
                if (r.getReplyUserId() != null) { User ru = userMapper.selectById(r.getReplyUserId()); if (ru != null) ru.setPassword(null); rm.put("replyUser", ru); }
                return rm;
            }).toList();
            map.put("replies", replyList);
            return map;
        }).toList();
        return PageResult.of(records, p.getTotal(), page, size);
    }

    public void delete(Long userId, Long id, boolean isAdmin) {
        Comment c = commentMapper.selectById(id);
        if (c == null) throw new BusinessException("评论不存在");
        if (!isAdmin && !c.getUserId().equals(userId)) throw new BusinessException(403, "只能删除自己的评论");
        commentMapper.deleteById(id);
    }
}
