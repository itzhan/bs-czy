package com.campus.zhihu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.zhihu.common.PageResult;
import com.campus.zhihu.entity.Notification;
import com.campus.zhihu.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class NotificationService {
    private final NotificationMapper notificationMapper;

    public PageResult<Notification> list(Long userId, int page, int size) {
        Page<Notification> p = notificationMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Notification>().eq(Notification::getUserId, userId).orderByDesc(Notification::getCreatedAt));
        return PageResult.of(p);
    }

    public void markAsRead(Long userId, Long id) {
        if (id != null) {
            notificationMapper.update(null, new LambdaUpdateWrapper<Notification>().eq(Notification::getId, id).eq(Notification::getUserId, userId).set(Notification::getIsRead, 1));
        } else {
            notificationMapper.update(null, new LambdaUpdateWrapper<Notification>().eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0).set(Notification::getIsRead, 1));
        }
    }

    public long getUnreadCount(Long userId) {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>().eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0));
    }

    public void send(Long userId, Long senderId, Integer type, String title, String content, Integer targetType, Long targetId) {
        Notification n = new Notification();
        n.setUserId(userId); n.setSenderId(senderId); n.setType(type);
        n.setTitle(title); n.setContent(content); n.setTargetType(targetType); n.setTargetId(targetId); n.setIsRead(0);
        notificationMapper.insert(n);
    }
}
