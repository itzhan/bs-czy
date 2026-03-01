package com.campus.zhihu.controller;

import com.campus.zhihu.common.Result;
import com.campus.zhihu.dto.AnswerDTO;
import com.campus.zhihu.service.AnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
public class AnswerController {
    private final AnswerService answerService;

    @PostMapping("/api/questions/{qid}/answers")
    public Result<?> create(Authentication auth, @PathVariable Long qid, @Valid @RequestBody AnswerDTO dto) {
        return Result.success(answerService.create((Long) auth.getPrincipal(), qid, dto));
    }

    @GetMapping("/api/questions/{qid}/answers")
    public Result<?> list(@PathVariable Long qid, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(answerService.listByQuestion(qid, page, size));
    }

    @PutMapping("/api/answers/{id}")
    public Result<?> update(Authentication auth, @PathVariable Long id, @Valid @RequestBody AnswerDTO dto) {
        return Result.success(answerService.update((Long) auth.getPrincipal(), id, dto));
    }

    @DeleteMapping("/api/answers/{id}")
    public Result<?> delete(Authentication auth, @PathVariable Long id) {
        answerService.delete((Long) auth.getPrincipal(), id, false); return Result.success();
    }

    @PutMapping("/api/answers/{id}/accept")
    public Result<?> accept(Authentication auth, @PathVariable Long id) {
        answerService.acceptAnswer((Long) auth.getPrincipal(), id); return Result.success();
    }
}
