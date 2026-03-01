package com.campus.zhihu.controller;

import com.campus.zhihu.common.Result;
import com.campus.zhihu.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/tags") @RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping
    public Result<?> list() { return Result.success(tagService.list()); }

    @GetMapping("/hot")
    public Result<?> hot(@RequestParam(defaultValue = "10") int limit) { return Result.success(tagService.hot(limit)); }

    @GetMapping("/{id}/questions")
    public Result<?> questions(@PathVariable Long id, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(tagService.getQuestionsByTag(id, page, size));
    }
}
