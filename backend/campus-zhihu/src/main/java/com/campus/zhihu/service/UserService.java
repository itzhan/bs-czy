package com.campus.zhihu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.zhihu.common.BusinessException;
import com.campus.zhihu.common.PageResult;
import com.campus.zhihu.dto.UserUpdateDTO;
import com.campus.zhihu.entity.*;
import com.campus.zhihu.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserFollowMapper userFollowMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final NotificationService notificationService;

    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(null);
        return user;
    }

    public User updateProfile(Long userId, UserUpdateDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getBio() != null) user.setBio(dto.getBio());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getDepartment() != null) user.setDepartment(dto.getDepartment());
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }

    public boolean toggleFollow(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) throw new BusinessException("不能关注自己");
        UserFollow existing = userFollowMapper.selectOne(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getUserId, userId).eq(UserFollow::getFollowUserId, targetUserId));
        if (existing != null) {
            userFollowMapper.deleteById(existing.getId());
            userMapper.update(null, new LambdaUpdateWrapper<User>().eq(User::getId, userId).setSql("following_count = following_count - 1"));
            userMapper.update(null, new LambdaUpdateWrapper<User>().eq(User::getId, targetUserId).setSql("follower_count = follower_count - 1"));
            return false;
        } else {
            UserFollow follow = new UserFollow();
            follow.setUserId(userId);
            follow.setFollowUserId(targetUserId);
            userFollowMapper.insert(follow);
            userMapper.update(null, new LambdaUpdateWrapper<User>().eq(User::getId, userId).setSql("following_count = following_count + 1"));
            userMapper.update(null, new LambdaUpdateWrapper<User>().eq(User::getId, targetUserId).setSql("follower_count = follower_count + 1"));
            // 通知被关注者
            User follower = userMapper.selectById(userId);
            String followerName = follower != null && follower.getNickname() != null ? follower.getNickname() : "有人";
            notificationService.send(targetUserId, userId, 2, "新粉丝", followerName + " 关注了您", null, null);
            return true;
        }
    }

    public PageResult<User> getFollowers(Long userId, int page, int size) {
        Page<UserFollow> p = userFollowMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<UserFollow>().eq(UserFollow::getFollowUserId, userId).orderByDesc(UserFollow::getCreatedAt));
        List<User> users = p.getRecords().stream().map(f -> {
            User u = userMapper.selectById(f.getUserId());
            if (u != null) u.setPassword(null);
            return u;
        }).filter(u -> u != null).toList();
        return PageResult.of(users, p.getTotal(), page, size);
    }

    public PageResult<User> getFollowing(Long userId, int page, int size) {
        Page<UserFollow> p = userFollowMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<UserFollow>().eq(UserFollow::getUserId, userId).orderByDesc(UserFollow::getCreatedAt));
        List<User> users = p.getRecords().stream().map(f -> {
            User u = userMapper.selectById(f.getFollowUserId());
            if (u != null) u.setPassword(null);
            return u;
        }).filter(u -> u != null).toList();
        return PageResult.of(users, p.getTotal(), page, size);
    }

    public PageResult<Question> getUserQuestions(Long userId, int page, int size) {
        Page<Question> p = questionMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Question>().eq(Question::getUserId, userId).eq(Question::getStatus, 1).orderByDesc(Question::getCreatedAt));
        return PageResult.of(p);
    }

    public PageResult<Answer> getUserAnswers(Long userId, int page, int size) {
        Page<Answer> p = answerMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Answer>().eq(Answer::getUserId, userId).eq(Answer::getStatus, 1).orderByDesc(Answer::getCreatedAt));
        return PageResult.of(p);
    }

    public PageResult<Question> getUserFavorites(Long userId, int page, int size) {
        Page<UserFavorite> fp = userFavoriteMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<UserFavorite>().eq(UserFavorite::getUserId, userId).orderByDesc(UserFavorite::getCreatedAt));
        List<Question> questions = fp.getRecords().stream().map(f -> questionMapper.selectById(f.getQuestionId())).filter(q -> q != null).toList();
        return PageResult.of(questions, fp.getTotal(), page, size);
    }

    public boolean isFollowing(Long userId, Long targetUserId) {
        return userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getUserId, userId).eq(UserFollow::getFollowUserId, targetUserId)) > 0;
    }
}
