package com.campus.zhihu.controller;

import com.campus.zhihu.common.Result;
import com.campus.zhihu.dto.LikeDTO;
import com.campus.zhihu.service.InteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class InteractionController {
    private final InteractionService interactionService;

    @PostMapping("/like")
    public Result<?> like(Authentication auth, @Valid @RequestBody LikeDTO dto) {
        boolean liked = interactionService.toggleLike((Long) auth.getPrincipal(), dto.getTargetType(), dto.getTargetId());
        return Result.success(liked ? "点赞成功" : "已取消点赞");
    }

    @PostMapping("/favorite")
    public Result<?> favorite(Authentication auth, @RequestParam Long questionId) {
        boolean faved = interactionService.toggleFavorite((Long) auth.getPrincipal(), questionId);
        return Result.success(faved ? "收藏成功" : "已取消收藏");
    }

    @GetMapping("/like/status")
    public Result<?> likeStatus(Authentication auth, @RequestParam Integer targetType, @RequestParam Long targetId) {
        return Result.success(interactionService.hasLiked((Long) auth.getPrincipal(), targetType, targetId));
    }

    @GetMapping("/favorite/status")
    public Result<?> favoriteStatus(Authentication auth, @RequestParam Long questionId) {
        return Result.success(interactionService.hasFavorited((Long) auth.getPrincipal(), questionId));
    }
}
