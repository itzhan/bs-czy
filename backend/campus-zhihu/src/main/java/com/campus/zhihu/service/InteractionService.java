package com.campus.zhihu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.zhihu.common.BusinessException;
import com.campus.zhihu.entity.*;
import com.campus.zhihu.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class InteractionService {
    private final UserLikeMapper userLikeMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    @Transactional
    public boolean toggleLike(Long userId, Integer targetType, Long targetId) {
        UserLike existing = userLikeMapper.selectOne(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, userId).eq(UserLike::getTargetType, targetType).eq(UserLike::getTargetId, targetId));
        String sql = existing != null ? "like_count = like_count - 1" : "like_count = like_count + 1";
        if (existing != null) { userLikeMapper.deleteById(existing.getId()); }
        else { UserLike like = new UserLike(); like.setUserId(userId); like.setTargetType(targetType); like.setTargetId(targetId); userLikeMapper.insert(like); }
        switch (targetType) {
            case 1 -> questionMapper.update(null, new LambdaUpdateWrapper<Question>().eq(Question::getId, targetId).setSql(sql));
            case 2 -> { answerMapper.update(null, new LambdaUpdateWrapper<Answer>().eq(Answer::getId, targetId).setSql(sql));
                Answer a = answerMapper.selectById(targetId); if (a != null) userMapper.update(null, new LambdaUpdateWrapper<User>().eq(User::getId, a.getUserId()).setSql(sql.replace("like_count", "like_count"))); }
            case 3 -> commentMapper.update(null, new LambdaUpdateWrapper<Comment>().eq(Comment::getId, targetId).setSql(sql));
        }
        return existing == null;
    }

    @Transactional
    public boolean toggleFavorite(Long userId, Long questionId) {
        UserFavorite existing = userFavoriteMapper.selectOne(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId).eq(UserFavorite::getQuestionId, questionId));
        if (existing != null) {
            userFavoriteMapper.deleteById(existing.getId());
            questionMapper.update(null, new LambdaUpdateWrapper<Question>().eq(Question::getId, questionId).setSql("favorite_count = favorite_count - 1"));
            return false;
        } else {
            UserFavorite fav = new UserFavorite(); fav.setUserId(userId); fav.setQuestionId(questionId);
            userFavoriteMapper.insert(fav);
            questionMapper.update(null, new LambdaUpdateWrapper<Question>().eq(Question::getId, questionId).setSql("favorite_count = favorite_count + 1"));
            return true;
        }
    }

    public boolean hasLiked(Long userId, Integer targetType, Long targetId) {
        return userLikeMapper.selectCount(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, userId).eq(UserLike::getTargetType, targetType).eq(UserLike::getTargetId, targetId)) > 0;
    }

    public boolean hasFavorited(Long userId, Long questionId) {
        return userFavoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId).eq(UserFavorite::getQuestionId, questionId)) > 0;
    }
}
