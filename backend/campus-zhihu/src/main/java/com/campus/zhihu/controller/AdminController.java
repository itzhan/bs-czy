package com.campus.zhihu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.zhihu.common.PageResult;
import com.campus.zhihu.common.Result;
import com.campus.zhihu.entity.*;
import com.campus.zhihu.mapper.*;
import com.campus.zhihu.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor
public class AdminController {
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final CommentMapper commentMapper;
    private final ReportMapper reportMapper;
    private final TagService tagService;
    private final QuestionService questionService;
    private final AnswerService answerService;

    // --- 数据统计 ---
    @GetMapping("/stats")
    public Result<?> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userMapper.selectCount(null));
        stats.put("questionCount", questionMapper.selectCount(null));
        stats.put("answerCount", answerMapper.selectCount(null));
        stats.put("commentCount", commentMapper.selectCount(null));
        stats.put("pendingReports", reportMapper.selectCount(new LambdaQueryWrapper<Report>().eq(Report::getStatus, 0)));
        return Result.success(stats);
    }

    // --- 用户管理 ---
    @GetMapping("/users")
    public Result<?> listUsers(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
                               @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer role) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt);
        if (keyword != null) w.and(q -> q.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
        if (role != null) w.eq(User::getRole, role);
        Page<User> p = userMapper.selectPage(new Page<>(page, size), w);
        p.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(PageResult.of(p));
    }

    @PutMapping("/users/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userMapper.update(null, new LambdaUpdateWrapper<User>().eq(User::getId, id).set(User::getStatus, status));
        return Result.success();
    }

    @PutMapping("/users/{id}/role")
    public Result<?> updateUserRole(@PathVariable Long id, @RequestParam Integer role) {
        userMapper.update(null, new LambdaUpdateWrapper<User>().eq(User::getId, id).set(User::getRole, role));
        return Result.success();
    }

    // --- 问题管理 ---
    @GetMapping("/questions")
    public Result<?> listQuestions(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
                                   @RequestParam(required = false) Integer status, @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Question> w = new LambdaQueryWrapper<Question>().orderByDesc(Question::getCreatedAt);
        if (status != null) w.eq(Question::getStatus, status);
        if (keyword != null) w.like(Question::getTitle, keyword);
        return Result.success(PageResult.of(questionMapper.selectPage(new Page<>(page, size), w)));
    }

    @PutMapping("/questions/{id}/status")
    public Result<?> updateQuestionStatus(@PathVariable Long id, @RequestParam Integer status) {
        questionMapper.update(null, new LambdaUpdateWrapper<Question>().eq(Question::getId, id).set(Question::getStatus, status));
        return Result.success();
    }

    @DeleteMapping("/questions/{id}")
    public Result<?> deleteQuestion(Authentication auth, @PathVariable Long id) {
        questionService.delete((Long) auth.getPrincipal(), id, true); return Result.success();
    }

    // --- 回答管理 ---
    @GetMapping("/answers")
    public Result<?> listAnswers(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
                                  @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Answer> w = new LambdaQueryWrapper<Answer>().orderByDesc(Answer::getCreatedAt);
        if (status != null) w.eq(Answer::getStatus, status);
        return Result.success(PageResult.of(answerMapper.selectPage(new Page<>(page, size), w)));
    }

    @PutMapping("/answers/{id}/status")
    public Result<?> updateAnswerStatus(@PathVariable Long id, @RequestParam Integer status) {
        answerMapper.update(null, new LambdaUpdateWrapper<Answer>().eq(Answer::getId, id).set(Answer::getStatus, status));
        return Result.success();
    }

    @DeleteMapping("/answers/{id}")
    public Result<?> deleteAnswer(Authentication auth, @PathVariable Long id) {
        answerService.delete((Long) auth.getPrincipal(), id, true); return Result.success();
    }

    // --- 评论管理 ---
    @GetMapping("/comments")
    public Result<?> listComments(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(PageResult.of(commentMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Comment>().orderByDesc(Comment::getCreatedAt))));
    }

    @DeleteMapping("/comments/{id}")
    public Result<?> deleteComment(@PathVariable Long id) {
        commentMapper.deleteById(id); return Result.success();
    }

    // --- 举报管理 ---
    @GetMapping("/reports")
    public Result<?> listReports(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
                                  @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Report> w = new LambdaQueryWrapper<Report>().orderByDesc(Report::getCreatedAt);
        if (status != null) w.eq(Report::getStatus, status);
        return Result.success(PageResult.of(reportMapper.selectPage(new Page<>(page, size), w)));
    }

    @PutMapping("/reports/{id}/handle")
    public Result<?> handleReport(Authentication auth, @PathVariable Long id,
                                   @RequestParam Integer status, @RequestParam(required = false) String note) {
        reportMapper.update(null, new LambdaUpdateWrapper<Report>().eq(Report::getId, id)
                .set(Report::getStatus, status).set(Report::getHandleNote, note)
                .set(Report::getHandleUserId, (Long) auth.getPrincipal())
                .set(Report::getHandleTime, LocalDateTime.now()));
        return Result.success();
    }

    // --- 标签管理 ---
    @GetMapping("/tags")
    public Result<?> listTags() { return Result.success(tagService.list()); }

    @PostMapping("/tags")
    public Result<?> createTag(@RequestBody Tag tag) { return Result.success(tagService.create(tag)); }

    @PutMapping("/tags/{id}")
    public Result<?> updateTag(@PathVariable Long id, @RequestBody Tag tag) { tag.setId(id); return Result.success(tagService.update(tag)); }

    @DeleteMapping("/tags/{id}")
    public Result<?> deleteTag(@PathVariable Long id) { tagService.delete(id); return Result.success(); }
}
