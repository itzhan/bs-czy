package com.campus.zhihu.controller;

import com.campus.zhihu.common.Result;
import com.campus.zhihu.dto.CommentDTO;
import com.campus.zhihu.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/comments") @RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public Result<?> create(Authentication auth, @Valid @RequestBody CommentDTO dto) {
        return Result.success(commentService.create((Long) auth.getPrincipal(), dto));
    }

    @GetMapping
    public Result<?> list(@RequestParam Integer targetType, @RequestParam Long targetId,
                          @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(commentService.list(targetType, targetId, page, size));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(Authentication auth, @PathVariable Long id) {
        commentService.delete((Long) auth.getPrincipal(), id, false); return Result.success();
    }
}
