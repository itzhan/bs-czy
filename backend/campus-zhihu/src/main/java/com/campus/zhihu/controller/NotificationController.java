package com.campus.zhihu.controller;

import com.campus.zhihu.common.Result;
import com.campus.zhihu.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/notifications") @RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public Result<?> list(Authentication auth, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(notificationService.list((Long) auth.getPrincipal(), page, size));
    }

    @PutMapping("/read")
    public Result<?> markRead(Authentication auth, @RequestParam(required = false) Long id) {
        notificationService.markAsRead((Long) auth.getPrincipal(), id); return Result.success();
    }

    @GetMapping("/unread-count")
    public Result<?> unreadCount(Authentication auth) {
        return Result.success(notificationService.getUnreadCount((Long) auth.getPrincipal()));
    }
}
