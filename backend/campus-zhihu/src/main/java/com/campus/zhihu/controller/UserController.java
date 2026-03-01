package com.campus.zhihu.controller;

import com.campus.zhihu.common.Result;
import com.campus.zhihu.dto.UserUpdateDTO;
import com.campus.zhihu.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/users") @RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public Result<?> getUser(@PathVariable Long id) { return Result.success(userService.getUserById(id)); }

    @PutMapping("/profile")
    public Result<?> updateProfile(Authentication auth, @RequestBody UserUpdateDTO dto) {
        return Result.success(userService.updateProfile((Long) auth.getPrincipal(), dto));
    }

    @PostMapping("/{id}/follow")
    public Result<?> follow(Authentication auth, @PathVariable Long id) {
        boolean followed = userService.toggleFollow((Long) auth.getPrincipal(), id);
        return Result.success(followed ? "关注成功" : "已取消关注");
    }

    @GetMapping("/{id}/followers")
    public Result<?> followers(@PathVariable Long id, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(userService.getFollowers(id, page, size));
    }

    @GetMapping("/{id}/following")
    public Result<?> following(@PathVariable Long id, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(userService.getFollowing(id, page, size));
    }

    @GetMapping("/{id}/questions")
    public Result<?> questions(@PathVariable Long id, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(userService.getUserQuestions(id, page, size));
    }

    @GetMapping("/{id}/answers")
    public Result<?> answers(@PathVariable Long id, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(userService.getUserAnswers(id, page, size));
    }

    @GetMapping("/{id}/favorites")
    public Result<?> favorites(@PathVariable Long id, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(userService.getUserFavorites(id, page, size));
    }

    @GetMapping("/{id}/is-following")
    public Result<?> isFollowing(Authentication auth, @PathVariable Long id) {
        return Result.success(userService.isFollowing((Long) auth.getPrincipal(), id));
    }
}
