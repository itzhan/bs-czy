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
    private final NotificationService notificationService;

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
        // 点赞时通知内容作者（取消点赞不通知）
        if (existing == null) {
            Long authorId = null;
            String contentDesc = "";
            User liker = userMapper.selectById(userId);
            String likerName = liker != null && liker.getNickname() != null ? liker.getNickname() : "有人";
            switch (targetType) {
                case 1 -> { Question q = questionMapper.selectById(targetId); if (q != null) { authorId = q.getUserId(); contentDesc = "问题「" + q.getTitle() + "」"; } }
                case 2 -> { Answer a2 = answerMapper.selectById(targetId); if (a2 != null) { authorId = a2.getUserId(); String p = a2.getContent().length() > 20 ? a2.getContent().substring(0, 20) + "..." : a2.getContent(); contentDesc = "回答「" + p + "」"; } }
            }
            if (authorId != null && !authorId.equals(userId)) {
                notificationService.send(authorId, userId, 1, "收到点赞", likerName + " 赞了您的" + contentDesc, targetType, targetId);
            }
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
            // 收藏时通知问题作者
            Question q = questionMapper.selectById(questionId);
            if (q != null && !q.getUserId().equals(userId)) {
                User faver = userMapper.selectById(userId);
                String faverName = faver != null && faver.getNickname() != null ? faver.getNickname() : "有人";
                notificationService.send(q.getUserId(), userId, 1, "问题被收藏", faverName + " 收藏了您的问题「" + q.getTitle() + "」", 1, questionId);
            }
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
