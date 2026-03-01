package com.campus.zhihu.controller;

import com.campus.zhihu.common.Result;
import com.campus.zhihu.dto.QuestionDTO;
import com.campus.zhihu.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/questions") @RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @PostMapping
    public Result<?> create(Authentication auth, @Valid @RequestBody QuestionDTO dto) {
        return Result.success(questionService.create((Long) auth.getPrincipal(), dto));
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
                          @RequestParam(required = false) String keyword, @RequestParam(required = false) Long tagId,
                          @RequestParam(defaultValue = "latest") String sort) {
        return Result.success(questionService.list(page, size, keyword, tagId, sort));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) { return Result.success(questionService.getById(id)); }

    @GetMapping("/recommend")
    public Result<?> recommend(Authentication auth, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        return Result.success(questionService.getRecommend(userId, page, size));
    }

    @GetMapping("/hot")
    public Result<?> hot(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(questionService.list(page, size, null, null, "hot"));
    }

    @GetMapping("/search")
    public Result<?> search(@RequestParam String keyword, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(questionService.list(page, size, keyword, null, "latest"));
    }

    @GetMapping("/following")
    public Result<?> following(Authentication auth, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(questionService.getFollowingQuestions((Long) auth.getPrincipal(), page, size));
    }

    @PutMapping("/{id}")
    public Result<?> update(Authentication auth, @PathVariable Long id, @Valid @RequestBody QuestionDTO dto) {
        return Result.success(questionService.update((Long) auth.getPrincipal(), id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(Authentication auth, @PathVariable Long id) {
        questionService.delete((Long) auth.getPrincipal(), id, false);
        return Result.success();
    }
}
